package com.lteconsulting.offlinedemo.client.sql;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.core.client.JsArrayMixed;
import com.google.gwt.core.client.JsArrayString;
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONValue;
import com.lteconsulting.offlinedemo.shared.synchro.dto.Record;

/*
 * Provides an easy access to rows and columns resulting from a SQL query ran against SQLite
 * Since the SQLite JSON result format may change, this will provide an independant access to results.
 */
public class SQLiteResult implements Iterable<SQLiteResult.Row>
{
	HashMap<String,Integer> columnsIdx = new HashMap<>();
	JsArray<JsArrayMixed> rows;

	public SQLiteResult( JavaScriptObject jso )
	{
		JSONValue r = new JSONArray( jso ).get( 0 );
		if( r == null )
			return;

		JSONObject root = r.isObject();

		JsArrayString columns = root.get( "columns" ).isArray().getJavaScriptObject().cast();
		for( int i=0; i<columns.length(); i++ )
			columnsIdx.put( columns.get( i ), i );

		rows = root.get( "values" ).isArray().getJavaScriptObject().cast();
	}

	public int size()
	{
		return rows == null ? 0 : rows.length();
	}

	public Row getRow( int rowIdx )
	{
		return new Row( rowIdx );
	}

	public static class Cell
	{
		public String column;
		public String value;
	}

	public class Row implements Iterable<Cell>
	{
		int rowIdx;

		Row( int rowIdx )
		{
			this.rowIdx = rowIdx;
		}

		public String getString( String columnName )
		{
			Integer colIdx = columnsIdx.get( columnName );
			if( colIdx == null )
				throw new IllegalArgumentException( columnName + " column does not exist." );

			//return rows.get( rowIdx ).getString( colIdx );
			return getString0( rows.get( rowIdx ), colIdx );
		}

		private native String getString0( JsArrayMixed jso, int index )
		/*-{
			var value = jso[index];
			return (value==null||value==undefined) ? null : value;
		}-*/;

		public Integer getInt( String columnName )
		{
			String stringValue = getString( columnName );
			if( stringValue==null || stringValue.isEmpty() || stringValue.equals( "null" ) )
				return null;

			return Integer.parseInt( stringValue );
		}

		@Override
		public String toString()
		{
			return rows.get( rowIdx ).toString();
		}

		@Override
		public Iterator<Cell> iterator()
		{
			return new Iterator<SQLiteResult.Cell>()
			{
				int current = 0;

				List<Entry<String,Integer>> entries = new ArrayList<>( columnsIdx.entrySet() );

				@Override
				public void remove()
				{
					assert false;
				}

				@Override
				public Cell next()
				{
					Entry<String,Integer> entry = entries.get( current );
					current++;

					Cell cell = new Cell();
					cell.column = entry.getKey();
					cell.value = rows.get( rowIdx ).getString( entry.getValue() );

					return cell;
				}

				@Override
				public boolean hasNext()
				{
					return current < entries.size();
				}
			};
		}
	}

	@Override
	public Iterator<Row> iterator()
	{
		return new Iterator<SQLiteResult.Row>()
		{
			int current = 0;

			@Override
			public void remove()
			{
				assert false;
			}

			@Override
			public Row next()
			{
				Row row = new Row( current );
				current++;

				return row;
			}

			@Override
			public boolean hasNext()
			{
				return rows == null ? false : (current < rows.length());
			}
		};
	}

	public List<Record> getAsMap()
	{
		ArrayList<Record> res = new ArrayList<>();

		String[] columns = new String[columnsIdx.size()];
		for( Entry<String,Integer> e: columnsIdx.entrySet())
			columns[e.getValue()] = e.getKey();

		for( int r=0; r<size(); r++ )
		{
			HashMap<String, String> rowObject = new HashMap<>();

			JsArrayMixed row = rows.get( r );
			for( int c=0; c<row.length(); c++ )
				rowObject.put( columns[c], row.getString( c ) );

			Record record = new Record();
			record.setProperties( rowObject );

			res.add( record );
		}

		return res;
	}
}
