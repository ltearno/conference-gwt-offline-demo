package com.lteconsulting.offlinedemo.shared.synchro.dto;

import java.io.Serializable;
import java.util.List;

import com.google.gwt.user.client.rpc.IsSerializable;

public class ClientTableHistory implements Serializable, IsSerializable
{
	private static final long serialVersionUID = 7564288356718204747L;

	String tableName;

	List<Record> insertedRecords;
	List<Record> updatedRecords;

	public String getTableName()
	{
		return tableName;
	}

	public void setTableName( String tableName )
	{
		this.tableName = tableName;
	}

	public List<Record> getInsertedRecords()
	{
		return insertedRecords;
	}

	public void setInsertedRecords( List<Record> insertedRecords )
	{
		this.insertedRecords = insertedRecords;
	}

	public List<Record> getUpdatedRecords()
	{
		return updatedRecords;
	}

	public void setUpdatedRecords( List<Record> updatedRecords )
	{
		this.updatedRecords = updatedRecords;
	}
}
