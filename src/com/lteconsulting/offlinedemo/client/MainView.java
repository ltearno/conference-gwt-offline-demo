package com.lteconsulting.offlinedemo.client;

import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.lteconsulting.offlinedemo.client.appcache.AppCache;
import com.lteconsulting.offlinedemo.client.appcache.AppCacheEventHandler;
import com.lteconsulting.offlinedemo.client.appcache.AppCacheEventName;

public class MainView
{
	private static MainView INSTANCE;

	public static MainView get()
	{
		if( INSTANCE == null )
			INSTANCE = new MainView();

		return INSTANCE;
	}

	private SimplePanel mainPanel;
	private Anchor saveButton;
	private Anchor refreshButton;

	private MainView()
	{
		mainPanel = new SimplePanel();
		RootPanel.get( "main" ).add( mainPanel );

		saveButton = Anchor.wrap( Document.get().getElementById( "saveButton" ) );
		refreshButton = Anchor.wrap( Document.get().getElementById( "refreshButton" ) );

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

			case IDLE: //sun-o
				name = "check";
				break;

			case UPDATEREADY: //bolt
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

	private int nbTimesToBlink = 0;
	private Timer timer = new Timer()
	{
		@Override
		public void run()
		{
			String color = Document.get().getElementById( "refreshButton" ).getStyle().getColor();
			if( nbTimesToBlink <= 0 || color==null || "red".equals( color ) )
				Document.get().getElementById( "refreshButton" ).getStyle().clearColor();
			else
				Document.get().getElementById( "refreshButton" ).getStyle().setColor( "red" );

			if( nbTimesToBlink <= 0 )
				cancel();

			nbTimesToBlink--;
		}
	};

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
}
