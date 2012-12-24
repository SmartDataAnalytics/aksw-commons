package org.aksw.commons.util.jdbc;

import com.google.common.base.Joiner;

public class PrimaryKey
	extends DatabaseObject
{
	private ColumnsReference source;

	public PrimaryKey(String name, ColumnsReference source) {
		super(name);
		this.source = source;
	}
	
	public ColumnsReference getSource()
	{
		return source;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((source == null) ? 0 : source.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		PrimaryKey other = (PrimaryKey) obj;
		if (source == null) {
			if (other.source != null)
				return false;
		} else if (!source.equals(other.source))
			return false;
		return true;
	}

	
	@Override
	public String toString() {
		return "CONSTRAINT " + this.getName() + " PRIMARY KEY " + source.getTableName() + "(" + Joiner.on(",").join(source.getColumnNames()) + ")";
	}	
}