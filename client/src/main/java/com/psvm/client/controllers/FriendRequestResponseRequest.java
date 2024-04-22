package com.psvm.client.controllers;

import com.psvm.client.controllers.objects.SocketTalk;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Map;

public class FriendRequestResponseRequest extends SocketTalk {
	final static int FRIEND_REQUEST_ACCEPT = 1;

	public FriendRequestResponseRequest(Socket socket, ObjectInputStream socketIn, ObjectOutputStream socketOut, String currentUser, String senderId) {
		super(socket, socketIn, socketOut, SocketTalk.TALK_CODE_PROCESS_FRIEND_REQUEST, Map.of(
				"responseType", FRIEND_REQUEST_ACCEPT,
				"username", currentUser,
				"senderId", senderId
		));
	}
}
