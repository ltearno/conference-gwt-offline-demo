package com.lteconsulting.offlinedemo.client.sql;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArrayInteger;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.ScriptElement;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONValue;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.TextResource;
import com.google.gwt.storage.client.Storage;
import com.lteconsulting.offlinedemo.client.sql.SQLiteResult.Row;

public class SQLite extends JavaScriptObject
{
	interface SQLiteBundle extends ClientBundle
	{
		@Source( "sql.js" )
		TextResource SqlJs();
	}

	private static SQLiteBundle bundle;

	private final static String LOCAL_CURRENT_ID_INCREMENT = "LOCAL_CURRENT_ID_INCREMENT";

	public static final DateTimeFormat dateTimeFormat = DateTimeFormat.getFormat( "yyyy-MM-dd HH:mm:ss" );

	protected SQLite()
	{
	}

	public final static SQLite create()
	{
		return create( null );
	}

	public final static SQLite create( JsArrayInteger data )
	{
		if( bundle == null )
		{
			bundle = (SQLiteBundle) GWT.create( SQLiteBundle.class );

			Document doc = Document.get();
			ScriptElement sqljs = doc.createScriptElement();
			sqljs.setAttribute( "type", "text/javascript" );
			sqljs.setInnerText( bundle.SqlJs().getText() );
			doc.getDocumentElement().getFirstChildElement().appendChild( sqljs );
		}

		if( data != null )
			return createWithDataJsni( data );

		return createJsni();
	}

	public final static native SQLite createJsni()
	/*-{
		return $wnd.SQL.open();
	}-*/;

	public final static native SQLite createWithDataJsni( JsArrayInteger data )
	/*-{
		return $wnd.SQL.open(data);
	}-*/;

	public final native void close()
	/*-{
		this.close();
	}-*/;

	public final native JsArrayInteger exportData()
	/*-{
		return this.exportData();
	}-*/;

	public final JavaScriptObject execute( String statement )
	{
		try
		{
			return execute0( statement );
		}
		catch( Exception e )
		{
			throw new RuntimeException( "SQLite execute exception: " + statement + " => " + e.toString() );
		}
	}

	private final native JavaScriptObject execute0( String statement )
	/*-{
		return this.exec(statement);
	}-*/;

	// Create a negative ID.
	public final static int createLocalId()
	{
		Storage storage = Storage.getLocalStorageIfSupported();
		if( storage == null )
			return 0;

		int increment = 0;
		String incrementString = storage.getItem( LOCAL_CURRENT_ID_INCREMENT );
		try
		{
			increment = Integer.parseInt( incrementString );
		}
		catch( Exception e )
		{
		}

		increment += 1;

		storage.setItem( LOCAL_CURRENT_ID_INCREMENT, increment + "" );

		return -increment;
	}

	public final int getLastInsertedId()
	{
		JavaScriptObject js = execute( "select last_insert_rowid();" );
		JSONValue json = new JSONObject( js );
		int lastInsertedId = Integer.parseInt( json.isObject().get( "0" ).isArray().get( 0 ).isObject().get( "value" ).isString().stringValue() );
		return lastInsertedId;
	}

	public final boolean hasColumn( String tableName, String columnName )
	{
		SQLiteResult res = new SQLiteResult( execute( "PRAGMA table_info("+tableName+");" ) );
		for( Row row : res )
		{
			if( row.getString( "name" ).equals( columnName ) )
				return true;
		}

		return false;
	}
}