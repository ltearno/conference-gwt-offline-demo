package com.lteconsulting.offlinedemo.client.sql;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArrayInteger;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.ScriptElement;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.TextResource;
import com.lteconsulting.offlinedemo.client.sql.SQLiteResult.Row;

/*
 * GWT wrapper around the SQLite JavaScript implementation
 */
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
		// Loads the SQLite.js script if not done already
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

	/*
	 * Opens a new database
	 */
	private final static native SQLite createJsni()
	/*-{
		return $wnd.SQL.open();
	}-*/;

	/*
	 * Opens a database, initializing it with a saved file
	 */
	private final static native SQLite createWithDataJsni( JsArrayInteger data )
	/*-{
		return $wnd.SQL.open(data);
	}-*/;

	/*
	 * Close the database
	 */
	public final native void close()
	/*-{
		this.close();
	}-*/;

	/*
	 * Export the database content as a JsArrayInteger representation of a SQLite file
	 */
	public final native JsArrayInteger exportData()
	/*-{
		return this.exportData();
	}-*/;

	/*
	 * Executes a SQL statement and return the resulting JavaScriptObject
	 */
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

	/*
	 * Returns whether a particular table has got a particular column.
	 * This uses special PRAGMA commands of SQLite
	 */
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

	private final native JavaScriptObject execute0( String statement )
	/*-{
		return this.exec(statement);
	}-*/;
}