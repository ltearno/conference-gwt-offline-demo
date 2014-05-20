package com.lteconsulting.offlinedemo.client.synchro;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.lteconsulting.offlinedemo.client.DataAccess;
import com.lteconsulting.offlinedemo.client.sql.SQLite;
import com.lteconsulting.offlinedemo.shared.SyncServiceAsync;
import com.lteconsulting.offlinedemo.shared.synchro.SynchroConfig;
import com.lteconsulting.offlinedemo.shared.synchro.dto.DownstreamSynchroParameter;
import com.lteconsulting.offlinedemo.shared.synchro.dto.SynchroResult;
import com.lteconsulting.offlinedemo.shared.synchro.dto.UpstreamSynchroParameter;

/*
 * Manages tables synchronization on the client side
 * It combines the two synchronization aspects :
 *  - Server to Client -> Downstream Synchronization
 *  - Client to Server -> Upstream Synchronization
 */
public class SynchroManager
{
	private final SQLite sqlDb = DataAccess.get().getSqlDb();

	private UpstreamSynchroManager upstreamSynchroManager;
	private DownstreamSynchroManager downstreamSynchroManager;

	public SynchroManager()
	{
	}

	/*
	 * Initialize the synchronization configuration
	 */
	public void setConfig( SynchroConfig config )
	{
		upstreamSynchroManager = new UpstreamSynchroManager( config, sqlDb );
		downstreamSynchroManager = new DownstreamSynchroManager( config, sqlDb );
	}

	/*
	 * Do a synchronization step of the database with the server.
	 * The integer returned in the onSuccess method is the number of records that were synchronized
	 * Note that two synchronizations are happening in the same call :
	 *  - Upstream = client to server
	 *  - Downstram = server to client
	 */
	public void doSynchro( final AsyncCallback<Integer> callback )
	{
		// find records that have been locally updated, created or deleted
		UpstreamSynchroParameter upstreamSynchroParameter = upstreamSynchroManager.getClientHistory();

		// compute current synchronization cursors, so that the server can update us with new or deleted records
		DownstreamSynchroParameter downstreamSynchroParameter = downstreamSynchroManager.getSynchroParameter();

		SyncServiceAsync.service.syncData( upstreamSynchroParameter, downstreamSynchroParameter, new AsyncCallback<SynchroResult>()
		{
			@Override
			public void onSuccess( SynchroResult result )
			{
				int res = processSyncResult( result );

				callback.onSuccess( res );
			}

			@Override
			public void onFailure( Throwable caught )
			{
				callback.onFailure( caught );
				GWT.log( "Failure when calling server", caught );
			}
		} );
	}

	// process the result of a synchronization call to the server
	// returns the number of changes that happened during the process
	private int processSyncResult( SynchroResult result )
	{
		if( result == null )
			return 0;

		// update local ids (local one were < 0 and have now been generated on the server side)
		int nbUpstreamCommits = upstreamSynchroManager.processSynchroResult( result.getUpstreamSynchroResult() );

		int nbDownstreamCommits = downstreamSynchroManager.processSynchroResult( result.getDownstreamSynchroResult() );

		int nbCommits = nbUpstreamCommits + nbDownstreamCommits;

		// save db if needed
		if( nbCommits > 0 )
			DataAccess.get().scheduleSaveDb();

		// produce a report
		if( nbCommits > 0 )
		{
			GWT.log( "Sync:" );
			GWT.log( nbUpstreamCommits + " local changes" );
			GWT.log( nbDownstreamCommits + " server changes" );
			GWT.log( "updateCursors: " + result.getDownstreamSynchroResult().getUpdatedUpdateCursors() );
			GWT.log( "deletionCursor: " + result.getDownstreamSynchroResult().getUpdatedDeletionCursor() );
		}

		return nbCommits;
	}

	public Integer getRealId( String table, Integer id )
	{
		return upstreamSynchroManager.getRealId( table, id );
	}
}
