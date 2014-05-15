package com.lteconsulting.offlinedemo.client.serialization;



public class Serializer
{
	private static OrderItemSerializer orderItemSerializer = new OrderItemSerializer();
	private static ArticleSerializer articleSerializer = new ArticleSerializer();
	private static OrderSerializer orderSerializer = new OrderSerializer();

	@SuppressWarnings( "unchecked" )
	public static <T> TableRecordSerializer<T> getSerializer( String table )
	{
		if( table.equals( "articles" ) )
			return (TableRecordSerializer<T>) articleSerializer;
		else if( table.equals( "orders" ) )
			return (TableRecordSerializer<T>) orderSerializer;
		else if( table.equals( "order_items" ) )
			return (TableRecordSerializer<T>) orderItemSerializer;

		assert false : "No serializer for table " + table;
		return null;
	}
}
