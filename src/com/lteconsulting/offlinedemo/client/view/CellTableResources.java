package com.lteconsulting.offlinedemo.client.view;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.cellview.client.CellTable;

public interface CellTableResources extends CellTable.Resources
{
	public static CellTable.Resources INSTANCE = GWT.create( CellTableResources.class );

	@Override
	@Source("TableStyle.css")
	CellTable.Style cellTableStyle();

	interface TableStyle extends CellTable.Style
	{
	}
}
