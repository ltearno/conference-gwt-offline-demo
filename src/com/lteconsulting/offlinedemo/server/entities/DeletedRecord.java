package com.lteconsulting.offlinedemo.server.entities;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.Generated;
import org.hibernate.annotations.GenerationTime;

@Entity
@Table( name = "deleted_records" )
public class DeletedRecord
{
	@Id
	@GeneratedValue
	private int id;

	@Column( name = "table_name" )
	String tableName;

	@Column( name = "record_id" )
	int recordId;

	@Column( name = "update_date", columnDefinition="timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP" )
	@Generated( GenerationTime.ALWAYS )
	Date updateDate;

	public String getTableName()
	{
		return tableName;
	}

	public void setTableName( String tableName )
	{
		this.tableName = tableName;
	}

	public int getRecordId()
	{
		return recordId;
	}

	public void setRecordId( int recordId )
	{
		this.recordId = recordId;
	}

	public Date getUpdateDate()
	{
		return updateDate;
	}

	public void setUpdateDate( Date updateDate )
	{
		this.updateDate = updateDate;
	}

	public int getId()
	{
		return id;
	}
}
