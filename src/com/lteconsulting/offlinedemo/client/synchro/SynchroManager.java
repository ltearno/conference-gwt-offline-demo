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
 */
public class SynchroManager
{
	private final SQLite sqlDb = DataAccess.get().getSqlDb();

	private UpstreamSynchroManager upstreamSynchroManager;
	private DownstreamSynchroManager downstreamSynchroManager;

	public SynchroManager()
	{
	}

	public void setConfig( SynchroConfig config )
	{
		upstreamSynchroManager = new UpstreamSynchroManager( config, sqlDb );
		downstreamSynchroManager = new DownstreamSynchroManager( config, sqlDb );
	}

	/*
	 * Do a synchronization step of the database with the server.
	 */
	public void doSynchro( final AsyncCallback<Integer> callback )
	{
		// find records that have been locally updated, created or deleted
		final UpstreamSynchroParameter upstreamSynchroParameter = upstreamSynchroManager.getClientHistory();

		// compute current synchronization cursors, so that the server can update us with new or deleted records
		DownstreamSynchroParameter downstreamSynchroParameter = downstreamSynchroManager.getSynchroParameter();

		SyncServiceAsync.service.syncData( upstreamSynchroParameter, downstreamSynchroParameter, new AsyncCallback<SynchroResult>()
		{
			@Override
			public void onSuccess( SynchroResult result )
			{
				int res = processSyncResult( result, upstreamSynchroParameter );

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
	private int processSyncResult( SynchroResult result, UpstreamSynchroParameter clientHistory )
	{
		if( result == null )
			return 0;

		// update local ids (local one were < 0 and have now been generated on the server side)
		upstreamSynchroManager.processUpstreamSynchroResult( result.getUpstreamSynchroResult() );

		downstreamSynchroManager.processSynchroResult( result.getDownstreamSynchroResult() );

		// save db if needed
		int nbLocalCommits = clientHistory.getNbChanges();
		int nbServerChanges = result.getDownstreamSynchroResult().getNbServerChanges();
		if( nbLocalCommits + nbServerChanges > 0 )
			DataAccess.get().scheduleSaveDb();

		// produce a report
		if( nbLocalCommits + nbServerChanges > 0 )
		{
			GWT.log( "Sync:" );
			GWT.log( nbLocalCommits + " local changes" );
			GWT.log( nbServerChanges + " server changes" );
			GWT.log( "updateCursors: " + result.getDownstreamSynchroResult().getUpdatedUpdateCursors() );
			GWT.log( "deletionCursor: " + result.getDownstreamSynchroResult().getUpdatedDeletionCursor() );
		}

		return nbLocalCommits + nbServerChanges;
	}

	public Integer getRealId( String table, Integer id )
	{
		return upstreamSynchroManager.getRealId( table, id );
	}
}
