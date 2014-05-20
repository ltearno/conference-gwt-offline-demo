package com.lteconsulting.offlinedemo.shared;

import java.util.Arrays;

import com.lteconsulting.offlinedemo.shared.synchro.SynchroConfig;
import com.lteconsulting.offlinedemo.shared.synchro.TableConfig;

/*
 * This class holds the synchronization configuration data structure that will
 * be used both on the server and client sides.
 *
 * This means that if you want to use this synchronization implementation for other
 * tables, you might just need to change only this class !
 */
public class ApplicationSharedConstants
{
	public final static SynchroConfig SYNCHRO_CONFIG;

	static
	{
		SYNCHRO_CONFIG = new SynchroConfig( "offlinedemo" );
		SYNCHRO_CONFIG.setTableConfigs( Arrays.asList( new TableConfig( "articles" ).addStringField( "code" ).addStringField( "name" ).addIntField( "price" ).addStringField( "picture" ).addStringField( "pdf" ),
				new TableConfig( "orders" ).addStringField( "date" ).addStringField( "addressCode" ),
				new TableConfig( "order_items" ).addLookupField( "order_id", "orders" ).addLookupField( "article_id", "articles" ).addIntField( "quantity" ).addIntField( "unit_price" ).addIntField( "amount" ) ) );
	}
}
