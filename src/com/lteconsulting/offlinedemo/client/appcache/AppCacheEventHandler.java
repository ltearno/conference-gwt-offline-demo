package com.lteconsulting.offlinedemo.client.appcache;

import com.google.gwt.event.shared.EventHandler;

public interface AppCacheEventHandler extends EventHandler
{
	void onAppCacheStatus( AppCacheEventName status );
}