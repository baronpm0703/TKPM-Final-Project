package com.psvm.client.views.components;

import com.psvm.client.controllers.FriendListRequest;
import com.psvm.shared.socket.SocketResponse;

import javax.swing.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.List;
import java.util.Random;
import java.util.Vector;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

class FriendListThread extends SwingWorker<Void, Vector<String>> {
	private Socket clientSocket;
	ObjectInputStream socketIn;
	ObjectOutputStream socketOut;
	private Observer observer;

	public interface Observer {
		public void workerDidUpdate(Vector<String> message);
	}

	public FriendListThread(Socket clientSocket, ObjectInputStream socketIn, ObjectOutputStream socketOut, Observer observer) {
		this.clientSocket = clientSocket;
		this.socketIn = socketIn;
		this.socketOut = socketOut;
		this.observer = observer;

	}

	@Override
	protected Void doInBackground() throws Exception {
		FriendListRequest request = new FriendListRequest(clientSocket, socketIn, socketOut, "Highman");
		SocketResponse response = request.talk();

		Vector<String> temp = new Vector<>();
		temp.add(response.getData().toString());
		publish(temp);

		return null;
	}

	@Override
	protected void process(List<Vector<String>> chunks) {
		super.process(chunks);

		for (Vector<String> message: chunks) {
			observer.workerDidUpdate(message);
		}
	}

	@Override
	protected void done() {
		try {
			get();
			System.out.println("Done!");
		} catch (InterruptedException e) {
			System.out.println("Done!");
			throw new RuntimeException(e);
		} catch (ExecutionException e) {
			throw new RuntimeException(e);
		}
	}
}

public class FriendList extends JList<String> {
	ScheduledExecutorService service = Executors.newScheduledThreadPool(10);

	final String SOCKET_HOST = "localhost";
	final int SOCKET_PORT = 5555;
	Socket socket;
	ObjectInputStream socketIn;
	ObjectOutputStream socketOut;

	public FriendList() {
		try {
			socket = new Socket(SOCKET_HOST, SOCKET_PORT);
			socketIn = new ObjectInputStream(socket.getInputStream());
			socketOut = new ObjectOutputStream(socket.getOutputStream());
			startNextWorker();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	protected void startNextWorker() {
		FriendListThread worker = new FriendListThread(socket, socketIn, socketOut, new FriendListThread.Observer() {
			@Override
			public void workerDidUpdate(Vector<String> message) {
				setListData(message);
			}
		});
		worker.addPropertyChangeListener(new PropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				if (worker.getState() == SwingWorker.StateValue.DONE) {
					worker.removePropertyChangeListener(this);
					startNextWorker();
				}
			}
		});
		service.schedule(worker, 250, TimeUnit.MILLISECONDS);
	}
}
