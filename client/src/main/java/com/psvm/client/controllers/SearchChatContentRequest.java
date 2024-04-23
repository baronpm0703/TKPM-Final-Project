package com.psvm.client.controllers;

import com.psvm.client.controllers.objects.SocketTalk;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Map;

public class SearchChatContentRequest extends SocketTalk {
	public SearchChatContentRequest(Socket socket, ObjectInputStream socketIn, ObjectOutputStream socketOut, String conversationId) {
		super(socket, socketIn, socketOut, SocketTalk.TALK_CODE_DELETE_CHAT_HISTORY, Map.of(
				"conversationId", conversationId
		));
	}
}
