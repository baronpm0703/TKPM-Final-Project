package com.psvm.client.controllers;

import com.psvm.client.controllers.objects.SocketTalk;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Map;

public class LeaveGroupRequest extends SocketTalk {
	public LeaveGroupRequest(Socket socket, ObjectInputStream socketIn, ObjectOutputStream socketOut, String username, String conversationId) {
		super(socket, socketIn, socketOut, SocketTalk.TALK_CODE_LEAVE_GROUP, Map.of(
				"username", username,
				"conversationId", conversationId
		));
	}
}
