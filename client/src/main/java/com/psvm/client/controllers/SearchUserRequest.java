package com.psvm.client.controllers;

import com.psvm.client.controllers.objects.SocketTalk;
import com.psvm.client.settings.LocalData;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Map;

public class SearchUserRequest extends SocketTalk {
	public SearchUserRequest(Socket socket, ObjectInputStream socketIn, ObjectOutputStream socketOut, String otherUsername) {
		super(socket, socketIn, socketOut, SocketTalk.TALK_CODE_SEARCH_USER, Map.of(
				"username", LocalData.getCurrentUsername(),
				"otherUsername", otherUsername
			)
		);
	}
}
