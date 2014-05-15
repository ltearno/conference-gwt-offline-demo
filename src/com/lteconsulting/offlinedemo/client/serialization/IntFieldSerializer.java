package com.lteconsulting.offlinedemo.client.serialization;

import com.lteconsulting.offlinedemo.shared.synchro.dto.Record;

public abstract class IntFieldSerializer<T> implements FieldSerializer<T>
{
	protected abstract Integer getValue( T dto );

	private String name;

	public IntFieldSerializer( String name )
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
		Integer value = getValue( dto );
		if( value == null )
			return null;

		return String.valueOf( value );
	}

	@Override
	public String getSqlQuoted( Record info )
	{
		return String.valueOf( info.getInt( name ) );
	}
}