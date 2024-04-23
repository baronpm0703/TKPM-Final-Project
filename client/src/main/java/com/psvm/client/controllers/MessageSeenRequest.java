package com.psvm.client.controllers;

import com.psvm.client.controllers.objects.SocketTalk;
import com.psvm.client.settings.LocalData;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Map;

public class MessageSeenRequest extends SocketTalk {
	public MessageSeenRequest(Socket socket, ObjectInputStream socketIn, ObjectOutputStream socketOut, String conversationId) {
		super(socket, socketIn, socketOut, SocketTalk.TALK_CODE_MESSAGE_SEEN, Map.of(
				"username", LocalData.getCurrentUsername(),
				"conversationId", conversationId
		));
	}
}
