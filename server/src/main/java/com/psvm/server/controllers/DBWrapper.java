package com.psvm.server.controllers;

import com.psvm.server.models.DBConnection;
import com.psvm.server.models.DBInteraction;

import java.sql.Array;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
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

	public ResultSet getUser(String username, String hashedPassword) throws SQLException {
		String sql = "SELECT COUNT(Username) as UserFound, Status FROM User WHERE Username=? AND Password=?;";

		Vector<Object> questionMarks = new Vector<>();
		questionMarks.add(username);
		questionMarks.add(hashedPassword);

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

	public void BanUser(String bannedUserName) throws SQLException {
		int Status = 2;
		String sql = "Update hooyah.user Set Status = ? Where Username = ?; ";

		Vector<Object> questionMarks = new Vector<>();
		questionMarks.add(Status);
		questionMarks.add(bannedUserName);

		dbConn.doPreparedStatement(sql, questionMarks);
		UpdateUserLogBanType(bannedUserName);
	}

	public void UpdateUserLogBanType(String bannedUserId)  throws SQLException {
		int Status = 2;
		String Detail = "Banned";
		String date =  LocalDate.now().toString();
		String sql = "Insert Into hooyah.userlog (UserId, Datetime, LogType, LogDetail) Value (?,?,?,?)";

		Vector<Object> questionMarks = new Vector<>();
		questionMarks.add(bannedUserId);
		questionMarks.add(date);
		questionMarks.add(Status);
		questionMarks.add(Detail);

		dbConn.doPreparedStatement(sql, questionMarks);
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
			sql4 = "SELECT cv.ConversationId, cvmem.MemberId\n" +
					"FROM Conversation cv\n" +
					"JOIN ConversationMember cvmem ON cv.ConversationId = cvmem.ConversationId\n" +
					"LEFT JOIN ConversationMessage cvmes ON cv.ConversationId = cvmes.ConversationId \n" +
					"WHERE cv.IsGroup=false AND cvmem.MemberId != ? AND cvmem.MemberId LIKE ?\n" +
					"GROUP BY cv.ConversationId, cvmem.MemberId\n" +
					"HAVING COUNT(cvmes.MessageId) = 0;";

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
			sql4 = "SELECT cv.ConversationId, cvmem.MemberId\n" +
					"FROM Conversation cv\n" +
					"JOIN ConversationMember cvmem ON cv.ConversationId = cvmem.ConversationId\n" +
					"JOIN User u ON u.Username = rd.MemberId\n" +
					"LEFT JOIN ConversationMessage cvmes ON cv.ConversationId = cvmes.ConversationId \n" +
					"WHERE u.Status = 1 AND cv.IsGroup=false AND cvmem.MemberId != ? AND cvmem.MemberId LIKE ?\n" +
					"GROUP BY cv.ConversationId, cvmem.MemberId\n" +
					"HAVING COUNT(cvmes.MessageId) = 0;";

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
			sql4 = "SELECT cv.ConversationId, cvmem.MemberId\n" +
					"FROM Conversation cv\n" +
					"JOIN ConversationMember cvmem ON cv.ConversationId = cvmem.ConversationId\n" +
					"LEFT JOIN ConversationMessage cvmes ON cv.ConversationId = cvmes.ConversationId \n" +
					"WHERE cv.IsGroup=false AND cvmem.MemberId != ? AND cvmem.MemberId LIKE ? AND EXISTS (\n" +
					"\tSELECT *\n" +
					"    FROM Friend f\n" +
					"    WHERE (f.UserId = ? AND f.FriendId = cvmem.MemberId AND f.Status = 1) OR (f.UserId = cvmem.MemberId AND f.FriendId = ? AND f.Status = 2))\n" +
					"GROUP BY cv.ConversationId, cvmem.MemberId\n" +
					"HAVING COUNT(cvmes.MessageId) = 0;";

			questionMarks4.add(currentUsername);
			questionMarks4.add("%" + friendSearch + "%");
			questionMarks4.add(currentUsername);
			questionMarks4.add(currentUsername);
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

	public ResultSet getFieldUserList(String field) throws SQLException {
		String sql = "SELECT " + field + " FROM User";
		Vector<Object> questionMarks = new Vector<>();

		return dbConn.doPreparedQuery(sql, questionMarks);
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
		String sql = "SELECT Username, CONCAT_ws(' ',FirstName, LastName) as Hoten, Address, DoB, Gender, Email, CreationDate FROM User;";
		Vector<Object> questionMarks = new Vector<>();


		return dbConn.doPreparedQuery(sql, questionMarks);
	}

	public ResultSet getUserLogListWithDetailInfo() throws SQLException {
		String sql = "Select UserId, concat_ws(' ', FirstName, LastName) as Hoten, Datetime From userlog  Left Join user On userlog.UserId = user.Username";
		Vector<Object> questionMarks = new Vector<>();
//		questionMarks.add(dateTime);

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

	public ResultSet getSpamReportInfo() throws SQLException {
		String sql = "SELECT * FROM spamreport;";
		Vector<Object> questionMarks = new Vector<>();

		return dbConn.doPreparedQuery(sql, questionMarks);
	}
	public void close() {
		dbConn.close();
	}
}
