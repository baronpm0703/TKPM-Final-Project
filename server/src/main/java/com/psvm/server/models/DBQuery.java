package com.psvm.server.models;

import java.sql.*;
import java.util.HashMap;

public class DBQuery {
	private final Connection dbConn;
	private Statement dbAction;

	DBQuery(Connection connection) {
		dbConn = connection;
		try {
			dbAction = dbConn.createStatement();
			dbAction.executeUpdate("PRAGMA foreign_keys = ON;");
		}
		catch (SQLException exc) {
			System.out.println("Exception thrown while creating statement in " + this.getClass().getSimpleName() + ": " + exc.getMessage());
		}

	}

	/* Insert statement */
	// Insert a row
	public void doInsert(String tableName, Object[] data) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < data.length; i++) {
			if (data[i].getClass() == String.class) {
				String temp = "\"" + data[i] + "\"";
				sb.append(temp);
			}
			else sb.append(data[i]);

			if (i < data.length - 1) sb.append(", ");
		}

		try {
			dbAction.executeUpdate("INSERT INTO " + tableName + " VALUES (" + sb.toString() + ");");
		}
		catch (SQLException exc) {
			System.out.println("Exception thrown while performing Insert in " + this.getClass().getSimpleName() + ": " + exc.getMessage());
		}
	}
	// Insert with specific attributes. Unspecified ones are assigned null, and throw exception if null is assigned to non-null attributes
	public void doInsert(String tableName, HashMap<String, Object> data) {
//		StringBuilder sbAttr = new StringBuilder();
//		StringBuilder sbValue = new StringBuilder();
//		for (int i = 0; i < data.keySet().size(); i++) {
//			String attrName = "\"" = data.keySet().
//			sbAttr.append()
//			if (data[i].getClass() == String.class) {
//				String temp = "\"" + data[i] + "\"";
//				sb.append(temp);
//			}
//			else sb.append(data[i]);
//
//			if (i < data.keySet().size() - 1) sbValue.append(", ");
//		}
//
//		try (Statement stmt = dbConn.createStatement()) {
//			stmt.executeUpdate("INSERT INTO " + tableName + " VALUES (" + sb.toString() + ");");
//		}
//		catch (SQLException exc) {
//			System.out.println("Error: " + exc.getMessage());
//		}
	}

	// Update statement
	public void doUpdate(String tableName, HashMap<String, String> where, HashMap<String, Object> newData) {

	}

	// Delete statement
	public void doDelete(String tableName, HashMap<String, Object> where) {

	}

	// Select statement
	public ResultSet doQuery(String tableName) {
		try{
			return dbAction.executeQuery("select * from " + tableName);
		}
		catch (SQLException exc) {
			System.out.println("Exception thrown while performing Query in " + this.getClass().getSimpleName() + ": " + exc.getMessage());
			return null;
		}
	}
	public ResultSet doQuery(String tableName, String[] attributes) {
		try {
			StringBuilder attrString = new StringBuilder();
			for (int i = 0; i < attributes.length; i++) {
				attrString.append(attributes[i]);

				if (i < attributes.length - 1) attrString.append(", ");
			}

			return dbAction.executeQuery("select " + attrString.toString() + " from " + tableName);
		}
		catch (SQLException exc) {
			System.out.println("Exception thrown while performing Query in " + this.getClass().getSimpleName() + ": " + exc.getMessage());
			return null;
		}
	}
	public ResultSet doQuery(String tableName, HashMap<String, Object> where) {
		try {
			StringBuilder whereString = new StringBuilder();
			int j = 0;
			for (String whereAttr: where.keySet()) {
				String condition = whereAttr + "=" + where.get(whereAttr) + " ";
				whereString.append(condition);
				if (j < where.size() - 1) whereString.append(" and ");
				j++;
			}

			return dbAction.executeQuery("select * from " + tableName + " where " + whereString.toString());
		}
		catch (SQLException exc) {
			System.out.println("Exception thrown while performing Query in " + this.getClass().getSimpleName() + ": " + exc.getMessage());
			return null;
		}
	}
	public ResultSet doQuery(String tableName, String[] attributes, HashMap<String, Object> where) {
		try {
			StringBuilder attrString = new StringBuilder();
			for (int i = 0; i < attributes.length; i++) {
				attrString.append(attributes[i]);

				if (i < attributes.length - 1) attrString.append(", ");
			}

			StringBuilder whereString = new StringBuilder();
			int j = 0;
			for (String whereAttr: where.keySet()) {
				String condition = whereAttr + "=" + where.get(whereAttr) + " ";
				whereString.append(condition);
				if (j < where.size() - 1) whereString.append(" and ");
				j++;
			}

			return dbAction.executeQuery("select " + attrString.toString() + " from " + tableName + " where " + whereString.toString());
		}
		catch (SQLException exc) {
			System.out.println("Exception thrown while performing Query in " + this.getClass().getSimpleName() + ": " + exc.getMessage());
			return null;
		}
	}

	// Raw SQL
	public void doRawStatement(String rawSQL) {
		try {
			dbAction.executeUpdate(rawSQL);
		}
		catch (SQLException exc) {
			System.out.println("Error: " + exc.getMessage());
		}
	}
	public ResultSet doRawQuery(String rawSQL) {
		try {
			return dbAction.executeQuery(rawSQL);
		}
		catch (SQLException exc) {
			System.out.println("Error: " + exc.getMessage());
			return null;
		}
	}

	// Print a table in the database
//	public void printTable(String tableName, String... attributes) {
//		synchronized (db) {
//			try (Statement stmt = db.createStatement()) {
//				stmt.setQueryTimeout(30);
//				StringBuilder queryAttributes = new StringBuilder();
//				for (String attribute: attributes) {
//					queryAttributes.append(attribute + ",");
//				}
//
//				ResultSet rs = stmt.executeQuery(
//						"select " + (queryAttributes.isEmpty() ? "*" : queryAttributes.toString()) + " from " + tableName
//				);
//
//				// Get table columns
//				ResultSetMetaData rsMeta = rs.getMetaData();
//				String columns = "";
//				for (int i = 0; i < rsMeta.getColumnCount(); i++) {
//					columns += String.format("%-15s | ", rsMeta.getColumnName(i));
//				}
//				System.out.println(columns);
//			}
//			catch (SQLException exc) {
//				System.out.println("Error: " + exc.getMessage());
//			}
//		}
//	}
}
