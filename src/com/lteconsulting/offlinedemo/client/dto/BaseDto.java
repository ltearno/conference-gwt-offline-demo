package com.lteconsulting.offlinedemo.client.dto;

import com.lteconsulting.offlinedemo.client.Synchronization;

/*
 * BaseDTO class for client side. Allows to track changes made in memory, to save them later on.
 */
public class BaseDto
{
	private String table;

	// tells if a setter has been called since object's construction
	protected boolean isChanged = false;

	protected int id;
	protected String updateDate;

	protected BaseDto( String table )
	{
		this.table = table;
	}

	public final int getId()
	{
		return Synchronization.get().getRealId( table, id );
	}

	public final void setId( int id )
	{
		this.id = id;
	}

	public final boolean isChanged()
	{
		return isChanged;
	}

	public final void commitChange()
	{
		isChanged = false;
	}

	public final String getUpdateDate()
	{
		return updateDate;
	}

	public final void setUpdateDate( String update_date )
	{
		isChanged = true;
		this.updateDate = update_date;
	}
}
