package com.lteconsulting.offlinedemo.client.dto;

import java.util.Date;

import com.lteconsulting.offlinedemo.client.sql.SQLite;

public class Order extends BaseDto
{
	private String date;
	private String addressCode;

	public Order()
	{
		super( "orders" );

		updateDate = date = SQLite.dateTimeFormat.format( new Date() );
	}

	public Order( int id, String date, String addressCode, String updateDate )
	{
		super( "orders" );

		this.id = id;
		this.date = date;
		this.addressCode = addressCode;
		this.updateDate = updateDate;
	}

	public String getDate()
	{
		return date;
	}

	public void setDate( String date )
	{
		isChanged = true;
		this.date = date;
	}

	public String getAddressCode()
	{
		return addressCode;
	}

	public void setAddressCode( String addressCode )
	{
		isChanged = true;
		this.addressCode = addressCode;
	}
}