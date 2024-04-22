package com.psvm.client.controllers;

import com.psvm.client.controllers.objects.SocketTalk;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Map;

public class FriendListRequest extends SocketTalk {
	public FriendListRequest(Socket socket, ObjectInputStream socketIn, ObjectOutputStream socketOut, String currentUser) {
		super(socket, socketIn, socketOut, SocketTalk.TALK_CODE_FRIEND_LIST, Map.of(
				"username", currentUser
		));
	}
}
