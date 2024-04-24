package com.psvm.client.controllers.objects;

import com.psvm.shared.socket.SocketRequest;
import com.psvm.shared.socket.SocketResponse;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Map;

public class SocketTalk {
	public final static String TALK_CODE_CREATE_USER = "1";
	public final static String TALK_CODE_GET_USER = "3";
	public final static String TALK_CODE_FRIEND_MESSAGE_LIST = "4f";
	public final static String TALK_CODE_PROCESS_FRIEND_REQUEST = "4c";
	public final static String TALK_CODE_FRIEND_REMOVE = "4d";
	public final static String TALK_CODE_FRIEND_LIST = "5";

	public final static String READ_CODE_USER_DATA = "read_user";
	int talkId;
	Socket socket;
	ObjectInputStream socketIn;
	ObjectOutputStream socketOut;
	SocketRequest request;
	SocketResponse response;

	public SocketTalk(Socket clientSocket, ObjectInputStream socketIn, ObjectOutputStream socketOut, String talkCode, Map<String, Object> data) {
		socket = clientSocket;
		this.socketIn = socketIn;
		this.socketOut = socketOut;

		request = new SocketRequest(talkCode, data);
	}

	// Send request then receive a response
	public SocketResponse talk() {
		try {
			socketOut.writeObject(request);

			SocketResponse response = (SocketResponse) socketIn.readObject();
			return response;
		} catch (IOException | ClassNotFoundException e) {
			System.out.println("Error while initializing sending socket request in " + this.getClass().getSimpleName() + " : " + e.getMessage());
			return null;
		}
	}
}
