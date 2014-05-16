package com.lteconsulting.offlinedemo.shared;

import java.util.Arrays;

import com.lteconsulting.offlinedemo.shared.synchro.SynchroConfig;
import com.lteconsulting.offlinedemo.shared.synchro.TableConfig;

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
