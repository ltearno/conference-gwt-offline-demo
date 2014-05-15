package com.lteconsulting.offlinedemo.shared.synchro.dto;

import com.google.gwt.user.client.rpc.IsSerializable;

public class TableSyncCursor implements IsSerializable
{
	private String table = null;
	private String lastUpdateTime = null;
	private int lastUpdateId = -1;

	public TableSyncCursor()
	{
	}

	public TableSyncCursor( String table, String lastUpdateTime, int lastUpdateId )
	{
		this.table = table;
		this.lastUpdateTime = lastUpdateTime;
		this.lastUpdateId = lastUpdateId;
	}

	@Override
	public String toString()
	{
		return "[table="+table+",time="+lastUpdateTime+",id="+lastUpdateId+"]";
	}

	public String getTable()
	{
		return table;
	}

	public void setTable( String table )
	{
		this.table = table;
	}

	public String getLastUpdateTime()
	{
		return lastUpdateTime;
	}

	public void setLastUpdateTime( String lastUpdateTime )
	{
		this.lastUpdateTime = lastUpdateTime;
	}

	public int getLastUpdateId()
	{
		return lastUpdateId;
	}

	public void setLastUpdateId( int lastUpdateId )
	{
		this.lastUpdateId = lastUpdateId;
	}
}
