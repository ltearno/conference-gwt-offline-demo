package com.lteconsulting.offlinedemo.client.synchro;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONNumber;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.json.client.JSONString;
import com.google.gwt.json.client.JSONValue;
import com.lteconsulting.offlinedemo.client.ApplicationPersistedSetting;
import com.lteconsulting.offlinedemo.client.DataAccess;
import com.lteconsulting.offlinedemo.client.sql.SQLite;
import com.lteconsulting.offlinedemo.client.sql.SQLiteResult;
import com.lteconsulting.offlinedemo.shared.synchro.FieldConfig;
import com.lteconsulting.offlinedemo.shared.synchro.SynchroConfig;
import com.lteconsulting.offlinedemo.shared.synchro.TableConfig;
import com.lteconsulting.offlinedemo.shared.synchro.dto.DeletedRecord;
import com.lteconsulting.offlinedemo.shared.synchro.dto.DownstreamSynchroParameter;
import com.lteconsulting.offlinedemo.shared.synchro.dto.DownstreamSynchroResult;
import com.lteconsulting.offlinedemo.shared.synchro.dto.Record;
import com.lteconsulting.offlinedemo.shared.synchro.dto.TableSyncCursor;

/*
 * Manages downstream synchronization on the client side
 * Downstream synchronization consist of synchronizing the client database with recent server changes.
 * It requires to save a synchronization cursor on the client side, for each synchronized table.
 */
public class DownstreamSynchroManager
{
	// synchronization configuration object
	private SynchroConfig config;

	private SQLite sqlDb;

	public DownstreamSynchroManager( SynchroConfig config, SQLite sqlDb )
	{
		this.config = config;
		this.sqlDb = sqlDb;
	}

	public DownstreamSynchroParameter getSynchroParameter()
	{
		DownstreamSynchroParameter cursor = new DownstreamSynchroParameter();

		ArrayList<TableSyncCursor> tableCursors = loadLocalSyncCursors();
		String deleteCursor = DataAccess.get().getAppSettingString( ApplicationPersistedSetting.DOWNSTREAMSYNC_DELETECURSOR );

		cursor.setTableCursors( tableCursors );
		cursor.setDeleteCursor( deleteCursor );

		return cursor;
	}

	public int processSynchroResult( DownstreamSynchroResult result )
	{
		if( result == null )
			return 0;

		// process updated records from the server
		int nbUpdatedRecords = processUpdatedRecords( result.getModifiedRecords() );

		// store synchronization update cursors
		storeSyncUpdateCursors( result.getUpdatedUpdateCursors() );

		// process records deleted on the server side
		int nbDeletedRecords = processDeletedRecords( result.getDeletedRecords() );

		// store synchronization delete cursor
		DataAccess.get().setAppSetting( ApplicationPersistedSetting.DOWNSTREAMSYNC_DELETECURSOR, result.getUpdatedDeletionCursor() );

		return nbUpdatedRecords + nbDeletedRecords;
	}

	private ArrayList<TableSyncCursor> loadLocalSyncCursors()
	{
		try
		{
			ArrayList<TableSyncCursor> res = new ArrayList<>();
			String serialized = DataAccess.get().getAppSettingString( ApplicationPersistedSetting.DOWNSTREAMSYNC_SYNCCURSOR );
			JSONArray array = JSONParser.parseStrict( serialized ).isArray();
			for( int i = 0; i < array.size(); i++ )
				res.add( deserializeSyncCursor( array.get( i ) ) );
			return res;
		}
		catch( Exception e )
		{
			// if we don't have any stored cursor yet, send a set of empty cursors
			ArrayList<TableSyncCursor> res = new ArrayList<>();
			for( TableConfig tableConfig : config.getTableConfigs() )
				res.add( new TableSyncCursor( tableConfig.getName(), null, -1 ) );
			return res;
		}
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

	private int processDeletedRecords( List<DeletedRecord> deletedRecords )
	{
		if( deletedRecords == null )
			return 0;

		int nb = 0;

		for( int i = 0; i < deletedRecords.size(); i++ )
		{
			DeletedRecord info = deletedRecords.get( i );

			String table = info.getTable();
			int id = info.getRecordId();

			sqlDb.execute( "delete from " + table + " where id=" + id );

			nb++;
		}

		return nb;
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

	private void storeSyncUpdateCursors( ArrayList<TableSyncCursor> cursors )
	{
		JSONArray array = new JSONArray();

		int i = 0;
		for( TableSyncCursor cursor : cursors )
			array.set( i++, serializeSyncCursor( cursor ) );

		DataAccess.get().setAppSetting( ApplicationPersistedSetting.DOWNSTREAMSYNC_SYNCCURSOR, array.toString() );
	}

	private int processUpdatedRecords( HashMap<String, ArrayList<Record>> allModifiedRecords )
	{
		if( allModifiedRecords == null )
			return 0;

		int nb = 0;

		for( Entry<String, ArrayList<Record>> entry : allModifiedRecords.entrySet() )
		{
			String table = entry.getKey();
			List<Record> records = entry.getValue();

			TableConfig tableConfig = config.getTableConfig( table );

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

				nb++;
			}
		}

		return nb;
	}
}
