package com.lteconsulting.offlinedemo.server.synchro;

import java.math.BigInteger;
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
import com.lteconsulting.offlinedemo.shared.synchro.dto.ClientTableHistory;
import com.lteconsulting.offlinedemo.shared.synchro.dto.DeletedRecord;
import com.lteconsulting.offlinedemo.shared.synchro.dto.Record;
import com.lteconsulting.offlinedemo.shared.synchro.dto.UpstreamSynchroParameter;
import com.lteconsulting.offlinedemo.shared.synchro.dto.UpstreamSynchroResult;

/*
 * Handles Upstream synchronization process on the server side.
 * The clients sends us its local changes and created records.
 * Implementation returns the corresponding inserted ids.
 */
public class UpstreamSynchroManager
{
	private SynchroConfig config;

	private static class OldToNewIds
	{
		HashMap<String, HashMap<Integer, Integer>> oldToNewIds = new HashMap<>();

		protected Integer lookupNewId( String table, Integer id )
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

		protected void register( String table, int oldId, int newId )
		{
			// register the remote id (negative) with the server one
			HashMap<Integer, Integer> m = oldToNewIds.get( table );
			if( m == null )
			{
				m = new HashMap<>();
				oldToNewIds.put( table, m );
			}
			m.put( oldId, newId );
		}
	}

	public UpstreamSynchroManager( SynchroConfig config )
	{
		this.config = config;
	}

	public UpstreamSynchroResult processUpstreamSynchro( EntityManager em, UpstreamSynchroParameter parameter )
	{
		if( parameter == null )
			return null;

		OldToNewIds oldToNewIds = new OldToNewIds();

		UpstreamSynchroResult result = new UpstreamSynchroResult();

		insertRecords( em, parameter, result, oldToNewIds );
		updateRecords( em, parameter, result, oldToNewIds );
		deleteRecords( em, parameter.getDeletedRecords(), result );

		return result;
	}

	private void insertRecords( EntityManager em, UpstreamSynchroParameter parameter, UpstreamSynchroResult result, OldToNewIds oldToNewIds )
	{
		if( parameter == null )
			return;

		List<RecordAndTable> toInsertRecords = getRecordsToInsert( parameter );
		List<RecordAndTable> notInsertedRecords;

		int inserted;
		do
		{
			notInsertedRecords = new ArrayList<>();
			inserted = 0;

			while( ! toInsertRecords.isEmpty() )
			{
				RecordAndTable toInsert = toInsertRecords.remove( 0 );
				int newId = insertRecord( em, toInsert, oldToNewIds );
				if( newId > 0 )
				{
					// ok record was inserted
					result.addInsertedRecord( toInsert.table, toInsert.record.getInt( "id" ), newId );
					inserted++;

					oldToNewIds.register( toInsert.table, toInsert.record.getInt( "id" ), newId );
				}
				else
				{
					// record was not inserted
					notInsertedRecords.add( toInsert );
				}
			}

			toInsertRecords = notInsertedRecords;
		}
		while( inserted > 0 );

		for( RecordAndTable notInsertedRecord : toInsertRecords )
			result.addInsertedRecord( notInsertedRecord.table, notInsertedRecord.record.getInt( "id" ), -1 );
	}

	private void updateRecords( EntityManager em, UpstreamSynchroParameter parameter, UpstreamSynchroResult result, OldToNewIds oldToNewIds )
	{
		for( ClientTableHistory tableHistory : parameter.getLocalTableHistory() )
		{
			for( Record record : tableHistory.getUpdatedRecords() )
			{
				boolean res = updateRecord( em, tableHistory.getTableName(), record, oldToNewIds );
				result.addUpdatedRecord( tableHistory.getTableName(), record.getInt( "id" ), ! res );
			}
		}
	}

	private void deleteRecords( EntityManager em, List<DeletedRecord> deletedRecords, UpstreamSynchroResult result )
	{
		if( deletedRecords == null )
			return;

		for( DeletedRecord r : deletedRecords )
		{
			try
			{
				Query query = em.createNativeQuery( "delete from " + r.getTable() + " where id = " + r.getRecordId() );
				query.executeUpdate();

				com.lteconsulting.offlinedemo.server.entities.DeletedRecord deletedRecord = new com.lteconsulting.offlinedemo.server.entities.DeletedRecord();
				deletedRecord.setTableName( r.getTable() );
				deletedRecord.setRecordId( r.getRecordId() );
				deletedRecord.setUpdateDate( new Date() );
				em.persist( deletedRecord );

				result.addDeletedRecord( r.getTable(), r.getRecordId(), false );
			}
			catch( Exception e )
			{
				result.addDeletedRecord( r.getTable(), r.getRecordId(), true );
			}
		}
	}

	// returns the id of the inserted record
	private int insertRecord( EntityManager em, RecordAndTable toInsert, OldToNewIds oldToNewIds )
	{
		String sql = getSqlForInsert( toInsert, oldToNewIds );
		if( sql == null )
			return -1;

		// insert the record in DB
		Query query = em.createNativeQuery( sql );
		query.executeUpdate();

		BigInteger newId = (BigInteger) em.createNativeQuery( "select last_insert_id()" ).getSingleResult();
		if( newId == null )
			return -1;

		return newId.intValue();
	}

	private String getSqlForInsert( RecordAndTable recordAndTable, OldToNewIds oldToNewIds )
	{
		List<String> columns = new ArrayList<>();
		List<String> values = new ArrayList<>();

		if( !formatSqlParts( recordAndTable.table, recordAndTable.record.getProperties(), columns, values, oldToNewIds ) )
			return null;

		StringBuilder sql = new StringBuilder();
		sql.append( "insert into " + recordAndTable.table + " (" );
		appendList( sql, columns );
		sql.append( ") values (" );
		appendList( sql, values );
		sql.append( ")" );

		return sql.toString();
	}

	private boolean updateRecord( EntityManager em, String table, Record record, OldToNewIds oldToNewIds )
	{
		String sql = getSqlForUpdate( table, record, oldToNewIds );
		if( sql == null )
			return false;

		try
		{
			Query query = em.createNativeQuery( sql );
			query.executeUpdate();
			return true;
		}
		catch( Exception e )
		{
			return false;
		}
	}

	private String getSqlForUpdate( String table, Record record, OldToNewIds oldToNewIds )
	{
		List<String> columns = new ArrayList<>();
		List<String> values = new ArrayList<>();

		if( !formatSqlParts( table, record.getProperties(), columns, values, oldToNewIds ) )
			return null;

		StringBuilder sql = new StringBuilder();
		sql.append( "update " + table + " set " );
		for( int i = 0; i < columns.size(); i++ )
		{
			if( i > 0 )
				sql.append( "," );
			sql.append( columns.get( i ) );
			sql.append( "=" );
			sql.append( values.get( i ) );
		}
		sql.append( " where id=" );
		sql.append( record.getInt( "id" ) );

		return sql.toString();
	}



	private List<RecordAndTable> getRecordsToInsert( UpstreamSynchroParameter history )
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

	private boolean formatSqlParts( String table, HashMap<String, String> properties, List<String> columns, List<String> values, OldToNewIds oldToNewIds )
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
					lookupId = oldToNewIds.lookupNewId( lookupTable, lookupId );
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

	private String getLookupTable( String table, String columnName )
	{
		TableConfig tableConfig = config.getTableConfig( table );
		FieldConfig fieldConfig = tableConfig.getFieldConfig( columnName );
		return fieldConfig.getLookupTable();
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