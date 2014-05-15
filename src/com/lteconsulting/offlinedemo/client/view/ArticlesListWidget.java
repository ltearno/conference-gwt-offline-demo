package com.lteconsulting.offlinedemo.client.view;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.cell.client.ActionCell;
import com.google.gwt.cell.client.EditTextCell;
import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.cell.client.SafeHtmlCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.safehtml.client.SafeHtmlTemplates;
import com.google.gwt.safehtml.client.SafeHtmlTemplates.Template;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.view.client.ListDataProvider;
import com.google.gwt.view.client.ProvidesKey;
import com.lteconsulting.offlinedemo.client.Navigate;
import com.lteconsulting.offlinedemo.client.dto.Article;

public class ArticlesListWidget extends Composite
{
	private ListDataProvider<Article> dataProvider;
	private ArrayList<Article> deletedItems = new ArrayList<>();

	public ArticlesListWidget()
	{
		CellTable<Article> table = new CellTable<Article>( 10000, CellTableResources.INSTANCE );
		table.getElement().addClassName( "table" );
		table.getElement().addClassName( "table-striped" );
		table.getElement().addClassName( "table-bordered" );
		table.getElement().addClassName( "table-hover" );

		table.addColumn( new TextColumn<Article>()
		{
			@Override
			public String getValue( Article object )
			{
				return String.valueOf( object.getId() );
			}
		}, "ID" );

		Column<Article, String> codeColumn = new Column<Article, String>( new EditTextCell() )
		{
			@Override
			public String getValue( Article object )
			{
				return object.getCode();
			}
		};
		codeColumn.setFieldUpdater( new FieldUpdater<Article, String>()
		{
			@Override
			public void update( int index, Article object, String value )
			{
				object.setCode( value );
			}
		} );
		table.addColumn( codeColumn, "Code" );

		Column<Article, String> nameColumn = new Column<Article, String>( new EditTextCell() )
		{
			@Override
			public String getValue( Article object )
			{
				return object.getName();
			}
		};
		nameColumn.setFieldUpdater( new FieldUpdater<Article, String>()
		{
			@Override
			public void update( int index, Article object, String value )
			{
				object.setName( value );
			}
		} );
		table.addColumn( nameColumn, "Name" );

		final EditTextCell priceCell = new EditTextCell();
		Column<Article, String> priceColumn = new Column<Article, String>( priceCell )
		{
			@Override
			public String getValue( Article object )
			{
				return String.valueOf( object.getPrice() );
			}
		};
		priceColumn.setFieldUpdater( new FieldUpdater<Article, String>()
		{
			@Override
			public void update( int index, Article object, String value )
			{
				try
				{
					object.setPrice( Integer.parseInt( value ) );
				}
				catch( Exception e )
				{
					priceCell.clearViewData( KEY_PROVIDER.getKey( object ) );
					Window.alert( "You must enter a number !" );
				}
			}
		} );
		table.addColumn( priceColumn, "Price" );

		table.addColumn( new Column<Article, String>( new ImageCell( 150 ) )
		{
			@Override
			public String getValue( Article object )
			{
				return "pictures/" + object.getPicture();
			}
		}, "Picture" );

		table.addColumn( new Column<Article, SafeHtml>( new SafeHtmlCell() )
		{
			@Override
			public SafeHtml getValue( Article object )
			{
				if( object.getPdf() != null )
					return SafeHtmlUtils.fromTrustedString( "<a href='pdfs/" + object.getPdf() + "' target='_blank'>file</a>" );
				return SafeHtmlUtils.fromSafeConstant( "" );
			}
		}, "Pdf" );

		ActionCell<Article> seeDetails = new ActionCell<Article>( "Details", new ActionCell.Delegate<Article>()
		{
			@Override
			public void execute( Article object )
			{
				Navigate.toArticleDetail( object.getId() );
			}
		} );
		table.addColumn( new Column<Article, Article>( seeDetails )
		{
			@Override
			public Article getValue( Article object )
			{
				return object;
			}
		}, "" );

		ActionCell<Article> deleteActionCell = new ActionCell<Article>( "Delete", new ActionCell.Delegate<Article>()
		{
			@Override
			public void execute( Article object )
			{
				deletedItems.add( object );
				dataProvider.getList().remove( object );
			}
		} );

		table.addColumn( new Column<Article, Article>( deleteActionCell )
		{
			@Override
			public Article getValue( Article object )
			{
				return object;
			}
		}, "" );

		dataProvider = new ListDataProvider<Article>();
		dataProvider.addDataDisplay( table );

		Button addButton = new Button( "Add" );

		FlowPanel flow = new FlowPanel();
		flow.add( table );
		flow.add( addButton );

		initWidget( flow );

		addButton.addClickHandler( new ClickHandler()
		{
			@Override
			public void onClick( ClickEvent event )
			{
				Article a = new Article();
				dataProvider.getList().add( a );
			}
		} );
	}

	public void setData( List<Article> articles )
	{
		deletedItems = new ArrayList<>();
		dataProvider.setList( articles );
	}

	public List<Article> getData()
	{
		return dataProvider.getList();
	}

	public List<Article> getDeletedItems()
	{
		return deletedItems;
	}

	private static final ProvidesKey<Article> KEY_PROVIDER = new ProvidesKey<Article>()
	{
		@Override
		public Object getKey( Article item )
		{
			return item.getId();
		}
	};
}

class ImageCell extends AbstractCell<String>
{
	interface Template extends SafeHtmlTemplates
	{
		@Template( "<img src=\"{0}\" width=\"{1}\"/>" )
		SafeHtml img( String url, int width );
	}

	private static Template template;
	private int width;

	public ImageCell( int width )
	{
		if( template == null )
			template = GWT.create( Template.class );

		this.width = width;
	}

	@Override
	public void render( Context context, String value, SafeHtmlBuilder sb )
	{
		if( value != null )
			sb.append( template.img( value, width ) );
	}
}