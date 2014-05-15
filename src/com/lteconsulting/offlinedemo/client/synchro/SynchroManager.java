package com.lteconsulting.offlinedemo.client.synchro;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONNumber;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.json.client.JSONString;
import com.google.gwt.json.client.JSONValue;
import com.google.gwt.storage.client.Storage;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.lteconsulting.offlinedemo.client.DataAccess;
import com.lteconsulting.offlinedemo.client.sql.SQLite;
import com.lteconsulting.offlinedemo.client.sql.SQLiteResult;
import com.lteconsulting.offlinedemo.shared.SyncServiceAsync;
import com.lteconsulting.offlinedemo.shared.synchro.FieldConfig;
import com.lteconsulting.offlinedemo.shared.synchro.SynchroConfig;
import com.lteconsulting.offlinedemo.shared.synchro.TableConfig;
import com.lteconsulting.offlinedemo.shared.synchro.dto.ClientHistory;
import com.lteconsulting.offlinedemo.shared.synchro.dto.ClientTableHistory;
import com.lteconsulting.offlinedemo.shared.synchro.dto.DeletedRecord;
import com.lteconsulting.offlinedemo.shared.synchro.dto.OldToNewId;
import com.lteconsulting.offlinedemo.shared.synchro.dto.Record;
import com.lteconsulting.offlinedemo.shared.synchro.dto.SyncCursor;
import com.lteconsulting.offlinedemo.shared.synchro.dto.SyncResult;
import com.lteconsulting.offlinedemo.shared.synchro.dto.TableSyncCursor;

/*
 * Manages tables synchronization on the client side
 */
public class SynchroManager
{
	// keys for storing sync cursors in the local storage
	private final String LOCALSTORAGE_KEY_DELETE_CURSOR;
	private final String LOCALSTORAGE_KEY_SYNC_CURSORS;

	private final Storage storage = Storage.getLocalStorageIfSupported();
	private final SQLite sqlDb = DataAccess.get().getSqlDb();

	// map to track id updates from the server when synchronizing (local id -4 can become 64 for example)
	private HashMap<String, HashMap<Integer, Integer>> oldIdsToNewIds;

	// synchronization configuration object
	private SynchroConfig config;

	public SynchroManager( String prefix )
	{
		LOCALSTORAGE_KEY_DELETE_CURSOR = prefix + "_smdt";
		LOCALSTORAGE_KEY_SYNC_CURSORS = prefix + "_smsc";
	}

	public void setConfig( SynchroConfig config )
	{
		this.config = config;

		// ensures that the database contains the correct columns and triggers to support synchronization
		ensureDatabaseReady();
	}

	/*
	 * Do a synchronization step of the database with the server.
	 */
	public void doSynchro( final AsyncCallback<Integer> callback )
	{
		// find records that have been locally updated, created or deleted
		final ClientHistory clientHistory = getLocalHistory();

		// compute current synchronization cursors, so that the server can update us with new or deleted records
		SyncCursor cursor = getSyncCursor();

		SyncServiceAsync.service.syncData( clientHistory, cursor, new AsyncCallback<SyncResult>()
		{
			@Override
			public void onSuccess( SyncResult result )
			{
				int res = processSyncResult( result, clientHistory );

				callback.onSuccess( res );
			}

			@Override
			public void onFailure( Throwable caught )
			{
				callback.onFailure( caught );
			}
		} );
	}

	private void ensureDatabaseReady()
	{
		// create the deleted records table if needed, to keep track of locally deleted records
		sqlDb.execute( "create table if not exists deleted_records (id INTEGER PRIMARY KEY AUTOINCREMENT, table_name TEXT, record_id INTEGER);" );

		// create triggers if needed
		// those triggers allows to keep track of locally updated records and deleted records
		for( TableConfig tableConfig : config.getTableConfigs() )
		{
			if( sqlDb.hasColumn( tableConfig.getName(), "locally_modified" ) )
				continue;

			sqlDb.execute( "alter table "+tableConfig.getName()+" add column locally_modified INTEGER" );

			sqlDb.execute( "create trigger if not exists "+tableConfig.getName()+"_update after update on "+tableConfig.getName()+" for each row begin update "+tableConfig.getName()+" set locally_modified = case when new.locally_modified=2 then 2 when new.locally_modified=-1 then 0 else 1 end where id=old.id; end;" );
			sqlDb.execute( "create trigger if not exists "+tableConfig.getName()+"_delete after delete on "+tableConfig.getName()+" for each row begin insert into  `deleted_records` ( `table_name` , `record_id` ) VALUES ( '"+tableConfig.getName()+"',  old.id ); end;" );
		}
	}

	private SyncCursor getSyncCursor()
	{
		SyncCursor cursor = new SyncCursor();

		ArrayList<TableSyncCursor> tableCursors = loadLocalSyncCursors();
		String deleteCursor = storage.getItem( LOCALSTORAGE_KEY_DELETE_CURSOR );

		cursor.setTableCursors( tableCursors );
		cursor.setDeleteCursor( deleteCursor );

		return cursor;
	}

	// process the result of a synchronization call to the server
	// returns the number of changes that happened during the process
	private int processSyncResult( SyncResult result, ClientHistory clientHistory )
	{
		if( result == null )
			return 0;

		// commit local changes and update ids
		commitLocalChanges( result, clientHistory );

		// process server updates
		processServerUpdates( result );

		// save db if needed
		int nbLocalCommits = clientHistory.getNbChanges();
		int nbServerChanges = result.getNbServerChanges();
		if( nbLocalCommits + nbServerChanges > 0 )
			DataAccess.get().scheduleSaveDb();

		// produce a report
		if( nbLocalCommits + nbServerChanges > 0 )
		{
			GWT.log( "Sync:" );
			GWT.log( nbLocalCommits + " local changes" );
			GWT.log( nbServerChanges + " server changes" );
			GWT.log( "updateCursors: " + result.getUpdatedUpdateCursors() );
			GWT.log( "deletionCursor: " + result.getUpdatedDeletionCursor() );
		}

		return nbLocalCommits + nbServerChanges;
	}

	private void commitLocalChanges( SyncResult result, ClientHistory clientHistory )
	{
		// commit local history : forget telling the server about those records
		commitDeletedRecords( clientHistory.getDeletedRecords() );
		commitUpdatedRecord();

		// update local ids (local one were < 0 and have now been generated on the server side)
		processIdUpdates( result.getIdUpdates() );
	}

	// returns all the records that have not been committed by the server until now
	private ClientHistory getLocalHistory()
	{
		ClientHistory history = new ClientHistory();

		// locally inserted and updated records
		for( TableConfig table : config.getTableConfigs() )
		{
			ClientTableHistory tableHistory = getTableHistory( table.getName() );
			history.addClientTableHistory( tableHistory );
		}

		// deleted records
		SQLiteResult deletedRecordsResult = new SQLiteResult( sqlDb.execute( "select * from deleted_records" ) );
		List<DeletedRecord> deletedRecords = new ArrayList<>();
		for( SQLiteResult.Row row : deletedRecordsResult )
			deletedRecords.add( new DeletedRecord( row.getString( "table_name" ), row.getInt( "record_id" ) ) );
		history.setDeletedRecords( deletedRecords );

		return history;
	}

	private void commitDeletedRecords( List<DeletedRecord> deletedRecords )
	{
		if( deletedRecords.isEmpty() )
			return;

		for( int i = 0; i < deletedRecords.size(); i++ )
		{
			String table = deletedRecords.get( i ).getTable();
			int id = deletedRecords.get( i ).getRecordId();

			sqlDb.execute( "delete from deleted_records where record_id=" + id + " and table_name like '" + table + "'" );
		}
	}

	private void commitUpdatedRecord()
	{
		// the sql trigger will set locally_modified to 0 !
		for( TableConfig tableConfig : config.getTableConfigs() )
			sqlDb.execute( "update " + tableConfig.getName() + " set locally_modified=-1 where locally_modified=2" );
	}

	private int processIdUpdates( List<OldToNewId> idUpdatesByTable )
	{
		int nb = 0;

		for( OldToNewId otni : idUpdatesByTable )
		{
			String table = otni.getTable();
			int oldId = otni.getOldId();
			int newId = otni.getNewId();

			sqlDb.execute( "update " + table + " set id=" + newId + " where id=" + oldId );
			storeIdMapping( table, oldId, newId );
			nb++;
		}

		return nb;
	}

	private void processServerUpdates( SyncResult result )
	{
		HashMap<String, ArrayList<Record>> allModifiedRecords = result.getModifiedRecords();
		List<DeletedRecord> deletedRecords = result.getDeletedRecords();

		// process updated records from the server
		processUpdatedRecords( allModifiedRecords );

		// store synchronization update cursors
		storeSyncUpdateCursors( result.getUpdatedUpdateCursors() );

		// process records deleted on the server side
		processDeletedRecords( deletedRecords );

		// store synchronization delete cursor
		storage.setItem( LOCALSTORAGE_KEY_DELETE_CURSOR, result.getUpdatedDeletionCursor() );
	}

	private void processDeletedRecords( List<DeletedRecord> deletedRecords )
	{
		if( deletedRecords == null )
			return;

		for( int i = 0; i < deletedRecords.size(); i++ )
		{
			DeletedRecord info = deletedRecords.get( i );

			String table = info.getTable();
			int id = info.getRecordId();

			sqlDb.execute( "delete from " + table + " where id=" + id );
		}
	}

	private TableConfig getTableConfig( String table )
	{
		for( TableConfig tableConfig : config.getTableConfigs() )
			if( tableConfig.getName().equals( table ) )
				return tableConfig;
		return null;
	}

	private void processUpdatedRecords( HashMap<String, ArrayList<Record>> allModifiedRecords )
	{
		if( allModifiedRecords == null )
			return;

		for( Entry<String, ArrayList<Record>> entry : allModifiedRecords.entrySet() )
		{
			String table = entry.getKey();
			List<Record> records = entry.getValue();

			TableConfig tableConfig = getTableConfig( table );

			for( Record record : records )
			{
				int id = record.getInt( "id" );
				String updateDate = record.getString( "update_date" );

				SQLiteResult res = new SQLiteResult( sqlDb.execute( "select update_date from " + table + " where id=" + id ) );
				if( res.size() == 0 )
				{
					sqlDb.execute( getSqlInsertStatement( record, tableConfig ) );
				}
				else
				{
					if( res.getRow( 0 ).getString( "update_date" ).equals( updateDate ) )
						continue;

					sqlDb.execute( getSqlUpdateStatement( record, tableConfig ) );
				}
			}
		}
	}

	private String getSqlInsertStatement( Record info, TableConfig tableConfig )
	{
		StringBuilder sb = new StringBuilder();

		sb.append( "insert into " );
		sb.append( tableConfig.getName() );
		sb.append( " (id, " );
		for( FieldConfig fieldConfig : tableConfig.getFields() )
		{
			sb.append( fieldConfig.getName() );
			sb.append( ", " );
		}
		sb.append( "update_date) values (" );

		sb.append( info.getInt( "id" ) );
		sb.append( ", " );
		for( FieldConfig fieldConfig : tableConfig.getFields() )
		{
			appendFieldValue( fieldConfig, info, sb );
			sb.append( ", " );
		}
		sb.append( "'" );
		sb.append( info.getString( "update_date" ) );
		sb.append( "')" );

		return sb.toString();
	}

	private String getSqlUpdateStatement( Record info, TableConfig tableConfig )
	{
		StringBuilder sb = new StringBuilder();

		sb.append( "update " );
		sb.append( tableConfig.getName() );
		sb.append( " set " );

		for( FieldConfig fieldConfig : tableConfig.getFields() )
		{
			sb.append( fieldConfig.getName() );
			sb.append( "=" );
			appendFieldValue( fieldConfig, info, sb );
			sb.append( ", " );
		}

		sb.append( "update_date='" );
		sb.append( info.getString( "update_date" ) );
		sb.append( "', locally_modified=-1 where id=" );
		sb.append( info.getInt( "id" ) );

		return sb.toString();
	}

	private void appendFieldValue( FieldConfig fieldConfig, Record info, StringBuilder sb )
	{
		switch( fieldConfig.getType() )
		{
			case INTEGER:
				sb.append( String.valueOf( info.getInt( fieldConfig.getName() ) ) );
				break;
			case STRING:
				sb.append( "'" + info.getString( fieldConfig.getName() ) + "'" );
				break;
		}
	}

	private JSONValue serializeSyncCursor( TableSyncCursor cursor )
	{
		JSONObject json = new JSONObject();

		json.put( "table", new JSONString( cursor.getTable() ) );
		json.put( "last_update_time", cursor.getLastUpdateTime() == null ? null : new JSONString( cursor.getLastUpdateTime() ) );
		json.put( "last_update_id", new JSONNumber( cursor.getLastUpdateId() ) );

		return json;
	}

	private TableSyncCursor deserializeSyncCursor( JSONValue parsed )
	{
		if( parsed == null )
			return null;

		JSONObject json = parsed.isObject();
		if( json == null )
			return null;

		TableSyncCursor cursor = new TableSyncCursor();

		cursor.setTable( json.get( "table" ).isString().stringValue() );
		cursor.setLastUpdateTime( json.get( "last_update_time" ) == null ? null : json.get( "last_update_time" ).isString().stringValue() );
		cursor.setLastUpdateId( (int) json.get( "last_update_id" ).isNumber().doubleValue() );

		return cursor;
	}

	private ArrayList<TableSyncCursor> loadLocalSyncCursors()
	{
		try
		{
			ArrayList<TableSyncCursor> res = new ArrayList<>();
			String serialized = storage.getItem( LOCALSTORAGE_KEY_SYNC_CURSORS );
			JSONArray array = JSONParser.parseStrict( serialized ).isArray();
			for( int i = 0; i < array.size(); i++ )
				res.add( deserializeSyncCursor( array.get( i ) ) );
			return res;
		}
		catch( Exception e )
		{
			ArrayList<TableSyncCursor> res = new ArrayList<>();
			res.add( new TableSyncCursor( "articles", null, -1 ) );
			res.add( new TableSyncCursor( "orders", null, -1 ) );
			res.add( new TableSyncCursor( "order_items", null, -1 ) );
			return res;
		}
	}

	private void storeSyncUpdateCursors( ArrayList<TableSyncCursor> cursors )
	{
		JSONArray array = new JSONArray();

		int i = 0;
		for( TableSyncCursor cursor : cursors )
			array.set( i++, serializeSyncCursor( cursor ) );

		storage.setItem( LOCALSTORAGE_KEY_SYNC_CURSORS, array.toString() );
	}

	private ClientTableHistory getTableHistory( String table )
	{
		ClientTableHistory tableHistory = new ClientTableHistory();

		tableHistory.setTableName( table );

		// inserted records
		SQLiteResult insertedRecords = new SQLiteResult( sqlDb.execute( "select * from " + table + " where id<0" ) );
		tableHistory.setInsertedRecords( insertedRecords.getAsMap() );

		// modified records
		final SQLiteResult modifiedRecords = new SQLiteResult( sqlDb.execute( "select * from " + table + " where locally_modified>0 and id>0" ) );
		sqlDb.execute( "update " + table + " set locally_modified=2 where locally_modified>0 and id>0" ); // mark
																											// as
																											// updating...
		tableHistory.setUpdatedRecords( modifiedRecords.getAsMap() );

		return tableHistory;
	}

	private void storeIdMapping( String table, int oldId, int newId )
	{
		if( oldId == newId )
			return;

		if( oldIdsToNewIds == null )
			oldIdsToNewIds = new HashMap<String, HashMap<Integer, Integer>>();

		HashMap<Integer, Integer> idMap = oldIdsToNewIds.get( table );
		if( idMap == null )
		{
			idMap = new HashMap<Integer, Integer>();
			oldIdsToNewIds.put( table, idMap );
		}

		idMap.put( oldId, newId );
	}

	public Integer getRealId( String table, Integer id )
	{
		if( id == null )
			return null;
		if( id >= 0 )
			return id;
		if( oldIdsToNewIds == null )
			return id;
		HashMap<Integer, Integer> tableIds = oldIdsToNewIds.get( table );
		if( tableIds == null )
			return id;
		Integer res = tableIds.get( id );
		if( res == null )
			return id;
		return res;
	}
}
