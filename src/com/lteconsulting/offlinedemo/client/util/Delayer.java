package com.lteconsulting.offlinedemo.client.util;

import com.google.gwt.user.client.Timer;

/*
 * A class to delay execution.
 * you call trigger() and it will call onExecute() xxx milliseconds after (only once for many calls of the trigger() method)
 */

public abstract class Delayer
{
	protected abstract void onExecute();

	private int milliseconds;
	private boolean fTriggered = false;

	public Delayer( int milliseconds )
	{
		this.milliseconds = milliseconds;
	}

	public void trigger()
	{
		// if an action is already registered, postpone it
		if( fTriggered )
		{
			reallyDoTimer.cancel();
			reallyDoTimer.schedule( milliseconds );
			return;
		}

		// schedule action ...
		fTriggered = true;
		reallyDoTimer.schedule( milliseconds );
	}

	public void triggerImmediately()
	{
		// if an action is already registered, postpone it
		if( fTriggered )
		{
			reallyDoTimer.cancel();
			reallyDoTimer.schedule( 0 );
			return;
		}

		// schedule action ...
		fTriggered = true;
		reallyDoTimer.schedule( 0 );
	}

	private Timer reallyDoTimer = new Timer()
	{
		@Override
		public void run()
		{
			fTriggered = false;
			reallyDoTimer.cancel();

			onExecute();
		}
	};
}
