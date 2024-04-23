package com.psvm.client.controllers;

import com.psvm.client.controllers.objects.SocketTalk;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Map;

public class GetGroupInfoRequest extends SocketTalk {
	public GetGroupInfoRequest(Socket socket, ObjectInputStream socketIn, ObjectOutputStream socketOut, String conversationId) {
		super(socket, socketIn, socketOut, SocketTalk.TALK_CODE_GET_GROUP_INFO, Map.of(
				"conversationId", conversationId
		));
	}
}
