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


 	public Schema(Map<String, Relation> relations, Map<String, PrimaryKey> primaryKeys, Multimap<String, ForeignKey> foreignKeys) {
		this.relations = relations;
		this.primaryKeys = primaryKeys;
		this.foreignKeys = foreignKeys;
	}


    /**
     *
     * @return A Map from relation names to relation objects
     */
	public Map<String, Relation> getRelations() {
		return relations;
	}


    /**
     *
     * @return A Map from relation names to their primary key. No entry for relations without a primary key.
     */
	public Map<String, PrimaryKey> getPrimaryKeys() {
		return primaryKeys;
	}


    /**
     *
     * @return A Multimap from relation names to their sets of foreign keys. Empty set if there are none.
     */
	public Multimap<String, ForeignKey> getForeignKeys() {
		return foreignKeys;
	}

    
    public static Schema create(Connection conn)
            throws SQLException
    {
        Map<String, Relation> relations = JdbcUtils.fetchColumns(conn);
        Map<String, PrimaryKey> primaryKeys = JdbcUtils.fetchPrimaryKeys(conn, relations.keySet());
        Multimap<String, ForeignKey> foreignKeys = JdbcUtils.fetchForeignKeys(conn, relations.keySet());

        Schema result = new Schema(relations, primaryKeys, foreignKeys);
        return result;
    }

}


