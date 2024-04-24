package com.psvm.client.controllers;

import com.psvm.client.controllers.objects.SocketTalk;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Map;

public class GetUserInfoRequest extends SocketTalk {
	public GetUserInfoRequest(Socket socket, ObjectInputStream socketIn, ObjectOutputStream socketOut, String username) {
		super(socket, socketIn, socketOut, SocketTalk.TALK_CODE_GET_USER_INFO, Map.of(
				"username", username
		));
	}
}
