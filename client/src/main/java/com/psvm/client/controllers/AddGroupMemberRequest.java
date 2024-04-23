package com.psvm.client.controllers;

import com.psvm.client.controllers.objects.SocketTalk;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Map;

public class AddGroupMemberRequest extends SocketTalk {
	public AddGroupMemberRequest(Socket socket, ObjectInputStream socketIn, ObjectOutputStream socketOut, String conversationId, String newMemberId) {
		super(socket, socketIn, socketOut, SocketTalk.TALK_CODE_ADD_GROUP_MEMBER, Map.of(
				"conversationId", conversationId,
				"newMemberId", newMemberId
			)
		);
	}
}
