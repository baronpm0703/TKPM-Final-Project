package com.psvm.client.views.components;

import com.psvm.client.controllers.FriendRemoveRequest;
import com.psvm.client.controllers.FriendRequestResponseRequest;
import com.psvm.shared.socket.SocketResponse;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

class TestRemoveButtonThread extends Thread {
	private Socket clientSocket;
	ObjectInputStream socketIn;
	ObjectOutputStream socketOut;

	private int responseCode;

	public TestRemoveButtonThread(Socket clientSocket, ObjectInputStream socketIn, ObjectOutputStream socketOut) {
		this.clientSocket = clientSocket;
		this.socketIn = socketIn;
		this.socketOut = socketOut;
	}

	@Override
	public void run() {
		super.run();

		FriendRemoveRequest request = new FriendRemoveRequest(clientSocket, socketIn, socketOut, "Highman", "Kizark");
		SocketResponse response = request.talk();

		responseCode = response.getResponseCode();
	}

	public int getResponseCode() {
		return responseCode;
	}
}

public class TestRemoveButton extends JButton {

	final String SOCKET_HOST = "localhost";
	final int SOCKET_PORT = 5555;
	Socket socket;
	ObjectInputStream socketIn;
	ObjectOutputStream socketOut;

	public TestRemoveButton() {
		try {
			setText("Remove");

			socket = new Socket(SOCKET_HOST, SOCKET_PORT);
			socketIn = new ObjectInputStream(socket.getInputStream());
			socketOut = new ObjectOutputStream(socket.getOutputStream());
			monitorClick();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	void monitorClick() {
		addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				TestRemoveButtonThread buttonThread = new TestRemoveButtonThread(socket, socketIn, socketOut);
				buttonThread.start();
				try {
					buttonThread.join();
				} catch (InterruptedException ex) {
					throw new RuntimeException(ex);
				}
				finally {
					int responseCode = buttonThread.getResponseCode();
				}
			}
		});
	}
}
