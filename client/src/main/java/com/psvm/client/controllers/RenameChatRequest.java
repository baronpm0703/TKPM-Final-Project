package com.psvm.client.controllers;

import com.psvm.client.controllers.objects.SocketTalk;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Map;

public class RenameChatRequest extends SocketTalk {
	public RenameChatRequest(Socket socket, ObjectInputStream socketIn, ObjectOutputStream socketOut, String conversationId, String conversationName) {
		super(socket, socketIn, socketOut, SocketTalk.TALK_CODE_RENAME_CHAT, Map.of(
				"conversationId", conversationId,
				"conversationName", conversationName
		));
	}
}
