package com.lteconsulting.offlinedemo.server.synchro;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import com.lteconsulting.offlinedemo.shared.synchro.FieldConfig;
import com.lteconsulting.offlinedemo.shared.synchro.SynchroConfig;
import com.lteconsulting.offlinedemo.shared.synchro.TableConfig;
import com.lteconsulting.offlinedemo.shared.synchro.dto.DownstreamSynchroResult;
import com.lteconsulting.offlinedemo.shared.synchro.dto.DownstreamSynchroParameter;
import com.lteconsulting.offlinedemo.shared.synchro.dto.TableSyncCursor;

public class DownstreamSynchroManager
{
	private SynchroConfig config;

	public DownstreamSynchroManager( SynchroConfig config )
	{
		this.config = config;
	}

	public DownstreamSynchroResult processDownstreamSynchro( EntityManager em, DownstreamSynchroParameter parameter )
	{
		DownstreamSynchroResult res = new DownstreamSynchroResult();

		// find out changes for clients
		findServerUpdates( em, parameter, res );

		// get deleted records
		findServerDeletes( em, parameter.getDeleteCursor(), res );

		return res;
	}

	private void findServerUpdates( EntityManager em, DownstreamSynchroParameter syncCursors, DownstreamSynchroResult syncData )
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

	private void findServerDeletes( EntityManager em, String deleteCursor, DownstreamSynchroResult syncData )
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

	private List<String> getFieldNames( String table )
	{
		TableConfig tableConfig = config.getTableConfig( table );

		List<String> res = new ArrayList<>();
		res.add( "id" );
		for( FieldConfig fieldConfig : tableConfig.getFields() )
			res.add( fieldConfig.getName() );
		res.add( "update_date" );

		return res;
	}
}
