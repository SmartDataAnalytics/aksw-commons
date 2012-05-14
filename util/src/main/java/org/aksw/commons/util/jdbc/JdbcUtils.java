package org.aksw.commons.util.jdbc;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;


public class JdbcUtils {

	public static Multimap<String, ForeignKey> fetchForeignKeys(Connection conn)
			throws SQLException
	{
		//Map<String, ForeignKey> result = new HashMap<String, ForeignKey>();
		HashMultimap<String, ForeignKey> result = HashMultimap.create();
		
		
		DatabaseMetaData meta = conn.getMetaData();

		ResultSet rs = meta.getExportedKeys(conn.getCatalog(), null, null);
		
	
		//System.out.println(getColumnNames(rs));
		
		ForeignKey current = null;
		
		
		while (rs.next()) {
			//System.out.println(getRow(rs));

			String pkTableName = rs.getString("PKTABLE_NAME");
			String pkColumnName = rs.getString("PKCOLUMN_NAME");
			String fkName = rs.getString("FK_NAME");
			String fkTableName = rs.getString("FKTABLE_NAME");
			String fkColumnName = rs.getString("FKCOLUMN_NAME");
			//int fkSequence = rs.getInt("KEY_SEQ");
			
			
			if(current == null || !fkTableName.equals(current.getSource().getTableName())) {
				current = new ForeignKey(fkName, new ColumnsReference(fkTableName), new ColumnsReference(pkTableName));
				
				result.put(fkTableName, current);
			}
			current.getSource().getColumnNames().add(pkColumnName);
			current.getTarget().getColumnNames().add(fkColumnName);
		}

		//st.close();
		conn.close();
		
		return result;
	}

	public static Map<String, PrimaryKey> fetchPrimaryKeys(Connection conn) throws SQLException {
		//Map<String, ForeignKey> result = new HashMap<String, ForeignKey>();
		Map<String, PrimaryKey> result = new HashMap<String, PrimaryKey>();
		
		
		DatabaseMetaData meta = conn.getMetaData();

		ResultSet rs = meta.getPrimaryKeys(conn.getCatalog(), null, null);
		
	
		//System.out.println(getColumnNames(rs));
		
		PrimaryKey current = null;
		
		
		while (rs.next()) {
			//System.out.println(getRow(rs));

			String tableName = rs.getString("TABLE_NAME");
			String columnName = rs.getString("COLUMN_NAME");
			String pkName = rs.getString("PK_NAME");
						
			if(current == null || !tableName.equals(current.getSource().getTableName())) {
				current = new PrimaryKey(pkName, new ColumnsReference(tableName));
				
				result.put(tableName, current);
			}
			current.getSource().getColumnNames().add(columnName);
		}

		//st.close();
		conn.close();
		
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
	public static Set<String> fetchRelationNames(Connection conn) throws SQLException {
		String[] types = {"TABLE", "VIEW"};
		ResultSet rs = conn.getMetaData().getTables(conn.getCatalog(), null, null, types);

		Set<String> result = new TreeSet<String>();
		while (rs.next()) {
			result.add(rs.getString("TABLE_NAME"));
		}
		
		return result;
	}

    public static Map<String, Relation> fetchColumns(Connection conn)
            throws SQLException
    {
        Map<String, Relation> result = new HashMap<String, Relation>();

        DatabaseMetaData meta = conn.getMetaData();

        ResultSet rs = meta.getColumns(conn.getCatalog(), null, null, null);

        Relation current = null;

        while (rs.next()) {
            //System.out.println(getRow(rs));

            String tableName = rs.getString("TABLE_NAME");
            String columnName = rs.getString("COLUMN_NAME");
            String typeName = rs.getString("TYPE_NAME");

            if(current == null || !tableName.equals(current.getName())) {
                current = new Relation(tableName);

                result.put(tableName, current);
            }
            Column column = new Column(columnName, typeName);
            current.getColumns().put(columnName, column);
        }

        //st.close();
        conn.close();


        return result;
    }
		    

}

