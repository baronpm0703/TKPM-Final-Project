package com.psvm.server.controllers;

import com.psvm.client.settings.LocalData;
import com.psvm.shared.socket.SocketRequest;
import com.psvm.shared.socket.SocketResponse;

import java.io.*;
import java.net.Socket;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.time.LocalDateTime;
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
					case "1": {
						talkCode_CreateUser(request.getData());

						break;
					}
					case "3": {
						talkCode_GetUser(request.getData());
						break;
					}
					case "4a_1": {
						talkCode_SearchUser(request.getData());
						break;
					}
					case "4a_2": {
						talkCode_SendFriendRequest(request.getData());
						break;
					}
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
					case "4f": {
						talkCode_FriendMessageList(request.getData());
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
					case "9d": {
						talkCode_SingleFriendChatLog(request.getData());
						break;
					}
					case "9h": {
						talkCode_MessageSeen(request.getData());

						break;
					}
					case "11": {
						talkCode_SendMessage(request.getData());
						break;
					}
					case "read_user": {
						Map<String, Object> data = request.getData();
						try {
							ResultSet queryResult = db.getFieldUserList(data.get("field").toString());
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

	void talkCode_CreateUser(Map<String, Object> data) throws IOException {
		try {
			db.createUser(data.get("username").toString(), data.get("firstName").toString(), data.get("lastName").toString(), data.get("password").toString(), data.get("address").toString(), (LocalDateTime) data.get("dob"), (boolean) data.get("gender"), data.get("email").toString());
			handlerOut.writeObject(new SocketResponse(SocketResponse.RESPONSE_CODE_SUCCESS, null));
		} catch (SQLException e) {
			System.out.println(e.getMessage());
			handlerOut.writeObject(new SocketResponse(SocketResponse.RESPONSE_CODE_FAILURE, null));
		}
	}

	void talkCode_ForgetPassword(Map<String, Object> data) throws IOException {
		try {
			db.createUser(data.get("username").toString(), data.get("firstName").toString(), data.get("lastName").toString(), data.get("password").toString(), data.get("address").toString(), (LocalDateTime) data.get("dob"), (boolean) data.get("gender"), data.get("email").toString());
			handlerOut.writeObject(new SocketResponse(SocketResponse.RESPONSE_CODE_SUCCESS, null));
		} catch (SQLException e) {
			System.out.println(e.getMessage());
			handlerOut.writeObject(new SocketResponse(SocketResponse.RESPONSE_CODE_FAILURE, null));
		}
	}

	void talkCode_GetUser(Map<String, Object> data) throws IOException {
		try {
			ResultSet queryResult = db.getUser(data.get("username").toString(), data.get("hashedPassword").toString());
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
	}

	void talkCode_SearchUser(Map<String, Object> data) throws IOException {
		try {
			ResultSet queryResult = db.searchUser(data.get("username").toString(), data.get("otherUsername").toString());
			ResultSetMetaData queryResultMeta;
			queryResultMeta = queryResult.getMetaData();

			Vector<Map<String, Object>> responseData = new Vector<>();
			while (queryResult.next()) {
				Map<String, Object> row = new HashMap<>();
				for (int i = 1; i <= queryResultMeta.getColumnCount(); i++) {
					row.put(queryResultMeta.getColumnLabel(i), queryResult.getObject(i));
				}

				responseData.add(row);
			}

			queryResult.getStatement().close();
			handlerOut.writeObject(new SocketResponse(SocketResponse.RESPONSE_CODE_SUCCESS, responseData));
		} catch (SQLException | NullPointerException e) {
			System.out.println(e.getMessage());
			handlerOut.writeObject(new SocketResponse(SocketResponse.RESPONSE_CODE_FAILURE, null));
		}
	}

	void talkCode_SendFriendRequest(Map<String, Object> data) throws IOException {
		try {
			db.sendFriendRequest(data.get("username").toString(), data.get("otherUsername").toString());
			handlerOut.writeObject(new SocketResponse(SocketResponse.RESPONSE_CODE_SUCCESS, null));
		} catch (SQLException e) {
			System.out.println(e.getMessage());
			handlerOut.writeObject(new SocketResponse(SocketResponse.RESPONSE_CODE_FAILURE, null));
		}
	}

	void talkCode_FriendMessageList(Map<String, Object> data) throws IOException {
		try {
			ResultSet[] queryResult = db.getFriendMessageList(data.get("username").toString(), data.get("friendSearch").toString(), data.get("chatSearch").toString());

			Vector<Map<String, Object>> responseData = new Vector<>();
			for (ResultSet qrEach: queryResult) {
				ResultSetMetaData queryResultMeta = qrEach.getMetaData();
				Vector<Map<String, Object>> rdEach = new Vector<>();
				while (qrEach.next()) {
					HashMap<String, Object> row = new HashMap<>();
					for (int i = 1; i <= queryResultMeta.getColumnCount(); i++) {
						row.put(queryResultMeta.getColumnLabel(i), qrEach.getObject(i));
					}
					rdEach.add(row);
				}

				qrEach.getStatement().close();
				responseData.add(Map.of("data", rdEach));
			}

		handlerOut.writeObject(new SocketResponse(SocketResponse.RESPONSE_CODE_SUCCESS, responseData));
		} catch (SQLException e) {
			System.out.println(e.getMessage());
			handlerOut.writeObject(new SocketResponse(SocketResponse.RESPONSE_CODE_FAILURE, null));
		}
	}

	void talkCode_SingleFriendChatLog(Map<String, Object> data) throws IOException {
		try {
			ResultSet queryResult = db.getSingleFriendChatLog(data.get("username").toString(), data.get("conversationId").toString(), data.get("memberId").toString());
			ResultSetMetaData queryResultMeta;
			queryResultMeta = queryResult.getMetaData();

			Vector<Map<String, Object>> responseData = new Vector<>();
			while (queryResult.next()) {
				Map<String, Object> row = new HashMap<>();
				for (int i = 1; i <= queryResultMeta.getColumnCount(); i++) {
					row.put(queryResultMeta.getColumnLabel(i), queryResult.getObject(i));
				}

				// Insert based on message id (message order)
				int insertIndex = (int) row.get("MessageId") - 1;
				if (responseData.size() - 1 < insertIndex) responseData.add(row);
				else responseData.add((int) row.get("MessageId") - 1, row);
			}

			queryResult.getStatement().close();
			handlerOut.writeObject(new SocketResponse(SocketResponse.RESPONSE_CODE_SUCCESS, responseData));
		} catch (SQLException | NullPointerException e) {
			System.out.println(e.getMessage());
			handlerOut.writeObject(new SocketResponse(SocketResponse.RESPONSE_CODE_FAILURE, null));
		}
	}

	void talkCode_MessageSeen(Map<String, Object> data) throws IOException {
		try {
			db.messageSeen(data.get("username").toString(), data.get("conversationId").toString());
			handlerOut.writeObject(new SocketResponse(SocketResponse.RESPONSE_CODE_SUCCESS, null));
		} catch (SQLException e) {
			System.out.println(e.getMessage());
			handlerOut.writeObject(new SocketResponse(SocketResponse.RESPONSE_CODE_FAILURE, null));
		}
	}

	void talkCode_SendMessage(Map<String, Object> data) throws IOException {
		try {
			db.sendMessage(data.get("username").toString(), data.get("friendId").toString(), data.get("conversationId").toString(), data.get("content").toString());
			handlerOut.writeObject(new SocketResponse(SocketResponse.RESPONSE_CODE_SUCCESS, null));
		} catch (SQLException e) {
			System.out.println(e.getMessage());
			handlerOut.writeObject(new SocketResponse(SocketResponse.RESPONSE_CODE_FAILURE, null));
		}
	}
}