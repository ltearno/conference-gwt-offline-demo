package com.lteconsulting.offlinedemo.shared.synchro.dto.upstream;

import com.google.gwt.user.client.rpc.IsSerializable;

public class UpdatedRecordStatus implements IsSerializable
{
	private String table;
	private int id;
	private boolean isError;

	public UpdatedRecordStatus()
	{
	}

	public UpdatedRecordStatus( String table, int id, boolean isError )
	{
		this.table = table;
		this.id = id;
		this.isError = isError;
	}

	public String getTable()
	{
		return table;
	}

	public void setTable( String table )
	{
		this.table = table;
	}

	public int getId()
	{
		return id;
	}

	public void setId( int id )
	{
		this.id = id;
	}

	public boolean isError()
	{
		return isError;
	}

	public void setError( boolean isError )
	{
		this.isError = isError;
	}
}
