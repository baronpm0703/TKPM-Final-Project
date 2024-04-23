package com.psvm.client.settings;

public enum LocalData {
	INSTANCE;

	private String currentUsername;	// Remove pre-set value when login register is implemented
	private String selectedConversation;
	private String selectedFriend;

	LocalData() {
		this.currentUsername = "";
		this.selectedConversation = "";
		this.selectedFriend = "";
	}

	public static void setCurrentUsername(String username) {
		INSTANCE.currentUsername = username;
	}
	public static String getCurrentUsername() {
		return INSTANCE.currentUsername;
	}

	public static void setSelectedConversation(String selectedConversation) {
		INSTANCE.selectedConversation = selectedConversation;
	}
	public static String getSelectedConversation() {
		return INSTANCE.selectedConversation;
	}

	public static void setSelectedFriend(String selectedConversation) {
		INSTANCE.selectedFriend = selectedConversation;
	}
	public static String getSelectedFriend() {
		return INSTANCE.selectedFriend;
	}
}
