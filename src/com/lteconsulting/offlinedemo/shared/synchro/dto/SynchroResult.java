package com.lteconsulting.offlinedemo.shared.synchro.dto;

import com.google.gwt.user.client.rpc.IsSerializable;

public final class SynchroResult implements IsSerializable
{
	// results of the upstream synchronization process
	private UpstreamSynchroResult upstreamSynchroResult;

	private DownstreamSynchroResult downstreamSynchroResult;

	public SynchroResult()
	{
	}

	public void setUpstreamSynchroResult( UpstreamSynchroResult upstreamSynchroResult )
	{
		this.upstreamSynchroResult = upstreamSynchroResult;
	}

	public UpstreamSynchroResult getUpstreamSynchroResult()
	{
		return upstreamSynchroResult;
	}

	public void setDownstreamSynchroResult( DownstreamSynchroResult downstreamSynchroResult )
	{
		this.downstreamSynchroResult = downstreamSynchroResult;
	}

	public DownstreamSynchroResult getDownstreamSynchroResult()
	{
		return downstreamSynchroResult;
	}
}
