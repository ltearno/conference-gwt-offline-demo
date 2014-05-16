package com.lteconsulting.offlinedemo.server;

import javax.persistence.EntityManager;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import com.lteconsulting.offlinedemo.server.Utils.Transaction;
import com.lteconsulting.offlinedemo.server.synchro.SynchroManager;
import com.lteconsulting.offlinedemo.shared.ApplicationSharedConstants;
import com.lteconsulting.offlinedemo.shared.SyncService;
import com.lteconsulting.offlinedemo.shared.synchro.dto.UpstreamSynchroParameter;
import com.lteconsulting.offlinedemo.shared.synchro.dto.DownstreamSynchroParameter;
import com.lteconsulting.offlinedemo.shared.synchro.dto.SynchroResult;

public class SyncServiceServlet extends RemoteServiceServlet implements SyncService
{
	private static final long serialVersionUID = 1L;

	@Override
	public SynchroResult syncData( final UpstreamSynchroParameter localHistory, final DownstreamSynchroParameter cursor )
	{
		return Utils.executeTransaction( new Transaction<SynchroResult>()
		{
			@Override
			public SynchroResult execute( EntityManager em )
			{
				SynchroManager syncer = new SynchroManager();

				syncer.setConfig( ApplicationSharedConstants.SYNCHRO_CONFIG );

				return syncer.syncData( em, localHistory, cursor );
			}
		} );
	}
}