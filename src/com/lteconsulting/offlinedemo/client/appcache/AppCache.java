package com.lteconsulting.offlinedemo.client.appcache;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.event.shared.SimpleEventBus;

/*
 * Front class allowing to control the Application Cache
 */
public class AppCache
{
	private static AppCache _INSTANCE;

	private AppCacheImpl impl;
	private EventBus eventBus = new SimpleEventBus();

	public static AppCache getIfSupported()
	{
		if( _INSTANCE==null && AppCacheImpl.getIfSupported()!=null )
			_INSTANCE = new AppCache();

		return _INSTANCE;
	}

	private AppCache()
	{
		impl = AppCacheImpl.getIfSupported();
		impl.registerEvents( new AppCacheImpl.Callback()
		{
			@Override
			public void handleAppCacheEvent( AppCacheEventName event )
			{
				GWT.log( event.name() );
				eventBus.fireEvent( new AppCacheEvent( event ) );
			}
		} );
	}

	public AppCacheStatus getStatus()
	{
		return impl.getStatus();
	}

	public void update()
	{
		impl.update();
	}

	public void swap()
	{
		impl.swap();
	}

	public HandlerRegistration addAppCacheEventHandler( AppCacheEventHandler handler )
	{
		return eventBus.addHandler( AppCacheEvent.TYPE, handler );
	}
}
