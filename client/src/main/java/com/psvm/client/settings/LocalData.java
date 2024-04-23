package com.psvm.client.settings;

public enum LocalData {
	INSTANCE;

	private String currentUsername;	// Remove pre-set value when login register is implemented
	private String selectedConversation;
	private String selectedSearchOption;
	private String conversationScrollSearch;

	LocalData() {
		this.currentUsername = "";
		this.selectedConversation = "";
		this.selectedSearchOption = "friend";
		this.conversationScrollSearch = "";
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

	public static void setSelectedSearchOption(String selectedSearchOption) {
		INSTANCE.selectedSearchOption = selectedSearchOption;
	}
	public static String getSelectedSearchOption() {
		return INSTANCE.selectedSearchOption;
	}

	public static void setConversationScrollSearch(String conversationScrollSearch) {
		INSTANCE.conversationScrollSearch = conversationScrollSearch;
	}
	public static String getConversationScrollSearch() {
		return INSTANCE.conversationScrollSearch;
	}
}
