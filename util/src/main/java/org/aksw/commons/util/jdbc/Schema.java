package org.aksw.commons.util.jdbc;

import com.google.common.collect.Multimap;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;

/**
 * @author Claus Stadler
 *
 *         Date: 11/9/12
 *         Time: 4:36 PM
 */
public class Schema {

	private Map<String, Relation> relations;
	private Map<String, PrimaryKey> primaryKeys;
	private Multimap<String, ForeignKey> foreignKeys;

	public static Schema create(Connection conn)
			throws SQLException
	{
		Map<String, Relation> relations = JdbcUtils.fetchColumns(conn);
		Map<String, PrimaryKey> primaryKeys = JdbcUtils.fetchPrimaryKeys(conn);
		Multimap<String, ForeignKey> foreignKeys = JdbcUtils.fetchForeignKeys(conn);

		Schema result = new Schema(relations, primaryKeys, foreignKeys);
		return result;
	}

	public Schema(Map<String, Relation> relations, Map<String, PrimaryKey> primaryKeys, Multimap<String, ForeignKey> foreignKeys) {
		this.relations = relations;
		this.primaryKeys = primaryKeys;
		this.foreignKeys = foreignKeys;
	}

	public Map<String, Relation> getRelations() {
		return relations;
	}

	public Map<String, PrimaryKey> getPrimaryKeys() {
		return primaryKeys;
	}

	public Multimap<String, ForeignKey> getForeignKeys() {
		return foreignKeys;
	}
}

