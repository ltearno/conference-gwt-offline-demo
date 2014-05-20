package com.lteconsulting.offlinedemo.client.dto;

import java.util.Date;

import com.lteconsulting.offlinedemo.client.Synchronization;
import com.lteconsulting.offlinedemo.client.sql.SQLite;

public class OrderItem extends BaseDto
{
	private int orderId;
	private Integer articleId;
	private int quantity;
	private int unitPrice;
	private int amount;

	public OrderItem( int orderId )
	{
		super( "order_items" );

		this.orderId = orderId;
		this.updateDate = SQLite.dateTimeFormat.format( new Date() );
	}

	public OrderItem( int id, int orderId, Integer articleId, int quantity, int unitPrice, int amount, String updateDate )
	{
		super( "order_items" );

		this.id = id;
		this.orderId = orderId;
		this.articleId = articleId;
		this.quantity = quantity;
		this.unitPrice = unitPrice;
		this.amount = amount;
		this.updateDate = updateDate;
	}

	public void computeAmount()
	{
		setAmount( quantity * unitPrice );
	}

	public int getOrderId()
	{
		return Synchronization.get().getRealId( "orders", orderId );
	}

	public void setOrderId( int orderId )
	{
		isChanged = true;
		this.orderId = orderId;
	}

	public Integer getArticleId()
	{
		return Synchronization.get().getRealId( "articles", articleId );
	}

	public void setArticleId( Integer articleId )
	{
		isChanged = true;
		this.articleId = articleId;
	}

	public int getQuantity()
	{
		return quantity;
	}

	public void setQuantity( int quantity )
	{
		isChanged = true;
		this.quantity = quantity;
	}

	public int getUnitPrice()
	{
		return unitPrice;
	}

	public void setUnitPrice( int unitPrice )
	{
		isChanged = true;
		this.unitPrice = unitPrice;
	}

	public int getAmount()
	{
		return amount;
	}

	public void setAmount( int amount )
	{
		isChanged = true;
		this.amount = amount;
	}
}
