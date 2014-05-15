package com.lteconsulting.offlinedemo.client.dto;

public class Article extends BaseDto
{
	private String code;
	private String name;
	private int price;
	private String picture;
	private String pdf;

	public Article()
	{
		this( 0, "code", "name", 0, null, null, null );
	}

	public Article( int id, String code, String name, int price, String picture, String pdf, String updateDate )
	{
		super( "articles" );

		this.id = id;
		this.code = code;
		this.name = name;
		this.price = price;
		this.picture = picture;
		this.pdf = pdf;
		this.updateDate = updateDate;
	}

	public String getCode()
	{
		return code;
	}

	public void setCode( String code )
	{
		isChanged = true;
		this.code = code;
	}

	public String getName()
	{
		return name;
	}

	public void setName( String name )
	{
		isChanged = true;
		this.name = name;
	}

	public int getPrice()
	{
		return price;
	}

	public void setPrice( int price )
	{
		isChanged = true;
		this.price = price;
	}

	public String getPicture()
	{
		return picture;
	}

	public void setPicture( String picture )
	{
		isChanged = true;
		this.picture = picture;
	}

	public String getPdf()
	{
		return pdf;
	}

	public void setPdf( String pdf )
	{
		isChanged = true;
		this.pdf = pdf;
	}
}
