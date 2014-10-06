package org.aksw.commons.util.jdbc;


public interface DataSourceConfig {
	String getDriverClassName();
	String getJdbcUrl();
	String getUsername();
	String getPassword();
}

