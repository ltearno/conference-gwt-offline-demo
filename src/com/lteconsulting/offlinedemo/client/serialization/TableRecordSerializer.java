package com.lteconsulting.offlinedemo.client.serialization;

import com.lteconsulting.offlinedemo.client.sql.SQLiteResult.Row;

public interface TableRecordSerializer<T>
{
	// constructs a DTO object from a SQLite result row
	T rowToDto( Row row );

	// constructs an insert statement corresponding to the DTO data
	String dtoToSqlInsert( int id, T dto );

	// constructs an update statement corresponding to the DTO data
	String dtoToSqlUpdate( T dto );
}
