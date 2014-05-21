package com.lteconsulting.offlinedemo.server;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import javax.persistence.EntityManager;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

import com.lteconsulting.offlinedemo.server.Utils.Transaction;
import com.lteconsulting.offlinedemo.server.entities.Article;

/*
 * Servlet handling uploads from the client
 */
public class UploadServlet extends HttpServlet
{
	private static final long serialVersionUID = -1336040361619095476L;

	@Override
	protected void doPost( final HttpServletRequest req, final HttpServletResponse resp ) throws ServletException, IOException
	{
		Utils.executeTransaction( new Transaction<Void>()
		{
			@Override
			public Void execute( EntityManager em )
			{
				uploadFile( em, req, resp );
				return null;
			}
		} );
	}

	private void uploadFile( EntityManager em, HttpServletRequest req, HttpServletResponse resp )
	{
		boolean isMultipart = ServletFileUpload.isMultipartContent( req );
		if( !isMultipart )
			return;

		DiskFileItemFactory factory = new DiskFileItemFactory();

		ServletContext servletContext = this.getServletConfig().getServletContext();
		File repository = (File) servletContext.getAttribute( "javax.servlet.context.tempdir" );
		factory.setRepository( repository );

		ServletFileUpload upload = new ServletFileUpload( factory );

		try
		{
			int articleId = -1;
			String fileName = null;
			String field = null;

			String applicationPath = req.getServletContext().getRealPath( "/" );

			List<FileItem> items = upload.parseRequest( req );
			Iterator<FileItem> iter = items.iterator();
			while( iter.hasNext() )
			{
				FileItem item = iter.next();

				if( item.isFormField() )
				{
					String name = item.getFieldName();
					String value = item.getString();

					if( "articleId".equals( name ) )
						articleId = Integer.parseInt( value );
					else if( "field".equals( name ) )
						field = value;
				}
				else
				{
					String fieldName = item.getFieldName();
					if( "file".equals( fieldName ) )
					{
						File directory = new File( applicationPath + File.separator + field );
						if( ! directory.exists() )
							directory.mkdir();

						fileName = UUID.randomUUID().toString() + item.getName().substring( item.getName().lastIndexOf( "." ) );
						File uploadedFile = new File( applicationPath + File.separator + field + File.separator + fileName );
						item.write( uploadedFile );
					}
				}
			}

			if( articleId > 0 && fileName != null )
			{
				Article article = em.find( Article.class, articleId );
				if( article != null )
				{
					if( "pictures".equals( field ) )
						article.setPicture( fileName );
					else if( "pdfs".equals( field ) )
						article.setPdf( fileName );
				}
			}
		}
		catch( FileUploadException e )
		{
			e.printStackTrace();
		}
		catch( Exception e )
		{
			e.printStackTrace();
		}
	}
}
