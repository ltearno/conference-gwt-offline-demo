package com.lteconsulting.offlinedemo.client.activity;

import com.google.gwt.user.client.ui.AcceptsOneWidget;

/*
 * This application's particular implementation of the Activity pattern
 */
public interface Activity
{
	// Starts an activity, letting it fill a UI container
	void start( AcceptsOneWidget container );

	// Instructs the Activity to save changes made by the user. Driven by the "save" button
	void save();

	// Instructs the Activity to refresh its UI views with fresh data
	void refresh();

	// return null if ok to stop activity, a message to be displayed to the user otherwise...
	String canStop();
}
