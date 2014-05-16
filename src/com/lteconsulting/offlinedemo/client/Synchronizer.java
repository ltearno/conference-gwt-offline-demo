package com.lteconsulting.offlinedemo.client;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.lteconsulting.offlinedemo.client.synchro.SynchroManager;
import com.lteconsulting.offlinedemo.client.util.Delayer;
import com.lteconsulting.offlinedemo.shared.ApplicationSharedConstants;

public class Synchronizer
{
	private static Synchronizer _instance;

	private SynchroManager mng;
	private boolean isOnline;

	public static Synchronizer get()
	{
		if( _instance == null )
			_instance = new Synchronizer();

		return _instance;
	}

	private Synchronizer()
	{
		// instantiate synchronization manager
		mng = new SynchroManager();

		// synchro configuration
		mng.setConfig( ApplicationSharedConstants.SYNCHRO_CONFIG );

		// consider as online first
		setOnline( true );
	}

	public void start()
	{
		_instance.checkWithServer();
	}

	public Integer getRealId( String table, Integer id )
	{
		return mng.getRealId( table, id );
	}

	private void checkWithServer()
	{
		// do a sync
		mng.doSynchro( new AsyncCallback<Integer>()
		{
			@Override
			public void onSuccess( Integer result )
			{
				setOnline( true );

				// schedule another sync
				if( result > 0 )
					delayer.triggerImmediately();
				else
					delayer.trigger();

				if( result > 0 )
					MainView.get().blinkRefresh();
			}

			@Override
			public void onFailure( Throwable caught )
			{
				setOnline( false );

				// schedule a ping
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
