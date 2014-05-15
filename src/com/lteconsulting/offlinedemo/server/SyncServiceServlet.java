package com.lteconsulting.offlinedemo.server;

import javax.persistence.EntityManager;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import com.lteconsulting.offlinedemo.server.Utils.Transaction;
import com.lteconsulting.offlinedemo.server.synchro.SynchroManager;
import com.lteconsulting.offlinedemo.shared.ApplicationSharedConstants;
import com.lteconsulting.offlinedemo.shared.SyncService;
import com.lteconsulting.offlinedemo.shared.synchro.dto.ClientHistory;
import com.lteconsulting.offlinedemo.shared.synchro.dto.SyncCursor;
import com.lteconsulting.offlinedemo.shared.synchro.dto.SyncResult;

public class SyncServiceServlet extends RemoteServiceServlet implements SyncService
{
	private static final long serialVersionUID = 1L;

	@Override
	public SyncResult syncData( final ClientHistory localHistory, final SyncCursor cursor )
	{
		return Utils.executeTransaction( new Transaction<SyncResult>()
		{
			@Override
			public SyncResult execute( EntityManager em )
			{
				SynchroManager syncer = new SynchroManager();

				syncer.setConfig( ApplicationSharedConstants.SYNCHRO_CONFIG );

				return syncer.syncData( em, localHistory, cursor );
			}
		} );
	}
}