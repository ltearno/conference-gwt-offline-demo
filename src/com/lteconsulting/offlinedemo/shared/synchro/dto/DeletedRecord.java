package com.lteconsulting.offlinedemo.shared.synchro.dto;

import java.io.Serializable;

import com.google.gwt.user.client.rpc.IsSerializable;


public class DeletedRecord implements Serializable, IsSerializable
{
	private static final long serialVersionUID = -7474645682184995737L;

	private String table;
	private int recordId;

	public DeletedRecord()
	{
	}

	public DeletedRecord( String table, int recordId )
	{
		this.table = table;
		this.recordId = recordId;
	}

	public String getTable()
	{
		return table;
	}

	public int getRecordId()
	{
		return recordId;
	}
}
