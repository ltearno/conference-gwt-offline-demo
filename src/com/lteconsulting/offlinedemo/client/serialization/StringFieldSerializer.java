package com.lteconsulting.offlinedemo.client.serialization;

import com.lteconsulting.offlinedemo.shared.synchro.dto.Record;

public abstract class StringFieldSerializer<T> implements FieldSerializer<T>
{
	abstract String getValue( T dto );

	private String name;

	public StringFieldSerializer( String name )
	{
		this.name = name;
	}

	@Override
	public String getName()
	{
		return name;
	}

	@Override
	public String getSqlQuoted( T dto )
	{
		String value = getValue( dto );
		return "'" + value + "'";
	}

	@Override
	public String getSqlQuoted( Record info )
	{
		return "'" + info.getString( name ) + "'";
	}
}