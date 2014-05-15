package com.lteconsulting.offlinedemo.client.serialization;

import java.util.ArrayList;

import com.lteconsulting.offlinedemo.client.dto.Article;
import com.lteconsulting.offlinedemo.client.sql.SQLiteResult.Row;

public class ArticleSerializer extends BaseSerializer<Article>
{
	public ArticleSerializer()
	{
		super( "articles" );

		fields = new ArrayList<>();

		fields.add( new StringFieldSerializer<Article>( "code" )
				{
					@Override
					protected String getValue( Article dto )
					{
						return dto.getCode();
					}
				} );

		fields.add( new StringFieldSerializer<Article>( "name" )
				{
					@Override
					protected String getValue( Article dto )
					{
						return dto.getName();
					}
				} );

		fields.add( new IntFieldSerializer<Article>( "price" )
				{
					@Override
					protected Integer getValue( Article dto )
					{
						return dto.getPrice();
					}
				} );



		fields.add( new StringFieldSerializer<Article>( "picture" )
				{
					@Override
					protected String getValue( Article dto )
					{
						return dto.getPicture();
					}
				} );

		fields.add( new StringFieldSerializer<Article>( "pdf" )
				{
					@Override
					protected String getValue( Article dto )
					{
						return dto.getPdf();
					}
				} );
	}

	@Override
	public Article rowToDto( Row row )
	{
		return new Article(
				row.getInt( "id" ),
				row.getString( "code" ),
				row.getString( "name" ),
				row.getInt( "price" ),
				row.getString( "picture" ),
				row.getString( "pdf" ),
				row.getString( "update_date" ) );
	}
}
