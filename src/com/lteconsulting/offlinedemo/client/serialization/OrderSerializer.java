package com.lteconsulting.offlinedemo.client.serialization;

import java.util.ArrayList;

import com.lteconsulting.offlinedemo.client.dto.Order;
import com.lteconsulting.offlinedemo.client.sql.SQLiteResult.Row;

public class OrderSerializer extends BaseSerializer<Order>
{
	public OrderSerializer()
	{
		super( "orders" );

		fields = new ArrayList<>();

		fields.add( new StringFieldSerializer<Order>( "date" )
				{
					@Override
					protected String getValue( Order dto )
					{
						return dto.getDate();
					}
				} );

		fields.add( new StringFieldSerializer<Order>( "addressCode" )
				{
					@Override
					protected String getValue( Order dto )
					{
						return dto.getAddressCode();
					}
				} );
	}

	@Override
	public Order rowToDto( Row row )
	{
		return new Order( row.getInt( "id" ), row.getString( "date" ), row.getString( "addressCode" ), row.getString( "update_date" ) );
	}
}
