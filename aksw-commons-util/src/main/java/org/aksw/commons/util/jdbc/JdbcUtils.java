package org.aksw.commons.util.jdbc;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;


public class JdbcUtils {

    private static final Logger logger = LoggerFactory.getLogger(JdbcUtils.class);

    public static Multimap<String, ForeignKey> fetchForeignKeys(Connection conn)
            throws SQLException
    {
        DatabaseMetaData meta = conn.getMetaData();
        String catalog = conn.getCatalog();

        Multimap<String, ForeignKey> result = fetchForeignKeys(meta, catalog);

        return result;
    }

    public static Multimap<String, ForeignKey> fetchForeignKeys(DatabaseMetaData meta, String catalog)
        throws SQLException
    {
        Multimap<String, ForeignKey> result = fetchForeignKeys(meta, catalog, null, (String)null);
        return result;
    }


    public static Multimap<String, ForeignKey> fetchForeignKeys(DatabaseMetaData meta, String catalog, String schema, Iterable<String> tables)
        throws SQLException
    {
        HashMultimap<String, ForeignKey> result = HashMultimap.create();

        if(tables == null) {
            tables = Collections.singleton(null);
        }

        for(String table : tables) {
            Multimap<String, ForeignKey> tmp = fetchForeignKeys(meta, catalog, schema, table);
            result.putAll(tmp);
        }

        return result;
    }

    public static Multimap<String, ForeignKey> fetchForeignKeys(DatabaseMetaData meta, String catalog, String schema, String table)
            throws SQLException
    {
        Multimap<String, ForeignKey> result = HashMultimap.create();

        ResultSet rs = meta.getExportedKeys(catalog, schema, table);

        try {
            Map<String, ForeignKey> fkNameMap = new HashMap<String, ForeignKey>();
            while (rs.next()) {

                String fkName = rs.getString("FK_NAME");
                String fkTableName = rs.getString("FKTABLE_NAME");
                String fkColumnName = rs.getString("FKCOLUMN_NAME");
                //int fkSequence = rs.getInt("KEY_SEQ");

                String pkTableName = rs.getString("PKTABLE_NAME");
                String pkColumnName = rs.getString("PKCOLUMN_NAME");

                ForeignKey current = fkNameMap.get(fkName);
                if(current == null) {
                    current = new ForeignKey(fkName, new ColumnsReference(fkTableName), new ColumnsReference(pkTableName));

                    fkNameMap.put(fkName, current);
                }

                current.getSource().getColumnNames().add(fkColumnName);
                current.getTarget().getColumnNames().add(pkColumnName);
            }

            for(ForeignKey fk : fkNameMap.values()) {
                result.put(fk.getSource().getTableName(), fk);
            }

        } finally {
            rs.close();
        }

        return result;
    }



    public static Map<String, PrimaryKey> fetchPrimaryKeys(Connection conn)
            throws SQLException
    {
        DatabaseMetaData meta = conn.getMetaData();
        String catalog = conn.getCatalog();

        Map<String, PrimaryKey> result = fetchPrimaryKeys(meta, catalog);

        return result;
    }

    public static Map<String, PrimaryKey> fetchPrimaryKeys(DatabaseMetaData meta, String catalog) throws SQLException {
        Map<String, PrimaryKey> result = fetchPrimaryKeys(meta, catalog, null, (String)null);
        return result;
    }

    public static Map<String, PrimaryKey> fetchPrimaryKeys(DatabaseMetaData meta, String catalog, String schema, Iterable<String> tables)
        throws SQLException
    {
        Map<String, PrimaryKey> result = new HashMap<String, PrimaryKey>();

        if(tables == null) {
            tables = Collections.singleton(null);
        }

        for(String table : tables) {
            Map<String, PrimaryKey> tmp = fetchPrimaryKeys(meta, catalog, schema, table);
            result.putAll(tmp);
        }

        return result;
    }

    public static Map<String, PrimaryKey> fetchPrimaryKeys(DatabaseMetaData meta, String catalog, String schema, String table) throws SQLException {
        Map<String, PrimaryKey> result = new HashMap<String, PrimaryKey>();

        //ResultSet rs = meta.getPrimaryKeys(conn.getCatalog(), null, null);
        ResultSet rs = meta.getPrimaryKeys(catalog, null, null);

        try {
            PrimaryKey current = null;
            while (rs.next()) {

                String tableName = rs.getString("TABLE_NAME");
                String columnName = rs.getString("COLUMN_NAME");
                String pkName = rs.getString("PK_NAME");

                if(current == null || !tableName.equals(current.getSource().getTableName())) {
                    current = new PrimaryKey(pkName, new ColumnsReference(tableName));

                    result.put(tableName, current);
                }
                current.getSource().getColumnNames().add(columnName);

            }
        } finally {
            rs.close();
        }

        return result;
    }



    public static Set<String> fetchRelationNames(Connection conn)
            throws SQLException
    {
        DatabaseMetaData meta = conn.getMetaData();
        String catalog = conn.getCatalog();

        Set<String> result = fetchRelationNames(meta, catalog);

        return result;
    }


    /**
     * For each table retrieve all columns and their foreign key relations
     *
     * Retrieving foreign keys:
     * http://www.java2s.com/Code/Java/Database-SQL-JDBC/GetForeignKeys.htm
     *
     * @param conn
     * @throws SQLException
     */
    public static Set<String> fetchRelationNames(DatabaseMetaData meta, String catalog) throws SQLException {
        String[] types = {"TABLE", "VIEW"};
        ResultSet rs = meta.getTables(catalog, null, null, types);

        Set<String> result = new TreeSet<String>();
        try {
            while (rs.next()) {
                result.add(rs.getString("TABLE_NAME"));
            }
        } finally {
            rs.close();
        }

        return result;
    }


    public static Map<String, Relation> fetchColumns(Connection conn)
            throws SQLException
    {
        DatabaseMetaData meta = conn.getMetaData();
        String catalog = conn.getCatalog();

        Map<String, Relation> result = fetchColumns(meta, catalog, null, (String)null);

        return result;
    }

    public static Map<String, Relation> fetchColumns(Connection conn, String schema, String table)
            throws SQLException
    {
        DatabaseMetaData meta = conn.getMetaData();
        String catalog = conn.getCatalog();

        Map<String, Relation> result = fetchColumns(meta, catalog, schema, table);

        return result;
    }

    public static Map<String, Relation> fetchColumns(DatabaseMetaData meta, String catalog)
            throws SQLException
    {
        Map<String, Relation> result = fetchColumns(meta, catalog, null, (String)null);

        return result;
    }

    public static Map<String, Relation> fetchColumns(DatabaseMetaData meta, String catalog, String schema, Iterable<String> tables)
            throws SQLException
    {
        Map<String, Relation> result = new HashMap<String, Relation>();

        if(tables == null) {
            tables = Collections.singleton(null);
        }

        for(String table : tables) {
            Map<String, Relation> tmp = fetchColumns(meta, catalog, schema, table);
            result.putAll(tmp);
        }

        return result;
    }

    public static Map<String, Relation> fetchColumns(DatabaseMetaData meta, String catalog, String schema, String table)
            throws SQLException
    {
        Map<String, Relation> result = new HashMap<String, Relation>();

        ResultSet rs = meta.getColumns(catalog, schema, table, null);

        try {
            Relation current = null;
            while (rs.next()) {

                String tableName = rs.getString("TABLE_NAME");
                String columnName = rs.getString("COLUMN_NAME");
                String typeName = rs.getString("TYPE_NAME");
                String rawIsNullable = rs.getString("IS_NULLABLE");
//                String schema = rs.getString("TABLE_SCHEM");
                int ordinalPosition = rs.getInt("ORDINAL_POSITION");

                Boolean isNullable = null;
                if("YES".equalsIgnoreCase(rawIsNullable)) {
                    isNullable = true;
                } else if("NO".equalsIgnoreCase(rawIsNullable)) {
                    isNullable = false;
                }


                if(current == null || !tableName.equals(current.getName())) {
                    current = new Relation(tableName);

                    result.put(tableName, current);
                }
                Column column = new Column(ordinalPosition, columnName, typeName, isNullable);
                current.getColumns().put(columnName, column);

            }
        } finally {
            rs.close();
        }

        return result;
    }


//	public static Multimap<String, Index> fetchIndexes(Connection conn)
//			throws SQLException
//	{
//		DatabaseMetaData meta = conn.getMetaData();
//		String catalog = conn.getCatalog();
//
//		Multimap<String, Index> result = fetchIndexes(meta, catalog);
//
//		return result;
//	}
//
//    public static Multimap<String, Index> fetchIndexes(DatabaseMetaData meta, String catalog, String schemaName, String tableName) throws SQLException {
//		Multimap<String, Index> result = fetchIndexes(meta, catalog, null, tableName, null);
//
//		return result;
//    }

    public static Multimap<String, Index> fetchIndexes(DatabaseMetaData meta, String catalog, String schemaName, Iterable<String> tableNames, boolean unique)
        throws SQLException
    {
        Multimap<String, Index> result = HashMultimap.create();

        if(tableNames == null) {
            tableNames = Collections.singleton(null);
        }

        for(String tableName : tableNames) {
            Multimap<String, Index> tmp = JdbcUtils.fetchIndexes(meta, catalog, null, tableName, unique);
            result.putAll(tmp);
        }

        return result;
    }


    // FIXME Index argument ignores non-unique indexes
    // FIX for ORA-01702: a view is not appropriate here - we just ignore exceptions as a cheap work around
    public static Multimap<String, Index> fetchIndexes(DatabaseMetaData meta, String catalog, String schemaName, String tableName, boolean unique)
            throws SQLException
    {
        Multimap<String, Index> result;

        try {
            result = fetchIndexesUnsafe(meta, catalog,  schemaName, tableName, unique);
        } catch(Exception e) {
            logger.warn("Failed to retrieve indexes", e);
            result = ArrayListMultimap.create();
        }

        return result;
    }

    public static Multimap<String, Index> fetchIndexesUnsafe(DatabaseMetaData meta, String catalog, String schemaName, String tableName, boolean unique)
            throws SQLException
    {
        Multimap<String, Index> result = ArrayListMultimap.create();// new HashMap<String, Index>();

        ResultSet rs = meta.getIndexInfo(catalog, schemaName, tableName, unique, false);
        //Index current = null;

        Index current = null;

        while(rs.next()) {

            String indexName = rs.getString("INDEX_NAME");
            String table = rs.getString("TABLE_NAME");
            //String schema = rs.getString("TABLE_SCHEM");
            String columnName = rs.getString("COLUMN_NAME");
            //short ordinalPosition = rs.getShort("ORDINAL_POSITION");
            boolean isNonUnique = rs.getBoolean("NON_UNIQUE");
            if(indexName == null) {
                continue;
            }

            if(current == null || !indexName.equals(current.getName())) {
                current = new Index(tableName, table, !isNonUnique);

                result.put(tableName, current);
            }

            current.getColumns().getColumnNames().add(columnName);
        }

        return result;
    }

}

