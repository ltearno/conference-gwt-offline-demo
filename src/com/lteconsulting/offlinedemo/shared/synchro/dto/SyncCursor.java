package com.lteconsulting.offlinedemo.shared.synchro.dto;

import java.util.ArrayList;

import com.google.gwt.user.client.rpc.IsSerializable;

public class SyncCursor implements IsSerializable
{
	private ArrayList<TableSyncCursor> tableCursors;
	private String deleteCursor;

	public SyncCursor()
	{
	}

	public ArrayList<TableSyncCursor> getTableCursors()
	{
		return tableCursors;
	}

	public void setTableCursors( ArrayList<TableSyncCursor> tableCursors )
	{
		this.tableCursors = tableCursors;
	}

	public String getDeleteCursor()
	{
		return deleteCursor;
	}

	public void setDeleteCursor( String deleteCursor )
	{
		this.deleteCursor = deleteCursor;
	}
}
