package com.psvm.server.models;

import com.psvm.server.models.objects.DBObject;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

public class DBInteraction {
	private final int PRIMARY_KEY_CONTRAINT_ERROR = 0;
	private final Connection dbConn;
	private Statement dbAction;

	public DBInteraction(Connection connection) throws SQLException {
		dbConn = connection;
		try {
			dbAction = dbConn.createStatement();
//			dbAction.executeUpdate("PRAGMA foreign_keys = ON;");
		}
		catch (SQLException exc) {
			System.out.println("Exception thrown while creating statement in " + this.getClass().getSimpleName() + ": " + exc.getMessage());
		}

	}

	public void close() {
		try {
			dbConn.close();
		} catch (SQLException exc) {
			System.out.println("Exception thrown while returning connection to pool in " + this.getClass().getSimpleName() + ": " + exc.getMessage());
		}
	}

	/* Insert */
	// No overwrite
	public void doInsert(String tableName, DBObject[] data) {
		doInsert(tableName, data, false);
	}
	// Allow overwriting (if a PRIMARY KEY constraint failure is caught, overwrite the corresponding row)
	public void doInsert(String tableName, DBObject[] data, boolean overwrite) {
		DBObject currentDBObject = null;
		try {
			for (DBObject datum: data) {
				currentDBObject = datum;
				Map<String, Object> columns = datum.getAllColumns();

				StringBuilder insertInto = new StringBuilder();
				StringBuilder values = new StringBuilder();
				insertInto.append("(");
				values.append("(");
				int i = 0;
				for (String columnName: columns.keySet()) {
					insertInto.append(columnName);
					Object value = columns.get(columnName);
					if (value.getClass() == String.class) {
						String temp = "\"" + value + "\"";
						values.append(temp);
					}
					else values.append(value);

					if (i < columns.size() - 1) {
						insertInto.append(", ");
						values.append(", ");
					}
					i++;
				}
				insertInto.append(")");
				values.append(")");

				dbAction.executeUpdate("INSERT INTO " + tableName + insertInto.toString() + " VALUES " + values.toString() + ";");
			}

			dbConn.commit();
		}
		catch (SQLException exc) {
			if (overwrite) {
				doUpdate(tableName, currentDBObject);
				return;
			}
			System.out.println("Exception thrown while performing Insert in " + this.getClass().getSimpleName() + ": " + exc.getMessage());
		}
	}

	/* Update */
	void doUpdate(String tableName, DBObject data) {
		try {
			DatabaseMetaData dbMeta = dbConn.getMetaData();
			ResultSet primaryKeys = dbMeta.getPrimaryKeys(null, null, tableName);

			StringBuilder where = new StringBuilder();
			int i = 0;
			while (primaryKeys.next()) {
				if (i > 0) where.append(" AND ");
				i++;

				String primaryKey = primaryKeys.getString(4);
				Object temp = data.getColumn(primaryKey);
				String whereCriterion = primaryKey + "=";
				if (temp.getClass() == String.class) {
					whereCriterion += "\"" + temp + "\"";
				}
				else whereCriterion += temp;

				where.append(whereCriterion);

			}

			Map<String, Object> columns = data.getAllColumns();
			StringBuilder values = new StringBuilder();
			int j = 0;
			for (String columnName: columns.keySet()) {
				Object value = columns.get(columnName);
				String attr = columnName + "=";
				values.append(attr);
				if (value.getClass() == String.class) {
					String temp = "\"" + value + "\"";
					values.append(temp);
				}
				else values.append(value);

				if (j < columns.size() - 1) {
					values.append(", ");
				}
				j++;
			}
			System.out.println(values);
			dbAction.executeUpdate("UPDATE " + tableName + " SET " + values.toString() + " WHERE " + where.toString() + ";");
			dbConn.commit();
		}
		catch (NullPointerException exc) {
			System.out.println("Exception thrown while performing Update in " + this.getClass().getSimpleName() + ": " + exc.getMessage());
		}
		catch (SQLException exc) {
			System.out.println("Exception thrown while performing Update in " + this.getClass().getSimpleName() + ": " + exc.getMessage());
		}
	}

	/* Delete */
	public void doDeleteAll(String tableName) {
		try {
			dbAction.executeUpdate("DELETE FROM " + tableName);
			dbConn.commit();
		}
		catch (SQLException exc) {
			System.out.println("Exception thrown while performing Query in " + this.getClass().getSimpleName() + ": " + exc.getMessage());
		}
	}
	public void doDelete(String tableName, Map<String, Object> where) {
		try {
			StringBuilder whereString = new StringBuilder();
			int j = 0;
			for (String whereAttr: where.keySet()) {
				String condition = whereAttr + "=";
				Object whereValue = where.get(whereAttr);
				if (whereValue.getClass() == String.class) {
					String temp = "\"" + whereValue + "\"";
					condition += temp + " ";
				}
				else condition += whereValue;
				whereString.append(condition);

				if (j < where.size() - 1) whereString.append(" and ");
				j++;
			}

			dbAction.executeUpdate("DELETE FROM " + tableName + " WHERE " + whereString.toString());
			dbConn.commit();
		}
		catch (SQLException exc) {
			System.out.println("Exception thrown while performing Query in " + this.getClass().getSimpleName() + ": " + exc.getMessage());
		}
	}

	/* Select */
	static public ArrayList<DBObject> extractQuery(ResultSet rs) {
		try{
			ArrayList<DBObject> dbObjects = new ArrayList<>();

			while (rs.next()) {
				Map<String, Object> rsData = new HashMap<>();

				ResultSetMetaData rsMeta = rs.getMetaData();
				for (int i = 1; i <= rsMeta.getColumnCount(); i++) {
					rsData.put(rsMeta.getColumnLabel(i), rs.getObject(i));
				}

				DBObject dbObject = new DBObject();
				dbObject.setColumnValues(rsData);
				dbObjects.add(dbObject);
			}

			return dbObjects;
		}
		catch (SQLException exc) {
			System.out.println("Exception thrown while processing ResultSet in " + DBInteraction.class.getSimpleName() + ": " + exc.getMessage());
			return null;
		}
	}
	public ArrayList<DBObject> doQuery(String tableName, String[] attributes) {
		try {
			StringBuilder attrString = new StringBuilder();
			for (int i = 0; i < attributes.length; i++) {
				attrString.append(attributes[i]);

				if (i < attributes.length - 1) attrString.append(", ");
			}

			ResultSet rs = dbAction.executeQuery("SELECT " + attrString.toString() + " FROM " + tableName);

			return extractQuery(rs);
		}
		catch (SQLException exc) {
			System.out.println("Exception thrown while performing Query in " + this.getClass().getSimpleName() + ": " + exc.getMessage());
			return null;
		}
	}
	public ArrayList<DBObject> doQuery(String tableName, Map<String, Object> where) {
		try {
			StringBuilder whereString = new StringBuilder();
			int j = 0;
			for (String whereAttr: where.keySet()) {
				String condition = whereAttr + "=";
				Object whereValue = where.get(whereAttr);
				if (whereValue.getClass() == String.class) {
					String temp = "\"" + whereValue + "\"";
					condition += temp + " ";
				}
				else condition += whereValue;
				whereString.append(condition);

				if (j < where.size() - 1) whereString.append(" and ");
				j++;
			}

			ResultSet rs = dbAction.executeQuery("SELECT * FROM " + tableName + " WHERE " + whereString.toString());

			return extractQuery(rs);
		}
		catch (SQLException exc) {
			System.out.println("Exception thrown while performing Query in " + this.getClass().getSimpleName() + ": " + exc.getMessage());
			return null;
		}
	}
	public ArrayList<DBObject> doQuery(String tableName, String[] attributes, Map<String, Object> where) {
		try {
			StringBuilder attrString = new StringBuilder();
			for (int i = 0; i < attributes.length; i++) {
				attrString.append(attributes[i]);

				if (i < attributes.length - 1) attrString.append(", ");
			}

			StringBuilder whereString = new StringBuilder();
			int j = 0;
			for (String whereAttr: where.keySet()) {
				String condition = whereAttr + "=";
				Object whereValue = where.get(whereAttr);
				if (whereValue.getClass() == String.class) {
					String temp = "\"" + whereValue + "\"";
					condition += temp + " ";
				}
				else condition += whereValue;
				whereString.append(condition);

				if (j < where.size() - 1) whereString.append(" and ");
				j++;
			}

			ResultSet rs = dbAction.executeQuery("SELECT " + attrString.toString() + " FROM " + tableName + " WHERE " + whereString.toString());

			return extractQuery(rs);
		}
		catch (SQLException exc) {
			System.out.println("Exception thrown while performing Query in " + this.getClass().getSimpleName() + ": " + exc.getMessage());
			return null;
		}
	}

	/* Raw SQL */
	public void doRawStatement(String rawSQL) {
		try {
			dbAction.executeUpdate(rawSQL);
			dbConn.commit();
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
	public void doPreparedStatement(String preparedSQL, Vector<Object> questionMarks) throws SQLException {
		PreparedStatement preparedStatement = dbConn.prepareStatement(preparedSQL);

		for (int i = 1; i <= questionMarks.size(); i++) {
			Object value = questionMarks.get(i - 1);

			if (value.getClass() == String.class)
				preparedStatement.setString(i, value.toString());
			else if (value.getClass() == Integer.class)
				preparedStatement.setInt(i, (int)value);
			else if (value.getClass() == Boolean.class)
				preparedStatement.setBoolean(i, (boolean) value);
			else
				preparedStatement.setTimestamp(i, new Timestamp((long) value));
		}

		System.out.println(preparedStatement.toString());
		System.out.println(preparedStatement.executeUpdate());
		preparedStatement.close();
		dbConn.commit();
	}
	public void doBatchPreparedStatement(String[] preparedSQL, Vector<Object>[] questionMarks) throws SQLException {
		for (int j = 0; j < preparedSQL.length; j++) {
			PreparedStatement preparedStatement = dbConn.prepareStatement(preparedSQL[j]);

			for (int i = 1; i <= questionMarks[j].size(); i++) {
				Object value = questionMarks[j].get(i - 1);

				if (value.getClass() == String.class)
					preparedStatement.setString(i, value.toString());
				else if (value.getClass() == Integer.class)
					preparedStatement.setInt(i, (int)value);
				else if (value.getClass() == Boolean.class)
					preparedStatement.setBoolean(i, (boolean) value);
				else
					preparedStatement.setTimestamp(i, new Timestamp((long) value));
			}

			preparedStatement.executeUpdate();
			preparedStatement.close();
		}
		dbConn.commit();
	}
	public ResultSet doPreparedQuery(String preparedSQL, Vector<Object> questionMarks) throws SQLException {
		PreparedStatement preparedStatement = dbConn.prepareStatement(preparedSQL);

		for (int i = 1; i <= questionMarks.size(); i++) {
			Object value = questionMarks.get(i - 1);

			if (value.getClass() == String.class)
				preparedStatement.setString(i, value.toString());
			else if (value.getClass() == Integer.class)
				preparedStatement.setInt(i, (int)value);
			else if (value.getClass() == Boolean.class)
				preparedStatement.setBoolean(i, (boolean) value);
			else
				preparedStatement.setTimestamp(i, new Timestamp((long) value));
		}


		ResultSet resultSet = preparedStatement.executeQuery();
		return resultSet;
	}

	/* Displaying/debugging */
	// Get current connection id
	public void getConnectionId() {
		try {
			System.out.println(dbConn.getClientInfo());
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}
	// Print result in table form
	public static void printResult(ResultSet rs) {
		try {
			// Print column names
			ResultSetMetaData rsMeta = rs.getMetaData();
			ArrayList<String> attrList = new ArrayList<>();
			StringBuilder tableAttr = new StringBuilder();
			StringBuilder divider = new StringBuilder();
			for (int i = 1; i <= rsMeta.getColumnCount(); i++) {
				tableAttr.append(String.format("%-15s | ", rsMeta.getColumnLabel(i)));
				divider.append(String.format("%-15s | ", ' ').replace(' ', '-'));
				attrList.add(rsMeta.getColumnName(i));
			}
			System.out.println(tableAttr.toString());
			System.out.println(divider.toString());

			while (rs.next()) {
				StringBuilder row = new StringBuilder();
				for (String attr: attrList) {
					row.append(String.format("%-15s | ", rs.getObject(attr)));
				}
				System.out.println(row);
			}
		}
		catch (SQLException exc) {
			System.out.println("Exception thrown while accessing ResultSet in " + DBInteraction.class.getSimpleName() + ": " + exc.getMessage());
		}
	}
}
