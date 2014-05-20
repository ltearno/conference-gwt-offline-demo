package com.lteconsulting.offlinedemo.client.activity;

import java.util.List;

import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.lteconsulting.offlinedemo.client.DataAccess;
import com.lteconsulting.offlinedemo.client.MainView;
import com.lteconsulting.offlinedemo.client.dto.Order;
import com.lteconsulting.offlinedemo.client.view.OrdersListWidget;

/*
 * Allows to list, remove and add Orders
 */
public class OrdersListActivity implements Activity
{
	OrdersListWidget view = new OrdersListWidget();

	@Override
	public void start( AcceptsOneWidget container )
	{
		MainView.get().setActivityTitle( "Orders" );

		container.setWidget( view );

		loadOrders();
	}

	@Override
	public void refresh()
	{
		loadOrders();
	}

	@Override
	public void save()
	{
		for( Order a : view.getData() )
		{
			if( (! a.isChanged()) && (a.getId()!=0) )
				continue;

			DataAccess.get().updateOrder( a );
		}
	}

	@Override
	public String canStop()
	{
		for( Order a : view.getData() )
		{
			if( (a.isChanged()) || (a.getId()==0) )
				return "You have made some changes, please save them or discard them before leaving !";
		}

		return null;
	}

	private void loadOrders()
	{
		List<Order> orders = DataAccess.get().getOrders();
		view.setData( orders );
	}
}
