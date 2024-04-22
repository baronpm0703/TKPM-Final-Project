package com.psvm.server.controllers;

import com.psvm.server.models.DBConnection;
import com.psvm.server.models.DBInteraction;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
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


	public ResultSet getFieldUserList(String field) throws SQLException {
		String sql = "SELECT " + field + " FROM User";
		Vector<Object> questionMarks = new Vector<>();

		return dbConn.doPreparedQuery(sql, questionMarks);
	}

	public ResultSet getSuitableConversationId(String memId, String dateTime) throws SQLException {
		String sql = "Select ConversationId From hooyah.conversationmember cMsg Where cMsg.MemberId = ? and cMsg.ConversationId = (\n" +
				"\tSelect cM.ConversationId From hooyah.conversationmessage cM Where year( cM.Datetime ) = ? and cM.ConversationId = cMsg.ConversationId \n" +
				"\t\tORDER BY Datetime DESC LIMIT 1\n" +
				")";
		Vector<Object> questionMarks = new Vector<>();
		questionMarks.add(memId);
		questionMarks.add(dateTime);

        return dbConn.doPreparedQuery(sql, questionMarks);
    }

	public ResultSet getInfoConversationUserParticipateIn(String conversationId) throws SQLException {
		String sql = "Select IsGroup From hooyah.conversation Where ConversationId = ?";
		Vector<Object> questionMarks = new Vector<>();
		questionMarks.add(conversationId);

		return dbConn.doPreparedQuery(sql, questionMarks);
	}

	public ResultSet getLogInUserIdListWithDateTime(String dateTime) throws SQLException {
		String sql = "Select UserId From hooyah.userlog Where year(Datetime) = ? and LogType = 0;";
		Vector<Object> questionMarks = new Vector<>();
		questionMarks.add(dateTime);

		return dbConn.doPreparedQuery(sql, questionMarks);
	}

	public ResultSet getUserListInfo() throws SQLException {
		String sql = "SELECT Username, CONCAT_ws(\" \",FirstName, LastName) as Hoten, Address, DoB, Gender, Email FROM hooyah.user;";
		Vector<Object> questionMarks = new Vector<>();
//		questionMarks.add(dateTime);

		return dbConn.doPreparedQuery(sql, questionMarks);
	}

	public ResultSet getUserLogListWithDetailInfo() throws SQLException {
		String sql = "Select UserId, concat_ws(FirstName, LastName) as Hoten, Datetime From hooyah.userlog  Join hooyah.user On hooyah.userlog.UserId = hooyah.user.Username";
		Vector<Object> questionMarks = new Vector<>();
//		questionMarks.add(dateTime);

		return dbConn.doPreparedQuery(sql, questionMarks);
	}

	public ResultSet getConversationInfo() throws SQLException {
		String sql = "SELECT * FROM hooyah.conversation;";
		Vector<Object> questionMarks = new Vector<>();
//		questionMarks.add(dateTime);

		return dbConn.doPreparedQuery(sql, questionMarks);
	}

	public ResultSet getConversationMemberInfo(String conversationId) throws SQLException {
		String sql = "Select ConversationId, MemberId, concat_ws(' ', FirstName, LastName) as Hoten, IsAdmin From hooyah.conversationmember Join hooyah.user On MemberId = Username\n" +
				"Where ConversationId = ?";
		Vector<Object> questionMarks = new Vector<>();
		questionMarks.add(conversationId);

		return dbConn.doPreparedQuery(sql, questionMarks);
	}
	public void close() {
		dbConn.close();
	}
}
