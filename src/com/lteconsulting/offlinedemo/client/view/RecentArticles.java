package com.lteconsulting.offlinedemo.client.view;

import java.util.List;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.lteconsulting.offlinedemo.client.DataAccess;
import com.lteconsulting.offlinedemo.client.dto.Article;

/*
 * A widget that displays most ordered articles.
 * When the user clicks on an article's picture, it calls the onSuccess method of the callback
 */
public class RecentArticles extends Composite
{
	public RecentArticles( final AsyncCallback<Article> callback )
	{
		FlowPanel flow = new FlowPanel();
		flow.add( new Label( "Most ordered articles" ) );
		FlexTable images = new FlexTable();
		flow.add( images );

		List<Article> articles = DataAccess.get().getMostOrderedArticles();
		for( int i=0; i<4 && i<articles.size(); i++ )
		{
			final Article article = articles.get(i);

			Image image = new Image( "pictures/" + article.getPicture() );
			images.setWidget( 0, i, image );
			images.setText( 1, i, article.getName() );
			image.setHeight( "100px" );

			image.addClickHandler( new ClickHandler()
			{
				@Override
				public void onClick( ClickEvent event )
				{
					callback.onSuccess( article );
				}
			} );
		}

		initWidget( flow );
		addStyleName( "well" );
	}
}
