package com.lteconsulting.offlinedemo.shared.synchro;

public class FieldConfig
{
	private String name;
	private FieldType type;
	private String lookupTable;

	public FieldConfig( String name, FieldType type, String lookUpTable )
	{
		this.name = name;
		this.type = type;
		this.lookupTable = lookUpTable;
	}

	public String getName()
	{
		return name;
	}

	public void setName( String name )
	{
		this.name = name;
	}

	public FieldType getType()
	{
		return type;
	}

	public void setType( FieldType type )
	{
		this.type = type;
	}

	public String getLookupTable()
	{
		return lookupTable;
	}

	public void setLookupTable( String lookupTable )
	{
		this.lookupTable = lookupTable;
	}
}
