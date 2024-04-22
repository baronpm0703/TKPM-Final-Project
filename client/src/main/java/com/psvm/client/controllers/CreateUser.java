package com.psvm.client.controllers;

import com.psvm.client.controllers.objects.SocketTalk;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.time.LocalDateTime;
import java.util.Map;

public class CreateUser extends SocketTalk {
	public CreateUser(Socket socket, ObjectInputStream socketIn, ObjectOutputStream socketOut, String username, String fName, String lName, String password, String address, LocalDateTime dob, boolean isMale, String email) {
		super(socket, socketIn, socketOut, SocketTalk.TALK_CODE_CREATE_USER, Map.of(
				"username", username,
				"firstName", fName,
				"lastName", lName,
				"password", password,
				"address", address,
				"dob", dob,
				"gender", isMale,
				"email", email
		));
	}
}
