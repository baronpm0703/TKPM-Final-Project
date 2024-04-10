package com.psvm.server.models.objects;

import java.lang.reflect.Field;
import java.util.Map;

public class DBConversationMember extends DBObject {
	String conversationId;
	String memberId;
	boolean isAdmin;

	public void setColumnValues(String id, String memberId, boolean status) {
		this.conversationId = id;
		this.memberId = memberId;
		this.isAdmin = status;

		fields.put("ConversationId", id);
		fields.put("MemberId", memberId);
		fields.put("IsAdmin", (isAdmin ? 1 : 0));
	}

	@Override
	public void castFrom(DBObject object) {
		if (this.fields == null) this.fields = object.fields;

		this.conversationId = (String) fields.get("ConversationId");
		this.memberId = (String) fields.get("MemberId");
		if (fields.get("MemberId") != null) this.isAdmin = (boolean) fields.get("MemberId");
	}

	public String getId() {
		return conversationId;
	}
	public String getMemberId() {
		return memberId;
	}
	public boolean isAdmin() {
		return isAdmin;
	}
}
