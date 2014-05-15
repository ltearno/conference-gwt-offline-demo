package com.lteconsulting.offlinedemo.client.view;

import java.util.HashMap;
import java.util.List;

import com.google.gwt.cell.client.ActionCell;
import com.google.gwt.cell.client.EditTextCell;
import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.view.client.ListDataProvider;
import com.google.gwt.view.client.ProvidesKey;
import com.lteconsulting.offlinedemo.client.DataAccess;
import com.lteconsulting.offlinedemo.client.Navigate;
import com.lteconsulting.offlinedemo.client.dto.Article;
import com.lteconsulting.offlinedemo.client.dto.OrderItem;

public class OrderDetailWidget extends Composite
{
	private ListDataProvider<OrderItem> dataProvider;

	public OrderDetailWidget( final int orderId )
	{
		CellTable<OrderItem> table = new CellTable<OrderItem>( 100, CellTableResources.INSTANCE, KEY_PROVIDER );
		table.getElement().addClassName( "table" );
		table.getElement().addClassName( "table-striped" );
		table.getElement().addClassName( "table-bordered" );
		table.getElement().addClassName( "table-hover" );

		table.addColumn( new TextColumn<OrderItem>()
		{
			@Override
			public String getValue( OrderItem object )
			{
				Article a = getArticleForOrderItem( object );
				if( a == null )
					return "-";

				return a.getCode();
			}
		}, "Item code" );

		HashMap<Integer, String> articles = new HashMap<Integer, String>();
		for( Article article : DataAccess.get().getArticles() )
			articles.put( article.getId(), article.getName() );
		SelectionCell itemCell = new SelectionCell( articles );
		Column<OrderItem, Integer> itemColumn = new Column<OrderItem, Integer>( itemCell )
		{
			@Override
			public Integer getValue( OrderItem object )
			{
				return object.getArticleId();
			}
		};
		itemColumn.setFieldUpdater( new FieldUpdater<OrderItem, Integer>()
		{
			@Override
			public void update( int index, OrderItem object, Integer value )
			{
				object.setArticleId( value );

				Article article = DataAccess.get().getArticle( value );
				if( object.getQuantity() == 0 )
					object.setQuantity( 1 );
				object.setUnitPrice( article.getPrice() );
				object.computeAmount();

				dataProvider.getList().set( index, dataProvider.getList().get( index ) );
				dataProvider.flush();
			}
		} );
		table.addColumn( itemColumn, "Item" );

		final EditTextCell qtyCell = new EditTextCell();
		Column<OrderItem, String> qtyColumn = new Column<OrderItem, String>( qtyCell )
		{
			@Override
			public String getValue( OrderItem object )
			{
				return String.valueOf( object.getQuantity() );
			}
		};
		qtyColumn.setFieldUpdater( new FieldUpdater<OrderItem, String>()
		{
			@Override
			public void update( int index, OrderItem object, String value )
			{
				try
				{
					object.setQuantity( Integer.parseInt( value ) );
					object.computeAmount();
				}
				catch( Exception e )
				{
					qtyCell.clearViewData( KEY_PROVIDER.getKey( object ) );
					Window.alert( "You must enter a number !" );
				}

				dataProvider.getList().set( index, dataProvider.getList().get( index ) );
				dataProvider.flush();
			}
		} );
		table.addColumn( qtyColumn, "Quantity" );

		Column<OrderItem, String> unitPriceColumn = new Column<OrderItem, String>( new EditTextCell() )
		{
			@Override
			public String getValue( OrderItem object )
			{
				return String.valueOf( object.getUnitPrice() );
			}
		};
		unitPriceColumn.setFieldUpdater( new FieldUpdater<OrderItem, String>()
		{
			@Override
			public void update( int index, OrderItem object, String value )
			{
				try
				{
					object.setUnitPrice( Integer.parseInt( value ) );
				}
				catch( Exception e )
				{
				}

				object.computeAmount();

				dataProvider.getList().set( index, dataProvider.getList().get( index ) );
				dataProvider.flush();
			}
		} );
		table.addColumn( unitPriceColumn, "Unit price" );

		Column<OrderItem, String> amountColumn = new Column<OrderItem, String>( new EditTextCell() )
		{
			@Override
			public String getValue( OrderItem object )
			{
				return String.valueOf( object.getAmount() );
			}
		};
		amountColumn.setFieldUpdater( new FieldUpdater<OrderItem, String>()
		{
			@Override
			public void update( int index, OrderItem object, String value )
			{
				try
				{
					object.setAmount( Integer.parseInt( value ) );
				}
				catch( Exception e )
				{
				}

				dataProvider.getList().set( index, dataProvider.getList().get( index ) );
				dataProvider.flush();
			}
		} );
		table.addColumn( amountColumn, "Amount" );

		ActionCell<OrderItem> seeDetails = new ActionCell<OrderItem>( "Article details", new ActionCell.Delegate<OrderItem>()
		{
			@Override
			public void execute( OrderItem object )
			{
				Navigate.toArticleDetail( object.getArticleId() );
			}
		} );

		table.addColumn( new Column<OrderItem, OrderItem>( seeDetails )
		{
			@Override
			public OrderItem getValue( OrderItem object )
			{
				return object;
			}
		}, "" );

		ActionCell<OrderItem> deleteActionCell = new ActionCell<OrderItem>( "Delete", new ActionCell.Delegate<OrderItem>()
		{
			@Override
			public void execute( OrderItem object )
			{
				DataAccess.get().deleteOrderItem( object.getId() );
				dataProvider.getList().remove( object );
			}
		} );

		table.addColumn( new Column<OrderItem, OrderItem>( deleteActionCell )
		{
			@Override
			public OrderItem getValue( OrderItem object )
			{
				return object;
			}
		}, "" );

		dataProvider = new ListDataProvider<OrderItem>();

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
				OrderItem a = new OrderItem( orderId );
				dataProvider.getList().add( a );
			}
		} );
	}

	public void setData( List<OrderItem> orderItems )
	{
		dataProvider.setList( orderItems );
	}

	public List<OrderItem> getData()
	{
		return dataProvider.getList();
	}

	private Article getArticleForOrderItem( OrderItem item )
	{
		if( item == null )
			return null;

		Integer articleId = item.getArticleId();
		if( articleId == null || articleId == 0 )
			return null;

		Article a = DataAccess.get().getArticle( articleId );

		return a;
	}

	private static final ProvidesKey<OrderItem> KEY_PROVIDER = new ProvidesKey<OrderItem>()
	{
		@Override
		public Object getKey( OrderItem item )
		{
			return item.getId();
		}
	};
}
