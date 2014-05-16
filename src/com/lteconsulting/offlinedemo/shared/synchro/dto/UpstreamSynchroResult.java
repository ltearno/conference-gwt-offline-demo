package com.lteconsulting.offlinedemo.shared.synchro.dto;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.user.client.rpc.IsSerializable;
import com.lteconsulting.offlinedemo.shared.synchro.dto.upstream.DeletedRecordStatus;
import com.lteconsulting.offlinedemo.shared.synchro.dto.upstream.InsertedRecordStatus;
import com.lteconsulting.offlinedemo.shared.synchro.dto.upstream.UpdatedRecordStatus;

public class UpstreamSynchroResult implements IsSerializable
{
	List<InsertedRecordStatus> insertedRecords;

	List<UpdatedRecordStatus> updatedRecords;

	List<DeletedRecordStatus> deletedRecords;

	public UpstreamSynchroResult()
	{
	}

	public void addInsertedRecord( String table, int oldId, int newId )
	{
		if( insertedRecords == null )
			insertedRecords = new ArrayList<>();

		insertedRecords.add( new InsertedRecordStatus( table, oldId, newId ) );
	}

	public void addUpdatedRecord( String table, Integer id, boolean isError )
	{
		if( updatedRecords == null )
			updatedRecords = new ArrayList<>();

		updatedRecords.add( new UpdatedRecordStatus( table, id, isError ) );
	}

	public void addDeletedRecord( String table, Integer id, boolean isError )
	{
		if( deletedRecords == null )
			deletedRecords = new ArrayList<>();

			deletedRecords.add( new DeletedRecordStatus( table, id, isError ) );
	}

	public List<InsertedRecordStatus> getInsertedRecords()
	{
		return insertedRecords;
	}

	public List<UpdatedRecordStatus> getUpdatedRecords()
	{
		return updatedRecords;
	}

	public List<DeletedRecordStatus> getDeletedRecords()
	{
		return deletedRecords;
	}
}
