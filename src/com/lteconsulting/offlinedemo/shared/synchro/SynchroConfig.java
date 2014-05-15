package com.lteconsulting.offlinedemo.shared.synchro;

import java.util.List;

public class SynchroConfig
{
	private List<TableConfig> tableConfigs;

	public SynchroConfig()
	{
	}

	public void setTableConfigs( List<TableConfig> configs )
	{
		this.tableConfigs = configs;
	}

	public List<TableConfig> getTableConfigs()
	{
		return tableConfigs;
	}
}
