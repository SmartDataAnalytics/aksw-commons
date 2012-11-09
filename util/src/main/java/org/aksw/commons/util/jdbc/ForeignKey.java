package org.aksw.commons.util.jdbc;

import com.google.common.base.Joiner;

public class ForeignKey
	extends DatabaseObject
	
{
	private ColumnsReference source; //= new ColumnReference();
	private ColumnsReference target; //= new ColumnReference();

	public ForeignKey(String name, ColumnsReference source, ColumnsReference target) {
		super(name);
		this.source = source;
		this.target = target;
	}
	
	public ColumnsReference getSource() {
		return source;
	}

	public ColumnsReference getTarget() {
		return target;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((source == null) ? 0 : source.hashCode());
		result = prime * result + ((target == null) ? 0 : target.hashCode());
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
		ForeignKey other = (ForeignKey) obj;
		if (source == null) {
			if (other.source != null)
				return false;
		} else if (!source.equals(other.source))
			return false;
		if (target == null) {
			if (other.target != null)
				return false;
		} else if (!target.equals(other.target))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "CONSTRAINT " + this.getName() + " FOREIGN KEY " + source.getTableName() + "(" + Joiner.on(", ").join(source.getColumnNames()) + ") REFERENCES " + target.getTableName() + "(" + Joiner.on(", ").join(target.getColumnNames()) + ")";
	}
}