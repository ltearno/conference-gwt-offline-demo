package com.lteconsulting.offlinedemo.client;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.lteconsulting.offlinedemo.client.synchro.SynchroManager;
import com.lteconsulting.offlinedemo.client.util.Delayer;
import com.lteconsulting.offlinedemo.shared.ApplicationSharedConstants;

public class Synchronization
{
	private static Synchronization _instance;

	private SynchroManager mng;
	private boolean isOnline;

	public static Synchronization get()
	{
		if( _instance == null )
			_instance = new Synchronization();

		return _instance;
	}

	private Synchronization()
	{
		// instantiate synchronization manager
		mng = new SynchroManager();

		// synchro configuration
		mng.setConfig( ApplicationSharedConstants.SYNCHRO_CONFIG );

		// when starting, consider the application as online
		setOnline( true );
	}

	// Start the synchronization process
	public void start()
	{
		_instance.checkWithServer();
	}

	/*
	 * Because of synchronization, local DTOs may have been created with a local negative id.
	 * When the synchronization happens, those negative ids are converted into positive ones by the server.
	 * This method allows to retreive the new id of synchronized records.
	 */
	public Integer getRealId( String table, Integer id )
	{
		return mng.getRealId( table, id );
	}

	private void checkWithServer()
	{
		// do a sync
		mng.doSynchro( new AsyncCallback<Integer>()
		{
			/*
			 * Handles a synchronization RPC call success.
			 */
			@Override
			public void onSuccess( Integer result )
			{
				// First, we change our status to "Online" since the server was reachable
				setOnline( true );

				// if some records were synchronized, schedule another RPC call immediateley
				if( result > 0 )
					delayer.triggerImmediately();
				// otherwise, let the server breath a bit before checking another syncrhonization
				else
					delayer.trigger();

				// When some records were synchronized, makes the refresh button blink so that user knows local data was updated
				if( result > 0 )
					MainView.get().blinkRefresh();
			}

			/*
			 * Handles a synchronization RCP call failure. This means that we go in the "OFFLINE" mode
			 * @see com.google.gwt.user.client.rpc.AsyncCallback#onFailure(java.lang.Throwable)
			 */
			@Override
			public void onFailure( Throwable caught )
			{
				setOnline( false );

				// schedule another synchronization RPC call
				delayer.trigger();
			}
		});
	}

	private void setOnline( boolean isOnline )
	{
		if( this.isOnline == isOnline )
			return;

		MainView.get().setOnline( isOnline );

		this.isOnline = isOnline;
	}

	private Delayer delayer = new Delayer( 5000 )
	{
		@Override
		protected void onExecute()
		{
			checkWithServer();
		}
	};
}
