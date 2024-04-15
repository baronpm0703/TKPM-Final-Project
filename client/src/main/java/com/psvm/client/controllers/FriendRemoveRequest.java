package com.psvm.client.controllers;

import com.psvm.client.controllers.objects.SocketTalk;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Map;

public class FriendRemoveRequest extends SocketTalk {

	public FriendRemoveRequest(Socket socket, ObjectInputStream socketIn, ObjectOutputStream socketOut, String currentUser, String friendId) {
		super(socket, socketIn, socketOut, SocketTalk.TALK_CODE_FRIEND_REMOVE, Map.of(
				"username", currentUser,
				"friendId", friendId
		));
	}
}
