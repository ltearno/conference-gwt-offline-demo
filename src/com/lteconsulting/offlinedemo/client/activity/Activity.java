package com.lteconsulting.offlinedemo.client.activity;

import com.google.gwt.user.client.ui.AcceptsOneWidget;

public interface Activity
{
	void start( AcceptsOneWidget container );

	void save();

	void refresh();

	// return null if ok to stop activity, a message to be displayed to the user otherwise...
	String canStop();
}
