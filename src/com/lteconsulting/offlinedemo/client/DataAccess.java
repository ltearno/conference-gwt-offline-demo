package com.lteconsulting.offlinedemo.client;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.core.client.JsArrayInteger;
import com.google.gwt.core.shared.GWT;
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.json.client.JSONValue;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.TextResource;
import com.google.gwt.storage.client.Storage;
import com.lteconsulting.offlinedemo.client.dto.Article;
import com.lteconsulting.offlinedemo.client.dto.Order;
import com.lteconsulting.offlinedemo.client.dto.OrderItem;
import com.lteconsulting.offlinedemo.client.serialization.Serializer;
import com.lteconsulting.offlinedemo.client.serialization.TableRecordSerializer;
import com.lteconsulting.offlinedemo.client.sql.SQLite;
import com.lteconsulting.offlinedemo.client.sql.SQLiteResult;
import com.lteconsulting.offlinedemo.client.util.Delayer;

public class DataAccess
{
	private static final String LOCALSTORAGE_KEY_DB = "db";
	private static final String LOCALSTORAGE_KEY_LASTLOCALID = "llid";

	interface DbSchema extends ClientBundle
	{
		@Source( "schema.sql" )
		TextResource sqlForSchema();
	}

	private final Storage storage = Storage.getLocalStorageIfSupported();
	private SQLite sqlDb;

	private static DataAccess _instance;

	public static DataAccess get()
	{
		if( _instance == null )
			_instance = new DataAccess();

		return _instance;
	}

	private DataAccess()
	{
	}

	public void init()
	{
		String serializedDb = storage.getItem( LOCALSTORAGE_KEY_DB );

		if( serializedDb == null || serializedDb.isEmpty() )
		{
			sqlDb = SQLite.create();
			DbSchema dbSchema = (DbSchema) GWT.create( DbSchema.class );
			sqlDb.execute( dbSchema.sqlForSchema().getText() );
		}
		else
		{
			JSONValue dbContent = JSONParser.parseStrict( serializedDb );
			sqlDb = SQLite.create( dbContent.isArray().getJavaScriptObject().<JsArrayInteger> cast() );
		}
	}

	public JsArrayInteger exportDbData()
	{
		return sqlDb.exportData();
	}

	public void cleanLocalStorage()
	{
		storage.clear();
	}

	public SQLite getSqlDb()
	{
		return sqlDb;
	}

	public void scheduleSaveDb()
	{
		saveDbDelayer.trigger();
	}

	/*
	 * Data access from the local database
	 */

	public List<Article> getArticles()
	{
		TableRecordSerializer<Article> recordSerializer = Serializer.getSerializer( "articles" );

		SQLiteResult rows = new SQLiteResult( sqlDb.execute( "select * from articles" ) );
		List<Article> res = new ArrayList<Article>();

		for( SQLiteResult.Row row : rows )
			res.add( recordSerializer.rowToDto( row ) );

		return res;
	}

	public List<Article> getMostOrderedArticles()
	{
		TableRecordSerializer<Article> recordSerializer = Serializer.getSerializer( "articles" );

		// select articles by the sum of their ordered quantities
		String inner = "select * from (select articles.*, sum(order_items.quantity) 'total' from order_items left join articles on order_items.article_id=articles.id group by articles.id) order by total desc limit 4";

		SQLiteResult rows = new SQLiteResult( sqlDb.execute( inner ) );
		List<Article> res = new ArrayList<Article>();

		for( SQLiteResult.Row row : rows )
			res.add( recordSerializer.rowToDto( row ) );

		return res;
	}

	public Article getArticle( int id )
	{
		id = Synchronizer.get().getRealId( "articles", id );

		TableRecordSerializer<Article> recordSerializer = Serializer.getSerializer( "articles" );

		SQLiteResult rows = new SQLiteResult( sqlDb.execute( "select * from articles where id="+id ) );

		if( ! rows.iterator().hasNext() )
			return null;

		return recordSerializer.rowToDto( rows.iterator().next() );
	}

	public void updateArticle( Article a )
	{
		TableRecordSerializer<Article> recordSerializer = Serializer.getSerializer( "articles" );

		if( a.getId() == 0 )
		{
			// creation
			int localId = createLocalId();
			a.setId( localId );
			sqlDb.execute( recordSerializer.dtoToSqlInsert( localId, a ) );
		}
		else
		{
			// update
			sqlDb.execute( recordSerializer.dtoToSqlUpdate( a ) );
		}

		a.commitChange();

		scheduleSaveDb();
	}

	public void deleteArticle( int id )
	{
		sqlDb.execute( "delete from articles where id=" + id );

		scheduleSaveDb();
	}

	public List<Order> getOrders()
	{
		TableRecordSerializer<Order> recordSerializer = Serializer.getSerializer( "orders" );

		SQLiteResult rows = new SQLiteResult( sqlDb.execute( "select * from orders" ) );
		List<Order> res = new ArrayList<Order>();

		for( SQLiteResult.Row row : rows )
			res.add( recordSerializer.rowToDto( row ) );

		return res;
	}

	public Order getOrder( int orderId )
	{
		orderId = Synchronizer.get().getRealId( "orders", orderId );

		TableRecordSerializer<Order> recordSerializer = Serializer.getSerializer( "orders" );

		SQLiteResult rows = new SQLiteResult( sqlDb.execute( "select * from orders where id="+orderId ) );

		if( ! rows.iterator().hasNext() )
			return null;

		return recordSerializer.rowToDto( rows.iterator().next() );
	}

	public void updateOrder( Order a )
	{
		TableRecordSerializer<Order> recordSerializer = Serializer.getSerializer( "orders" );

		if( a.getId() == 0 )
		{
			// creation
			int localId = createLocalId();
			a.setId( localId );
			sqlDb.execute( recordSerializer.dtoToSqlInsert( localId, a ) );
		}
		else
		{
			// update
			sqlDb.execute( recordSerializer.dtoToSqlUpdate( a ) );
		}

		a.commitChange();

		scheduleSaveDb();
	}

	public void deleteOrder( int id )
	{
		sqlDb.execute( "delete from orders where id=" + id );

		scheduleSaveDb();
	}

	public List<OrderItem> getOrderItems( int orderId )
	{
		orderId = Synchronizer.get().getRealId( "orders", orderId );

		TableRecordSerializer<OrderItem> recordSerializer = Serializer.getSerializer( "order_items" );

		SQLiteResult rows = new SQLiteResult( sqlDb.execute( "select * from order_items where order_id="+orderId ) );

		List<OrderItem> res = new ArrayList<OrderItem>();

		for( SQLiteResult.Row row : rows )
			res.add( recordSerializer.rowToDto( row ) );

		return res;
	}

	public void updateOrderItem( OrderItem order )
	{
		TableRecordSerializer<OrderItem> recordSerializer = Serializer.getSerializer( "order_items" );

		if( order.getId() == 0 )
		{
			// creation
			int localId = createLocalId();
			order.setId( localId );
			sqlDb.execute( recordSerializer.dtoToSqlInsert( localId, order ) );
		}
		else
		{
			// update
			sqlDb.execute( recordSerializer.dtoToSqlUpdate( order ) );
		}

		order.commitChange();

		scheduleSaveDb();
	}

	public void deleteOrderItem( int orderItemId )
	{
		sqlDb.execute( "delete from order_items where id=" + orderItemId );

		scheduleSaveDb();
	}

	/*
	 * Technical private things...
	 */

	private final Delayer saveDbDelayer = new Delayer( GWT.isProdMode() ? 500 : 5000 )
	{
		@Override
		protected void onExecute()
		{
			GWT.log( "SAVING DB TO LOCAL STORAGE" );
			JSONArray dbData = new JSONArray( sqlDb.exportData() );
			storage.setItem( LOCALSTORAGE_KEY_DB, dbData.toString() );
		}
	};

	private int createLocalId()
	{
		String lastLocalIdString = storage.getItem( LOCALSTORAGE_KEY_LASTLOCALID );
		int lastLocalId = 0;
		if( lastLocalIdString != null )
			lastLocalId = Integer.parseInt( lastLocalIdString );

		int localId = lastLocalId - 1;

		storage.setItem( LOCALSTORAGE_KEY_LASTLOCALID, String.valueOf( localId ) );

		return localId;
	}
}
