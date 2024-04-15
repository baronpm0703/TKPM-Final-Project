package com.psvm.server.models.objects;

import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

class Joining {
	String joinType;
	String newTable;	// New table to join
	String newTableAlias;
	String joiningTableAlias;	// Table alias to join with
	Vector<String[]> joinConditions;
	Joining(String joinType, String table1, String table1Alias, String table2Alias, Vector<String[]> joinConditions) {
		this.joinType = joinType;
		this.newTable = table1;
		this.newTableAlias = table1Alias;
		this.joiningTableAlias = table2Alias;
		this.joinConditions = joinConditions;
	}

	public String getSQL() {
		StringBuilder sql = new StringBuilder();

		String joinStatement = joinType + " " + newTable + " " + newTableAlias + " ";
		sql.append(joinStatement);

		sql.append(" ON ");

		StringBuilder joinCondition = new StringBuilder();
		for (int i = 0; i < joinConditions.size(); i++) {
			String currentCondition = newTableAlias + "." + joinConditions.get(i)[0] + joinConditions.get(i)[1] + joiningTableAlias + "." + joinConditions.get(i)[2];
			sql.append(currentCondition);

			if (i < joinConditions.size() - 1) joinCondition.append(" AND ");
		}
		sql.append(joinCondition);

		return sql.toString();
	}
}

public class DBJointQuery {
	public static final String JOIN = "JOIN";
	public static final String LEFT_JOIN = "LEFT JOIN";
	public static final String RIGHT_JOIN = "RIGHT JOIN";
	public static final String INNER_JOIN = "INNER JOIN";
	public static final String OUTER_JOIN = "OUTER JOIN";

	Vector<String> tables;
	Vector<String> tableAliases;
	Vector<Joining> joinClauses;
	Map<String, Map<String, Object>> conditions;
	Map<String, String[]> selections;

	DBJointQuery(String table, String tableAlias) {
		tables = new Vector<>();
		tableAliases = new Vector<>();
		tables.add(table);
		tableAliases.add(tableAlias);
	}

	public void addTable(String table, String tableAlias) {
		if (tables.contains(table) || tableAliases.contains(tableAlias)) return;

		tables.add(table);
		tableAliases.add(tableAlias);
	}

	// Each join condition: index 0: table1's attribute, index 1: comparator, index 2: table2's attribute
	public void joinTables(String joinType, String newTable, String joiningTable, Vector<String[]> joinConditions) {
		try {
			if (joinClauses == null)
				joinClauses = new Vector<>();

			Joining joinClause = new Joining(joinType, newTable, tableAliases.get(tableAliases.indexOf(newTable)), tableAliases.get(tableAliases.indexOf(joiningTable)), joinConditions);
			joinClauses.add(joinClause);
		}
		catch (ArrayIndexOutOfBoundsException exc) {
			System.out.println("Table doesn't exist or hasn't been added. Error: " + exc.getMessage());
		}
	}

	public void addConditions(String table, Map<String, Object> where) {
		if (conditions == null)
			conditions = new HashMap<>();

		conditions.put(table, where);
	}

	public void addSelection(String table, String[] attributes) {
		if (selections == null)
			selections = new HashMap<>();

		selections.put(table, attributes);
	}

	public String getSQL() {
		try {
			StringBuilder sql = new StringBuilder();

			// SELECT
			if (selections == null)
				sql.append("SELECT *");
			else {
				sql.append("SELECT ");
				int j = 0;
				for (String table: selections.keySet()) {
					String tableAlias = tableAliases.get(tables.indexOf(table));
					String[] selectedAttributes = selections.get(table);

					StringBuilder currentSelection = new StringBuilder();
					for (int i = 0; i < selectedAttributes.length; i++) {
						String select = tableAlias + "." + selectedAttributes[i];
						currentSelection.append(select);

						if (i < selectedAttributes.length - 1) currentSelection.append(",");
					}
					sql.append(currentSelection);

					if (j < selections.size() - 1) sql.append(",");
					j++;
				}
			}
			sql.append(" ");

			// FROM
			String from = "FROM " + tables.get(0) + " " + tableAliases.get(0);
			sql.append(from);
			sql.append(" ");

			// JOIN
			for (Joining joining: joinClauses) {
				sql.append(joining.getSQL());
				sql.append(" ");
			}

			// WHERE
			if (conditions != null) {
				sql.append("WHERE ");
				int l = 0;
				for (String table: conditions.keySet()) {
					StringBuilder currentConditionClause = new StringBuilder();
					String tableAlias = tableAliases.get(tables.indexOf(table));
					Map<String, Object> currentConditions = conditions.get(table);

					int k = 0;
					for (String conditionAttribute: currentConditions.keySet()) {
						Object conditionValue = currentConditions.get(conditionAttribute);
						String temp;
						if (currentConditions.get(conditionAttribute) == String.class) {
							temp = tableAlias + "." + conditionAttribute + "=\"" + conditionValue + "\"";

						}
						else {
							temp = tableAlias + "." + conditionAttribute + "=" + conditionValue;
						}
						currentConditionClause.append(temp);

						if (k < currentConditions.size() - 1) currentConditionClause.append(",");
					}
					sql.append(currentConditionClause);

					if (l < conditions.size() - 1) sql.append(",");
				}
			}

			return sql.toString();
		}
		catch (ArrayIndexOutOfBoundsException exc) {
			System.out.println("Error while creating joint query in " + this.getClass().getSimpleName() + ": " + exc.getMessage());
			return null;
		}
	}
}
