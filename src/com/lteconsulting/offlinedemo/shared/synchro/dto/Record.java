package com.lteconsulting.offlinedemo.shared.synchro.dto;

import java.io.Serializable;
import java.util.HashMap;

import com.google.gwt.user.client.rpc.IsSerializable;

public class Record implements Serializable, IsSerializable
{
	private static final long serialVersionUID = -8901709333758434119L;

	private HashMap<String, String> properties;

	public HashMap<String, String> getProperties()
	{
		return properties;
	}

	public void setProperties( HashMap<String, String> properties )
	{
		this.properties = properties;
	}

	public String getString( String key )
	{
		return properties.get( key );
	}

	public Integer getInt( String key )
	{
		try
		{
			return Integer.parseInt( properties.get( key ) );
		}
		catch( Exception e )
		{
			return null;
		}
	}
}
