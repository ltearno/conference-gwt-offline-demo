package com.lteconsulting.offlinedemo.client;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.core.client.JavaScriptObject;
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

/*
 * This class is responsible for giving acces to the underlying SQLite database.
 * It manages :
 *  - initialization, loading and saving the SQLite database
 *  - providing the application an functional access of entities stored in the database
 */
public class DataAccess
{
	private static final String LOCALSTORAGE_KEY_DB = "db";

	// ClientBundle containing the SQL file which is used to initialize the SQLite database
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

	/*
	 * Initialize the local database, ie : loads it or create it if not found
	 */
	public void init()
	{
		// try to get the serialized representation of the SQLite DB from the local storage
		String serializedDb = storage.getItem( LOCALSTORAGE_KEY_DB );

		if( serializedDb == null || serializedDb.isEmpty() )
		{
			// if nothing is found, we create the database from scratch
			sqlDb = SQLite.create();

			// and inject the SQL file which creates the tables structure
			DbSchema dbSchema = (DbSchema) GWT.create( DbSchema.class );
			sqlDb.execute( dbSchema.sqlForSchema().getText() );
		}
		else
		{
			// if the local storage already contains some data, parse it as a JSON integer array
			JSONValue dbContent = JSONParser.parseStrict( serializedDb );

			// and initialize SQLite with this "file"
			sqlDb = SQLite.create( dbContent.isArray().getJavaScriptObject().<JsArrayInteger> cast() );
		}

		// create the application parameters table if needed
		sqlDb.execute( "create table if not exists params (key TEXT, value TEXT, value_int INTEGER);" );
	}

	/*
	 * Exports the SQLite database file as an array of integer
	 */
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
		JavaScriptObject sqlResults = sqlDb.execute( "select * from articles" );

		return deserializeRecords( sqlResults, "articles" );
	}

	public List<Article> getMostOrderedArticles()
	{
		// select articles by the sum of their ordered quantities
		String sql = "select * from (select articles.*, sum(order_items.quantity) 'total' from order_items left join articles on order_items.article_id=articles.id group by articles.id) order by total desc limit 4";

		JavaScriptObject sqlResults = sqlDb.execute( sql );

		return deserializeRecords( sqlResults, "articles" );
	}

	public Article getArticle( int id )
	{
		id = Synchronization.get().getRealId( "articles", id );

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
		JavaScriptObject sqlResults = sqlDb.execute( "select * from orders" );

		return deserializeRecords( sqlResults, "orders" );
	}

	public Order getOrder( int orderId )
	{
		orderId = Synchronization.get().getRealId( "orders", orderId );

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
		orderId = Synchronization.get().getRealId( "orders", orderId );

		JavaScriptObject sqlResults = sqlDb.execute( "select * from order_items where order_id="+orderId );

		return deserializeRecords( sqlResults, "order_items" );
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

	private <T> List<T> deserializeRecords( JavaScriptObject sqlResults, String tableName )
	{
		TableRecordSerializer<T> recordSerializer = Serializer.getSerializer( tableName );

		List<T> res = new ArrayList<T>();

		SQLiteResult rows = new SQLiteResult( sqlResults );
		for( SQLiteResult.Row row : rows )
			res.add( recordSerializer.rowToDto( row ) );

		return res;
	}

	/*
	 * Application settings and state saving and retrieving
	 * Those settings are stored directly into the SQLite database !
	 * Hence when cleaning the database, every setting will be resetted
	 */

	public String getAppSettingString( ApplicationPersistedSetting settingKey )
	{
		SQLiteResult res = new SQLiteResult( sqlDb.execute( "select value from params where key='"+settingKey.name()+"'" ) );
		if( res.size() == 0 )
			return null;

		return res.getRow( 0 ).getString( "value" );
	}

	public Integer getAppSettingInteger( ApplicationPersistedSetting settingKey )
	{
		String stringValue = getAppSettingString( settingKey );
		try
		{
			return Integer.parseInt( stringValue );
		}
		catch( Exception e )
		{
			return null;
		}
	}

	public void setAppSetting( ApplicationPersistedSetting settingKey, String value )
	{
		SQLiteResult res = new SQLiteResult( sqlDb.execute( "select key, value from params where key='" + settingKey.name() + "'" ) );
		if( res.size() == 0 )
		{
			// insert
			sqlDb.execute( "insert into params ( key, value ) values ( '"+settingKey.name()+"', '"+value+"')"  );
			DataAccess.get().scheduleSaveDb();
		}
		else
		{
			if( ! value.equals( res.getRow( 0 ).getString( "value" ) ) )
			{
				// update
				sqlDb.execute( "update params set value='"+value+"' where key='"+settingKey.name()+"'"  );
				DataAccess.get().scheduleSaveDb();
			}
		}
	}

	public void setAppSetting( ApplicationPersistedSetting settingKey, int value )
	{
		setAppSetting( settingKey, String.valueOf( value ) );
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

	/*
	 * Creation of a local record id
	 */
	private int createLocalId()
	{
		Integer lastLocalId = getAppSettingInteger( ApplicationPersistedSetting.LAST_LOCAL_ID );
		if( lastLocalId == null )
			lastLocalId = 0;

		int localId = lastLocalId - 1;

		setAppSetting( ApplicationPersistedSetting.LAST_LOCAL_ID, localId );

		return localId;
	}
}
