package com.lteconsulting.offlinedemo.client.appcache;

import com.google.gwt.core.client.JavaScriptObject;

/*
 * Wrapper to the Application Cache API
 */
final class AppCacheImpl extends JavaScriptObject
{
	public interface Callback
	{
		void handleAppCacheEvent( AppCacheEventName event );
	}

	// status
	private static final int UNCACHED = 0;
	private static final int IDLE = 1;
	private static final int CHECKING = 2;
	private static final int DOWNLOADING = 3;
	private static final int UPDATEREADY = 4;
	private static final int OBSOLETE = 5;
	private static final int UNKNOWN = -1;

	protected AppCacheImpl()
	{
	}

	public static native final AppCacheImpl getIfSupported()
	/*-{
		return $wnd.applicationCache || null;
	}-*/;

	public final AppCacheStatus getStatus()
	{
		int s = getStatusImpl();
		switch( s )
		{
		case UNCACHED:
			return AppCacheStatus.UNCACHED;
		case IDLE:
			return AppCacheStatus.IDLE;
		case CHECKING:
			return AppCacheStatus.CHECKING;
		case DOWNLOADING:
			return AppCacheStatus.DOWNLOADING;
		case UPDATEREADY:
			return AppCacheStatus.UPDATEREADY;
		case OBSOLETE:
			return AppCacheStatus.OBSOLETE;
		default:
			return AppCacheStatus.UNKNOWN;
		}
	}

	private final native int getStatusImpl()
	/*-{
		return this.status;
	}-*/;

	// to launch an app cache update
	public final native void update()
	/*-{
		this.update();
	}-*/;

	// To be used when UPDATEREADY status is reached,
	// new version of the cache will be used on next reload
	public final native void swap()
	/*-{
		this.swapCache();
	}-*/;

	public final native void registerEvents( Callback callback )
	/*-{
		handleCached = function(s) {
			callback.@com.lteconsulting.offlinedemo.client.appcache.AppCacheImpl.Callback::handleAppCacheEvent(Lcom/lteconsulting/offlinedemo/client/appcache/AppCacheEventName;)(@com.lteconsulting.offlinedemo.client.appcache.AppCacheEventName::CACHED);
		};

		this.addEventListener('cached', handleCached, false);

		handleChecking = function(s) {
			callback.@com.lteconsulting.offlinedemo.client.appcache.AppCacheImpl.Callback::handleAppCacheEvent(Lcom/lteconsulting/offlinedemo/client/appcache/AppCacheEventName;)(@com.lteconsulting.offlinedemo.client.appcache.AppCacheEventName::CHECKING);
		};

		this.addEventListener('checking', handleChecking, false);

		handleDownloading = function(s) {
			callback.@com.lteconsulting.offlinedemo.client.appcache.AppCacheImpl.Callback::handleAppCacheEvent(Lcom/lteconsulting/offlinedemo/client/appcache/AppCacheEventName;)(@com.lteconsulting.offlinedemo.client.appcache.AppCacheEventName::DOWNLOADING);
		};

		this.addEventListener('downloading', handleDownloading, false);

		handleError = function(s) {
			callback.@com.lteconsulting.offlinedemo.client.appcache.AppCacheImpl.Callback::handleAppCacheEvent(Lcom/lteconsulting/offlinedemo/client/appcache/AppCacheEventName;)(@com.lteconsulting.offlinedemo.client.appcache.AppCacheEventName::ERROR);
		};

		this.addEventListener('error', handleError, false);

		handleNoUpdate = function(s) {
			callback.@com.lteconsulting.offlinedemo.client.appcache.AppCacheImpl.Callback::handleAppCacheEvent(Lcom/lteconsulting/offlinedemo/client/appcache/AppCacheEventName;)(@com.lteconsulting.offlinedemo.client.appcache.AppCacheEventName::NOUPDATE);
		};

		this.addEventListener('noupdate', handleNoUpdate, false);

		handleObsolete = function(s) {
			callback.@com.lteconsulting.offlinedemo.client.appcache.AppCacheImpl.Callback::handleAppCacheEvent(Lcom/lteconsulting/offlinedemo/client/appcache/AppCacheEventName;)(@com.lteconsulting.offlinedemo.client.appcache.AppCacheEventName::OBSOLETE);
		};

		this.addEventListener('obsolete', handleObsolete, false);

		handleProgress = function(s) {
			callback.@com.lteconsulting.offlinedemo.client.appcache.AppCacheImpl.Callback::handleAppCacheEvent(Lcom/lteconsulting/offlinedemo/client/appcache/AppCacheEventName;)(@com.lteconsulting.offlinedemo.client.appcache.AppCacheEventName::PROGRESS);
		};

		this.addEventListener('progress', handleProgress, false);

		handleUpdateReady = function(s) {
			callback.@com.lteconsulting.offlinedemo.client.appcache.AppCacheImpl.Callback::handleAppCacheEvent(Lcom/lteconsulting/offlinedemo/client/appcache/AppCacheEventName;)(@com.lteconsulting.offlinedemo.client.appcache.AppCacheEventName::UPDATEREADY);
		};

		this.addEventListener('updateready', handleUpdateReady, false);
	}-*/;
}
