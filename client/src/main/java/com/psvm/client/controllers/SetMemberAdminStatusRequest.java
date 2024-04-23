package com.psvm.client.controllers;

import com.psvm.client.controllers.objects.SocketTalk;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Map;

public class SetMemberAdminStatusRequest extends SocketTalk {
	public SetMemberAdminStatusRequest(Socket socket, ObjectInputStream socketIn, ObjectOutputStream socketOut, String currentUsername, String conversationId, String memberId, boolean isAdmin) {
		super(socket, socketIn, socketOut, SocketTalk.TALK_CODE_SET_MEMBER_ADMIN_STATUS, Map.of(
				"username", currentUsername,
				"conversationId", conversationId,
				"memberId", memberId,
				"isAdmin", isAdmin
		));
	}
}
