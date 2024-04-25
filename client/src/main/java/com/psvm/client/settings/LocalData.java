package com.psvm.client.settings;

public enum LocalData {
	INSTANCE;

	private String currentUsername;	// Remove pre-set value when login register is implemented
	private String selectedConversation;
	private String selectedSearchOption;
	private String conversationScrollSearch;
	private boolean toReloadMessageList;
	private boolean toReloadChat;
	private boolean toRemoveChat;
	private boolean toRemoveChatDetail;

	LocalData() {
		this.currentUsername = "";
		this.selectedConversation = "";
		this.selectedSearchOption = "friend";
		this.conversationScrollSearch = "";

		this.toReloadMessageList = false;
		this.toReloadChat = false;
		this.toRemoveChat = false;
		this.toRemoveChatDetail = false;
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

	public static void setToReloadMessageList(boolean bool) {
		INSTANCE.toReloadMessageList = bool;
	}
	public static boolean getToReloadMessageList() { return INSTANCE.toReloadMessageList; }

	public static void setToReloadChat(boolean bool) {
		INSTANCE.toReloadChat = bool;
	}
	public static boolean getToReloadChat() { return INSTANCE.toReloadChat; }

	public static void setToRemoveChat(boolean bool) {
		INSTANCE.toRemoveChat = bool;
	}
	public static boolean getToRemoveChat() { return INSTANCE.toRemoveChat; }

	public static void setToRemoveChatDetail(boolean bool) {
		INSTANCE.toRemoveChatDetail = bool;
	}
	public static boolean getToRemoveChatDetail() { return INSTANCE.toRemoveChatDetail; }
}
