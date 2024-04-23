package com.psvm.client.controllers;

import com.psvm.client.controllers.objects.SocketTalk;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Map;

public class RemoveGroupMemberRequest extends SocketTalk {
	public RemoveGroupMemberRequest(Socket socket, ObjectInputStream socketIn, ObjectOutputStream socketOut, String currentUsername, String conversationId, String newMemberId) {
		super(socket, socketIn, socketOut, SocketTalk.TALK_CODE_REMOVE_GROUP_MEMBER, Map.of(
				"username", currentUsername,
				"conversationId", conversationId,
				"newMemberId", newMemberId
			)
		);
	}
}
