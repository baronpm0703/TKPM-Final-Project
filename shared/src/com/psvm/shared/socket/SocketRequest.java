package com.psvm.shared.socket;

import java.io.Serializable;
import java.util.Map;

public class SocketRequest implements Serializable {
	String currentTalkCode;
	Map<String, Object> data;

	public SocketRequest(String talkCode, Map<String, Object> data) {
		this.currentTalkCode = talkCode;
		this.data = data;
	}

	public String getTalkCode() {return currentTalkCode;}

	public Map<String, Object> getData() {return data;}
}
