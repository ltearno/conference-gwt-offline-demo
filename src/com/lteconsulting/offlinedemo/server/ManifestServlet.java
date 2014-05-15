package com.lteconsulting.offlinedemo.server;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class ManifestServlet extends HttpServlet
{
	private static final long serialVersionUID = -4114799336246028959L;

	@Override
	protected void doGet( HttpServletRequest req, HttpServletResponse resp ) throws ServletException, IOException
	{
		resp.setContentType( "text/cache-manifest" );
		resp.addHeader( "Cache-Control", "no-cache, must-revalidate" );
		resp.addHeader( "Pragma", "no-cache" );
		resp.addHeader( "Expires", "0" );

		resp.getWriter().println( "CACHE MANIFEST" );
		resp.getWriter().println( "" );
		resp.getWriter().println( "CACHE:" );
		resp.getWriter().println( "" );
		resp.getWriter().println( "favicon.ico" );
		resp.getWriter().println( "index.html" );
		resp.getWriter().println( "" );

		// dump the gwt compilation artifacts list file
		readFile( "offlinedemo-artifacts.lst", resp.getWriter() );

		// dump other files as required
		readFile( "offlinedemo-other.lst", resp.getWriter() );

		listFiles( "jquery/", resp.getWriter() );
		listFiles( "bootstrap/", resp.getWriter() );
		listFiles( "font-awesome/", resp.getWriter() );

		listFiles( "pdfs/", resp.getWriter() );
		listFiles( "pictures/", resp.getWriter() );

		resp.getWriter().println( "" );
		resp.getWriter().println( "NETWORK:" );
		resp.getWriter().println( "*" );
		resp.getWriter().println( "http://" );
		resp.getWriter().println( "" );
		resp.getWriter().println( "SETTINGS:" );
		resp.getWriter().println( "fast" );
	}

	private void readFile( String name, PrintWriter printWriter ) throws IOException
	{
		try
		{
			BufferedReader in = new BufferedReader( new FileReader( getServletContext().getRealPath( "/" ) + name ) );
			String line;
			while( (line = in.readLine()) != null )
				printWriter.println( line );
			in.close();
		}
		catch(FileNotFoundException e)
		{
		}
	}

	private void listFiles( String directoryPath, PrintWriter printWriter )
	{
		File directory = new File( getServletContext().getRealPath( "/" ) + directoryPath );
		File[] files = directory.listFiles();
		if( files == null )
			return;

		for(int i=0;i<files.length;i++)
		{
			File file = files[i];

			if( file.isDirectory() )
			{
				listFiles( directoryPath + file.getName() + "/", printWriter );
			}
			else
			{
				printWriter.println( directoryPath + file.getName() );
			}
		}
	}
}
