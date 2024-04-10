package com.psvm.server.models;

import java.sql.*;
import org.apache.commons.dbcp2.BasicDataSource;

public class DBConnection {
	static final int MAX_CONNECTION = 10;
	static final String currentDir = System.getProperty("user.dir");
	static final String dbRelativePath = "\\server\\src\\main\\resources\\data\\hooYah.db";

	private static BasicDataSource ds = new BasicDataSource();

	static {
		ds.setUrl("jdbc:sqlite:" + currentDir + dbRelativePath);
		ds.setMaxTotal(MAX_CONNECTION);
		ds.setMinIdle(MAX_CONNECTION);
		ds.setMaxOpenPreparedStatements(100);
	}

	public static Connection getConnection() throws SQLException {
		return ds.getConnection();
	}

	private DBConnection(){ }
}
