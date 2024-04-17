package com.psvm.client.settings;

public enum LocalData {
	INSTANCE;

	private String currentUsername;	// Remove pre-set value when login register is implemented

	LocalData() {
		this.currentUsername = "Highman";
	}

	public static void setCurrentUsername(String username) {
		INSTANCE.currentUsername = username;
	}
	public static String getCurrentUsername() {
		return INSTANCE.currentUsername;
	}
}
