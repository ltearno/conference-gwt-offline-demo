package com.lteconsulting.offlinedemo.shared.synchro.dto;

import com.google.gwt.user.client.rpc.IsSerializable;


public class DeletedRecord implements IsSerializable
{
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
