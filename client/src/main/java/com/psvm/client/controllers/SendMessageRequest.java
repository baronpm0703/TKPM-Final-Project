package com.psvm.client.controllers;

import com.psvm.client.controllers.objects.SocketTalk;
import com.psvm.client.settings.LocalData;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Map;

public class SendMessageRequest extends SocketTalk {
	public SendMessageRequest(Socket socket, ObjectInputStream socketIn, ObjectOutputStream socketOut, String conversationId, String content) {
		super(socket, socketIn, socketOut, SocketTalk.TALK_CODE_SEND_MESSAGE, Map.of(
				"username", LocalData.getCurrentUsername(),
				"conversationId", conversationId,
				"content", content
		));
	}
}
