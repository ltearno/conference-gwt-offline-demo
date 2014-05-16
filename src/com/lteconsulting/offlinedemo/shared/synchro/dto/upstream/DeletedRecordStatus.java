package com.lteconsulting.offlinedemo.shared.synchro.dto.upstream;

import com.google.gwt.user.client.rpc.IsSerializable;

public class DeletedRecordStatus implements IsSerializable
{
	private String table;
	private int id;
	private boolean isError;

	public DeletedRecordStatus()
	{
	}

	public DeletedRecordStatus( String table, int id, boolean isError )
	{
		this.table = table;
		this.id = id;
		this.isError = isError;
	}

	public String getTable()
	{
		return table;
	}

	public int getId()
	{
		return id;
	}

	public boolean isError()
	{
		return isError;
	}
}
