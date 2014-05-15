package com.lteconsulting.offlinedemo.server.synchro;

import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import com.lteconsulting.offlinedemo.shared.synchro.FieldConfig;
import com.lteconsulting.offlinedemo.shared.synchro.SynchroConfig;
import com.lteconsulting.offlinedemo.shared.synchro.TableConfig;
import com.lteconsulting.offlinedemo.shared.synchro.dto.ClientHistory;
import com.lteconsulting.offlinedemo.shared.synchro.dto.ClientTableHistory;
import com.lteconsulting.offlinedemo.shared.synchro.dto.DeletedRecord;
import com.lteconsulting.offlinedemo.shared.synchro.dto.Record;
import com.lteconsulting.offlinedemo.shared.synchro.dto.SyncCursor;
import com.lteconsulting.offlinedemo.shared.synchro.dto.SyncResult;
import com.lteconsulting.offlinedemo.shared.synchro.dto.TableSyncCursor;

public class SynchroManager
{
	HashMap<String, HashMap<Integer, Integer>> oldToNewIds = new HashMap<>();

	HashMap<String,TableConfig> tableConfigs;

	public void setConfig( SynchroConfig config )
	{
		this.tableConfigs = new HashMap<>();
		for( TableConfig tableConfig : config.getTableConfigs() )
			this.tableConfigs.put( tableConfig.getName(), tableConfig );
	}

	public SyncResult syncData( EntityManager em, ClientHistory localHistory, SyncCursor syncCursors )
	{
		SyncResult syncData = new SyncResult();

		// process client's changes
		if( localHistory != null )
		{
			insertRecords( em, getRecordsToInsert( localHistory ) );
			updateRecords( em, getRecordsToUpdate( localHistory ) );
			deleteRecords( em, localHistory.getDeletedRecords() );
		}

		syncData.setIdUpdateData( oldToNewIds );

		// find out changes for clients
		findServerUpdates( em, syncCursors, syncData );

		// get deleted records
		findServerDeletes( em, syncCursors.getDeleteCursor(), syncData );

		return syncData;
	}

	private void findServerUpdates( EntityManager em, SyncCursor syncCursors, SyncResult syncData )
	{
		for( TableSyncCursor cursor : syncCursors.getTableCursors() )
		{
			String table = cursor.getTable();
			String lastUpdateTime = cursor.getLastUpdateTime();
			int lastUpdateId = cursor.getLastUpdateId();

			List<String> fieldNames = getFieldNames( table );
			String fieldList = implode( fieldNames );

			StringBuilder sb = new StringBuilder();
			sb.append( "select " + fieldList + " from " + table + " " );
			if( lastUpdateTime != null )
				sb.append( "where (update_date>'" + lastUpdateTime + "') OR (update_date='" + lastUpdateTime + "' AND id>"+lastUpdateId+") " );
			sb.append( "order by update_date, id limit 50" );

			Query q = em.createNativeQuery( sb.toString() );

			@SuppressWarnings( "unchecked" )
			List<Object[]> updatedRecords = q.getResultList();
			for( Object[] row : updatedRecords )
			{
				syncData.addUpdatedRecord( table, fieldNames, row );

				String recordUpdateTime = row[row.length-1]==null ? null : row[row.length-1].toString(); // update_time is the last
				if( lastUpdateTime==null || lastUpdateTime.compareTo( recordUpdateTime )<0 )
					lastUpdateTime = recordUpdateTime;

				int recordId = (Integer) row[0]; // id is the first field
				lastUpdateId = recordId;
			}

			syncData.addSyncCursor( new TableSyncCursor( table, lastUpdateTime, lastUpdateId ) );
		}
	}

	private void findServerDeletes( EntityManager em, String deleteCursor, SyncResult syncData )
	{
		if( deleteCursor != null )
		{
			Query q = em.createNativeQuery( "select table_name, record_id, update_date from deleted_records where update_date > '" + deleteCursor + "' order by update_date limit 100" );
			@SuppressWarnings( "unchecked" )
			List<Object[]> deletedRecords = q.getResultList();
			for( Object[] row : deletedRecords )
			{
				String table = row[0].toString();
				int recordId = Integer.parseInt( row[1].toString() );
				String updateDate = row[2].toString();

				if( deleteCursor.compareTo( updateDate ) < 0 )
					deleteCursor = updateDate;

				syncData.addDeletedRecord( table, recordId );
			}

			syncData.setDeletionCursor( deleteCursor );
		}
		else
		{
			SimpleDateFormat formater = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
			syncData.setDeletionCursor( formater.format( new Date() ) );
		}
	}

	private String implode( List<String> list )
	{
		StringBuilder sb = new StringBuilder();

		boolean addComa = false;
		for( String item : list )
		{
			if( addComa )
				sb.append( "," );
			else
				addComa = true;
			sb.append( item );
		}

		return sb.toString();
	}

	private List<RecordAndTable> getRecordsToInsert( ClientHistory history )
	{
		List<RecordAndTable> toInsertRecords = new ArrayList<>();
		if( history == null )
			return toInsertRecords;

		for( ClientTableHistory tableHistory : history.getLocalTableHistory() )
		{
			for( Record record : tableHistory.getInsertedRecords() )
				toInsertRecords.add( new RecordAndTable( record, tableHistory.getTableName() ) );
		}

		return toInsertRecords;
	}

	private List<RecordAndTable> getRecordsToUpdate( ClientHistory history )
	{
		List<RecordAndTable> toUpdateRecords = new ArrayList<>();
		if( history == null )
			return toUpdateRecords;
		for( ClientTableHistory tableHistory : history.getLocalTableHistory() )
		{
			for( Record record : tableHistory.getUpdatedRecords() )
				toUpdateRecords.add( new RecordAndTable( record, tableHistory.getTableName() ) );
		}

		return toUpdateRecords;
	}

	private void insertRecords( EntityManager em, List<RecordAndTable> toInsertRecords )
	{
		int limit = 10000;
		while( !toInsertRecords.isEmpty() )
		{
			if( limit-- <= 0 )
			{
				System.out.println( "LIMIT REACHED !" );
				break;
			}

			RecordAndTable toInsert = toInsertRecords.remove( 0 );
			String sql = getSqlForInsert( toInsert );
			if( sql == null )
			{
				toInsertRecords.add( toInsert );
				continue;
			}

			// insert the record in DB
			Query query = em.createNativeQuery( sql );
			query.executeUpdate();

			BigInteger newId = (BigInteger) em.createNativeQuery( "select last_insert_id()" ).getSingleResult();

			// register the remote id (negative) with the server one
			HashMap<Integer, Integer> m = oldToNewIds.get( toInsert.table );
			if( m == null )
			{
				m = new HashMap<>();
				oldToNewIds.put( toInsert.table, m );
			}

			m.put( Integer.parseInt( toInsert.record.getProperties().get( "id" ) ), newId.intValue() );
		}
	}

	private void updateRecords( EntityManager em, List<RecordAndTable> recordsToUpdate )
	{
		for( RecordAndTable rat : recordsToUpdate )
		{
			String sql = getSqlForUpdate( rat );

			Query query = em.createNativeQuery( sql );
			query.executeUpdate();
		}
	}

	private String getSqlForInsert( RecordAndTable recordAndTable )
	{
		List<String> columns = new ArrayList<>();
		List<String> values = new ArrayList<>();

		if( !formatSqlParts( recordAndTable.table, recordAndTable.record.getProperties(), columns, values ) )
			return null;

		StringBuilder sql = new StringBuilder();
		sql.append( "insert into " + recordAndTable.table + " (" );
		appendList( sql, columns );
		sql.append( ") values (" );
		appendList( sql, values );
		sql.append( ")" );

		return sql.toString();
	}

	private String getSqlForUpdate( RecordAndTable recordAndTable )
	{
		List<String> columns = new ArrayList<>();
		List<String> values = new ArrayList<>();

		if( !formatSqlParts( recordAndTable.table, recordAndTable.record.getProperties(), columns, values ) )
			return null;

		StringBuilder sql = new StringBuilder();
		sql.append( "update " + recordAndTable.table + " set " );
		for( int i = 0; i < columns.size(); i++ )
		{
			if( i > 0 )
				sql.append( "," );
			sql.append( columns.get( i ) );
			sql.append( "=" );
			sql.append( values.get( i ) );
		}
		sql.append( " where id=" );
		sql.append( recordAndTable.record.getInt( "id" ) );

		return sql.toString();
	}

	private void deleteRecords( EntityManager em, List<DeletedRecord> deletedRecords )
	{
		if( deletedRecords == null )
			return;

		for( DeletedRecord r : deletedRecords )
		{
			Query query = em.createNativeQuery( "delete from " + r.getTable() + " where id = " + r.getRecordId() );
			query.executeUpdate();

			com.lteconsulting.offlinedemo.server.entities.DeletedRecord deletedRecord = new com.lteconsulting.offlinedemo.server.entities.DeletedRecord();
			deletedRecord.setTableName( r.getTable() );
			deletedRecord.setRecordId( r.getRecordId() );
			deletedRecord.setUpdateDate( new Date() );
			em.persist( deletedRecord );
		}
	}

	private List<String> getFieldNames( String table )
	{
		TableConfig tableConfig = tableConfigs.get( table );

		List<String> res = new ArrayList<>();
		res.add( "id" );
		for( FieldConfig fieldConfig : tableConfig.getFields() )
			res.add( fieldConfig.getName() );
		res.add( "update_date" );

		return res;
	}

	private String getLookupTable( String table, String columnName )
	{
		TableConfig config = tableConfigs.get( table );
		FieldConfig fieldConfig = config.getFieldConfig( columnName );
		return fieldConfig.getLookupTable();
	}

	private boolean formatSqlParts( String table, HashMap<String, String> properties, List<String> columns, List<String> values )
	{
		for( Entry<String, String> entry : properties.entrySet() )
		{
			String column = entry.getKey();
			String value = entry.getValue();

			// skip fields we don't care of server side
			if( column.equals( "id" ) || column.equals( "locally_modified" ) || column.equals( "update_date" ) )
				continue;

			// transform negative identifiers if needed.
			// if the transformation is not possible, return null;
			String lookupTable = getLookupTable( table, column );
			if( lookupTable != null )
			{
				try
				{
					Integer lookupId = Integer.parseInt( value );
					lookupId = lookupNewId( lookupTable, lookupId );
					if( lookupId == null )
						return false;

					if( lookupId == 0 )
						value = null;
					else
						value = String.valueOf( lookupId );
				}
				catch( Exception e )
				{
					value = null;
				}
			}

			columns.add( column );
			values.add( sqlTransform( value ) );
		}

		return true;
	}

	private String sqlTransform( String raw )
	{
		if( raw == null )
			return "null";
		try
		{
			Integer.parseInt( raw );
			return raw;
		}
		catch( NumberFormatException e )
		{
			return "'" + raw + "'";
		}
	}

	private <T> void appendList( StringBuilder sb, List<T> list )
	{
		boolean first = true;

		for( T object : list )
		{
			if( first )
				first = false;
			else
				sb.append( "," );

			sb.append( object.toString() );
		}
	}

	private Integer lookupNewId( String table, Integer id )
	{
		if( id == null )
			return null;
		if( id >= 0 )
			return id;

		HashMap<Integer, Integer> m = oldToNewIds.get( table );
		if( m == null )
			return null;

		return m.get( id );
	}
}

class RecordAndTable
{
	Record record;
	String table;

	public RecordAndTable( Record record, String table )
	{
		this.record = record;
		this.table = table;
	}
}