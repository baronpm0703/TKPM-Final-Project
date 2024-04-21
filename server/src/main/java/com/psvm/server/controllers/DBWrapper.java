package com.psvm.server.controllers;

import com.psvm.server.models.DBConnection;
import com.psvm.server.models.DBInteraction;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Vector;

public class DBWrapper {
	DBInteraction dbConn;

	DBWrapper() {
		try {
			dbConn = new DBInteraction(DBConnection.getConnection());
		} catch (SQLException exc) {
			System.out.println("Exception thrown while connecting to database in " + this.getClass().getSimpleName() + ": " + exc.getMessage());
		}
	}

	public ResultSet getFriendList(String currentUsername) throws SQLException {
		String sql = "SELECT FriendId FROM Friend WHERE UserId=?";

		Vector<Object> questionMarks = new Vector<>();
		questionMarks.add(currentUsername);

		return dbConn.doPreparedQuery(sql, questionMarks);
	}

	public void close() {
		dbConn.close();
	}
}
