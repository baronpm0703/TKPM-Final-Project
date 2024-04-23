package com.psvm.client.controllers;

import com.psvm.client.controllers.objects.SocketTalk;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Map;

public class GetUserRequest extends SocketTalk {
	public GetUserRequest(Socket socket, ObjectInputStream socketIn, ObjectOutputStream socketOut, String username, String hashedPassword) {
		super(socket, socketIn, socketOut, SocketTalk.TALK_CODE_GET_USER, Map.of(
				"username", username,
				"hashedPassword", hashedPassword
		));
	}
}
