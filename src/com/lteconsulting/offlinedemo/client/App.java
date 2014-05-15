package com.lteconsulting.offlinedemo.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.JsArrayInteger;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.RootPanel;
import com.lteconsulting.offlinedemo.client.util.Base64Coder;

/**
 * Entry point classes define <code>onModuleLoad()</code>.
 */
public class App implements EntryPoint
{
	@Override
	public void onModuleLoad()
	{
		DataAccess.get().init();

		// button to clear local storage
		Anchor clearLocalStorage = Anchor.wrap( RootPanel.get( "clearLocalStorage" ).getElement() );
		clearLocalStorage.addClickHandler( new ClickHandler()
		{
			@Override
			public void onClick( ClickEvent event )
			{
				event.preventDefault();

				DataAccess.get().cleanLocalStorage();

				Window.alert( "Local storage cleaned !" );
			}
		} );

		// button to export database into a file
		final Anchor exportDb = Anchor.wrap( RootPanel.get( "exportDb" ).getElement() );
		exportDb.addClickHandler( new ClickHandler()
		{
			@Override
			public void onClick( ClickEvent event )
			{
				// we change the element's attributes before the default event handling happens...
				JsArrayInteger data = DataAccess.get().exportDbData();

				byte[] bytes = new byte[data.length()];
				for( int i=0; i<bytes.length; i++ )
					bytes[i] = (byte) data.get( i );
				String encoded = new String( Base64Coder.encode( bytes ) );

				exportDb.getElement().setAttribute( "download", "OfflineDemo.db" );
				exportDb.setHref( "data:application/octet-stream;charset=UTF-8;base64," + encoded );
				exportDb.setTarget( "_blank" );
			}
		} );

		// start syncing between local and server database
		Synchronizer.get().start();

		// start handling of the hash tag in the url for history management (browser's back button for instance)
		Navigate.start();
	}
}
