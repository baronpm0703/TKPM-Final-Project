package com.psvm.server.controllers;

import com.psvm.server.models.DBConnection;
import com.psvm.server.models.DBInteraction;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Vector;

public class DBWrapper {
	DBInteraction dbConn;

	public DBWrapper() {
		try {
			dbConn = new DBInteraction(DBConnection.getConnection());
		} catch (SQLException exc) {
			System.out.println("Exception thrown while connecting to database in " + this.getClass().getSimpleName() + ": " + exc.getMessage());
		}
	}

	public void createUser(String username, String fName, String lName, String password, String address, LocalDateTime dob, boolean isMale, String email) throws SQLException {
		String sql = "INSERT INTO User VALUES (?, ?, ?, ?, ?, ?, ?, ?, 0, '', current_timestamp())";

		Vector<Object> questionMarks = new Vector<>();
		questionMarks.add(username);
		questionMarks.add(fName);
		questionMarks.add(lName);
		questionMarks.add(password);
		questionMarks.add(address);
		questionMarks.add(dob);
		questionMarks.add(isMale);
		questionMarks.add(email);

		dbConn.doPreparedStatement(sql, questionMarks);

	}
	public void resetPassword(String username, String email, String hashedPassword) throws SQLException {
		String sql = "UPDATE User SET Password=? WHERE Username=? AND Email=?";

		Vector<Object> questionMarks = new Vector<>();
		questionMarks.add(hashedPassword);
		questionMarks.add(username);
		questionMarks.add(email);

		dbConn.doPreparedStatement(sql, questionMarks);
	}
	public void updateUser(String id, String username, String fName, String lName, String address, LocalDateTime dob, boolean isMale, String email,  LocalDateTime creationDate) throws SQLException {
		String sql = "Update hooyah.user\n" +
				"Set Username=?, FirstName=?, LastName=?, Address= ?, Dob= ?, Gender= ?, Email=?, CreationDate=?\n" +
				"where Username = ?";

		Vector<Object> questionMarks = new Vector<>();
		questionMarks.add(username);
		questionMarks.add(fName);
		questionMarks.add(lName);
		questionMarks.add(address);
		questionMarks.add(dob);
		questionMarks.add(isMale);
		questionMarks.add(email);
		questionMarks.add(creationDate);
		questionMarks.add(id);
		dbConn.doPreparedStatement(sql, questionMarks);
	}
	public void deleteUser(String id) throws SQLException {
		String sql = "Delete from User Where Username = ?";

		Vector<Object> questionMarks = new Vector<>();
		questionMarks.add(id);
		dbConn.doPreparedStatement(sql, questionMarks);
	}

	public void deleteConv(String conid) throws SQLException {
		String sql1 = "Delete from ConversationMember Where ConversationId = ?";
		Vector<Object> questionMarks1 = new Vector<>();
		questionMarks1.add(conid);
		dbConn.doPreparedStatement(sql1, questionMarks1);

		String sql2 = "Delete from Conversation Where ConversationId = ?";
		Vector<Object> questionMarks2 = new Vector<>();
		questionMarks2.add(conid);
		dbConn.doPreparedStatement(sql2, questionMarks2);
	}


	public ResultSet getUser(String username, String hashedPassword) throws SQLException {
		String sql = "SELECT COUNT(Username) as UserFound, Status FROM User WHERE Username=? AND Password=?;";

		Vector<Object> questionMarks = new Vector<>();
		questionMarks.add(username);
		questionMarks.add(hashedPassword);

		return dbConn.doPreparedQuery(sql, questionMarks);
	}

	public ResultSet getFriendCount(String userId) throws SQLException {
		String sql = "SELECT count(Status) as friendNum from Friend Where UserId = ? or FriendId = ?";

		Vector<Object> questionMarks = new Vector<>();
		questionMarks.add(userId);
		questionMarks.add(userId);

		return dbConn.doPreparedQuery(sql, questionMarks);
	}

	public void logActivitySignIn(String username) throws SQLException {
		String sql1 = "INSERT INTO UserLog VALUES (?, current_timestamp(), 0, '')";

		Vector<Object> questionMarks1 = new Vector<>();
		questionMarks1.add(username);

		String sql2 = "UPDATE User SET Status=1 WHERE Username=?";

		Vector<Object> questionMarks2 = new Vector<>();
		questionMarks2.add(username);

		dbConn.doBatchPreparedStatement(new String[] {sql1, sql2}, new Vector[] {questionMarks1, questionMarks2});
	}

	public void logActivitySignOut(String username) throws SQLException {
		String sql1 = "INSERT INTO UserLog VALUES (?, current_timestamp(), 1, '')";

		Vector<Object> questionMarks1 = new Vector<>();
		questionMarks1.add(username);

		String sql2 = "UPDATE User SET Status=0 WHERE Username=?";

		Vector<Object> questionMarks2 = new Vector<>();
		questionMarks2.add(username);

		dbConn.doBatchPreparedStatement(new String[] {sql1, sql2}, new Vector[] {questionMarks1, questionMarks2});
	}

	public ResultSet searchUser(String currentUsername, String otherUsername) throws SQLException {
		String sql = "SELECT u.Username, fq.TargetId\n" +
				"FROM User u\n" +
				"LEFT JOIN FriendRequest fq ON (fq.SenderId = ? AND fq.TargetId = u.Username) OR (fq.SenderId = u.Username AND fq.TargetId = ?)\n" +
				"WHERE u.Username LIKE ? AND u.Username!=?;";

		Vector<Object> questionMarks = new Vector<>();
		questionMarks.add(currentUsername);
		questionMarks.add(currentUsername);
		questionMarks.add("%" + otherUsername + "%");
		questionMarks.add(currentUsername);

		return dbConn.doPreparedQuery(sql, questionMarks);
	}

	public void sendFriendRequest(String currentUsername, String otherUsername) throws SQLException {
		String sql = "INSERT INTO FriendRequest (SenderId, TargetId, Datetime) VALUES (?, ?, current_timestamp());";

		Vector<Object> questionMarks = new Vector<>();
		questionMarks.add(currentUsername);
		questionMarks.add(otherUsername);

		dbConn.doPreparedStatement(sql, questionMarks);
	}

	public ResultSet getFriendRequest(String username) throws SQLException {
		String sql = "SELECT * FROM FriendRequest WHERE TargetId=?";

		Vector<Object> questionMarks = new Vector<>();
		questionMarks.add(username);

		return dbConn.doPreparedQuery(sql, questionMarks);
	}

	public void respondFriendRequest(int status, String currentUsername, String senderId) throws SQLException {
		// Remove first sql line (before first semicolon) when done testing
		String sql = "UPDATE FriendRequest SET Status=? WHERE SenderId=? AND TargetId=? ORDER BY Datetime DESC LIMIT 1;";

		Vector<Object> questionMarks = new Vector<>();
		questionMarks.add(status);
		questionMarks.add(senderId);
		questionMarks.add(currentUsername);

		dbConn.doPreparedStatement(sql, questionMarks);
	}

	public void removeFriend(String currentUsername, String friendId) throws SQLException {
		String sql = "DELETE FROM Friend WHERE (UserId=? AND FriendId=?) OR (UserId=? AND FriendId=?)";

		Vector<Object> questionMarks = new Vector<>();
		questionMarks.add(currentUsername);
		questionMarks.add(friendId);
		questionMarks.add(friendId);
		questionMarks.add(currentUsername);

		dbConn.doPreparedStatement(sql, questionMarks);
	}

	public ResultSet getUserInfo(String currentUsername, String conversationId) throws SQLException {
		String sql = "SELECT cv.IsGroup, u.Username, u.FirstName, u.LastName\n" +
				"FROM User u\n" +
				"JOIN ConversationMember cvmem ON cvmem.MemberId = u.Username\n" +
				"JOIN Conversation cv ON cv.ConversationId = cvmem.ConversationId\n" +
				"WHERE cv.ConversationId = ? AND cvmem.MemberId != ? AND cv.IsGroup=false;";

		Vector<Object> questionMarks = new Vector<>();
		questionMarks.add(conversationId);
		questionMarks.add(currentUsername);

		return dbConn.doPreparedQuery(sql, questionMarks);
	}

	public ResultSet getGroupInfo(String conversationId) throws SQLException {
		String sql = "SELECT IsGroup, ConversationId, ConversationName\n" +
				"FROM Conversation\n" +
				"WHERE ConversationId = ? AND IsGroup=true;";

		Vector<Object> questionMarks = new Vector<>();
		questionMarks.add(conversationId);

		return dbConn.doPreparedQuery(sql, questionMarks);
	}

	public void BanUser(String bannedUserName) throws SQLException {
		int Status = 2;
		String sql = "Update hooyah.user Set Status = ? Where Username = ?; ";

		Vector<Object> questionMarks = new Vector<>();
		questionMarks.add(Status);
		questionMarks.add(bannedUserName);

		dbConn.doPreparedStatement(sql, questionMarks);
		UpdateUserLog(bannedUserName, 2);
	}

	public void UnBanUser(String bannedUserName) throws SQLException {
		int Status = 0;
		String sql = "Update hooyah.user Set Status = ? Where Username = ?; ";

		Vector<Object> questionMarks = new Vector<>();
		questionMarks.add(Status);
		questionMarks.add(bannedUserName);

		dbConn.doPreparedStatement(sql, questionMarks);
		UpdateUserLog(bannedUserName, 0);
	}

	public void UpdateUserLog(String userId, int status)  throws SQLException {

		String Detail = "";
		switch (status) {
			case 0:
				Detail = "Login";
				break;
			case 1:
				Detail = "LogOut";
				break;
			case 2:
				Detail = "Banned";
				break;
			case 3:
				Detail = "Password changed";
				break;
		}
		String date =  LocalDateTime.now().toString();
		String sql = "Insert Into hooyah.userlog (UserId, Datetime, LogType, LogDetail) Value (?,?,?,?)";

		Vector<Object> questionMarks = new Vector<>();
		questionMarks.add(userId);
		questionMarks.add(date);
		questionMarks.add(status);
		questionMarks.add(Detail);

		dbConn.doPreparedStatement(sql, questionMarks);
	}

	public ResultSet DiplayUserLogWithType(String userId, int type) throws SQLException {
		String Detail = "";
		switch (type) {
			case 0:
				Detail = "Login";
				break;
			case 1:
				Detail = "LogOut";
				break;
			case 2:
				Detail = "Banned";
				break;
			case 3:
				Detail = "Password changed";
				break;
		}

		String sql = "SELECT Datetime FROM hooyah.userlog Where UserId = ? and LogType = ? Order by DateTime DESC;";

		Vector<Object> questionMarks = new Vector<>();
		questionMarks.add(userId);
		questionMarks.add(type);

        return dbConn.doPreparedQuery(sql, questionMarks);
    }

	public ResultSet FriendListOfUser(String userId) throws SQLException {

		String sql = "Select concat_ws(' ', u.FirstName, u.Lastname) as Hoten, f.Status From hooyah.friend as f Join hooyah.user as u On u.Username = f.FriendId Where UserId=?";

		Vector<Object> questionMarks = new Vector<>();
		questionMarks.add(userId);

		return dbConn.doPreparedQuery(sql, questionMarks);
	}


	public ResultSet getFriendList(String currentUsername) throws SQLException {
		String sql = "SELECT FriendId FROM Friend WHERE UserId=? OR FriendId=?";

		Vector<Object> questionMarks = new Vector<>();
		questionMarks.add(currentUsername);
		questionMarks.add(currentUsername);

		return dbConn.doPreparedQuery(sql, questionMarks);
	}

	public ResultSet[] getFriendMessageList(String currentUsername, String friendSearch, String chatSearch, boolean isGroup) throws SQLException {
		String sql1 = "WITH ranked_data AS (\n" +
				"\tSELECT cv.ConversationId, cv.ConversationName, cvmes.MessageId, cvmes.SenderId, cvmem2.MemberId, cvmes.Datetime, \n" +
				"    cvmes.Content, cv.IsGroup,\n" +
				"        ROW_NUMBER() OVER (PARTITION BY cv.ConversationId ORDER BY cvmes.Datetime DESC) AS rn\n" +
				"\tFROM Conversation cv\n" +
				"\tJOIN ConversationMember cvmem ON cv.ConversationId = cvmem.ConversationId\n" +
				"    JOIN ConversationMember cvmem2 ON cv.ConversationId = cvmem2.ConversationId AND cvmem2.MemberId LIKE ? AND cvmem2.MemberId != ?\n" +
				"\tJOIN ConversationMessage cvmes ON cvmes.ConversationId = cvmem.ConversationId\n" +
				"    JOIN conversationmessage cvmes2 ON cvmes2.ConversationId = cvmem.ConversationId AND cvmes2.Content LIKE ?\n" +
				"\tWHERE cvmem.MemberId=?\n" +
				"\tGROUP BY cv.ConversationId, cv.ConversationName, cvmes.MessageId, cvmes.SenderId, cvmem2.MemberId\n" +
				")\n" +
				"SELECT DISTINCT rd.ConversationId, rd.ConversationName, rd.MessageId, rd.MemberId, SenderId, rd.Datetime, Content, IsGroup\n" +
				"FROM ranked_data rd\n" +
				"WHERE rn = 1 AND IsGroup=? AND NOT EXISTS (\n" +
				"\tSELECT *\n" +
				"    FROM MessageSeen ms\n" +
				"    WHERE ms.MessageId = rd.MessageId AND ms.ConversationId = rd.ConversationId AND ms.SeenId = ?\n" +
				") AND rd.Datetime >= all(\n" +
				"\tSELECT ul.Datetime\n" +
				"    FROM UserLog ul\n" +
				"    WHERE ul.UserId = ? AND ul.LogType = 0\n" +
				");";

		Vector<Object> questionMarks1 = new Vector<>();
		questionMarks1.add("%" + friendSearch + "%");
		questionMarks1.add(currentUsername);
		questionMarks1.add("%" + chatSearch + "%");
		questionMarks1.add(currentUsername);
		questionMarks1.add(isGroup);
		questionMarks1.add(currentUsername);
		questionMarks1.add(currentUsername);

		ResultSet rs1 = dbConn.doPreparedQuery(sql1, questionMarks1);

		String sql2 = "WITH ranked_data AS (\n" +
				"\tSELECT cv.ConversationId, cv.ConversationName, cvmes.MessageId, cvmes.SenderId, cvmem2.MemberId, cvmes.Datetime, \n" +
				"\t\tcvmes.Content, cv.IsGroup,\n" +
				"        ROW_NUMBER() OVER (PARTITION BY cv.ConversationId ORDER BY cvmes.Datetime DESC) AS rn\n" +
				"\tFROM Conversation cv\n" +
				"\tJOIN ConversationMember cvmem ON cv.ConversationId = cvmem.ConversationId\n" +
				"    JOIN ConversationMember cvmem2 ON cv.ConversationId = cvmem2.ConversationId AND cvmem2.MemberId LIKE ? AND cvmem2.MemberId != ?\n" +
				"\tJOIN ConversationMessage cvmes ON cvmes.ConversationId = cvmem.ConversationId\n" +
				"    JOIN conversationmessage cvmes2 ON cvmes2.ConversationId = cvmem.ConversationId AND cvmes2.Content LIKE ?\n" +
				"\tWHERE cvmem.MemberId=?\n" +
				"\tGROUP BY cv.ConversationId, cv.ConversationName, cvmes.MessageId, cvmes.SenderId, cvmem2.MemberId\n" +
				")\n" +
				"SELECT DISTINCT rd.ConversationId, rd.ConversationName, rd.MessageId, rd.MemberId, SenderId, rd.Datetime, Content, IsGroup\n" +
				"FROM ranked_data rd\n" +
				"WHERE rn = 1 AND IsGroup=? AND NOT EXISTS (\n" +
				"\tSELECT *\n" +
				"    FROM MessageSeen ms\n" +
				"    WHERE ms.MessageId = rd.MessageId AND ms.ConversationId = rd.ConversationId AND ms.SeenId = ?\n" +
				") AND rd.Datetime < any(\n" +
				"\tSELECT ul.Datetime\n" +
				"    FROM UserLog ul\n" +
				"    WHERE ul.UserId = ? AND ul.LogType = 0\n" +
				");";

		Vector<Object> questionMarks2 = new Vector<>();
		questionMarks2.add("%" + friendSearch + "%");
		questionMarks2.add(currentUsername);
		questionMarks2.add("%" + chatSearch + "%");
		questionMarks2.add(currentUsername);
		questionMarks2.add(isGroup);
		questionMarks2.add(currentUsername);
		questionMarks2.add(currentUsername);

		ResultSet rs2 = dbConn.doPreparedQuery(sql2, questionMarks2);

		String sql3 = "WITH ranked_data AS (\n" +
				"\tSELECT cv.ConversationId, cv.ConversationName, cvmes.MessageId, cvmes.SenderId, cvmem2.MemberId, cvmes.Datetime, \n" +
				"\t\tcvmes.Content, cv.IsGroup,\n" +
				"        ROW_NUMBER() OVER (PARTITION BY cv.ConversationId ORDER BY cvmes.Datetime DESC) AS rn\n" +
				"\tFROM Conversation cv\n" +
				"\tJOIN ConversationMember cvmem ON cv.ConversationId = cvmem.ConversationId\n" +
				"    JOIN ConversationMember cvmem2 ON cv.ConversationId = cvmem2.ConversationId AND cvmem2.MemberId LIKE ? AND cvmem2.MemberId != ?\n" +
				"\tJOIN ConversationMessage cvmes ON cvmes.ConversationId = cvmem.ConversationId\n" +
				"    JOIN conversationmessage cvmes2 ON cvmes2.ConversationId = cvmem.ConversationId AND cvmes2.Content LIKE ?\n" +
				"\tWHERE cvmem.MemberId=?\n" +
				"\tGROUP BY cv.ConversationId, cv.ConversationName, cvmes.MessageId, cvmes.SenderId, cvmem2.MemberId, cvmes2.Content\n" +
				")\n" +
				"SELECT DISTINCT rd.ConversationId, rd.ConversationName, rd.MessageId, rd.MemberId, SenderId, rd.Datetime, Content, IsGroup\n" +
				"FROM ranked_data rd\n" +
				"WHERE rn = 1 AND IsGroup=? AND EXISTS (\n" +
				"\tSELECT *\n" +
				"    FROM MessageSeen ms\n" +
				"    WHERE ms.MessageId = rd.MessageId AND ms.ConversationId = rd.ConversationId AND ms.SeenId = ?\n" +
				");";

		Vector<Object> questionMarks3 = new Vector<>();
		questionMarks3.add("%" + friendSearch + "%");
		questionMarks3.add(currentUsername);
		questionMarks3.add("%" + chatSearch + "%");
		questionMarks3.add(currentUsername);
		questionMarks3.add(isGroup);
		questionMarks3.add(currentUsername);

		ResultSet rs3 = dbConn.doPreparedQuery(sql3, questionMarks3);

		ResultSet rs4;
		String sql4 = "";
		Vector<Object> questionMarks4 = new Vector<>();
		// If the client is search for chat message's content then no Unmessaged friends will be displayed
		if (chatSearch.isEmpty()) {
			sql4 = "SELECT cv.ConversationId, cvmem2.MemberId\n" +
					"FROM Conversation cv\n" +
					"JOIN ConversationMember cvmem ON cv.ConversationId = cvmem.ConversationId\n" +
					"JOIN ConversationMember cvmem2 ON cv.ConversationId = cvmem2.ConversationId\n" +
					"LEFT JOIN ConversationMessage cvmes ON cv.ConversationId = cvmes.ConversationId \n" +
					"WHERE cv.IsGroup=false AND cvmem.MemberId = ? AND cvmem2.MemberId != ? AND cvmem2.MemberId LIKE ?\n" +
					"GROUP BY cv.ConversationId, cvmem2.MemberId\n" +
					"HAVING COUNT(cvmes.MessageId) = 0;";

			questionMarks4.add(currentUsername);
			questionMarks4.add(currentUsername);
			questionMarks4.add("%" + friendSearch + "%");
		}
		else {
			sql4 = "SELECT * FROM Emptiness;";
		}
		rs4 = dbConn.doPreparedQuery(sql4, questionMarks4);

		return new ResultSet[] {rs1, rs2, rs3, rs4};
	}

	public ResultSet[] getOnlineFriendMessageList(String currentUsername, String friendSearch, String chatSearch) throws SQLException {
		String sql1 = "WITH ranked_data AS (\n" +
				"\tSELECT cv.ConversationId, cv.ConversationName, cvmes.MessageId, cvmes.SenderId, cvmem2.MemberId, cvmes.Datetime, \n" +
				"    cvmes.Content, cv.IsGroup,\n" +
				"        ROW_NUMBER() OVER (PARTITION BY cv.ConversationId ORDER BY cvmes.Datetime DESC) AS rn\n" +
				"\tFROM Conversation cv\n" +
				"\tJOIN ConversationMember cvmem ON cv.ConversationId = cvmem.ConversationId\n" +
				"    JOIN ConversationMember cvmem2 ON cv.ConversationId = cvmem2.ConversationId AND cvmem2.MemberId LIKE ? AND cvmem2.MemberId != ?\n" +
				"\tJOIN ConversationMessage cvmes ON cvmes.ConversationId = cvmem.ConversationId\n" +
				"    JOIN conversationmessage cvmes2 ON cvmes2.ConversationId = cvmem.ConversationId AND cvmes2.Content LIKE ?\n" +
				"\tWHERE cvmem.MemberId=?\n" +
				"\tGROUP BY cv.ConversationId, cv.ConversationName, cvmes.MessageId, cvmes.SenderId, cvmem2.MemberId\n" +
				")\n" +
				"SELECT DISTINCT rd.ConversationId, rd.ConversationName, rd.MessageId, rd.MemberId, SenderId, rd.Datetime, Content, IsGroup\n" +
				"FROM ranked_data rd\n" +
				"JOIN User u ON u.Username = rd.MemberId\n" +
				"WHERE u.Status = 1 AND rn = 1 AND IsGroup=false AND NOT EXISTS (\n" +
				"\tSELECT *\n" +
				"    FROM MessageSeen ms\n" +
				"    WHERE ms.MessageId = rd.MessageId AND ms.ConversationId = rd.ConversationId AND ms.SeenId = ?\n" +
				") AND rd.Datetime >= all(\n" +
				"\tSELECT ul.Datetime\n" +
				"    FROM UserLog ul\n" +
				"    WHERE ul.UserId = ? AND ul.LogType = 0\n" +
				");";

		Vector<Object> questionMarks1 = new Vector<>();
		questionMarks1.add("%" + friendSearch + "%");
		questionMarks1.add(currentUsername);
		questionMarks1.add("%" + chatSearch + "%");
		questionMarks1.add(currentUsername);
		questionMarks1.add(currentUsername);
		questionMarks1.add(currentUsername);

		ResultSet rs1 = dbConn.doPreparedQuery(sql1, questionMarks1);

		String sql2 = "WITH ranked_data AS (\n" +
				"\tSELECT cv.ConversationId, cv.ConversationName, cvmes.MessageId, cvmes.SenderId, cvmem2.MemberId, cvmes.Datetime, \n" +
				"\t\tcvmes.Content, cv.IsGroup,\n" +
				"        ROW_NUMBER() OVER (PARTITION BY cv.ConversationId ORDER BY cvmes.Datetime DESC) AS rn\n" +
				"\tFROM Conversation cv\n" +
				"\tJOIN ConversationMember cvmem ON cv.ConversationId = cvmem.ConversationId\n" +
				"    JOIN ConversationMember cvmem2 ON cv.ConversationId = cvmem2.ConversationId AND cvmem2.MemberId LIKE ? AND cvmem2.MemberId != ?\n" +
				"\tJOIN ConversationMessage cvmes ON cvmes.ConversationId = cvmem.ConversationId\n" +
				"    JOIN conversationmessage cvmes2 ON cvmes2.ConversationId = cvmem.ConversationId AND cvmes2.Content LIKE ?\n" +
				"\tWHERE cvmem.MemberId=?\n" +
				"\tGROUP BY cv.ConversationId, cv.ConversationName, cvmes.MessageId, cvmes.SenderId, cvmem2.MemberId\n" +
				")\n" +
				"SELECT DISTINCT rd.ConversationId, rd.ConversationName, rd.MessageId, rd.MemberId, SenderId, rd.Datetime, Content, IsGroup\n" +
				"FROM ranked_data rd\n" +
				"JOIN User u ON u.Username = rd.MemberId\n" +
				"WHERE u.Status = 1 AND rn = 1 AND IsGroup=false AND NOT EXISTS (\n" +
				"\tSELECT *\n" +
				"    FROM MessageSeen ms\n" +
				"    WHERE ms.MessageId = rd.MessageId AND ms.ConversationId = rd.ConversationId AND ms.SeenId = ?\n" +
				") AND rd.Datetime < all(\n" +
				"\tSELECT ul.Datetime\n" +
				"    FROM UserLog ul\n" +
				"    WHERE ul.UserId = ? AND ul.LogType = 0\n" +
				");";

		Vector<Object> questionMarks2 = new Vector<>();
		questionMarks2.add("%" + friendSearch + "%");
		questionMarks2.add(currentUsername);
		questionMarks2.add("%" + chatSearch + "%");
		questionMarks2.add(currentUsername);
		questionMarks2.add(currentUsername);
		questionMarks2.add(currentUsername);

		ResultSet rs2 = dbConn.doPreparedQuery(sql2, questionMarks2);

		String sql3 = "WITH ranked_data AS (\n" +
				"\tSELECT cv.ConversationId, cv.ConversationName, cvmes.MessageId, cvmes.SenderId, cvmem2.MemberId, cvmes.Datetime, \n" +
				"\t\tcvmes.Content, cv.IsGroup,\n" +
				"        ROW_NUMBER() OVER (PARTITION BY cv.ConversationId ORDER BY cvmes.Datetime DESC) AS rn\n" +
				"\tFROM Conversation cv\n" +
				"\tJOIN ConversationMember cvmem ON cv.ConversationId = cvmem.ConversationId\n" +
				"    JOIN ConversationMember cvmem2 ON cv.ConversationId = cvmem2.ConversationId AND cvmem2.MemberId LIKE ? AND cvmem2.MemberId != ?\n" +
				"\tJOIN ConversationMessage cvmes ON cvmes.ConversationId = cvmem.ConversationId\n" +
				"    JOIN conversationmessage cvmes2 ON cvmes2.ConversationId = cvmem.ConversationId AND cvmes2.Content LIKE ?\n" +
				"\tWHERE cvmem.MemberId=?\n" +
				"\tGROUP BY cv.ConversationId, cv.ConversationName, cvmes.MessageId, cvmes.SenderId, cvmem2.MemberId, cvmes2.Content\n" +
				")\n" +
				"SELECT DISTINCT rd.ConversationId, rd.ConversationName, rd.MessageId, rd.MemberId, SenderId, rd.Datetime, Content, IsGroup\n" +
				"FROM ranked_data rd\n" +
				"JOIN User u ON u.Username = rd.MemberId\n" +
				"WHERE u.Status = 1 AND rn = 1 AND IsGroup=false AND EXISTS (\n" +
				"\tSELECT *\n" +
				"    FROM MessageSeen ms\n" +
				"    WHERE ms.MessageId = rd.MessageId AND ms.ConversationId = rd.ConversationId AND ms.SeenId = ?\n" +
				");";

		Vector<Object> questionMarks3 = new Vector<>();
		questionMarks3.add("%" + friendSearch + "%");
		questionMarks3.add(currentUsername);
		questionMarks3.add("%" + chatSearch + "%");
		questionMarks3.add(currentUsername);
		questionMarks3.add(currentUsername);

		ResultSet rs3 = dbConn.doPreparedQuery(sql3, questionMarks3);

		ResultSet rs4;
		String sql4 = "";
		Vector<Object> questionMarks4 = new Vector<>();
		// If the client is search for chat message's content then no Unmessaged friends will be displayed
		if (chatSearch.isEmpty()) {
			sql4 = "SELECT cv.ConversationId, cvmem2.MemberId\n" +
					"FROM Conversation cv\n" +
					"JOIN ConversationMember cvmem ON cv.ConversationId = cvmem.ConversationId\n" +
					"JOIN ConversationMember cvmem2 ON cv.ConversationId = cvmem2.ConversationId\n" +
					"JOIN User u ON u.Username = cvmem2.MemberId\n" +
					"LEFT JOIN ConversationMessage cvmes ON cv.ConversationId = cvmes.ConversationId \n" +
					"WHERE u.Status = 1 AND cv.IsGroup=false AND cvmem.MemberId = ? AND cvmem2.MemberId != ? AND cvmem2.MemberId LIKE ?\n" +
					"GROUP BY cv.ConversationId, cvmem2.MemberId\n" +
					"HAVING COUNT(cvmes.MessageId) = 0;";

			questionMarks4.add(currentUsername);
			questionMarks4.add(currentUsername);
			questionMarks4.add("%" + friendSearch + "%");
		}
		else {
			sql4 = "SELECT * FROM Emptiness;";
		}
		rs4 = dbConn.doPreparedQuery(sql4, questionMarks4);

		return new ResultSet[] {rs1, rs2, rs3, rs4};
	}

	public ResultSet[] getBlockedFriendMessageList(String currentUsername, String friendSearch, String chatSearch) throws SQLException {
		String sql1 = "WITH ranked_data AS (\n" +
				"\tSELECT cv.ConversationId, cv.ConversationName, cvmes.MessageId, cvmes.SenderId, cvmem2.MemberId, cvmes.Datetime, \n" +
				"    cvmes.Content, cv.IsGroup,\n" +
				"        ROW_NUMBER() OVER (PARTITION BY cv.ConversationId ORDER BY cvmes.Datetime DESC) AS rn\n" +
				"\tFROM Conversation cv\n" +
				"\tJOIN ConversationMember cvmem ON cv.ConversationId = cvmem.ConversationId\n" +
				"    JOIN ConversationMember cvmem2 ON cv.ConversationId = cvmem2.ConversationId AND cvmem2.MemberId LIKE ? AND cvmem2.MemberId != ?\n" +
				"\tJOIN ConversationMessage cvmes ON cvmes.ConversationId = cvmem.ConversationId\n" +
				"    JOIN conversationmessage cvmes2 ON cvmes2.ConversationId = cvmem.ConversationId AND cvmes2.Content LIKE ?\n" +
				"\tWHERE cvmem.MemberId=?\n" +
				"\tGROUP BY cv.ConversationId, cv.ConversationName, cvmes.MessageId, cvmes.SenderId, cvmem2.MemberId\n" +
				")\n" +
				"SELECT DISTINCT rd.ConversationId, rd.ConversationName, rd.MessageId, rd.MemberId, SenderId, rd.Datetime, Content, IsGroup\n" +
				"FROM ranked_data rd\n" +
				"WHERE rn = 1 AND IsGroup=false AND NOT EXISTS (\n" +
				"\tSELECT *\n" +
				"    FROM MessageSeen ms\n" +
				"    WHERE ms.MessageId = rd.MessageId AND ms.ConversationId = rd.ConversationId AND ms.SeenId = ?\n" +
				") AND rd.Datetime >= all(\n" +
				"\tSELECT ul.Datetime\n" +
				"    FROM UserLog ul\n" +
				"    WHERE ul.UserId = ? AND ul.LogType = 0\n" +
				") AND EXISTS (\n" +
				"\tSELECT *\n" +
				"    FROM Friend f\n" +
				"    WHERE (f.UserId = rd.SenderId AND f.FriendId = rd.MemberId AND f.Status = 1) OR (f.UserId = rd.MemberId AND f.FriendId = rd.SenderId AND f.Status = 2)\n" +
				");";

		Vector<Object> questionMarks1 = new Vector<>();
		questionMarks1.add("%" + friendSearch + "%");
		questionMarks1.add(currentUsername);
		questionMarks1.add("%" + chatSearch + "%");
		questionMarks1.add(currentUsername);
		questionMarks1.add(currentUsername);
		questionMarks1.add(currentUsername);

		ResultSet rs1 = dbConn.doPreparedQuery(sql1, questionMarks1);

		String sql2 = "WITH ranked_data AS (\n" +
				"\tSELECT cv.ConversationId, cv.ConversationName, cvmes.MessageId, cvmes.SenderId, cvmem2.MemberId, cvmes.Datetime, \n" +
				"\t\tcvmes.Content, cv.IsGroup,\n" +
				"        ROW_NUMBER() OVER (PARTITION BY cv.ConversationId ORDER BY cvmes.Datetime DESC) AS rn\n" +
				"\tFROM Conversation cv\n" +
				"\tJOIN ConversationMember cvmem ON cv.ConversationId = cvmem.ConversationId\n" +
				"    JOIN ConversationMember cvmem2 ON cv.ConversationId = cvmem2.ConversationId AND cvmem2.MemberId LIKE ? AND cvmem2.MemberId != ?\n" +
				"\tJOIN ConversationMessage cvmes ON cvmes.ConversationId = cvmem.ConversationId\n" +
				"    JOIN conversationmessage cvmes2 ON cvmes2.ConversationId = cvmem.ConversationId AND cvmes2.Content LIKE ?\n" +
				"\tWHERE cvmem.MemberId=?\n" +
				"\tGROUP BY cv.ConversationId, cv.ConversationName, cvmes.MessageId, cvmes.SenderId, cvmem2.MemberId\n" +
				")\n" +
				"SELECT DISTINCT rd.ConversationId, rd.ConversationName, rd.MessageId, rd.MemberId, SenderId, rd.Datetime, Content, IsGroup\n" +
				"FROM ranked_data rd\n" +
				"WHERE rn = 1 AND IsGroup=false AND NOT EXISTS (\n" +
				"\tSELECT *\n" +
				"    FROM MessageSeen ms\n" +
				"    WHERE ms.MessageId = rd.MessageId AND ms.ConversationId = rd.ConversationId AND ms.SeenId = ?\n" +
				") AND rd.Datetime < all(\n" +
				"\tSELECT ul.Datetime\n" +
				"    FROM UserLog ul\n" +
				"    WHERE ul.UserId = ? AND ul.LogType = 0\n" +
				") AND EXISTS (\n" +
				"\tSELECT *\n" +
				"    FROM Friend f\n" +
				"    WHERE (f.UserId = rd.SenderId AND f.FriendId = rd.MemberId AND f.Status = 1) OR (f.UserId = rd.MemberId AND f.FriendId = rd.SenderId AND f.Status = 2)\n" +
				");";

		Vector<Object> questionMarks2 = new Vector<>();
		questionMarks2.add("%" + friendSearch + "%");
		questionMarks2.add(currentUsername);
		questionMarks2.add("%" + chatSearch + "%");
		questionMarks2.add(currentUsername);
		questionMarks2.add(currentUsername);
		questionMarks2.add(currentUsername);

		ResultSet rs2 = dbConn.doPreparedQuery(sql2, questionMarks2);

		String sql3 = "WITH ranked_data AS (\n" +
				"\tSELECT cv.ConversationId, cv.ConversationName, cvmes.MessageId, cvmes.SenderId, cvmem2.MemberId, cvmes.Datetime, \n" +
				"\t\tcvmes.Content, cv.IsGroup,\n" +
				"        ROW_NUMBER() OVER (PARTITION BY cv.ConversationId ORDER BY cvmes.Datetime DESC) AS rn\n" +
				"\tFROM Conversation cv\n" +
				"\tJOIN ConversationMember cvmem ON cv.ConversationId = cvmem.ConversationId\n" +
				"    JOIN ConversationMember cvmem2 ON cv.ConversationId = cvmem2.ConversationId AND cvmem2.MemberId LIKE ? AND cvmem2.MemberId != ?\n" +
				"\tJOIN ConversationMessage cvmes ON cvmes.ConversationId = cvmem.ConversationId\n" +
				"    JOIN conversationmessage cvmes2 ON cvmes2.ConversationId = cvmem.ConversationId AND cvmes2.Content LIKE ?\n" +
				"\tWHERE cvmem.MemberId=?\n" +
				"\tGROUP BY cv.ConversationId, cv.ConversationName, cvmes.MessageId, cvmes.SenderId, cvmem2.MemberId, cvmes2.Content\n" +
				")\n" +
				"SELECT DISTINCT rd.ConversationId, rd.ConversationName, rd.MessageId, rd.MemberId, SenderId, rd.Datetime, Content, IsGroup\n" +
				"FROM ranked_data rd\n" +
				"WHERE rn = 1 AND IsGroup=false AND EXISTS (\n" +
				"\tSELECT *\n" +
				"    FROM MessageSeen ms\n" +
				"    WHERE ms.MessageId = rd.MessageId AND ms.ConversationId = rd.ConversationId AND ms.SeenId = ?\n" +
				") AND EXISTS (\n" +
				"\tSELECT *\n" +
				"    FROM Friend f\n" +
				"    WHERE (f.UserId = rd.SenderId AND f.FriendId = rd.MemberId AND f.Status = 1) OR (f.UserId = rd.MemberId AND f.FriendId = rd.SenderId AND f.Status = 2)\n" +
				");";

		Vector<Object> questionMarks3 = new Vector<>();
		questionMarks3.add("%" + friendSearch + "%");
		questionMarks3.add(currentUsername);
		questionMarks3.add("%" + chatSearch + "%");
		questionMarks3.add(currentUsername);
		questionMarks3.add(currentUsername);

		ResultSet rs3 = dbConn.doPreparedQuery(sql3, questionMarks3);

		ResultSet rs4;
		String sql4 = "";
		Vector<Object> questionMarks4 = new Vector<>();
		// If the client is search for chat message's content then no Unmessaged friends will be displayed
		if (chatSearch.isEmpty()) {
			sql4 = "SELECT cv.ConversationId, cvmem2.MemberId\n" +
					"FROM Conversation cv\n" +
					"JOIN ConversationMember cvmem ON cv.ConversationId = cvmem.ConversationId\n" +
					"JOIN ConversationMember cvmem2 ON cv.ConversationId = cvmem2.ConversationId\n" +
					"LEFT JOIN ConversationMessage cvmes ON cv.ConversationId = cvmes.ConversationId \n" +
					"WHERE cv.IsGroup=false AND cvmem.MemberId = ? AND cvmem2.MemberId != ? AND cvmem2.MemberId LIKE ? AND EXISTS (\n" +
					"\tSELECT *\n" +
					"    FROM Friend f\n" +
					"    WHERE (f.UserId = cvmem.MemberId AND f.FriendId = cvmem2.MemberId AND f.Status = 1) OR (f.UserId = cvmem2.MemberId AND f.FriendId = cvmem.MemberId AND f.Status = 2))\n" +
					"GROUP BY cv.ConversationId, cvmem2.MemberId\n" +
					"HAVING COUNT(cvmes.MessageId) = 0;";

			questionMarks4.add(currentUsername);
			questionMarks4.add(currentUsername);
			questionMarks4.add("%" + friendSearch + "%");
		}
		else {
			sql4 = "SELECT * FROM Emptiness;";
		}
		rs4 = dbConn.doPreparedQuery(sql4, questionMarks4);

		return new ResultSet[] {rs1, rs2, rs3, rs4};
	}

	public ResultSet getSingleFriendChatLog(String currentUsername, String conversationId) throws SQLException {
		String sql = "SELECT *\n" +
				"FROM ConversationMessage cvmes\n" +
				"JOIN ConversationMember cvmem ON cvmem.ConversationId = cvmes.ConversationId\n" +
				"WHERE cvmem.MemberId=? AND cvmes.ConversationId=?";

		Vector<Object> questionMarks = new Vector<>();
		questionMarks.add(currentUsername);
		questionMarks.add(conversationId);

		return dbConn.doPreparedQuery(sql, questionMarks);
	}

	public void deleteChatHistory(String conversationId) throws SQLException {
		String sql1 = "DELETE FROM MessageSeen WHERE ConversationId=?";
		Vector<Object> questionMarks1 = new Vector<>();
		questionMarks1.add(conversationId);
		dbConn.doPreparedStatement(sql1, questionMarks1);

		String sql2 = "DELETE FROM ConversationMessage WHERE ConversationId=?";
		Vector<Object> questionMarks2 = new Vector<>();
		questionMarks2.add(conversationId);
		dbConn.doPreparedStatement(sql2, questionMarks2);
	}

	public void messageSeen(String currentUsername, String conversationId) throws SQLException {
		String sql1 = "SELECT cvmes.MessageId\n" +
				"FROM ConversationMessage cvmes\n" +
				"WHERE cvmes.ConversationId=? AND NOT EXISTS (\n" +
				"\tSELECT *\n" +
				"    FROM MessageSeen ms\n" +
				"    WHERE ms.ConversationId = cvmes.ConversationId AND cvmes.MessageId = ms.MessageId AND ms.SeenId=?\n" +
				");";

		Vector<Object> questionMarks1 = new Vector<>();
		questionMarks1.add(conversationId);
		questionMarks1.add(currentUsername);

		ResultSet resultSet = dbConn.doPreparedQuery(sql1, questionMarks1);
		ArrayList<String> batchSql = new ArrayList<>();
		ArrayList<Vector<Object>> batchQuestionMarks = new ArrayList<>();
		while (resultSet.next()) {
			int unseenMessageId = resultSet.getInt("MessageId");

			String sql2 = "INSERT INTO MessageSeen VALUES (?, ?, ?)";

			Vector<Object> questionMarks2 = new Vector<>();
			questionMarks2.add(unseenMessageId);
			questionMarks2.add(conversationId);
			questionMarks2.add(currentUsername);

			batchSql.add(sql2);
			batchQuestionMarks.add(questionMarks2);
		}

		dbConn.doBatchPreparedStatement(batchSql.toArray(new String[batchSql.size()]), batchQuestionMarks.toArray(new Vector[batchQuestionMarks.size()]));
	}

	public void renameChat(String conversationId, String conversationName) throws SQLException {
		String sql = "UPDATE Conversation SET ConversationName=? WHERE ConversationId=?";
		Vector<Object> questionMarks = new Vector<>();
		questionMarks.add(conversationName);
		questionMarks.add(conversationId);
		dbConn.doPreparedStatement(sql, questionMarks);
	}

	public ResultSet getNonGroupMember(String searchUsername, String conversationId) throws SQLException {
		String sql = "SELECT Username FROM User u WHERE Username LIKE ? AND NOT EXISTS (SELECT * FROM ConversationMember cvmem WHERE cvmem.ConversationId=? AND u.Username = cvmem.MemberId)";
		Vector<Object> questionMarks = new Vector<>();
		questionMarks.add("%" + searchUsername + "%");
		questionMarks.add(conversationId);
		return dbConn.doPreparedQuery(sql, questionMarks);
	}

	public void addGroupMember(String conversationId, String newMemberId) throws SQLException {
		String sql = "INSERT INTO ConversationMember VALUES (?, ?, false)";
		Vector<Object> questionMarks = new Vector<>();
		questionMarks.add(conversationId);
		questionMarks.add(newMemberId);
		dbConn.doPreparedStatement(sql, questionMarks);
	}

	public void removeGroupMember(String conversationId, String memberId) throws SQLException {
		String sql = "DELETE FROM ConversationMember WHERE ConversationId=? AND MemberId=?";
		Vector<Object> questionMarks = new Vector<>();
		questionMarks.add(conversationId);
		questionMarks.add(memberId);
		dbConn.doPreparedStatement(sql, questionMarks);
	}

	public ResultSet groupMemberList(String conversationId) throws SQLException {
		String sql = "SELECT MemberId, IsAdmin FROM ConversationMember WHERE ConversationId=?";
		Vector<Object> questionMarks = new Vector<>();
		questionMarks.add(conversationId);
		return dbConn.doPreparedQuery(sql, questionMarks);
	}

	public void leaveGroup(String username, String conversationId) throws SQLException {
		String sql = "DELETE FROM ConversationMember WHERE ConversationId=? AND MemberId=?";
		Vector<Object> questionMarks = new Vector<>();
		questionMarks.add(conversationId);
		questionMarks.add(username);
		dbConn.doPreparedStatement(sql, questionMarks);
	}

	public ResultSet getMemberAdminStatus(String currentUsername, String conversationId) throws SQLException {
		String sql = "SELECT IsAdmin FROM ConversationMember WHERE MemberId=? AND ConversationId=?";
		Vector<Object> questionMarks = new Vector<>();
		questionMarks.add(currentUsername);
		questionMarks.add(conversationId);
		return dbConn.doPreparedQuery(sql, questionMarks);
	}

	public void setMemberAdminStatus(String conversationId, String username, boolean isAdmin) throws SQLException {
		String sql = "UPDATE ConversationMember SET IsAdmin=? WHERE ConversationId=? AND MemberId=?";
		Vector<Object> questionMarks = new Vector<>();
		questionMarks.add(isAdmin);
		questionMarks.add(conversationId);
		questionMarks.add(username);
		dbConn.doPreparedStatement(sql, questionMarks);
	}

	public void sendMessage(String currentUsername, String conversationId, String content) throws SQLException {
		String sql1 = "SELECT COUNT(cvmes.MessageId) as MessageCount\n" +
				"FROM ConversationMessage cvmes\n" +
				"WHERE cvmes.ConversationId=?" +
				"GROUP BY cvmes.ConversationId;";

		Vector<Object> questionMarks1 = new Vector<>();
		questionMarks1.add(conversationId);

		ResultSet resultSet1 = dbConn.doPreparedQuery(sql1, questionMarks1);
		int messageCount = 0;
		if (resultSet1.next()) messageCount = resultSet1.getInt(1);
		resultSet1.close();
		System.out.println(messageCount);
		String sql2 = "INSERT INTO ConversationMessage VALUES (?, ?, ?, current_timestamp(), ?, ?)";
		Vector<Object> questionMarks2 = new Vector<>();
		questionMarks2.add(messageCount + 1);
		questionMarks2.add(conversationId);
		questionMarks2.add(currentUsername);
		questionMarks2.add(content);
		questionMarks2.add("");

		String sql3 = "INSERT INTO MessageSeen VALUES (?, ?, ?)";
		Vector<Object> questionMarks3 = new Vector<>();
		questionMarks3.add(messageCount + 1);
		questionMarks3.add(conversationId);
		questionMarks3.add(currentUsername);

		dbConn.doBatchPreparedStatement(new String[]{sql2, sql3}, new Vector[]{questionMarks2, questionMarks3});
	}

	public ResultSet getFieldUserList(String field, String year) throws SQLException {
		if (year.isEmpty()) {
			String sql = "SELECT " + field + " FROM User";
			Vector<Object> questionMarks = new Vector<>();
			return dbConn.doPreparedQuery(sql, questionMarks);
		} else {
			String sql = "SELECT " + field + " FROM User where year(CreationDate) = ?";
			Vector<Object> questionMarks = new Vector<>();
			questionMarks.add(year);
			return dbConn.doPreparedQuery(sql, questionMarks);
		}
	}

	public ResultSet determineIsBlocked(String blocker, String blockedUser) throws SQLException {
		String sql = "Select status from Friend where (UserId = ? and FriendId = ?) or (UserId = ? and FriendId = ?)";
		Vector<Object> questionMarks = new Vector<>();
		questionMarks.add(blocker);
		questionMarks.add(blockedUser);
		questionMarks.add(blockedUser);
		questionMarks.add(blocker);

		return dbConn.doPreparedQuery(sql, questionMarks);
	}
	public void UserBlockUser(String blocker, String blockedUser) throws SQLException {
		int blockStatus = 1;
		int blockedStatus = 2;
		String sql = "Update friend Set Status=? Where UserId=? and FriendId=?;";
		Vector<Object> questionMarks = new Vector<>();
		questionMarks.add(blockStatus);
		questionMarks.add(blocker);
		questionMarks.add(blockedUser);

		dbConn.doPreparedStatement(sql, questionMarks);
		UserBeingBlockedByUser(blocker, blockedUser);
	}

	public void UserBeingBlockedByUser(String blocker, String blockedUser) throws SQLException {
		int blockedStatus = 2;
		String sql = "Update friend Set Status=? Where UserId=? and FriendId=?;";
		Vector<Object> questionMarks = new Vector<>();
		questionMarks.add(blockedStatus);
		questionMarks.add(blockedUser);
		questionMarks.add(blocker);

		dbConn.doPreparedStatement(sql, questionMarks);
	}
	public void UserUnBlockUser(String blocker, String blockedUser) throws SQLException {
		int normarlStatus = 0;
		String sql = "Update hooyah.friend Set Status = ? Where UserId= ? and FriendId= ?";
		Vector<Object> questionMarks = new Vector<>();
		questionMarks.add(normarlStatus);
		questionMarks.add(blocker);
		questionMarks.add(blockedUser);

		dbConn.doPreparedStatement(sql, questionMarks);
		UnBlockUserBeingBlockedByUser(blocker, blockedUser);
	}

	public void UnBlockUserBeingBlockedByUser(String blocker, String blockedUser) throws SQLException {
		int normarlStatus = 0;
		String sql = "Update hooyah.friend Set Status = ? Where UserId= ? and FriendId= ?";
		Vector<Object> questionMarks = new Vector<>();
		questionMarks.add(normarlStatus);
		questionMarks.add(blockedUser);
		questionMarks.add(blocker);

		dbConn.doPreparedStatement(sql, questionMarks);
	}

	public ResultSet getSuitableConversationId(String memId, String dateTime) throws SQLException {
		String sql = "Select ConversationId From conversationmember cMsg Where cMsg.MemberId = ? and cMsg.ConversationId = (\n" +
				"\tSelect cM.ConversationId From conversationmessage cM Where year( cM.Datetime ) = ? and cM.ConversationId = cMsg.ConversationId \n" +
				"\t\tORDER BY Datetime DESC LIMIT 1\n" +
				")";
		Vector<Object> questionMarks = new Vector<>();
		questionMarks.add(memId);
		questionMarks.add(dateTime);

        return dbConn.doPreparedQuery(sql, questionMarks);
    }

	public ResultSet getInfoConversationUserParticipateIn(String conversationId) throws SQLException {
		String sql = "Select IsGroup From conversation Where ConversationId = ?";
		Vector<Object> questionMarks = new Vector<>();
		questionMarks.add(conversationId);

		return dbConn.doPreparedQuery(sql, questionMarks);
	}

	public ResultSet getLogInUserIdListWithDateTime(String dateTime) throws SQLException {
		String sql = "Select UserId From userlog Where year(Datetime) = ? and LogType = 0;";
		Vector<Object> questionMarks = new Vector<>();
		questionMarks.add(dateTime);

		return dbConn.doPreparedQuery(sql, questionMarks);
	}

	public ResultSet getUserListInfo() throws SQLException {
		String sql = "SELECT Username, CONCAT_ws(' ',FirstName, LastName) as Hoten, Address, DoB, Gender, Email, CreationDate, Status FROM User;";
		Vector<Object> questionMarks = new Vector<>();

		return dbConn.doPreparedQuery(sql, questionMarks);
	}


	public ResultSet getUserLogListWithDetailInfo() throws SQLException {
		String sql = "Select UserId, concat_ws(' ', FirstName, LastName) as Hoten, Datetime From userlog  Left Join user On userlog.UserId = user.Username Order By Datetime Asc ";
		Vector<Object> questionMarks = new Vector<>();
//		questionMarks.add(dateTime);

		return dbConn.doPreparedQuery(sql, questionMarks);
	}

	public ResultSet getOnlineDateFromUserLog(String year, String month) throws SQLException {
		if (year.isEmpty()) {
			String sql = "Select  Datetime From Userlog  Left Join user On Userlog.UserId = user.Username Order By Datetime Asc ";
			Vector<Object> questionMarks = new Vector<>();

			return dbConn.doPreparedQuery(sql, questionMarks);
		} else {
			if (month.isEmpty()) {
				String sql = "Select Datetime From Userlog Left Join user On Userlog.UserId = user.Username Where year(Datetime) = ? Order By Datetime Asc ";
				Vector<Object> questionMarks = new Vector<>();
				questionMarks.add(year);
				return dbConn.doPreparedQuery(sql, questionMarks);
			} else {
				String sql = "Select Datetime From Userlog Left Join user On Userlog.UserId = user.Username Where year(Datetime) = ? and month(Datetime) = ? Order By Datetime Asc ";
				Vector<Object> questionMarks = new Vector<>();
				questionMarks.add(year);
				questionMarks.add(month);

				return dbConn.doPreparedQuery(sql, questionMarks);
			}
		}

	}

	public ResultSet getNewRegisterInfo(String userId) throws SQLException {
		String sql = "Select UserId, concat_ws(' ', FirstName, LastName) as Hoten, Datetime From hooyah.userlog  Left Join hooyah.user On userlog.UserId = user.Username \n" +
				"Where userlog.UserId = ? and LogType = '0' Order By DateTime ASC Limit 1";
		Vector<Object> questionMarks = new Vector<>();
		questionMarks.add(userId);

		return dbConn.doPreparedQuery(sql, questionMarks);
	}

	public ResultSet getConversationInfo() throws SQLException {
		String sql = "SELECT * FROM conversation;";
		Vector<Object> questionMarks = new Vector<>();
//		questionMarks.add(dateTime);

		return dbConn.doPreparedQuery(sql, questionMarks);
	}

	public ResultSet getConversationCreateDateInfo(String conId) throws SQLException {
		String sql = "Select DateTime From hooyah.conversationlog Where LogType = 0 and ConversationId = ?";
		Vector<Object> questionMarks = new Vector<>();
		questionMarks.add(conId);

		return dbConn.doPreparedQuery(sql, questionMarks);
	}


	public ResultSet getConversationMemberInfo(String conversationId) throws SQLException {
		String sql = "Select ConversationId, MemberId, concat_ws(' ', FirstName, LastName) as Hoten, IsAdmin From conversationmember Join user On MemberId = Username\n" +
				"Where ConversationId = ?";
		Vector<Object> questionMarks = new Vector<>();
		questionMarks.add(conversationId);

		return dbConn.doPreparedQuery(sql, questionMarks);
	}

	public ResultSet getConversationMemberId(String conversationId) throws SQLException {
		String sql = "Select MemberId From conversationmember Where ConversationId = ?";
		Vector<Object> questionMarks = new Vector<>();
		questionMarks.add(conversationId);

		return dbConn.doPreparedQuery(sql, questionMarks);
	}

	public ResultSet getSpamReportInfo() throws SQLException {
		String sql = "SELECT * FROM spamreport;";
		Vector<Object> questionMarks = new Vector<>();

		return dbConn.doPreparedQuery(sql, questionMarks);
	}
	public ResultSet getHighestConvId() throws SQLException {
		String sql = "SELECT max(ConversationId) as HighestId FROM Conversation;";
		Vector<Object> questionMarks = new Vector<>();

		return dbConn.doPreparedQuery(sql, questionMarks);
	}
	public void reportUser(String reporterId, String reportedId) throws SQLException {
		String sql = "Insert into spamreport value (?, ?, current_timestamp());";

		Vector<Object> questionMarks = new Vector<>();
		questionMarks.add(reporterId);
		questionMarks.add(reportedId);

		dbConn.doPreparedStatement(sql, questionMarks);
	}
	public void AddMemWhenUareAdmin(String conId,String adminId, String userId) throws SQLException {
		String sql = "Insert into ConversationMember value (?, ?, true), (?, ?, false)";

		Vector<Object> questionMarks = new Vector<>();
		questionMarks.add(conId);
		questionMarks.add(adminId);
		questionMarks.add(conId);
		questionMarks.add(userId);

		dbConn.doPreparedStatement(sql, questionMarks);
	}
	public void UpdateConvLog(String conId, String userId, int logType) throws SQLException {
		String detail = "";
		switch (logType) {
			case 0: {
				detail = "Created";
				break;
			}
			case 1: {
				detail = "Name Changed";
				break;
			}
			case 2: {
				detail = "Member added";
				break;
			}
			case 3: {
				detail = "Member deleted";
				break;
			}
			case 4: {
				detail = "Made admin";
				break;
			}
		}
		String sql = "Insert into Conversationlog value (?, current_timestamp(), ?, ?, ?)";

		Vector<Object> questionMarks = new Vector<>();
		questionMarks.add(conId);
		questionMarks.add(logType);
		questionMarks.add(userId);
		questionMarks.add(detail);

		dbConn.doPreparedStatement(sql, questionMarks);
	}
	public void CreateNewConv(String newConId, String newConName, boolean isGroup) throws SQLException {
		String sql = "Insert into Conversation value (?, ?, ?, false);";

		Vector<Object> questionMarks = new Vector<>();
		questionMarks.add(newConId);
		questionMarks.add(newConName);
		questionMarks.add(isGroup);

		dbConn.doPreparedStatement(sql, questionMarks);
	}
	public void close() {
		dbConn.close();
	}
}
