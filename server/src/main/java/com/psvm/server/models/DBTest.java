package com.psvm.server.models;

import com.psvm.server.models.objects.*;

import java.sql.*;
import java.util.ArrayList;
import java.util.Map;

class DBThread extends Thread {
	DBInteraction dbConn;
	DBThread(String name) {
		super(name);
		start();
	}

	@Override
	public void run() {
		super.run();
		try {
			dbConn = new DBInteraction(DBConnection.getConnection());
			dbConn.getConnectionId();
			DBConversationMember test = new DBConversationMember();
			test.setColumnValues("CV000002", "b", true);
//			dbConn.doDelete("ConversationMember",
//					Map.of(
//							"MemberId", "2men"
//					));
			dbConn.doInsert("ConversationMember", new DBConversationMember[] {test}, false);
//			ArrayList<DBObject> queryResult = dbConn.doQuery("ConversationMember",
//					new String[] {"MemberId"},
//					Map.of(
//						"IsAdmin", 1
//					)
//			);
//			System.out.println(queryResult.get(0).getAllColumns());
		}
		catch (SQLException exc) {
		}
		finally {
			dbConn.close();		// Return connection to connection pool
		}
	}
}

public class DBTest {
	public static void main(String[] args) {
//		DBConnection dbc = new DBConnection("Thread #1");
//		DBQuery dbq = new DBQuery(dbc.getDatabase());
//		dbq.printTable("ConversationMessage");
//		HooYahDB db = new HooYahDB("Thread #1");
		DBThread thread = new DBThread("Thread 1");
		DBThread thread2 = new DBThread("Thread 2");
		DBThread thread3 = new DBThread("Thread 3");
		DBThread thread4 = new DBThread("Thread 4");
	}
}
