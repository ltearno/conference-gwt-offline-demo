package com.lteconsulting.offlinedemo.shared;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.lteconsulting.offlinedemo.shared.synchro.dto.ClientHistory;
import com.lteconsulting.offlinedemo.shared.synchro.dto.SyncCursor;
import com.lteconsulting.offlinedemo.shared.synchro.dto.SyncResult;

public interface SyncServiceAsync
{
	public final static SyncServiceAsync service = (SyncServiceAsync) GWT.create( SyncService.class );

	void syncData( ClientHistory localHistory, SyncCursor cursor, AsyncCallback<SyncResult> callback );
}
