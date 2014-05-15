package com.lteconsulting.offlinedemo.shared.synchro.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import com.google.gwt.user.client.rpc.IsSerializable;

public class OldToNewId implements Serializable, IsSerializable
{
	private static final long serialVersionUID = -7562682717213270092L;

	private String table;
	private int oldId;
	private int newId;

	public static List<OldToNewId> makeList( HashMap<String, HashMap<Integer, Integer>> oldToNewIds )
	{
		List<OldToNewId> res = new ArrayList<>();

		for( Entry<String, HashMap<Integer, Integer>> entry : oldToNewIds.entrySet() )
		{
			for( Entry<Integer, Integer> e2 : entry.getValue().entrySet() )
				res.add( new OldToNewId( entry.getKey(), e2.getKey(), e2.getValue() ) );
		}

		return res;
	}

	public OldToNewId()
	{
	}

	public OldToNewId( String table, int oldId, int newId )
	{
		this.table = table;
		this.oldId = oldId;
		this.newId = newId;
	}

	public String getTable()
	{
		return table;
	}

	public int getOldId()
	{
		return oldId;
	}

	public int getNewId()
	{
		return newId;
	}
}
