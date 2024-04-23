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
		System.out.println(username);
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

	public ResultSet getUser(String username, String hashedPassword) throws SQLException {
		String sql = "SELECT COUNT(Username) as UserFound FROM User WHERE Username=? AND Password=? GROUP BY Username;";

		Vector<Object> questionMarks = new Vector<>();
		questionMarks.add(username);
		questionMarks.add(hashedPassword);

		return dbConn.doPreparedQuery(sql, questionMarks);
	}

	public ResultSet searchUser(String currentUsername, String otherUsername) throws SQLException {
		String sql = "SELECT u.Username, fq.TargetId\n" +
				"FROM User u\n" +
				"LEFT JOIN FriendRequest fq ON fq.SenderId = ? AND fq.TargetId = u.Username\n" +
				"WHERE u.Username LIKE ? AND u.Username!=?;";

		Vector<Object> questionMarks = new Vector<>();
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

	public void respondFriendRequest(String currentUsername, String senderId) throws SQLException {
		// Remove first sql line (before first semicolon) when done testing
		String sql1 = "INSERT INTO FriendRequest (SenderId, TargetId, Datetime) VALUES (?, ?, current_timestamp());";
		String sql2 = "UPDATE FriendRequest SET Status=1 WHERE SenderId=? AND TargetId=? ORDER BY Datetime DESC LIMIT 1;";
		String[] sqls = {sql1, sql2};

		Vector<Object> questionMark1 = new Vector<>();
		questionMark1.add(senderId);
		questionMark1.add(currentUsername);
		Vector<Object> questionMark2 = new Vector<>();
		questionMark2.add(senderId);
		questionMark2.add(currentUsername);
		Vector<Object>[] questionMarks = new Vector[] {questionMark1, questionMark2};

		dbConn.doBatchPreparedStatement(sqls, questionMarks);
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

	public ResultSet[] getFriendMessageList(String currentUsername, String friendSearch, String chatSearch) throws SQLException {
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
				"WHERE rn = 1 AND NOT EXISTS (\n" +
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
				"WHERE rn = 1 AND NOT EXISTS (\n" +
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
				"WHERE rn = 1 AND EXISTS (\n" +
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
			sql4 = "SELECT f.UserId, f.FriendId\n" +
					"FROM Friend f\n" +
					"WHERE (f.UserId = ? AND f.FriendId LIKE ? OR f.UserId LIKE ? AND f.FriendId = ?) AND NOT EXISTS (\n" +
					"\tSELECT *\n" +
					"    FROM Conversation cv\n" +
					"    JOIN ConversationMember cvmem ON cv.ConversationId = cvmem.ConversationId\n" +
					"    JOIN ConversationMember cvmem2 ON cvmem.ConversationId = cvmem2.ConversationId\n" +
					"    WHERE cvmem.MemberId = f.UserId AND cvmem2.MemberId = f.FriendId AND cvmem.MemberId != cvmem2.MemberId AND cv.IsGroup = false\n" +
					");";

			questionMarks4.add(currentUsername);
			questionMarks4.add("%" + friendSearch + "%");
			questionMarks4.add("%" + friendSearch + "%");
			questionMarks4.add(currentUsername);
		}
		else {
			sql4 = "SELECT * FROM Emptiness;";
		}
		rs4 = dbConn.doPreparedQuery(sql4, questionMarks4);

		return new ResultSet[] {rs1, rs2, rs3, rs4};
	}

	public ResultSet getSingleFriendChatLog(String currentUsername, String conversationId, String memberId) throws SQLException {
		String sql = "SELECT *\n" +
				"FROM ConversationMessage cvmes\n" +
				"JOIN ConversationMember cvmem ON cvmem.ConversationId = cvmes.ConversationId\n" +
				"WHERE cvmem.MemberId=? AND (cvmes.ConversationId=? OR EXISTS (\n" +
				"\tSELECT *\n" +
				"    FROM ConversationMember cvmem2\n" +
				"    JOIN Conversation cv ON cv.ConversationId = cvmem2.ConversationId\n" +
				"    WHERE cvmem.ConversationId = cvmem2.ConversationId AND cvmem2.MemberId=? AND cv.IsGroup=false\n" +
				"));";

		Vector<Object> questionMarks = new Vector<>();
		questionMarks.add(currentUsername);
		questionMarks.add(conversationId);
		questionMarks.add(memberId);

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

	public void sendMessage(String currentUsername, String friendId, String conversationId, String content) throws SQLException {
		String sql1 = "SELECT COUNT(cvmes.MessageId) as MessageCount\n" +
				"FROM ConversationMessage cvmes\n" +
				"WHERE cvmes.ConversationId=? OR EXISTS (\n" +
				"\tSELECT *\n" +
				"    FROM ConversationMember cvmem\n" +
				"    JOIN Conversation cv ON cv.ConversationId = cvmem.ConversationId\n" +
				"    WHERE cvmes.ConversationId = cvmem.ConversationId AND cvmem.MemberId=? AND cv.IsGroup=false\n" +
				")\n" +
				"GROUP BY cvmes.ConversationId;";

		Vector<Object> questionMarks1 = new Vector<>();
		questionMarks1.add(conversationId);
		questionMarks1.add(friendId);

		ResultSet resultSet1 = dbConn.doPreparedQuery(sql1, questionMarks1);
		int messageCount = 0;
		if (resultSet1.next()) messageCount = resultSet1.getInt(1);
		resultSet1.close();

		String sql2 = "INSERT INTO MessageSeen VALUES (?, ?, ?, current_timestamp(), ?, ?)";
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
