package com.lteconsulting.offlinedemo.shared;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;
import com.lteconsulting.offlinedemo.shared.synchro.dto.ClientHistory;
import com.lteconsulting.offlinedemo.shared.synchro.dto.SyncCursor;
import com.lteconsulting.offlinedemo.shared.synchro.dto.SyncResult;

@RemoteServiceRelativePath( "syncService" )
public interface SyncService extends RemoteService
{
	SyncResult syncData( ClientHistory localHistory, SyncCursor cursor );
}
