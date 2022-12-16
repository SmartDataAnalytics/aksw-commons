package org.aksw.commons.util.jdbc;


public class Index
	extends DatabaseObject
{
	// TODO Better replace with some enum or so 
	private boolean isUnique;
	private ColumnsReference columns;
	
	public Index(String name, String tableName, boolean isUnique) {
		this(name, new ColumnsReference(tableName), isUnique);
	}
	
	public Index(String name, ColumnsReference columns, boolean isUnique) {
		super(name);
		this.columns = columns;
		this.isUnique = isUnique;
	}

	public ColumnsReference getColumns() {
		return columns;
	}

	public boolean isUnique() {
		return isUnique;
	}	
}
