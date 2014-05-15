package com.lteconsulting.offlinedemo.client.serialization;

import java.util.ArrayList;

import com.lteconsulting.offlinedemo.client.dto.OrderItem;
import com.lteconsulting.offlinedemo.client.sql.SQLiteResult.Row;

public class OrderItemSerializer extends BaseSerializer<OrderItem>
{
	public OrderItemSerializer()
	{
		super( "order_items" );

		fields = new ArrayList<FieldSerializer<OrderItem>>();

		fields.add( new IntFieldSerializer<OrderItem>( "order_id" )
		{
			@Override
			protected Integer getValue( OrderItem dto )
			{
				return dto.getOrderId();
			}
		} );

		fields.add( new IntFieldSerializer<OrderItem>( "article_id" )
		{
			@Override
			protected Integer getValue( OrderItem dto )
			{
				return dto.getArticleId();
			}
		} );

		fields.add( new IntFieldSerializer<OrderItem>( "quantity" )
		{
			@Override
			protected Integer getValue( OrderItem dto )
			{
				return dto.getQuantity();
			}
		} );

		fields.add( new IntFieldSerializer<OrderItem>( "unit_price" )
		{
			@Override
			protected Integer getValue( OrderItem dto )
			{
				return dto.getUnitPrice();
			}
		} );

		fields.add( new IntFieldSerializer<OrderItem>( "amount" )
		{
			@Override
			protected Integer getValue( OrderItem dto )
			{
				return dto.getAmount();
			}
		} );
	}

	@Override
	public OrderItem rowToDto( Row row )
	{
		return new OrderItem(
				row.getInt( "id" ),
				row.getInt( "order_id" ),
				row.getInt( "article_id" ),
				row.getInt( "quantity" ),
				row.getInt( "unit_price" ),
				row.getInt( "amount" ),
				row.getString( "update_date" ) );
	}
}