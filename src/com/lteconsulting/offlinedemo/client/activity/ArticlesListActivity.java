package com.lteconsulting.offlinedemo.client.activity;

import java.util.List;

import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.lteconsulting.offlinedemo.client.DataAccess;
import com.lteconsulting.offlinedemo.client.MainView;
import com.lteconsulting.offlinedemo.client.dto.Article;
import com.lteconsulting.offlinedemo.client.view.ArticlesListWidget;

public class ArticlesListActivity implements Activity
{
	ArticlesListWidget view;

	@Override
	public void start( AcceptsOneWidget container )
	{
		MainView.get().setActivityTitle( "Articles" );

		// display list of articles, able to edit them...
		view = new ArticlesListWidget();

		container.setWidget( view );

		loadArticles();
	}

	@Override
	public void refresh()
	{
		loadArticles();
	}

	@Override
	public void save()
	{
		for( Article a : view.getData() )
		{
			if( (! a.isChanged()) && (a.getId()!=0) )
				continue;

			DataAccess.get().updateArticle( a );
		}

		if( view.getDeletedItems() != null )
		{
			for( Article a : view.getDeletedItems() )
				DataAccess.get().deleteArticle( a.getId() );
			view.getDeletedItems().clear();
		}
	}

	@Override
	public String canStop()
	{
		for( Article a : view.getData() )
		{
			if( (a.isChanged()) || (a.getId()==0) )
				return "You have made some changes, please save them or discard them before leaving !";
		}

		return null;
	}

	private void loadArticles()
	{
		List<Article> articles = DataAccess.get().getArticles();
		view.setData( articles );
	}
}
