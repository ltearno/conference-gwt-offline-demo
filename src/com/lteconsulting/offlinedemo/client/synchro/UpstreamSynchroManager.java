package com.lteconsulting.offlinedemo.client.synchro;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.google.gwt.core.shared.GWT;
import com.lteconsulting.offlinedemo.client.sql.SQLite;
import com.lteconsulting.offlinedemo.client.sql.SQLiteResult;
import com.lteconsulting.offlinedemo.client.sql.SQLiteResult.Row;
import com.lteconsulting.offlinedemo.shared.synchro.SynchroConfig;
import com.lteconsulting.offlinedemo.shared.synchro.TableConfig;
import com.lteconsulting.offlinedemo.shared.synchro.dto.ClientTableHistory;
import com.lteconsulting.offlinedemo.shared.synchro.dto.DeletedRecord;
import com.lteconsulting.offlinedemo.shared.synchro.dto.UpstreamSynchroParameter;
import com.lteconsulting.offlinedemo.shared.synchro.dto.UpstreamSynchroResult;
import com.lteconsulting.offlinedemo.shared.synchro.dto.upstream.DeletedRecordStatus;
import com.lteconsulting.offlinedemo.shared.synchro.dto.upstream.InsertedRecordStatus;
import com.lteconsulting.offlinedemo.shared.synchro.dto.upstream.UpdatedRecordStatus;

public class UpstreamSynchroManager
{
	// synchronization configuration object
	private SynchroConfig config;

	private SQLite sqlDb;

	// map to track id updates from the server when synchronizing (local id -4 can become 64 for example)
	private HashMap<String, HashMap<Integer, Integer>> oldIdsToNewIds;

	public UpstreamSynchroManager( SynchroConfig config, SQLite sqlDb )
	{
		this.config = config;
		this.sqlDb = sqlDb;

		// ensures that the database contains the correct columns and triggers to support synchronization
		ensureDatabaseReady();
	}

	/*
	 * Returns the local history that needs to be given to the server
	 * in order to update the central database with user's changes
	 */
	public UpstreamSynchroParameter getClientHistory()
	{
		UpstreamSynchroParameter history = new UpstreamSynchroParameter();

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

	/*
	 * Process the results given by the server after upstream synchronization
	 */
	public void processUpstreamSynchroResult( UpstreamSynchroResult result )
	{
		if( result == null )
			return;

		commitInsertedRecords( result.getInsertedRecords() );
		commitDeletedRecords( result.getDeletedRecords() );
		commitUpdatedRecord( result.getUpdatedRecords() );
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

	private void commitInsertedRecords( List<InsertedRecordStatus> insertedRecords )
	{
		if( insertedRecords == null )
			return;

		for( InsertedRecordStatus otni : insertedRecords )
		{
			String table = otni.getTable();
			int oldId = otni.getOldId();
			int newId = otni.getNewId();

			sqlDb.execute( "update " + table + " set id=" + newId + " where id=" + oldId );

			// TODO : update also referencing fields

			storeIdMapping( table, oldId, newId );
		}
	}

	private void commitDeletedRecords( List<DeletedRecordStatus> deletedRecords )
	{
		if( deletedRecords == null )
			return;

		for( DeletedRecordStatus deletedRecordStatus : deletedRecords )
		{
			if( deletedRecordStatus.isError() )
				continue;

			String table = deletedRecordStatus.getTable();
			int id = deletedRecordStatus.getId();

			sqlDb.execute( "delete from deleted_records where record_id=" + id + " and table_name like '" + table + "'" );
		}
	}

	private void commitUpdatedRecord( List<UpdatedRecordStatus> updatedRecords )
	{
		if( updatedRecords == null )
			return;

		for( UpdatedRecordStatus updatedRecord : updatedRecords )
		{
			if( updatedRecord.isError() )
				continue;

			// commit the local update if it has not been modified again during the synchronization
			// the sqlite trigger will reset locally_modified to 0
			sqlDb.execute( "update " + updatedRecord.getTable() + " set locally_modified=-1 where id="+updatedRecord.getId()+" AND locally_modified=2" );
		}
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

	private ClientTableHistory getTableHistory( String table )
	{
		ClientTableHistory tableHistory = new ClientTableHistory();

		tableHistory.setTableName( table );

		// inserted records
		SQLiteResult insertedRecords = new SQLiteResult( sqlDb.execute( "select * from " + table + " where id<0" ) );
		tableHistory.setInsertedRecords( insertedRecords.getAsMap() );

		// modified records
		SQLiteResult modifiedRecords = new SQLiteResult( sqlDb.execute( "select * from " + table + " where locally_modified=1 and id>0" ) );
		tableHistory.setUpdatedRecords( modifiedRecords.getAsMap() );
		for( Row row : modifiedRecords )
			sqlDb.execute( "update " + table + " set locally_modified=2 where id=" + row.getInt( "id" ) );

		if( modifiedRecords.size() > 0 )
			GWT.log( modifiedRecords.size() + " records locally modified" );

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
}
