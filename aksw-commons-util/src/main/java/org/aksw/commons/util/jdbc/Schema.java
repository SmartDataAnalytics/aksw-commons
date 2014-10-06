package org.aksw.commons.util.jdbc;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.sql.DataSource;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

/**
 * @author Claus Stadler
 *
 *         Date: 11/9/12
 *         Time: 4:36 PM
 */
public class Schema {

    // All maps are based on the relation name!
    private Map<String, Relation> relations;
    private Map<String, PrimaryKey> primaryKeys;
    private Multimap<String, ForeignKey> foreignKeys;
    private Multimap<String, Index> indexes;

    //private List<String>

    //private volatile Map<String, Multimap<String, Index>> tableToIndexes;

     public Schema(Map<String, Relation> relations, Map<String, PrimaryKey> primaryKeys, Multimap<String, ForeignKey> foreignKeys, Multimap<String, Index> indexes) {
        this.relations = relations;
        this.primaryKeys = primaryKeys;
        this.foreignKeys = foreignKeys;
        this.indexes = indexes;
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

    public Multimap<String, Index> getIndexes() {
        return indexes;
    }


    public static Schema create(Connection conn)
            throws SQLException
    {
        Schema result = create(conn, null, null);
        return result;
    }

    public static Schema create(Connection conn, String schema, Iterable<String> tableNames)
            throws SQLException
    {
        DatabaseMetaData meta = conn.getMetaData();
        String catalog = conn.getCatalog();
        Schema result = create(meta, catalog, schema, tableNames);

        return result;
    }

    public static Schema create(DatabaseMetaData meta, String catalog, String schema, Iterable<String> tableNames)
            throws SQLException
    {
        Map<String, Relation> relations = JdbcUtils.fetchColumns(meta, catalog, schema, tableNames);
        Map<String, PrimaryKey> primaryKeys = JdbcUtils.fetchPrimaryKeys(meta, catalog, schema, tableNames);
        Multimap<String, ForeignKey> foreignKeys = JdbcUtils.fetchForeignKeys(meta, catalog, schema, tableNames);
        Set<String> tNames = relations.keySet(); // should be equal to tableNames, unless tableNames was null
        Multimap<String, Index> indexes = JdbcUtils.fetchIndexes(meta, catalog, schema, tNames, true);

        Schema result = new Schema(relations, primaryKeys, foreignKeys, indexes);
        return result;
    }


    public static Schema create(DatabaseMetaData meta, String catalog)
            throws SQLException
    {
        Schema result = create(meta, catalog, null, null);

        return result;
    }

    public static Schema createSchema(DataSource dataSource) throws SQLException {
        Schema result;
        Connection conn = dataSource.getConnection();
        try {
            result = Schema.create(conn);
        }
        finally {
            if(conn != null) {
                conn.close();
            }
        }

        return result;
    }
}


