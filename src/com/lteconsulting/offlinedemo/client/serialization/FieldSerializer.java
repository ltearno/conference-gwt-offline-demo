package com.lteconsulting.offlinedemo.client.serialization;

import com.lteconsulting.offlinedemo.shared.synchro.dto.Record;


public interface FieldSerializer<T>
{
	// get the field name
	String getName();

	// get the quoted sql value needed from a dto object
	String getSqlQuoted( T dto );

	// get the quoted sql value needed from a record (which contains just a hashmap of all the field values)
	String getSqlQuoted( Record info );
}