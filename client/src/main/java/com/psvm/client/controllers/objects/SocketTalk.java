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
	public final static String TALK_CODE_RESET_PASSWORD = "2";
	public final static String TALK_CODE_GET_USER = "3";
	public final static String TALK_CODE_LOG_ACTIVITY_SIGN_IN = "3_1";
	public final static String TALK_CODE_LOG_ACTIVITY_SIGN_OUT = "3_2";
	public final static String TALK_CODE_SEARCH_USER = "4a_1";
	public final static String TALK_CODE_SEND_FRIEND_REQUEST = "4a_2";
	public final static String TALK_CODE_GET_FRIEND_REQUEST = "4b";
	public final static String TALK_CODE_PROCESS_FRIEND_REQUEST = "4c";
	public final static String TALK_CODE_FRIEND_REMOVE = "4d";
	public final static String TALK_CODE_GET_USER_INFO = "4e";
	public final static String TALK_CODE_FRIEND_MESSAGE_LIST = "4f";
	public final static String TALK_CODE_FRIEND_LIST = "5";
	public final static String TALK_CODE_SINGLE_FRIEND_CHAT_LOG = "9d";
	public final static String TALK_CODE_MESSAGE_SEEN = "9h";
	public final static String TALK_CODE_SEND_MESSAGE = "11";

	public final static String BLOCK_CODE_BLOCK_USER = "bu";
	public final static String BLOCK_CODE_UNBLOCK_USER = "ub";

	public final static String CONV_CODE_GET_MEM_INFO = "conMemInfo";
	public final static String CONV_CODE_CREATE_NEW_CONV = "newConv";
	public final static String CONV_CODE_GET_HIGHEST_ID = "highestId";

	public final static String CONV_CODE_ADD_MEM_WHEN_U_ARE_ADMIN = "convAddMemWhenUareAdmin";


	public final static String CONV_CODE_UPDATE_CONV_LOG = "updateConvLog";


	public final static String FRIEND_CODE_UNFRIEND = "uf";

	public final static String REPORT_CODE_REPORT_USER = "ru";

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
