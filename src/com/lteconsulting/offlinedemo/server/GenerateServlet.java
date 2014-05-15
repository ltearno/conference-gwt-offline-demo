package com.lteconsulting.offlinedemo.server;

import java.io.IOException;

import javax.persistence.EntityManager;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.lteconsulting.offlinedemo.server.Utils.Transaction;
import com.lteconsulting.offlinedemo.server.entities.Article;

public class GenerateServlet extends HttpServlet
{
	private static final long serialVersionUID = -695489334137082781L;

	@Override
	protected void doGet( HttpServletRequest req, HttpServletResponse resp ) throws ServletException, IOException
	{
		Utils.executeTransaction( new Transaction<Void>()
		{
			@Override
			public Void execute( EntityManager em )
			{
				String raw = "On sait depuis longtemps que travailler avec du texte lisible et contenant du sens est source de distractions, et empêche de se concentrer sur la mise en page elle-même. Lavantage du Lorem Ipsum sur un texte générique comme Du texte Du texte est quil possède une distribution de lettres plus ou moins normale, et en tout cas comparable avec celle du français standard. De nombreuses suites logicielles de mise en page ou éditeurs de sites Web ont fait du Lorem Ipsum leur faux texte par défaut, et une recherche pour";
				String[] raws = raw.split( " " );

				int nb = 20;
				while( nb-- > 0 )
				{
					Article a = new Article();
					a.setCode( String.valueOf( (int) (100.0f*Math.random()) ) );
					a.setName( randomString( raws ) );
					a.setPrice( (int) (Math.random() * 200) );

					em.persist( a );

				}
				return null;
			}
		} );
	}

	private String randomString( String[] raws )
	{
		StringBuilder sb = new StringBuilder();
		for( int i=0; i<3; i++ )
		{
			if( i>0)
				sb.append( " " );
			sb.append( raws[(int)(Math.random()*(raws.length-1))] );
		}
		return sb.toString();
	}
}
