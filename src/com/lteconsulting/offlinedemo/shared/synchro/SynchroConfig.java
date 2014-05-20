package com.lteconsulting.offlinedemo.shared.synchro;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;

/*
 * Data structure holding the synchronization configuration
 *  - Tables which are synchronized accross client and server,
 *  - Foreign constraints in the database are also declared here, because needed by the synchronization implementations
 */
public class SynchroConfig
{
	private final String name;
	private HashMap<String,TableConfig> tableConfigs;

	public SynchroConfig( String name )
	{
		this.name = name;
	}

	public void setTableConfigs( List<TableConfig> configs )
	{
		tableConfigs= new HashMap<>();
		for( TableConfig config : configs )
			tableConfigs.put( config.getName(), config );
	}

	public Collection<TableConfig> getTableConfigs()
	{
		return tableConfigs.values();
	}

	public TableConfig getTableConfig( String table )
	{
		return tableConfigs.get( table );
	}

	public String getName()
	{
		return name;
	}
}
