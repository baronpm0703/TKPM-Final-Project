package com.psvm.server.controllers;

import com.mysql.cj.xdevapi.Client;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Vector;

public class ServerLaunch {
	final static int SERVER_PORT = 5555;

	Vector<ClientHandler> clients = new Vector<>();

	public static void main(String[] args) {
		new ServerLaunch().startServer();
	}

	public void startServer() {
		try (ServerSocket serverSocket = new ServerSocket(SERVER_PORT)) {
			System.out.println("Server started. Waiting for clients...");

			while (true) {
				Socket clientSocket = serverSocket.accept();
				System.out.println("Client connected: " + clientSocket);

				ClientHandler clientHandler = new ClientHandler(clientSocket, this);
				clients.add(clientHandler);
				new Thread(clientHandler).start();
			}
		} catch (IOException e) {
			System.out.println("Error while starting server in " + this.getClass().getSimpleName());
		}
	}

	public void removeClient(ClientHandler clientHandler) {
		clients.remove(clientHandler);
	}
}
