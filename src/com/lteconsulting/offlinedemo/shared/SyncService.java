package com.lteconsulting.offlinedemo.shared;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;
import com.lteconsulting.offlinedemo.shared.synchro.dto.UpstreamSynchroParameter;
import com.lteconsulting.offlinedemo.shared.synchro.dto.DownstreamSynchroParameter;
import com.lteconsulting.offlinedemo.shared.synchro.dto.SynchroResult;

@RemoteServiceRelativePath( "syncService" )
public interface SyncService extends RemoteService
{
	SynchroResult syncData( UpstreamSynchroParameter localHistory, DownstreamSynchroParameter cursor );
}
