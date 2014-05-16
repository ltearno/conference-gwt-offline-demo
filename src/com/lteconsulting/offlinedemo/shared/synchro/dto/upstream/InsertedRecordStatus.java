package com.lteconsulting.offlinedemo.shared.synchro.dto.upstream;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import com.google.gwt.user.client.rpc.IsSerializable;

public class InsertedRecordStatus implements Serializable, IsSerializable
{
	private static final long serialVersionUID = -7562682717213270092L;

	private String table;

	private int oldId;

	// if newId <= 0, means that the record was not inserted
	private int newId;

	public static List<InsertedRecordStatus> makeList( HashMap<String, HashMap<Integer, Integer>> oldToNewIds )
	{
		List<InsertedRecordStatus> res = new ArrayList<>();

		for( Entry<String, HashMap<Integer, Integer>> entry : oldToNewIds.entrySet() )
		{
			for( Entry<Integer, Integer> e2 : entry.getValue().entrySet() )
				res.add( new InsertedRecordStatus( entry.getKey(), e2.getKey(), e2.getValue() ) );
		}

		return res;
	}

	public InsertedRecordStatus()
	{
	}

	public InsertedRecordStatus( String table, int oldId, int newId )
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
