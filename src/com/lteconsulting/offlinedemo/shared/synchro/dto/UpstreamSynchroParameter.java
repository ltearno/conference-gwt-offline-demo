package com.lteconsulting.offlinedemo.shared.synchro.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.google.gwt.user.client.rpc.IsSerializable;

public class UpstreamSynchroParameter implements Serializable, IsSerializable
{
	private static final long serialVersionUID = -8382433245934779446L;

	public UpstreamSynchroParameter()
	{
	}

	List<DeletedRecord> deletedRecords;
	List<ClientTableHistory> localTableHistory;

	public List<DeletedRecord> getDeletedRecords()
	{
		return deletedRecords;
	}

	public void setDeletedRecords( List<DeletedRecord> deletedRecords )
	{
		this.deletedRecords = deletedRecords;
	}

	public List<ClientTableHistory> getLocalTableHistory()
	{
		return localTableHistory;
	}

	public void addClientTableHistory( ClientTableHistory clientTableHistory )
	{
		if( localTableHistory == null )
			localTableHistory = new ArrayList<>();

		localTableHistory.add( clientTableHistory );
	}

	public int getNbChanges()
	{
		int res = 0;

		if( deletedRecords != null )
			res += deletedRecords.size();

		if( localTableHistory != null )
		{
			for( ClientTableHistory history : localTableHistory )
			{
				if( history.insertedRecords != null )
					res += history.insertedRecords.size();
				if( history.updatedRecords != null )
					res += history.updatedRecords.size();
			}
		}

		return res;
	}
}
