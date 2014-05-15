package com.lteconsulting.offlinedemo.server.entities;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.annotations.Generated;
import org.hibernate.annotations.GenerationTime;

@Entity
@Table( name = "order_items" )
public class OrderItem implements Serializable
{
	private static final long serialVersionUID = 4298260742129710369L;

	@Id
	@GeneratedValue
	private int id;

	@ManyToOne
	private Order order;

	@ManyToOne
	private Article article;

	private int quantity;

	@Column( name = "unit_price" )
	private int unitPrice;

	private int amount;

	@Column( name = "update_date", columnDefinition="timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP" )
	@Generated( GenerationTime.ALWAYS )
	private Date updateDate;

	public int getId()
	{
		return id;
	}

	public Order getOrder()
	{
		return order;
	}

	public void setOrder( Order order )
	{
		this.order = order;
	}

	public Article getArticle()
	{
		return article;
	}

	public void setArticle( Article article )
	{
		this.article = article;
	}

	public int getQuantity()
	{
		return quantity;
	}

	public void setQuantity( int quantity )
	{
		this.quantity = quantity;
	}

	public int getUnitPrice()
	{
		return unitPrice;
	}

	public void setUnitPrice( int unitPrice )
	{
		this.unitPrice = unitPrice;
	}

	public int getAmount()
	{
		return amount;
	}

	public void setAmount( int amount )
	{
		this.amount = amount;
	}
}
