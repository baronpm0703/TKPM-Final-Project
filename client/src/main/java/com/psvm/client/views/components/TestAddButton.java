package com.psvm.client.views.components;

import com.psvm.client.controllers.FriendListRequest;
import com.psvm.client.controllers.FriendRequestResponseRequest;
import com.psvm.shared.socket.SocketResponse;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.List;
import java.util.Random;
import java.util.Vector;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

class TestAddButtonThread extends Thread {
	private Socket clientSocket;
	ObjectInputStream socketIn;
	ObjectOutputStream socketOut;

	private int responseCode;

	public TestAddButtonThread(Socket clientSocket, ObjectInputStream socketIn, ObjectOutputStream socketOut) {
		this.clientSocket = clientSocket;
		this.socketIn = socketIn;
		this.socketOut = socketOut;
	}

	@Override
	public void run() {
		super.run();

		FriendRequestResponseRequest request = new FriendRequestResponseRequest(clientSocket, socketIn, socketOut, "Highman", "Kizark");
		SocketResponse response = request.talk();

		responseCode = response.getResponseCode();
	}

	public int getResponseCode() {
		return responseCode;
	}
}

public class TestAddButton extends JButton {

	final String SOCKET_HOST = "localhost";
	final int SOCKET_PORT = 5555;
	Socket socket;
	ObjectInputStream socketIn;
	ObjectOutputStream socketOut;

	public TestAddButton() {
		try {
			setText("Add");

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
				TestAddButtonThread buttonThread = new TestAddButtonThread(socket, socketIn, socketOut);
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
