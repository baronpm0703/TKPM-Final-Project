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

	DBWrapper() {
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

	public ResultSet getFriendMessageList(String currentUsername) throws SQLException {
		String sql = "WITH ranked_data AS (\n" +
				"\tSELECT cv.ConversationId, cv.ConversationName, cvmes.SenderId, cvmes.Datetime, cvmes.Content,\n" +
				"\t\tROW_NUMBER() OVER (PARTITION BY cv.ConversationId ORDER BY cvmes.Datetime DESC) AS rn\n" +
				"\tFROM Conversation cv\n" +
				"\tJOIN ConversationMember cvmem ON cv.ConversationId = cvmem.ConversationId\n" +
				"\tJOIN ConversationMessage cvmes ON cvmes.ConversationId = cvmem.ConversationId\n" +
				"\tWHERE cvmem.MemberId=?\n" +
				")\n" +
				"SELECT ConversationId, ConversationName, SenderId, Datetime, Content\n" +
				"FROM ranked_data\n" +
				"WHERE rn = 1;";

		Vector<Object> questionMarks = new Vector<>();
		questionMarks.add(currentUsername);

		return dbConn.doPreparedQuery(sql, questionMarks);
	}

	public void close() {
		dbConn.close();
	}
}
