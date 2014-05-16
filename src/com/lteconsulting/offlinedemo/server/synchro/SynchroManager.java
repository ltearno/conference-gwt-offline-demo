package com.lteconsulting.offlinedemo.server.synchro;

import javax.persistence.EntityManager;

import com.lteconsulting.offlinedemo.shared.synchro.SynchroConfig;
import com.lteconsulting.offlinedemo.shared.synchro.dto.DownstreamSynchroParameter;
import com.lteconsulting.offlinedemo.shared.synchro.dto.DownstreamSynchroResult;
import com.lteconsulting.offlinedemo.shared.synchro.dto.SynchroResult;
import com.lteconsulting.offlinedemo.shared.synchro.dto.UpstreamSynchroParameter;
import com.lteconsulting.offlinedemo.shared.synchro.dto.UpstreamSynchroResult;

public class SynchroManager
{
	private UpstreamSynchroManager upstreamSynchroManager;

	private DownstreamSynchroManager downstreamSynchroManager;

	public void setConfig( SynchroConfig config )
	{
		upstreamSynchroManager = new UpstreamSynchroManager( config );
		downstreamSynchroManager = new DownstreamSynchroManager( config );
	}

	public SynchroResult syncData( EntityManager em, UpstreamSynchroParameter localHistory, DownstreamSynchroParameter syncCursors )
	{
		SynchroResult syncData = new SynchroResult();

		UpstreamSynchroResult upstreamSynchroResult = upstreamSynchroManager.processUpstreamSynchro( em, localHistory );
		syncData.setUpstreamSynchroResult( upstreamSynchroResult );

		DownstreamSynchroResult downstreamSynchroResult = downstreamSynchroManager.processDownstreamSynchro( em, syncCursors );
		syncData.setDownstreamSynchroResult( downstreamSynchroResult );

		return syncData;
	}


}