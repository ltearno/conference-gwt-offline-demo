package com.lteconsulting.offlinedemo.client;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.Window;
import com.lteconsulting.offlinedemo.client.activity.Activity;
import com.lteconsulting.offlinedemo.client.activity.ArticleDetailActivity;
import com.lteconsulting.offlinedemo.client.activity.ArticlesListActivity;
import com.lteconsulting.offlinedemo.client.activity.OrderDetailActivity;
import com.lteconsulting.offlinedemo.client.activity.OrdersListActivity;

public class Navigate
{
	private static Activity currentActivity;

	static
	{
		MainView.get().addSaveClickHandler( new ClickHandler()
		{
			@Override
			public void onClick( ClickEvent event )
			{
				event.preventDefault();

				if( currentActivity != null )
					currentActivity.save();
			}
		} );

		MainView.get().addRefreshClickHandler( new ClickHandler()
		{
			@Override
			public void onClick( ClickEvent event )
			{
				event.preventDefault();

				if( currentActivity != null )
					currentActivity.refresh();
			}
		} );
	}

	public static void start()
	{
		History.addValueChangeHandler( new ValueChangeHandler<String>()
		{
			@Override
			public void onValueChange( ValueChangeEvent<String> event )
			{
				onNewToken( event.getValue() );
			}
		} );

		History.fireCurrentHistoryState();
	}

	public static void toOrdersList()
	{
		pushToken( "ol" );
	}

	private static void displayOrdersList()
	{
		start( new OrdersListActivity() );
	}

	public static void toOrderDetail( int orderId )
	{
		pushToken( "od_" + orderId );
	}

	private static void displayOrderDetail( int orderId )
	{
		start( new OrderDetailActivity( orderId ) );
	}

	public static void toArticleDetail( int articleId )
	{
		pushToken( "a_" + articleId );
	}

	private static void displayArticleDetail( int articleId )
	{
		start( new ArticleDetailActivity( articleId ) );
	}

	public static void toArticlesList()
	{
		pushToken( "al" );
	}

	private static void displayArticlesList()
	{
		start( new ArticlesListActivity() );
	}

	private static void pushToken( String token )
	{
		if( ! checkCanLeaveCurrentActivity() )
			return;

		if( History.getToken().equals(token) )
			onNewToken( token );
		else
			History.newItem( token, true );
	}

	private static boolean checkCanLeaveCurrentActivity()
	{
		if( currentActivity != null )
		{
			String cannotMessage = currentActivity.canStop();
			if( cannotMessage != null )
			{
				Window.alert( cannotMessage );
				return false;
			}
		}

		return true;
	}

	private static void onNewToken( String token )
	{
		if( ! checkCanLeaveCurrentActivity() )
			return;

		if( token==null || token.isEmpty() )
			displayOrdersList();
		else if( token.equals( "ol" ) )
			displayOrdersList();
		else if( token.equals( "al" ) )
			displayArticlesList();
		else if( token.startsWith( "od_" ) )
		{
			int orderId = Integer.parseInt( token.substring( 3 ) );
			displayOrderDetail( orderId );
		}
		else if( token.startsWith( "a_" ) )
		{
			int articleId = Integer.parseInt( token.substring( 2 ) );
			displayArticleDetail( articleId );
		}
		else
			displayOrdersList();
	}

	private static void start( Activity activity )
	{
		currentActivity = activity;
		activity.start( MainView.get().getMainPanel() );
	}
}
