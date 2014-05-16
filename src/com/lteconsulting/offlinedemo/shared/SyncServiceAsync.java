package com.lteconsulting.offlinedemo.shared;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.lteconsulting.offlinedemo.shared.synchro.dto.UpstreamSynchroParameter;
import com.lteconsulting.offlinedemo.shared.synchro.dto.DownstreamSynchroParameter;
import com.lteconsulting.offlinedemo.shared.synchro.dto.SynchroResult;

public interface SyncServiceAsync
{
	public final static SyncServiceAsync service = (SyncServiceAsync) GWT.create( SyncService.class );

	void syncData( UpstreamSynchroParameter localHistory, DownstreamSynchroParameter cursor, AsyncCallback<SynchroResult> callback );
}
