package com.psvm.server.controllers;

import com.psvm.shared.socket.SocketRequest;
import com.psvm.shared.socket.SocketResponse;

import java.io.*;
import java.net.Socket;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.*;

public class ClientHandler implements Runnable {
	private Socket clientSocket;
	private ObjectInputStream handlerIn;
	private ObjectOutputStream handlerOut;
	private ServerLaunch chatServer;
	private String roomID;

	private final DBWrapper db;

	private Integer clientID = 0;

	private static Integer clientCount = 0;

	public ClientHandler(Socket clientSocket, ServerLaunch chatServer) {
		this.clientSocket = clientSocket;
		this.chatServer = chatServer;
		this.db = new DBWrapper();
		this.clientID = clientCount ++;

		try {
			handlerOut = new ObjectOutputStream(clientSocket.getOutputStream());
			handlerIn = new ObjectInputStream(clientSocket.getInputStream());
		} catch (IOException e) {
			System.out.println("Exception thrown while handling client in " + this.getClass().getSimpleName() + " : " + e.getMessage());
		}
	}

	@Override
	public void run() {
		try {
			while (true) {
				SocketRequest request = (SocketRequest) handlerIn.readObject();

				switch (request.getTalkCode()) {
					case "4c": {
						Map<String, Object> data = request.getData();
						try {
							db.respondFriendRequest(data.get("username").toString(), data.get("senderId").toString());
							handlerOut.writeObject(new SocketResponse(SocketResponse.RESPONSE_CODE_SUCCESS, null));
						} catch (SQLException e) {
							System.out.println(e.getMessage());
							handlerOut.writeObject(new SocketResponse(SocketResponse.RESPONSE_CODE_FAILURE, null));
						}

						break;
					}
					case "4d": {
						Map<String, Object> data = request.getData();
						try {
							db.removeFriend(data.get("username").toString(), data.get("friendId").toString());
							handlerOut.writeObject(new SocketResponse(SocketResponse.RESPONSE_CODE_SUCCESS, null));
						} catch (SQLException e) {
							System.out.println(e.getMessage());
							handlerOut.writeObject(new SocketResponse(SocketResponse.RESPONSE_CODE_FAILURE, null));
						}

						break;
					}
					case "5": {
						Map<String, Object> data = request.getData();
						try {
							ResultSet queryResult = db.getFriendList(data.get("username").toString());
							ResultSetMetaData queryResultMeta;
							queryResultMeta = queryResult.getMetaData();

							Vector<Map<String, Object>> responseData = new Vector<>();
							while (queryResult.next()) {
								for (int i = 1; i <= queryResultMeta.getColumnCount(); i++) {
									responseData.add(Map.of(queryResultMeta.getColumnLabel(i), queryResult.getObject(i)));
								}
							}

							queryResult.getStatement().close();
							handlerOut.writeObject(new SocketResponse(SocketResponse.RESPONSE_CODE_SUCCESS, responseData));
						} catch (SQLException e) {
							System.out.println(e.getMessage());
							handlerOut.writeObject(new SocketResponse(SocketResponse.RESPONSE_CODE_FAILURE, null));
						}

						break;
					}
				}

			}
		} catch (IOException | ClassNotFoundException e) {
			System.out.println("Exception thrown while processing client's request in " + this.getClass().getSimpleName() + " : " + e.getMessage());
		} finally {
			chatServer.removeClient(this);
			db.close();
		}
	}
//	public void sendMessage(String message, String roomID) { // Broacast
//		try {
//			if (this.roomID.equals(roomID))
//				objectOutputStream.writeObject(new Message(MessageType.TEXT, "Client" + clientID + " Send: " + message, roomID));
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//	}
//
//	public void sendFile(byte[] fileData, String fileName) { // Broacast
//		try {
//			objectOutputStream.writeObject(new Message(MessageType.FILE, fileData, fileName));
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//	}
}