package com.psvm.shared.socket;

import java.io.Serializable;
import java.util.Map;
import java.util.Vector;

public class SocketResponse implements Serializable {
	public final static int RESPONSE_CODE_FAILURE = 0;
	public final static int RESPONSE_CODE_SUCCESS = 1;
	int responseCode;
	Vector<Map<String, Object>> data;

	public SocketResponse(int responseCode, Vector<Map<String, Object>> data) {
		this.responseCode = responseCode;
		this.data = data;
	}

	public int getResponseCode() {
		return responseCode;
	}

	public Vector<Map<String, Object>> getData() {
		return data;
	}
}
