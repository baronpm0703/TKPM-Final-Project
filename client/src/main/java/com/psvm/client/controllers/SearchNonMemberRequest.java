package com.psvm.client.controllers;

import com.psvm.client.controllers.objects.SocketTalk;
import com.psvm.client.settings.LocalData;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Map;

public class SearchNonMemberRequest extends SocketTalk {
	public SearchNonMemberRequest(Socket socket, ObjectInputStream socketIn, ObjectOutputStream socketOut, String searchUsername, String conversationId) {
		super(socket, socketIn, socketOut, SocketTalk.TALK_CODE_NON_GROUP_MEMBER_LIST, Map.of(
				"searchUsername", searchUsername,
				"conversationId", conversationId
			)
		);
	}
}
