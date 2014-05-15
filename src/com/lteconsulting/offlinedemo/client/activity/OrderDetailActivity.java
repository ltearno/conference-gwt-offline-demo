package com.lteconsulting.offlinedemo.client.activity;

import java.util.List;

import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.gwt.user.client.ui.FlowPanel;
import com.lteconsulting.offlinedemo.client.DataAccess;
import com.lteconsulting.offlinedemo.client.MainView;
import com.lteconsulting.offlinedemo.client.dto.Article;
import com.lteconsulting.offlinedemo.client.dto.OrderItem;
import com.lteconsulting.offlinedemo.client.util.SimpleAsyncCallback;
import com.lteconsulting.offlinedemo.client.view.OrderDetailWidget;
import com.lteconsulting.offlinedemo.client.view.RecentArticles;

public class OrderDetailActivity implements Activity
{
	private int orderId;
	private OrderDetailWidget itemListView;

	public OrderDetailActivity( int orderId )
	{
		this.orderId = orderId;
	}

	@Override
	public void start( AcceptsOneWidget container )
	{
		MainView.get().setActivityTitle( "Order " + orderId + " detail" );

		RecentArticles recentArticles = new RecentArticles( new SimpleAsyncCallback<Article>()
		{
			@Override
			public void onSuccess( Article result )
			{
				OrderItem a = new OrderItem( orderId );
				a.setQuantity( 1 );
				a.setArticleId( result.getId() );
				a.setUnitPrice( result.getPrice() );
				a.computeAmount();

				itemListView.getData().add( a );
			}
		} );

		itemListView = new OrderDetailWidget( orderId );

		FlowPanel flow = new FlowPanel();

		flow.add( recentArticles );
		flow.add( itemListView );

		container.setWidget( flow );

		load();
	}

	@Override
	public void refresh()
	{
		load();
	}

	@Override
	public void save()
	{
		for( OrderItem a : itemListView.getData() )
		{
			if( (! a.isChanged()) && (a.getId()!=0) )
				continue;

			DataAccess.get().updateOrderItem( a );
		}
	}

	@Override
	public String canStop()
	{
		for( OrderItem a : itemListView.getData() )
		{
			if( (a.isChanged()) || (a.getId()==0) )
				return "You have made some changes, please save them or discard them before leaving !";
		}

		return null;
	}

	void load()
	{
		List<OrderItem> orderItems = DataAccess.get().getOrderItems( orderId );
		itemListView.setData( orderItems );
	}
}
