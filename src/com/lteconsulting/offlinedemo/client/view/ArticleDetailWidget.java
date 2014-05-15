package com.lteconsulting.offlinedemo.client.view;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FileUpload;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FormPanel;
import com.google.gwt.user.client.ui.FormPanel.SubmitCompleteEvent;
import com.google.gwt.user.client.ui.FormPanel.SubmitCompleteHandler;
import com.google.gwt.user.client.ui.Hidden;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.lteconsulting.offlinedemo.client.appcache.AppCache;
import com.lteconsulting.offlinedemo.client.dto.Article;

public class ArticleDetailWidget extends Composite
{
	FlexTable table = new FlexTable();

	private TextBox code = new TextBox();
	private TextBox name = new TextBox();
	private TextBox price = new TextBox();
	private Image picture = new Image();
	private Anchor pdf = new Anchor( "pdf file" );

	public ArticleDetailWidget()
	{
		table.setText( 0, 0, "Code" );
		table.setWidget( 0, 1, code );

		table.setText( 1, 0, "Name" );
		table.setWidget( 1, 1, name );

		table.setText( 3, 0, "Price" );
		table.setWidget( 3, 1, price );

		table.setText( 6, 0, "Picture" );
		table.setWidget( 6, 1, picture );
		picture.setWidth( "100px" );

		table.setText( 7, 0, "PDF" );
		table.setWidget( 7, 1, pdf );
		pdf.setTarget( "_blank" );

		initWidget( table );
	}

	public void setData( Article a )
	{
		if( a == null )
		{
			code.setText( "" );
			name.setText( "" );
			price.setText( "" );
			picture.setUrl( "" );
			pdf.setHref( "" );
			table.setWidget( 6, 2, null );
			table.setWidget( 7, 2, null );
		}
		else
		{
			code.setText( a.getCode() );
			name.setText( a.getName() );
			price.setText( String.valueOf( a.getPrice() ) );
			picture.setUrl( "pictures/" + a.getPicture() );
			pdf.setHref( "pdfs/" + a.getPdf() );
			table.setWidget( 6, 2, new UploadButton( "pictures", a.getId() ) );
			table.setWidget( 7, 2, new UploadButton( "pdfs", a.getId() ) );
		}
	}

	public void updateArticle( Article a )
	{
		a.setCode( code.getText() );
		a.setName( name.getText() );
		a.setPrice( Integer.parseInt( price.getText() ) );
	}
}


class UploadButton extends Composite
{
	String fieldName;
	int articleId;
	SimplePanel panel = new SimplePanel();
	Button upload = new Button( "Upload" );
	FormPanel formPanel;

	public UploadButton( final String fieldName, final int articleId )
	{
		this.fieldName = fieldName;
		this.articleId = articleId;

		createFormPanel();

		initWidget( panel );

		upload.addClickHandler( new ClickHandler()
		{
			@Override
			public void onClick( ClickEvent event )
			{
				setMode( false );
			}
		} );

		setMode( true );
	}

	void createFormPanel()
	{
		if( formPanel != null )
			return;

		FileUpload fileUpload = new FileUpload();
		fileUpload.setName( "file" );

		Button submit = new Button( "Submit" );

		VerticalPanel vp = new VerticalPanel();
		vp.add( new Label( "You can select and upload a file" ) );
		vp.add( new Hidden( "field", fieldName ) );
		vp.add( new Hidden( "articleId", String.valueOf( articleId ) ) );
		vp.add( fileUpload );
		vp.add( submit );

		formPanel = new FormPanel();
		formPanel.setAction( "upload" );
		formPanel.setMethod( FormPanel.METHOD_POST );
		formPanel.setEncoding( FormPanel.ENCODING_MULTIPART );
		formPanel.add( vp );

		submit.addClickHandler( new ClickHandler()
		{
			@Override
			public void onClick( ClickEvent event )
			{
				formPanel.submit();
			}
		} );

		formPanel.addSubmitCompleteHandler( new SubmitCompleteHandler()
		{
			@Override
			public void onSubmitComplete( SubmitCompleteEvent event )
			{
				AppCache.getIfSupported().update();

				setMode( true );
			}
		} );
	}

	void setMode( boolean isButton )
	{
		if( isButton )
			panel.setWidget( upload );
		else
			panel.setWidget( formPanel );
	}
}