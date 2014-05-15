package com.lteconsulting.offlinedemo.server.entities;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.Generated;
import org.hibernate.annotations.GenerationTime;

@Entity
@Table( name = "orders" )
public class Order implements Serializable
{
	private static final long serialVersionUID = 2835713425769053622L;

	@Id
	@GeneratedValue
	private int id;

	private String date;

	private String addressCode;

	@Column( name = "update_date", columnDefinition="timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP" )
	@Generated( GenerationTime.ALWAYS )
	private Date updateDate;

	public int getId()
	{
		return id;
	}

	public String getDate()
	{
		return date;
	}

	public void setDate( String date )
	{
		this.date = date;
	}

	public String getAddressCode()
	{
		return addressCode;
	}

	public void setAddressCode( String addressCode )
	{
		this.addressCode = addressCode;
	}

}
