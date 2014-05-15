package com.lteconsulting.offlinedemo.client.view;

import java.util.List;

import com.google.gwt.cell.client.ActionCell;
import com.google.gwt.cell.client.EditTextCell;
import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.view.client.ListDataProvider;
import com.lteconsulting.offlinedemo.client.DataAccess;
import com.lteconsulting.offlinedemo.client.Navigate;
import com.lteconsulting.offlinedemo.client.dto.Order;

public class OrdersListWidget extends Composite
{
	private ListDataProvider<Order> dataProvider;

	public OrdersListWidget()
	{
		CellTable<Order> table = new CellTable<Order>( 100, CellTableResources.INSTANCE );
		table.getElement().addClassName( "table" );
		table.getElement().addClassName( "table-striped" );
		table.getElement().addClassName( "table-bordered" );
		table.getElement().addClassName( "table-hover" );

		table.addColumn( new TextColumn<Order>()
		{
			@Override
			public String getValue( Order object )
			{
				return String.valueOf( object.getId() );
			}
		}, "ID" );

		Column<Order, String> dateColumn = new Column<Order, String>( new EditTextCell() )
		{
			@Override
			public String getValue( Order object )
			{
				return object.getDate();
			}
		};
		dateColumn.setFieldUpdater( new FieldUpdater<Order, String>()
		{
			@Override
			public void update( int index, Order object, String value )
			{
				object.setDate( value );
			}
		} );
		table.addColumn( dateColumn, "Date" );

		EditTextCell cell = new EditTextCell();
		Column<Order, String> addressCodeColumn = new Column<Order, String>( cell )
		{
			@Override
			public String getValue( Order object )
			{
				return String.valueOf( object.getAddressCode() );
			}
		};
		addressCodeColumn.setFieldUpdater( new FieldUpdater<Order, String>()
		{
			@Override
			public void update( int index, Order object, String value )
			{
				object.setAddressCode( value );
			}
		} );
		table.addColumn( addressCodeColumn, "Address Code" );

		ActionCell<Order> seeDetails = new ActionCell<Order>( "Details", new ActionCell.Delegate<Order>()
		{
			@Override
			public void execute( Order object )
			{
				Navigate.toOrderDetail( object.getId() );
			}
		} );

		table.addColumn( new Column<Order, Order>( seeDetails )
		{
			@Override
			public Order getValue( Order object )
			{
				return object;
			}
		}, "" );

		ActionCell<Order> deleteActionCell = new ActionCell<Order>( "Delete", new ActionCell.Delegate<Order>()
		{
			@Override
			public void execute( Order object )
			{
				DataAccess.get().deleteOrder( object.getId() );
				dataProvider.getList().remove( object );
			}
		} );

		table.addColumn( new Column<Order, Order>( deleteActionCell )
		{
			@Override
			public Order getValue( Order object )
			{
				return object;
			}
		}, "" );

		dataProvider = new ListDataProvider<Order>();
		dataProvider.addDataDisplay( table );

		Button addButton = new Button( "Add" );

		FlowPanel flow = new FlowPanel();
		flow.add( table );
		flow.add( addButton );

		initWidget( flow );

		addButton.addClickHandler( new ClickHandler()
		{
			@Override
			public void onClick( ClickEvent event )
			{
				Order a = new Order();
				dataProvider.getList().add( a );
			}
		} );
	}

	public void setData( List<Order> orders )
	{
		dataProvider.setList( orders );
	}

	public List<Order> getData()
	{
		return dataProvider.getList();
	}
}
