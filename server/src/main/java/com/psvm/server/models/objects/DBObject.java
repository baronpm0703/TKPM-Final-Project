package com.psvm.server.models.objects;

import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;

public class DBObject {
	protected Map<String, Object> fields;

	public DBObject() {
		fields = new HashMap<>();
	}

	public void setColumnValues(Map<String, Object> data) {
		fields = data;
	}

	public void castFrom(DBObject object) {}

	public Object getColumn(String fieldName) {
		return fields.get(fieldName);
	}
	public Map<String, Object> getAllColumns() {
		return fields;
	}
}
