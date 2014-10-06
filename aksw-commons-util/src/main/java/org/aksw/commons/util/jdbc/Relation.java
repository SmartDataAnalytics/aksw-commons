package org.aksw.commons.util.jdbc;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Claus Stadler
 *         <p/>
 *         Date: 12/6/11
 *         Time: 4:48 PM
 */
public class Relation
	extends DatabaseObject
{
	private Map<String, Column> columns = new HashMap<String, Column>();

	public Relation(String tableName) {
		super(tableName);
	}

	/*
	public String getTableName() {
		return tableName;
	}
	*/

	public Map<String, Column> getColumns() {
		return columns;
	}
}
