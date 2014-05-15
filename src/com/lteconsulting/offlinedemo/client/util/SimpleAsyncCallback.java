package com.lteconsulting.offlinedemo.client.util;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;

public abstract class SimpleAsyncCallback<T> implements AsyncCallback<T>
{
	@Override
	public final void onFailure( Throwable caught )
	{
		// TODO : handle error !
		GWT.log( "RPC Exception: " + caught.toString() );
	}
}
