package com.psvm.client.views.components.account;

import com.psvm.client.controllers.CreateUser;
import com.psvm.client.controllers.GetUser;
import com.psvm.client.settings.LocalData;
import com.psvm.shared.socket.SocketResponse;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Vector;

class LoginBoxThread extends Thread {
	private Socket clientSocket;
	ObjectInputStream socketIn;
	ObjectOutputStream socketOut;

	private String username, hashedPassword;

	private int responseCode;
	private Vector<Map<String, Object>> data;

	public LoginBoxThread(Socket clientSocket, ObjectInputStream socketIn, ObjectOutputStream socketOut, String username, String hashedPassword) {
		this.clientSocket = clientSocket;
		this.socketIn = socketIn;
		this.socketOut = socketOut;

		this.username = username;
		this.hashedPassword = hashedPassword;
	}

	@Override
	public void run() {
		super.run();

		GetUser request = new GetUser(clientSocket, socketIn, socketOut, username, hashedPassword);
		SocketResponse response = request.talk();

		responseCode = response.getResponseCode();
		data = response.getData();
	}

	public Vector<Map<String, Object>> getData() { return data; }
	public int getResponseCode() {
		return responseCode;
	}
}

public class LoginBox extends Component {
	final String SOCKET_HOST = "localhost";
	final int SOCKET_PORT = 5555;

	Socket socket;
	ObjectInputStream socketIn;
	ObjectOutputStream socketOut;

	JTextField usernameInput;
	JTextField passwordInput;

	JButton forgetPasswordButton;
	JButton registerButton;


	public LoginBox() {}

	public int display() {
		int result = 0;

		while (result != -1 && result != 2) {
			result = JOptionPane.showConfirmDialog(null, getPanel(), "Tạo tài khoản", JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);

			if (result == -1 || result == 2) break;

			// Get username
			String username = usernameInput.getText();

			// Get password
			String password = passwordInput.getText();
			MessageDigest md = null;
			try {
				md = MessageDigest.getInstance("MD5");
			} catch (NoSuchAlgorithmException e) {
				System.out.println("Error occurred while registering a user in " + this.getClass().getSimpleName() + ": " + e.getMessage());
			}
			assert md != null;
			md.update(password.getBytes());
			byte[] digest = md.digest();
			// Convert the byte array to a hexadecimal string representation
			StringBuilder hashedPassword = new StringBuilder();
			for (byte b : digest) {
				hashedPassword.append(String.format("%02x", b & 0xff));
			}

			// Check for data validity before processing
			if (passwordInput.getText().length() < 8) {
				JOptionPane.showConfirmDialog(this, "Có lỗi xảy ra khi đăng nhập: Mật khẩu phải có ít nhất 8 kí tự.", "Đăng nhập thất bại", JOptionPane.DEFAULT_OPTION);
				continue;
			}

			// Process retrieved data
			try {
				socket = new Socket(SOCKET_HOST, SOCKET_PORT);
				socketIn = new ObjectInputStream(socket.getInputStream());
				socketOut = new ObjectOutputStream(socket.getOutputStream());

				LoginBoxThread loginBoxThread = new LoginBoxThread(socket, socketIn, socketOut, username, hashedPassword.toString());
				loginBoxThread.start();

				loginBoxThread.join();
				socket.close();
				int responseCode = loginBoxThread.getResponseCode();
				if (responseCode == SocketResponse.RESPONSE_CODE_SUCCESS) {
					break;
				}
				else {
					JOptionPane.showConfirmDialog(this, "Có lỗi xảy ra thì tạo tài khoản. Vui lòng kiểm tra lại thông tin đăng kí.", "Tạo tài khoản thất bại", JOptionPane.DEFAULT_OPTION);
				}
			} catch (IOException | InterruptedException | NullPointerException e) {
				System.out.println("Error occurred while registering a user in " + this.getClass().getSimpleName() + ": " + e.getMessage());
			}
		}

		// Successful login
		if (result == 0) {
			LocalData.setCurrentUsername(usernameInput.getText());
		}

		// Cancel
		return result;
	}

	private JPanel getPanel() {
		JPanel jPanel = new JPanel();
		jPanel.setLayout(new BoxLayout(jPanel, BoxLayout.Y_AXIS));

		JPanel username = new JPanel();
		JLabel usernameLabel = new JLabel("Tên đăng nhập: ");
		usernameInput = new JTextField(20);
		username.add(usernameLabel); username.add(usernameInput);

		JPanel password = new JPanel();
		JLabel passwordLabel = new JLabel("Mật khẩu: ");
		passwordInput = new JTextField(20);
		password.add(passwordLabel); password.add(passwordInput);

		JPanel options = new JPanel();
		forgetPasswordButton = new JButton("Quên mật khẩu");
		registerButton = new JButton("Đăng ký");
		Component thisLoginBox = this;
		registerButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// Perform action on button click
				RegisterBox registerBox = new RegisterBox(thisLoginBox);
				registerBox.display();
			}
		});
		options.add(forgetPasswordButton); options.add(registerButton);

		jPanel.add(username);
		jPanel.add(password);
		jPanel.add(options);

		return jPanel;
	}
}
