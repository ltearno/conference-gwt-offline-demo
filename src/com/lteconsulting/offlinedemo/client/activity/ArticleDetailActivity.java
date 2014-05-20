package com.lteconsulting.offlinedemo.client.activity;

import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.lteconsulting.offlinedemo.client.DataAccess;
import com.lteconsulting.offlinedemo.client.MainView;
import com.lteconsulting.offlinedemo.client.dto.Article;
import com.lteconsulting.offlinedemo.client.view.ArticleDetailWidget;

/*
 * Allows to see and edit an article's details
 */
public class ArticleDetailActivity implements Activity
{
	private Article article;
	private ArticleDetailWidget view;

	public ArticleDetailActivity( int articleId )
	{
		this.article = DataAccess.get().getArticle( articleId );
	}

	@Override
	public void start( AcceptsOneWidget container )
	{
		if( article != null )
			MainView.get().setActivityTitle( "Article " + article.getId() );
		else
			MainView.get().setActivityTitle( "Article not found !" );

		view = new ArticleDetailWidget();

		view.setData( article );

		container.setWidget( view );
	}

	@Override
	public void refresh()
	{
		article = DataAccess.get().getArticle( article.getId() );
		view.setData( article );
	}

	@Override
	public void save()
	{
		view.updateArticle( article );
		DataAccess.get().updateArticle( article );
	}

	@Override
	public String canStop()
	{
		if( article!=null && article.isChanged() )
			return "You have made some changes, please save them or discard them before leaving !";

		return null;
	}
}
