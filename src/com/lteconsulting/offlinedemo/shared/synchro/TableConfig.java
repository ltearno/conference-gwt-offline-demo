package com.lteconsulting.offlinedemo.shared.synchro;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class TableConfig
{
	private String name;
	private List<FieldConfig> fields;

	private transient HashMap<String, FieldConfig> _cache;

	public TableConfig( String name )
	{
		this.name = name;
	}

	public TableConfig addField( String name, FieldType type )
	{
		if( fields == null )
			fields = new ArrayList<>();

		fields.add( new FieldConfig( name, type, null ) );

		return this;
	}

	public TableConfig addLookupField( String name, String lookupTable )
	{
		if( fields == null )
			fields = new ArrayList<>();

		fields.add( new FieldConfig( name, FieldType.INTEGER, lookupTable ) );

		return this;
	}

	public TableConfig addStringField( String name )
	{
		return addField( name, FieldType.STRING );
	}

	public TableConfig addIntField( String name )
	{
		return addField( name, FieldType.INTEGER );
	}

	public FieldConfig getFieldConfig( String name )
	{
		if( _cache == null )
		{
			_cache = new HashMap<>();
			for( FieldConfig fieldConfig : fields )
				_cache.put( fieldConfig.getName(), fieldConfig );
		}

		return _cache.get( name );
	}

	public String getName()
	{
		return name;
	}

	public void setName( String name )
	{
		this.name = name;
	}

	public List<FieldConfig> getFields()
	{
		return fields;
	}

	public void setFields( List<FieldConfig> fields )
	{
		this.fields = fields;
	}
}
