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
					case "2": {
						talkCode_ForgetPassword(request.getData());
						break;
					}
					case "3": {
						talkCode_GetUser(request.getData());
						break;
					}
					case "3_1": {
						talkCode_LogSignInActivity(request.getData());
						break;
					}
					case "3_2": {
						talkCode_LogSignOutActivity(request.getData());
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
					case "4b": {
						talkCode_GetFriendRequest(request.getData());
						break;
					}
					case "4c": {
						Map<String, Object> data = request.getData();
						try {
							db.respondFriendRequest((Integer) data.get("responseType"), data.get("username").toString(), data.get("senderId").toString());
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
					case "4e": {
						talkCode_GetUserInfo(request.getData());
						break;
					}
					case "4e_1": {
						talkCode_GetGroupInfo(request.getData());
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
					case "9e": {
						talkCode_DeleteChatHistory(request.getData());
						break;
					}
					case "9h": {
						talkCode_MessageSeen(request.getData());
						break;
					}
					case "10b": {
						talkCode_RenameChat(request.getData());
						break;
					}
					case "10c_1": {
						talkCode_NonGroupMemberList(request.getData());
						break;
					}
					case "10c": {
						talkCode_AddGroupMember(request.getData());
						break;
					}
					case "10d": {
						if (talkCode_GetMemberAdminStatus(request.getData()))
							talkCode_RemoveGroupMember(request.getData());
						break;
					}
					case "10e": {
						talkCode_GroupMemberList(request.getData());
						break;
					}
					case "10f": {
						talkCode_LeaveGroup(request.getData());
						break;
					}
					case "10g": {
						if (talkCode_GetMemberAdminStatus(request.getData()))
							talkCode_SetMemberAdminStatus(request.getData());
						break;
					}
					case "11": {
						talkCode_SendMessage(request.getData());
						break;
					}
					case "uobu": {
						blockCode_UnorBlockUser(request.getData());
						break;
					}
					case "delConv": {
						conCode_DelteConv(request.getData());
						break;
					}
					case "conMemInfo": {
						conCode_GetMemInfo(request.getData());
						break;
					}

					case "uf": {
						friendCode_Unfriend(request.getData());
						break;
					}

					case "ru": {
						//friendCode_Unfriend(request.getData());
						reportCode_ReportUser(request.getData());
						break;
					}
					case "newConv": {
						conCode_CreateNewConv(request.getData());
						break;
					}
					case "highestId": {
						conCode_GetHighestId(request.getData());
						break;
					}
					case "convAddMemWhenUareAdmin": {
						conCode_AddMemWhenUareAdmin(request.getData());
						break;
					}
					case "updateConvLog": {
						conCode_UpdateConvLog(request.getData());
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
			db.resetPassword(data.get("username").toString(), data.get("email").toString(), data.get("hashedPassword").toString());
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
					int value = queryResult.getObject(i) == null ? -1 : Integer.parseInt(queryResult.getObject(i).toString());
					responseData.add(Map.of(queryResultMeta.getColumnLabel(i), value));
				}
			}

			queryResult.getStatement().close();
			handlerOut.writeObject(new SocketResponse(SocketResponse.RESPONSE_CODE_SUCCESS, responseData));
		} catch (SQLException e) {
			System.out.println(e.getMessage());
			handlerOut.writeObject(new SocketResponse(SocketResponse.RESPONSE_CODE_FAILURE, null));
		}
	}

	void talkCode_LogSignInActivity(Map<String, Object> data) throws IOException {
		try {
			db.logActivitySignIn(data.get("username").toString());
			handlerOut.writeObject(new SocketResponse(SocketResponse.RESPONSE_CODE_SUCCESS, null));
		} catch (SQLException e) {
			System.out.println(e.getMessage());
			handlerOut.writeObject(new SocketResponse(SocketResponse.RESPONSE_CODE_FAILURE, null));
		}
	}

	void talkCode_LogSignOutActivity(Map<String, Object> data) throws IOException {
		try {
			db.logActivitySignOut(data.get("username").toString());
			handlerOut.writeObject(new SocketResponse(SocketResponse.RESPONSE_CODE_SUCCESS, null));
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

	void talkCode_GetFriendRequest(Map<String, Object> data) throws IOException {
		try {
			ResultSet queryResult = db.getFriendRequest(data.get("username").toString());
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

	void talkCode_GetUserInfo(Map<String, Object> data) throws IOException {
		try {
			ResultSet queryResult = db.getUserInfo(data.get("username").toString(), data.get("conversationId").toString());
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

	void talkCode_GetGroupInfo(Map<String, Object> data) throws IOException {
		try {
			ResultSet queryResult = db.getGroupInfo(data.get("conversationId").toString());
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

	void talkCode_FriendMessageList(Map<String, Object> data) throws IOException {
		try {
			ResultSet[] queryResult = new ResultSet[0];
			switch (data.get("searchOption").toString()) {
				case "friend": {
					queryResult = db.getFriendMessageList(data.get("username").toString(), data.get("friendSearch").toString(), data.get("chatSearch").toString(), false);
					break;
				}
				case "friendOnline": {
					queryResult = db.getOnlineFriendMessageList(data.get("username").toString(), data.get("friendSearch").toString(), data.get("chatSearch").toString());
					break;
				}
				case "group": {
					queryResult = db.getFriendMessageList(data.get("username").toString(), data.get("friendSearch").toString(), data.get("chatSearch").toString(), true);
					break;
				}
				case "friendBlocked": {
					queryResult = db.getBlockedFriendMessageList(data.get("username").toString(), data.get("friendSearch").toString(), data.get("chatSearch").toString());
					break;
				}
			}

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
			ResultSet queryResult = db.getSingleFriendChatLog(data.get("username").toString(), data.get("conversationId").toString());
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

	void talkCode_DeleteChatHistory(Map<String, Object> data) throws IOException {
		try {
			db.deleteChatHistory(data.get("conversationId").toString());
			handlerOut.writeObject(new SocketResponse(SocketResponse.RESPONSE_CODE_SUCCESS, null));
		} catch (SQLException e) {
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

	void talkCode_RenameChat(Map<String, Object> data) throws IOException {
		try {
			db.renameChat(data.get("conversationId").toString(), data.get("conversationName").toString());
			handlerOut.writeObject(new SocketResponse(SocketResponse.RESPONSE_CODE_SUCCESS, null));
		} catch (SQLException e) {
			System.out.println(e.getMessage());
			handlerOut.writeObject(new SocketResponse(SocketResponse.RESPONSE_CODE_FAILURE, null));
		}
	}

	void talkCode_NonGroupMemberList(Map<String, Object> data) throws IOException {
		try {
			ResultSet queryResult = db.getNonGroupMember(data.get("searchUsername").toString(), data.get("conversationId").toString());
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

	void talkCode_AddGroupMember(Map<String, Object> data) throws IOException {
		try {
			db.addGroupMember(data.get("conversationId").toString(), data.get("newMemberId").toString());
			handlerOut.writeObject(new SocketResponse(SocketResponse.RESPONSE_CODE_SUCCESS, null));
		} catch (SQLException e) {
			System.out.println(e.getMessage());
			handlerOut.writeObject(new SocketResponse(SocketResponse.RESPONSE_CODE_FAILURE, null));
		}
	}

	void talkCode_RemoveGroupMember(Map<String, Object> data) throws IOException {
		try {
			db.removeGroupMember(data.get("conversationId").toString(), data.get("newMemberId").toString());
			handlerOut.writeObject(new SocketResponse(SocketResponse.RESPONSE_CODE_SUCCESS, null));
		} catch (SQLException e) {
			System.out.println(e.getMessage());
			handlerOut.writeObject(new SocketResponse(SocketResponse.RESPONSE_CODE_FAILURE, null));
		}
	}

	void talkCode_GroupMemberList(Map<String, Object> data) throws IOException {
		try {
			ResultSet queryResult = db.groupMemberList(data.get("conversationId").toString());
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

	void talkCode_LeaveGroup(Map<String, Object> data) throws IOException {
		try {
			db.leaveGroup(data.get("username").toString(), data.get("conversationId").toString());
			handlerOut.writeObject(new SocketResponse(SocketResponse.RESPONSE_CODE_SUCCESS, null));
		} catch (SQLException e) {
			System.out.println(e.getMessage());
			handlerOut.writeObject(new SocketResponse(SocketResponse.RESPONSE_CODE_FAILURE, null));
		}
	}

	boolean talkCode_GetMemberAdminStatus(Map<String, Object> data) throws IOException {
		try {
			ResultSet queryResult = db.getMemberAdminStatus(data.get("username").toString(), data.get("conversationId").toString());
			ResultSetMetaData queryResultMeta;
			queryResultMeta = queryResult.getMetaData();

			Vector<Map<String, Object>> responseData = new Vector<>();
			while (queryResult.next()) {
				for (int i = 1; i <= queryResultMeta.getColumnCount(); i++) {
					responseData.add(Map.of(queryResultMeta.getColumnLabel(i), queryResult.getObject(i)));
				}
			}

			queryResult.getStatement().close();

			if ((Boolean) responseData.get(0).get("IsAdmin")) {
				handlerOut.writeObject(new SocketResponse(SocketResponse.RESPONSE_CODE_SUCCESS, null));
				return true;
			}
			else {
				handlerOut.writeObject(new SocketResponse(SocketResponse.RESPONSE_CODE_FAILURE, null));
				return false;
			}
		} catch (SQLException e) {
			handlerOut.writeObject(new SocketResponse(SocketResponse.RESPONSE_CODE_FAILURE, null));
			return false;
		}
	}

	void talkCode_SetMemberAdminStatus(Map<String, Object> data) throws IOException {
		try {
			db.setMemberAdminStatus(data.get("conversationId").toString(), data.get("memberId").toString(), (Boolean) data.get("isAdmin"));
			handlerOut.writeObject(new SocketResponse(SocketResponse.RESPONSE_CODE_SUCCESS, null));
		} catch (SQLException e) {
			System.out.println(e.getMessage());
			handlerOut.writeObject(new SocketResponse(SocketResponse.RESPONSE_CODE_FAILURE, null));
		}
	}

	void talkCode_SendMessage(Map<String, Object> data) throws IOException {
		try {
			db.sendMessage(data.get("username").toString(), data.get("conversationId").toString(), data.get("content").toString());
			handlerOut.writeObject(new SocketResponse(SocketResponse.RESPONSE_CODE_SUCCESS, null));
		} catch (SQLException e) {
			System.out.println(e.getMessage());
			handlerOut.writeObject(new SocketResponse(SocketResponse.RESPONSE_CODE_FAILURE, null));
		}
	}

	void blockCode_UnorBlockUser(Map<String, Object> data) throws IOException {
		try {
			ResultSet isBlockedQuery = db.determineIsBlocked(data.get("blocker").toString(), data.get("blocked").toString());
			while (isBlockedQuery.next()){
				boolean isBlocked = (int) isBlockedQuery.getObject(1) != 0;
				if (!isBlocked) {
					db.UserBlockUser(data.get("blocker").toString(), data.get("blocked").toString());
					handlerOut.writeObject(new SocketResponse(SocketResponse.RESPONSE_BLOCK_CODE_BLOCK, null));
				} else {
					db.UserUnBlockUser(data.get("blocker").toString(), data.get("blocked").toString());
					handlerOut.writeObject(new SocketResponse(SocketResponse.RESPONSE_BLOCK_CODE_UN_BLOCK, null));
				}
			}

		} catch (SQLException e) {
			System.out.println(e.getMessage());
			handlerOut.writeObject(new SocketResponse(SocketResponse.RESPONSE_CODE_FAILURE, null));
		}
	}



	void friendCode_Unfriend(Map<String, Object> data) throws IOException {
		try {
			db.removeFriend(data.get("userId").toString(), data.get("friendId").toString());
			handlerOut.writeObject(new SocketResponse(SocketResponse.RESPONSE_CODE_SUCCESS, null));
		} catch (SQLException e) {
			System.out.println(e.getMessage());
			handlerOut.writeObject(new SocketResponse(SocketResponse.RESPONSE_CODE_FAILURE, null));
		}
	}

	void reportCode_ReportUser(Map<String, Object> data) throws IOException {
		try {
			db.reportUser(data.get("reporterId").toString(), data.get("reportedId").toString());
			handlerOut.writeObject(new SocketResponse(SocketResponse.RESPONSE_CODE_SUCCESS, null));
		} catch (SQLException e) {
			System.out.println(e.getMessage());
			handlerOut.writeObject(new SocketResponse(SocketResponse.RESPONSE_CODE_FAILURE, null));
		}
	}
	void conCode_CreateNewConv(Map<String, Object> data) throws IOException {
		try {
			db.CreateNewConv(data.get("newConId").toString(), data.get("conName").toString(), (boolean) data.get("type"));
			handlerOut.writeObject(new SocketResponse(SocketResponse.RESPONSE_CODE_SUCCESS, null));
		} catch (SQLException e) {
			System.out.println(e.getMessage());
			handlerOut.writeObject(new SocketResponse(SocketResponse.RESPONSE_CODE_FAILURE, null));
		}
	}
	void conCode_AddMemWhenUareAdmin(Map<String, Object> data) throws IOException {
		try {
			db.AddMemWhenUareAdmin(data.get("conId").toString(), data.get("adMin").toString(), data.get("userId").toString());
			handlerOut.writeObject(new SocketResponse(SocketResponse.RESPONSE_CODE_SUCCESS, null));
		} catch (SQLException e) {
			System.out.println(e.getMessage());
			handlerOut.writeObject(new SocketResponse(SocketResponse.RESPONSE_CODE_FAILURE, null));
		}
	}
	void conCode_UpdateConvLog(Map<String, Object> data) throws IOException {
		try {
			db.UpdateConvLog(data.get("conId").toString(), data.get("userId").toString(), (int) data.get("logType"));
			handlerOut.writeObject(new SocketResponse(SocketResponse.RESPONSE_CODE_SUCCESS, null));
		} catch (SQLException e) {
			System.out.println(e.getMessage());
			handlerOut.writeObject(new SocketResponse(SocketResponse.RESPONSE_CODE_FAILURE, null));
		}
	}
	void conCode_DelteConv(Map<String, Object> data) throws IOException {
		try {
			db.deleteConv(data.get("conId").toString());
			handlerOut.writeObject(new SocketResponse(SocketResponse.RESPONSE_CODE_SUCCESS, null));
		} catch (SQLException e) {
			System.out.println(e.getMessage());
			handlerOut.writeObject(new SocketResponse(SocketResponse.RESPONSE_CODE_FAILURE, null));
		}
	}
	void conCode_GetHighestId(Map<String, Object> data) throws IOException {
		try {
			ResultSet queryResult = db.getHighestConvId();
			Vector<Map<String, Object>> responseData = new Vector<>();
			while (queryResult.next()) {
				responseData.add(Map.of("highestConId", queryResult.getObject(1)));
			}
			System.out.println(responseData);
			queryResult.getStatement().close();
			handlerOut.writeObject(new SocketResponse(SocketResponse.RESPONSE_CODE_SUCCESS, responseData));
		} catch (SQLException e) {
			System.out.println("Error: " + e.getMessage());
			handlerOut.writeObject(new SocketResponse(SocketResponse.RESPONSE_CODE_FAILURE, null));
		}
	}
	void conCode_GetMemInfo(Map<String, Object> data) throws IOException {
		try {
			ResultSet queryResult = db.getInfoConversationUserParticipateIn(data.get("conId").toString());
			Vector<Map<String, Object>> responseData = new Vector<>();
			while (queryResult.next()) {
				if (!(boolean) queryResult.getObject(1)){
					ResultSet memberIdResult = db.getConversationMemberId(data.get("conId").toString());
					ResultSetMetaData queryResultMeta;
					queryResultMeta = memberIdResult.getMetaData();
					while (memberIdResult.next()) {
						for (int i = 1; i <= queryResultMeta.getColumnCount(); i++) {
							responseData.add(Map.of(queryResultMeta.getColumnLabel(i), memberIdResult.getObject(i)));
						}
					}
				}
			}
			System.out.println(responseData);
			queryResult.getStatement().close();
			handlerOut.writeObject(new SocketResponse(SocketResponse.RESPONSE_CODE_SUCCESS, responseData));
		} catch (SQLException e) {
			System.out.println("Error: " + e.getMessage());
			handlerOut.writeObject(new SocketResponse(SocketResponse.RESPONSE_CODE_FAILURE, null));
		}
	}
}