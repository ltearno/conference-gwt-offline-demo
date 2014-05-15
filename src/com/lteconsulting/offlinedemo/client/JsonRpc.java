package com.lteconsulting.offlinedemo.client;

import java.util.Date;

import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.json.client.JSONValue;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class JsonRpc
{
	private String url;
	private static String CRLF = "\r\n";

	public JsonRpc()
	{
	}

	public void setUrl( String url )
	{
		this.url = url;
	}

	public void call( JSONObject parameters, final AsyncCallback<JSONValue> callback )
	{
		String boundary = "AJAX------" + Math.random() + "" + new Date().getTime();

		RequestBuilder builderPost = new RequestBuilder( RequestBuilder.POST, url );
		builderPost.setHeader( "Content-Type", "multipart/form-data; charset=utf-8; boundary=" + boundary );

		StringBuilder data = new StringBuilder();
		data.append( "--" ).append( boundary ).append( CRLF );
		data.append( "--" ).append( boundary ).append( CRLF );

		data.append( "Content-Disposition: form-data; " );
		data.append( "name=\"parameters\"" ).append( CRLF ).append( CRLF );
		data.append( parameters.toString() ).append( CRLF );
		data.append( "--" ).append( boundary ).append( "--" ).append( CRLF );

		builderPost.setRequestData( data.toString() );
		builderPost.setCallback( new RequestCallback()
		{
			@Override
			public void onResponseReceived( Request request, Response response )
			{
				if( response.getStatusCode() != 200 )
				{
					callback.onFailure( new RuntimeException( "Failed request: " + response.getStatusText() ) );
					return;
				}

				JSONValue value = null;

				try
				{
					value = JSONParser.parseStrict( response.getText() );
				}
				catch( Exception e )
				{
					callback.onFailure( e );
					return;
				}

				callback.onSuccess( value );
			}

			@Override
			public void onError( Request request, Throwable exception )
			{
				callback.onFailure( exception );
			}
		} );

		try
		{
			builderPost.send();
		}
		catch( RequestException e )
		{
			callback.onFailure( e );
		}
	}
}
