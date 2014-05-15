package com.lteconsulting.offlinedemo.client.appcache;

import com.google.gwt.event.shared.GwtEvent;

public class AppCacheEvent extends GwtEvent<AppCacheEventHandler>
{
	public static final Type<AppCacheEventHandler> TYPE = new Type<AppCacheEventHandler>();

	private final AppCacheEventName status;

	public AppCacheEvent( AppCacheEventName status )
	{
		this.status = status;
	}

	@Override
	protected void dispatch( AppCacheEventHandler handler )
	{
		handler.onAppCacheStatus( status );
	}

	@Override
	public com.google.gwt.event.shared.GwtEvent.Type<AppCacheEventHandler> getAssociatedType()
	{
		return TYPE;
	}
}