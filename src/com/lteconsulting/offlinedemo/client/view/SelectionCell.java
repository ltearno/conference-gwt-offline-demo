package com.lteconsulting.offlinedemo.client.view;

import java.util.HashMap;
import java.util.Map;

import com.google.gwt.cell.client.AbstractInputCell;
import com.google.gwt.cell.client.ValueUpdater;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.BrowserEvents;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.dom.client.SelectElement;
import com.google.gwt.safehtml.client.SafeHtmlTemplates;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;

public class SelectionCell extends AbstractInputCell<Integer, Integer> {
	interface Template extends SafeHtmlTemplates {
		@Template("<option value=\"{0}\">{0}</option>")
		SafeHtml deselected(String option);

		@Template("<option value=\"{0}\" selected=\"selected\">{0}</option>")
		SafeHtml selected(String option);
	}

	private static Template template;

	// data id => index
	private HashMap<Integer, Integer> indexForOption = new HashMap<Integer, Integer>();
	private HashMap<Integer, Integer> optionForIndex = new HashMap<Integer, Integer>();

	// data id => label
	private final Map<Integer, String> options;

	/**
	 * Construct a new {@link SelectionCell} with the specified options.
	 * 
	 * @param options
	 *            the options in the cell
	 */
	public SelectionCell(Map<Integer, String> options)
	{
		super(BrowserEvents.CHANGE);
		if (template == null) {
			template = GWT.create(Template.class);
		}
		this.options = new HashMap<Integer, String>(options);
		int index = 0;
		for (Integer option : options.keySet()) {
			indexForOption.put(option, index);
			optionForIndex.put(index, option);
			index++;
		}
	}

	@Override
	public void onBrowserEvent( Context context, Element parent, Integer value, NativeEvent event, ValueUpdater<Integer> valueUpdater )
	{
		super.onBrowserEvent(context, parent, value, event, valueUpdater);
		String type = event.getType();
		if (BrowserEvents.CHANGE.equals(type)) {
			Object key = context.getKey();
			SelectElement select = parent.getFirstChild().cast();
			Integer newValue = optionForIndex.get(select.getSelectedIndex());
			setViewData(key, newValue);
			finishEditing(parent, newValue, key, valueUpdater);
			if (valueUpdater != null) {
				valueUpdater.update(newValue);
			}
		}
	}

	@Override
	public void render(Context context, Integer value, SafeHtmlBuilder sb)
	{
		// Get the view data.
		Object key = context.getKey();
		Integer viewData = getViewData(key);
		if (viewData != null && viewData.equals(value)) 
		{
			clearViewData(key);
			viewData = null;
		}

		int selectedIndex = getSelectedIndex( viewData == null ? value : viewData );
		
		sb.appendHtmlConstant("<select tabindex=\"-1\">");
		
		for (int i = 0; i < options.size(); i++)
		{
			String label = options.get(optionForIndex.get(i));
			if (i == selectedIndex)
				sb.append(template.selected(label));
			else
				sb.append(template.deselected(label));
		}
		
		if( selectedIndex < 0 )
			sb.append( template.selected( "NO PRODUCT SELECTED !" ) );
		
		sb.appendHtmlConstant("</select>");
	}

	private int getSelectedIndex(Integer value)
	{
		Integer index = indexForOption.get(value);
		if( index == null )
			return -1;
		
		return index.intValue();
	}
}
