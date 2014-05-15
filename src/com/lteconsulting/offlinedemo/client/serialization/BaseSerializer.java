package com.lteconsulting.offlinedemo.client.serialization;

import java.util.List;

import com.lteconsulting.offlinedemo.client.dto.BaseDto;

public abstract class BaseSerializer<T extends BaseDto> implements TableRecordSerializer<T>
{
	String table;
	protected List<FieldSerializer<T>> fields;
	private String sqlFieldList;

	protected BaseSerializer( String table )
	{
		this.table = table;
	}

	@Override
	public final String dtoToSqlInsert( int id, T dto )
	{
		StringBuilder sb = new StringBuilder();
		for( FieldSerializer<T> field : fields )
		{
			sb.append( field.getSqlQuoted( dto ) );
			sb.append( ", " );
		}

		return "insert into " + table + " (" + sqlFieldList() + ") values (" + id + ", " + sb.toString() + "'" + dto.getUpdateDate() + "')";
	}

	@Override
	public final String dtoToSqlUpdate( T dto )
	{
		StringBuilder sb = new StringBuilder();
		for( FieldSerializer<T> field : fields )
		{
			sb.append( field.getName() );
			sb.append( "=" );
			sb.append( field.getSqlQuoted( dto ) );
			sb.append( ", " );
		}

		return "update " + table + " set " + sb.toString() + "update_date='" + dto.getUpdateDate() + "' where id=" + dto.getId();
	}

	private String sqlFieldList()
	{
		if( sqlFieldList == null )
		{
			StringBuilder sb = new StringBuilder();
			sb.append( "id, " );
			for( FieldSerializer<T> field : fields )
			{
				sb.append( field.getName() );
				sb.append( ", " );
			}
			sb.append( "update_date" );
			sqlFieldList = sb.toString();
		}

		return sqlFieldList;
	}
}
