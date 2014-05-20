package com.lteconsulting.offlinedemo.client;

import com.google.gwt.core.client.JsArrayInteger;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.lteconsulting.offlinedemo.client.appcache.AppCache;
import com.lteconsulting.offlinedemo.client.appcache.AppCacheEventHandler;
import com.lteconsulting.offlinedemo.client.appcache.AppCacheEventName;
import com.lteconsulting.offlinedemo.client.util.Base64Coder;

/*
 * This class controls the application's main UI
 * It has got a SimplePanel where Activities panels go into
 */
public class MainView
{
	private static MainView INSTANCE;

	public static MainView get()
	{
		if( INSTANCE == null )
			INSTANCE = new MainView();

		return INSTANCE;
	}

	// widgets
	private SimplePanel mainPanel;
	private Anchor saveButton;
	private Anchor refreshButton;
	private Anchor clearLocalStorageButton;
	private Anchor exportDb;

	private MainView()
	{
		mainPanel = new SimplePanel();
		RootPanel.get( "main" ).add( mainPanel );

		// wrap some html elements into gwt widgets
		saveButton = Anchor.wrap( Document.get().getElementById( "saveButton" ) );
		refreshButton = Anchor.wrap( Document.get().getElementById( "refreshButton" ) );
		clearLocalStorageButton = Anchor.wrap( Document.get().getElementById( "clearLocalStorage" ) );
		exportDb = Anchor.wrap( Document.get().getElementById( "exportDb" ) );

		initHandlers();
	}

	public AcceptsOneWidget getMainPanel()
	{
		return mainPanel;
	}

	public void setActivityTitle( String title )
	{
		Document.get().getElementById( "activityTitle" ).setInnerText( title );
	}

	public void setOnline( boolean isOnline )
	{
		Document.get().getElementById( "onlineMarker" ).getStyle().setColor( isOnline ? "green" : "grey" );
	}

	// Makes the refresh button blink in red, so that the user knows he should be clicking on the refresh button
	public void blinkRefresh()
	{
		nbTimesToBlink = 10;
		timer.scheduleRepeating( 500 );
	}

	public HandlerRegistration addSaveClickHandler( ClickHandler handler )
	{
		return saveButton.addClickHandler( handler );
	}

	public HandlerRegistration addRefreshClickHandler( ClickHandler handler )
	{
		return refreshButton.addClickHandler( handler );
	}

	private int nbTimesToBlink = 0;
	private Timer timer = new Timer()
	{
		@Override
		public void run()
		{
			String color = Document.get().getElementById( "refreshButton" ).getStyle().getColor();
			if( nbTimesToBlink <= 0 || color == null || "red".equals( color ) )
				Document.get().getElementById( "refreshButton" ).getStyle().clearColor();
			else
				Document.get().getElementById( "refreshButton" ).getStyle().setColor( "red" );

			if( nbTimesToBlink <= 0 )
				cancel();

			nbTimesToBlink--;
		}
	};

	private void refreshAppCacheStatus()
	{
		boolean isActive = false;
		String name = "";
		String text = null;

		switch( AppCache.getIfSupported().getStatus() )
		{
			case CHECKING:// cogs
				name = "cogs";
				break;

			case DOWNLOADING:// exchange
				name = "exchange";
				break;

			case IDLE: // sun-o
				name = "check";
				break;

			case UPDATEREADY: // bolt
				text = "UPDATE READY, REFRESH PLEASE";
				name = "bolt";
				break;

			case OBSOLETE: // exclamation
				text = "OBSOLETE";
				name = "exclamation";
				break;

			case UNCACHED:
				text = "UNCACHED";
				name = "exclamation";
				break;

			case UNKNOWN:
				text = "UNKNOWN";
				name = "exclamation";
				break;
		}

		setAppCacheStatus( isActive, name, text );
	}

	private void setAppCacheStatus( boolean isActive, String name, String text )
	{
		Element elem = Document.get().getElementById( "appCacheIndicator" );
		elem.setClassName( "fa" );
		elem.addClassName( "fa-" + name );

		elem.getStyle().setColor( isActive ? "red" : "grey" );

		if( text != null )
			elem.setInnerText( " " + text );
		else
			elem.setInnerText( "" );
	}

	private void initHandlers()
	{
		initClearLocalStorageButtonHandler();

		initExportDbButton();

		initAppCacheEventHandler();
	}

	private void initClearLocalStorageButtonHandler()
	{
		// button to clear local storage
		clearLocalStorageButton.addClickHandler( new ClickHandler()
		{
			@Override
			public void onClick( ClickEvent event )
			{
				event.preventDefault();

				// clears all data stored locally
				DataAccess.get().cleanLocalStorage();

				Window.alert( "Local storage cleaned !" );
			}
		} );
	}

	private void initExportDbButton()
	{
		// button to export database into a file
		exportDb.addClickHandler( new ClickHandler()
		{
			@Override
			public void onClick( ClickEvent event )
			{
				// when the user clicks, we serialize the database content into
				// a Base 64 encoded Data URI
				// this leads the browser to show the "download file" dialog

				// export the SQLite database into an integer array
				JsArrayInteger data = DataAccess.get().exportDbData();

				// convert it into a Base64 stream
				byte[] bytes = new byte[data.length()];
				for( int i = 0; i < bytes.length; i++ )
					bytes[i] = (byte) data.get( i );
				String encoded = new String( Base64Coder.encode( bytes ) );

				// we change the element's attributes before the default event
				// handling happens
				// so that the browser shows a file download, although all
				// happens locally in the browser.
				// Please note that i'm not sure that this will support 5Mb
				// files.
				exportDb.getElement().setAttribute( "download", "OfflineDemo.db" );
				exportDb.setHref( "data:application/octet-stream;charset=UTF-8;base64," + encoded );
				exportDb.setTarget( "_blank" );
			}
		} );
	}

	private void initAppCacheEventHandler()
	{
		// Subscribe to the Application Cache events
		AppCache.getIfSupported().addAppCacheEventHandler( new AppCacheEventHandler()
		{
			@Override
			public void onAppCacheStatus( AppCacheEventName status )
			{
				refreshAppCacheStatus();
			}
		} );
		refreshAppCacheStatus();
	}
}
