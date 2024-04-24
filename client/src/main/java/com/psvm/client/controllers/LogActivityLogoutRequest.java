package com.psvm.client.controllers;

import com.psvm.client.controllers.objects.SocketTalk;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Map;

public class LogActivityLogoutRequest extends SocketTalk {
	public LogActivityLogoutRequest(Socket socket, ObjectInputStream socketIn, ObjectOutputStream socketOut, String username) {
		super(socket, socketIn, socketOut, SocketTalk.TALK_CODE_LOG_ACTIVITY_SIGN_OUT, Map.of(
				"username", username
		));
	}
}
