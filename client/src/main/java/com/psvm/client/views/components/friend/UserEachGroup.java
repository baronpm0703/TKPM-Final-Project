package com.psvm.client.views.components.friend;

import com.psvm.client.controllers.LeaveGroupRequest;
import com.psvm.client.settings.LocalData;
import com.psvm.shared.socket.SocketResponse;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.time.LocalDateTime;

class LeaveGroupThread extends Thread {
	private Socket clientSocket;
	ObjectInputStream socketIn;
	ObjectOutputStream socketOut;

	private String username;
	private String conversationId;

	private int responseCode;

	public LeaveGroupThread(Socket clientSocket, ObjectInputStream socketIn, ObjectOutputStream socketOut, String username, String conversationId) {
		this.clientSocket = clientSocket;
		this.socketIn = socketIn;
		this.socketOut = socketOut;

		this.username = username;
		this.conversationId = conversationId;
	}

	@Override
	public void run() {
		super.run();
		LeaveGroupRequest request = new LeaveGroupRequest(clientSocket, socketIn, socketOut, username, conversationId);
		SocketResponse response = request.talk();

		responseCode = response.getResponseCode();
	}

	public int getResponseCode() {
		return responseCode;
	}
}

public class UserEachGroup extends UserEachFriend {
	// Multithreading + Socket
	final String SOCKET_HOST = "localhost";
	final int SOCKET_PORT = 5555;

	private String conversationId;

	UserEachGroup(String conversationId, String avatar, String username, String name, String lastChat, LocalDateTime lastTime, String lastChatStatus){
		super(avatar,username,name,lastChat,lastTime,lastChatStatus);

		this.conversationId = conversationId;
		this.addMouseListener(new MouseListener() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (SwingUtilities.isRightMouseButton(e)){
					showPopupMenuForGroup(e.getX(),e.getY());
				}
			}

			@Override
			public void mousePressed(MouseEvent e) {

			}

			@Override
			public void mouseReleased(MouseEvent e) {

			}

			@Override
			public void mouseEntered(MouseEvent e) {

			}

			@Override
			public void mouseExited(MouseEvent e) {

			}
		});
	}
	private void showPopupMenuForGroup(int x, int y){

		JPopupMenu popupMenu = new JPopupMenu();
		popupMenu.setBackground(Color.WHITE);

		JMenuItem leaveGroup = new JMenuItem("🚪 Rời nhóm");
		leaveGroup.setFont(new Font(null,Font.PLAIN,16));
		leaveGroup.setForeground(Color.RED);

		popupMenu.add(leaveGroup);

		leaveGroup.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				int response = JOptionPane.showConfirmDialog(null,
						"Bạn có muốn rời khỏi nhóm này?",
						"Xác nhận", JOptionPane.YES_NO_OPTION);
				if (response == JOptionPane.YES_OPTION) {
					try {
						Socket leaveGroupSocket = new Socket(SOCKET_HOST, SOCKET_PORT);
						ObjectInputStream leaveGroupSocketIn = new ObjectInputStream(leaveGroupSocket.getInputStream());
						ObjectOutputStream leaveGroupSocketOut = new ObjectOutputStream(leaveGroupSocket.getOutputStream());

						LeaveGroupThread leaveGroupThread = new LeaveGroupThread(leaveGroupSocket, leaveGroupSocketIn, leaveGroupSocketOut, LocalData.getCurrentUsername(), conversationId);
						leaveGroupThread.start();

						try {
							leaveGroupThread.join();
							if (leaveGroupThread.getResponseCode() == SocketResponse.RESPONSE_CODE_SUCCESS) {
								LocalData.setToRemoveChat(true);
								LocalData.setToReloadMessageList(true);
								LocalData.setSelectedConversation("");
								LocalData.setToRemoveChatDetail(true);
							}
						} catch (InterruptedException ex) {
							throw new RuntimeException(ex);
						}

						leaveGroupSocket.close();
					} catch (IOException ie) {
						throw new RuntimeException(ie);
					}
				}
			}
		});

		popupMenu.show(this, x, y);
	}



}