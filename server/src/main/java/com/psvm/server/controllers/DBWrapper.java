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

	public ResultSet[] getFriendMessageList(String currentUsername) throws SQLException {
		String sql1 = "WITH ranked_data AS (\n" +
				"\tSELECT cv.ConversationId, cv.ConversationName, cvmes.MessageId, cvmes.SenderId, cvmem2.MemberId, cvmes.Datetime, cvmes.Content,\n" +
				"\t\tCOUNT(cvmem3.MemberId) as MemberCount,\n" +
				"        ROW_NUMBER() OVER (PARTITION BY cv.ConversationId ORDER BY cvmes.Datetime DESC) AS rn\n" +
				"\tFROM Conversation cv\n" +
				"\tJOIN ConversationMember cvmem ON cv.ConversationId = cvmem.ConversationId\n" +
				"    JOIN ConversationMember cvmem2 ON cv.ConversationId = cvmem2.ConversationId AND cvmem2.MemberId != ?\n" +
				"    JOIN ConversationMember cvmem3 ON cv.ConversationId = cvmem3.ConversationId\n" +
				"\tJOIN ConversationMessage cvmes ON cvmes.ConversationId = cvmem.ConversationId\n" +
				"\tWHERE cvmem.MemberId=?\n" +
				"\tGROUP BY cv.ConversationId, cv.ConversationName, cvmes.MessageId, cvmes.SenderId, cvmem2.MemberId\n" +
				")\n" +
				"SELECT DISTINCT rd.ConversationId, rd.ConversationName, rd.MessageId, rd.MemberId, SenderId, rd.Datetime, Content, MemberCount\n" +
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
		questionMarks1.add(currentUsername);
		questionMarks1.add(currentUsername);
		questionMarks1.add(currentUsername);
		questionMarks1.add(currentUsername);

		ResultSet rs1 = dbConn.doPreparedQuery(sql1, questionMarks1);

		String sql2 = "WITH ranked_data AS (\n" +
				"\tSELECT cv.ConversationId, cv.ConversationName, cvmes.MessageId, cvmes.SenderId, cvmem2.MemberId, cvmes.Datetime, cvmes.Content,\n" +
				"\t\tCOUNT(cvmem3.MemberId) as MemberCount,\n" +
				"        ROW_NUMBER() OVER (PARTITION BY cv.ConversationId ORDER BY cvmes.Datetime DESC) AS rn\n" +
				"\tFROM Conversation cv\n" +
				"\tJOIN ConversationMember cvmem ON cv.ConversationId = cvmem.ConversationId\n" +
				"    JOIN ConversationMember cvmem2 ON cv.ConversationId = cvmem2.ConversationId AND cvmem2.MemberId != ?\n" +
				"    JOIN ConversationMember cvmem3 ON cv.ConversationId = cvmem3.ConversationId\n" +
				"\tJOIN ConversationMessage cvmes ON cvmes.ConversationId = cvmem.ConversationId\n" +
				"\tWHERE cvmem.MemberId=?\n" +
				"\tGROUP BY cv.ConversationId, cv.ConversationName, cvmes.MessageId, cvmes.SenderId, cvmem2.MemberId\n" +
				")\n" +
				"SELECT DISTINCT rd.ConversationId, rd.ConversationName, rd.MessageId, rd.MemberId, SenderId, rd.Datetime, Content, MemberCount\n" +
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
		questionMarks2.add(currentUsername);
		questionMarks2.add(currentUsername);
		questionMarks2.add(currentUsername);
		questionMarks2.add(currentUsername);

		ResultSet rs2 = dbConn.doPreparedQuery(sql2, questionMarks2);

		String sql3 = "WITH ranked_data AS (\n" +
				"\tSELECT cv.ConversationId, cv.ConversationName, cvmes.MessageId, cvmes.SenderId, cvmem2.MemberId, cvmes.Datetime, cvmes.Content,\n" +
				"\t\tCOUNT(cvmem3.MemberId) as MemberCount,\n" +
				"        ROW_NUMBER() OVER (PARTITION BY cv.ConversationId ORDER BY cvmes.Datetime DESC) AS rn\n" +
				"\tFROM Conversation cv\n" +
				"\tJOIN ConversationMember cvmem ON cv.ConversationId = cvmem.ConversationId\n" +
				"    JOIN ConversationMember cvmem2 ON cv.ConversationId = cvmem2.ConversationId AND cvmem2.MemberId != ?\n" +
				"    JOIN ConversationMember cvmem3 ON cv.ConversationId = cvmem3.ConversationId\n" +
				"\tJOIN ConversationMessage cvmes ON cvmes.ConversationId = cvmem.ConversationId\n" +
				"\tWHERE cvmem.MemberId=?\n" +
				"\tGROUP BY cv.ConversationId, cv.ConversationName, cvmes.MessageId, cvmes.SenderId, cvmem2.MemberId\n" +
				")\n" +
				"SELECT DISTINCT rd.ConversationId, rd.ConversationName, rd.MessageId, rd.MemberId, SenderId, rd.Datetime, Content, MemberCount\n" +
				"FROM ranked_data rd\n" +
				"WHERE rn = 1 AND EXISTS (\n" +
				"\tSELECT *\n" +
				"    FROM MessageSeen ms\n" +
				"    WHERE ms.MessageId = rd.MessageId AND ms.ConversationId = rd.ConversationId AND ms.SeenId = ?\n" +
				");";

		Vector<Object> questionMarks3 = new Vector<>();
		questionMarks3.add(currentUsername);
		questionMarks3.add(currentUsername);
		questionMarks3.add(currentUsername);

		ResultSet rs3 = dbConn.doPreparedQuery(sql3, questionMarks3);

		return new ResultSet[] {rs1, rs2, rs3};
	}


	public ResultSet getFieldUserList(String field) throws SQLException {
		String sql = "SELECT " + field + " FROM User";
		Vector<Object> questionMarks = new Vector<>();

		return dbConn.doPreparedQuery(sql, questionMarks);
	}
	public void close() {
		dbConn.close();
	}
}
