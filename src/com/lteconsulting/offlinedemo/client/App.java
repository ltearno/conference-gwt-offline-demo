package com.lteconsulting.offlinedemo.client;

import com.google.gwt.core.client.EntryPoint;

/**
 * Entry point class for the application
 */
public class App implements EntryPoint
{
	@Override
	public void onModuleLoad()
	{
		// Initialize the database (ie : create or load existing)
		DataAccess.get().init();

		// start syncing between local and server database
		Synchronization.get().start();

		// start handling of the hash tag in the url for history management
		Navigate.start();
	}
}
