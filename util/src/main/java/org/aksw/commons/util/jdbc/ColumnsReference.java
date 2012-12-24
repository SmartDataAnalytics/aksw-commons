package org.aksw.commons.util.jdbc;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ColumnsReference
{
	private String tableName;
	private List<String> columnNames = new ArrayList<String>();

	public ColumnsReference(String tableName, String ... columnNames)
	{
		this.tableName = tableName;
        this.columnNames.addAll(Arrays.asList(columnNames));
	}
	
	public String getTableName() {
		return tableName;
	}
	public List<String> getColumnNames() {
		return columnNames;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((columnNames == null) ? 0 : columnNames.hashCode());
		result = prime * result
				+ ((tableName == null) ? 0 : tableName.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ColumnsReference other = (ColumnsReference) obj;
		if (columnNames == null) {
			if (other.columnNames != null)
				return false;
		} else if (!columnNames.equals(other.columnNames))
			return false;
		if (tableName == null) {
			if (other.tableName != null)
				return false;
		} else if (!tableName.equals(other.tableName))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "ColumnsReference [tableName=" + tableName + ", columnNames="
				+ columnNames + "]";
	}
}
