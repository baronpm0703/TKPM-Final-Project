package com.psvm.client.controllers;

import com.psvm.client.controllers.objects.SocketTalk;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Map;

public class ResetPasswordRequest extends SocketTalk {
	public ResetPasswordRequest(Socket socket, ObjectInputStream socketIn, ObjectOutputStream socketOut, String username, String email, String hashedPassword) {
		super(socket, socketIn, socketOut, SocketTalk.TALK_CODE_RESET_PASSWORD, Map.of(
				"username", username,
				"email", email,
				"hashedPassword", hashedPassword
		));
	}
}
