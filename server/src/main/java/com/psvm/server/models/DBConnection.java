package com.psvm.server.models;

import java.sql.*;
import org.apache.commons.dbcp2.BasicDataSource;

public class DBConnection {
	static final int MAX_CONNECTION = 10;
	static final String currentDir = System.getProperty("user.dir");
	static final String dbRelativePath = "/hooyah";
	static final String dbUser = System.getenv("DB_USER");
	static final String dbPwd = System.getenv("DB_PWD");

	private static BasicDataSource ds = new BasicDataSource();

	static {
		ds.setDefaultAutoCommit(false);
		ds.setDriverClassName("com.mysql.cj.jdbc.Driver");
		ds.setUrl("jdbc:mysql://localhost" + dbRelativePath);
		ds.setUsername(dbUser);
		ds.setPassword(dbPwd);
		ds.setMaxTotal(MAX_CONNECTION);
		ds.setMinIdle(MAX_CONNECTION);
		ds.setMaxOpenPreparedStatements(100);
	}

	public static Connection getConnection() throws SQLException {
		return ds.getConnection();
	}

	private DBConnection(){ }
}
