package com.lteconsulting.offlinedemo.shared.synchro.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import com.google.gwt.user.client.rpc.IsSerializable;

public final class SyncResult implements Serializable, IsSerializable
{
	private static final long serialVersionUID = 5207583888408942691L;

	private List<OldToNewId> idUpdateData;

	// set of updated records, beginning at the cursor position provided by the client
	private HashMap<String, ArrayList<Record>> updatedRecords;
	// updated cursors, to be stored by the client
	private ArrayList<TableSyncCursor> updatedSyncCursors;

	// set of deleted records
	private ArrayList<DeletedRecord> deletedRecords;
	// updated deletion cursor
	private String deletionCursor;

	public SyncResult()
	{
	}

	public List<OldToNewId> getIdUpdates()
	{
		return idUpdateData;
	}

	public void setIdUpdateData( HashMap<String, HashMap<Integer, Integer>> oldToNewIds )
	{
		idUpdateData = new ArrayList<>();

		for( Entry<String, HashMap<Integer, Integer>> entry : oldToNewIds.entrySet() )
		{
			for( Entry<Integer, Integer> e2 : entry.getValue().entrySet() )
				idUpdateData.add( new OldToNewId( entry.getKey(), e2.getKey(), e2.getValue() ) );
		}
	}

	public HashMap<String, ArrayList<Record>> getModifiedRecords()
	{
		return updatedRecords;
	}

	public List<DeletedRecord> getDeletedRecords()
	{
		return deletedRecords;
	}

	public String getUpdatedDeletionCursor()
	{
		return deletionCursor;
	}

	// get total number of changes from the server (delete, create, update)
	public int getNbServerChanges()
	{
		int res = 0;

		if( updatedRecords != null )
		{
			for( ArrayList<Record> records : updatedRecords.values() )
				res += records.size();
		}

		if( deletedRecords != null )
			res += deletedRecords.size();

		return res;
	}

	public void addUpdatedRecord( String table, List<String> fieldNames, Object[] fields )
	{
		if( updatedRecords == null )
			updatedRecords = new HashMap<>();

		ArrayList<Record> records = updatedRecords.get( table );
		if( records == null )
		{
			records = new ArrayList<>();
			updatedRecords.put( table, records );
		}

		HashMap<String, String> properties = new HashMap<>();
		for( int i=0; i<fields.length; i++ )
			properties.put( fieldNames.get( i ), fields[i]!=null?fields[i].toString():null );

		Record record = new Record();
		record.setProperties( properties );

		records.add( record );
	}

	public void addSyncCursor( TableSyncCursor cursor )
	{
		if( updatedSyncCursors == null )
			updatedSyncCursors = new ArrayList<>();

		updatedSyncCursors.add( cursor );
	}

	public void addDeletedRecord( String table, int recordId )
	{
		if( deletedRecords == null )
			deletedRecords = new ArrayList<>();

		deletedRecords.add( new DeletedRecord( table, recordId ) );
	}

	public void setDeletionCursor( String deletionCursor )
	{
		this.deletionCursor = deletionCursor;
	}

	public ArrayList<TableSyncCursor> getUpdatedUpdateCursors()
	{
		return updatedSyncCursors;
	}
}
