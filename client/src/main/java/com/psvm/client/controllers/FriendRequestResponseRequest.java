package com.psvm.client.controllers;

import com.psvm.client.controllers.objects.SocketTalk;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Map;

public class FriendRequestResponseRequest extends SocketTalk {
	public final static int FRIEND_REQUEST_REFUSE = 0;
	public final static int FRIEND_REQUEST_ACCEPT = 1;

	public FriendRequestResponseRequest(Socket socket, ObjectInputStream socketIn, ObjectOutputStream socketOut, int responseType, String currentUser, String senderId) {
		super(socket, socketIn, socketOut, SocketTalk.TALK_CODE_PROCESS_FRIEND_REQUEST, Map.of(
				"responseType", responseType,
				"username", currentUser,
				"senderId", senderId
		));
	}
}
