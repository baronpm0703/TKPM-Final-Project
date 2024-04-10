package com.psvm.server.models;

import java.sql.*;

class DBThread extends Thread {
	DBQuery dbConn;
	DBThread(String name) {
		super(name);
		start();
	}

	@Override
	public void run() {
		super.run();
		try {
			dbConn = new DBQuery(DBConnection.getConnection());
			dbConn.doInsert("ConversationMember", new Object[] {"CV000001", "concac", 1});
			System.out.println(getName());
		}
		catch (SQLException exc) {

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
