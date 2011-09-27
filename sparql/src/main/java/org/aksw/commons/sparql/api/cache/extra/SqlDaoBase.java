package org.aksw.commons.sparql.api.cache.extra;


import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.aksw.commons.sparql.api.cache.core.QueryString;
import org.apache.log4j.Logger;


public class SqlDaoBase
	implements SqlDao
{
	protected Connection conn;
	protected Map<Object, PreparedStatement> queryToStmt = new HashMap<Object, PreparedStatement>();

	protected Map<Object, String> idToQuery = new HashMap<Object, String>();

	protected SqlDaoBase()
	{
	}


	protected SqlDaoBase(Collection<? extends QueryString> queries)
	{
		for(QueryString item : queries) {
			idToQuery.put(item, item.getQueryString());
		}
	}


	protected void setPreparedStatement(Object id, String query)
	{
		Logger.getLogger(this.getClass()).trace("Preparing statement [" + id + "]: " + query);

		idToQuery.put(id, query);
	}


	private void close()
		throws SQLException
	{
		for(PreparedStatement item : queryToStmt.values()) {
			if(item != null)
				item.close();
		}

		queryToStmt.clear();
	}


	@Override
	public void setConnection(Connection conn)
		throws SQLException
	{
		close();

		for(Map.Entry<Object, String> entry : idToQuery.entrySet()) {
			PreparedStatement stmt = conn.prepareStatement(entry.getValue());

			queryToStmt.put(entry.getKey(), stmt);
		}

		this.conn = conn;
	}

	private PreparedStatement getPreparedStatement(Object id)
	{
		PreparedStatement stmt = queryToStmt.get(id);
		if(stmt == null)
			throw new RuntimeException("No such query with id " + id);

		return stmt;
	}


	public <T> T execute(Object id, Class<T> clazz, Object ...args)
		throws SQLException
	{
		PreparedStatement stmt = getPreparedStatement(id);

		T result = SqlUtils.execute(stmt, clazz, args);

		return result;
	}

	public <T> List<T> executeList(Object id, Class<T> clazz, Object ...args)
		throws SQLException
	{
		PreparedStatement stmt = getPreparedStatement(id);

		List<T> result = SqlUtils.executeList(stmt, clazz, args);

		return result;
	}

	public ResultSet executeQuery(Object id, Object ...args)
		throws SQLException
	{
		PreparedStatement stmt = getPreparedStatement(id);

		return SqlUtils.execute(stmt, args);
	}


	@Override
	public Connection getConnection()
	{
		return conn;
	}
}
