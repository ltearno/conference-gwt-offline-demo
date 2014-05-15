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
@Table( name = "articles" )
public class Article implements Serializable
{
	private static final long serialVersionUID = 5629208808683518221L;

	@Id
	@GeneratedValue
	private int id;

	private String code;
	private String name;
	private int price;
	private String picture;
	private String pdf;

	@Column( name = "update_date", columnDefinition = "timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP" )
	@Generated( GenerationTime.ALWAYS )
	private Date updateDate;

	public int getId()
	{
		return id;
	}

	public String getCode()
	{
		return code;
	}

	public void setCode( String code )
	{
		this.code = code;
	}

	public String getName()
	{
		return name;
	}

	public void setName( String name )
	{
		this.name = name;
	}

	public int getPrice()
	{
		return price;
	}

	public void setPrice( int price )
	{
		this.price = price;
	}

	public String getPicture()
	{
		return picture;
	}

	public void setPicture( String picture )
	{
		this.picture = picture;
	}

	public String getPdf()
	{
		return pdf;
	}

	public void setPdf( String pdf )
	{
		this.pdf = pdf;
	}
}
