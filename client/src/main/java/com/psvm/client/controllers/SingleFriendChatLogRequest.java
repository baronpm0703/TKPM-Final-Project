package com.psvm.client.controllers;

import com.psvm.client.controllers.objects.SocketTalk;
import com.psvm.client.settings.LocalData;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Map;

public class SingleFriendChatLogRequest extends SocketTalk {
	public SingleFriendChatLogRequest(Socket socket, ObjectInputStream socketIn, ObjectOutputStream socketOut, String conversationId, String memberId) {
		super(socket, socketIn, socketOut, SocketTalk.TALK_CODE_SINGLE_FRIEND_CHAT_LOG, Map.of(
				"username", LocalData.getCurrentUsername(),
				"conversationId", conversationId,
				"memberId", memberId
			)
		);
	}
}
