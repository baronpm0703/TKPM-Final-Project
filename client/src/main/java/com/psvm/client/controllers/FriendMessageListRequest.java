package com.psvm.client.controllers;

import com.psvm.client.controllers.objects.SocketTalk;
import com.psvm.client.settings.LocalData;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Map;

public class FriendMessageListRequest extends SocketTalk {
	public FriendMessageListRequest(Socket socket, ObjectInputStream socketIn, ObjectOutputStream socketOut) {
		super(socket, socketIn, socketOut, SocketTalk.TALK_CODE_FRIEND_MESSAGE_LIST, Map.of(
				"username", LocalData.getCurrentUsername())
		);
	}
}
