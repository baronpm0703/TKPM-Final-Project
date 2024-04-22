package com.psvm.client.views.components.account;

import com.psvm.client.settings.LocalData;
import com.psvm.shared.socket.SocketResponse;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class LoginBox extends Component {
	final String SOCKET_HOST = "localhost";
	final int SOCKET_PORT = 5555;

	Socket socket;
	ObjectInputStream socketIn;
	ObjectOutputStream socketOut;

	JTextField usernameInput;
	JTextField passwordInput;

	public LoginBox() {}

	public int display() {
		int result = 0;

		while (result != -1 && result != 2) {
			result = JOptionPane.showConfirmDialog(null, getPanel(), "Tạo tài khoản", JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);

			// Get username
			String username = usernameInput.getText();

			// Get password
			String password = passwordInput.getText();

			// Check for data validity before processing
			if (passwordInput.getText().length() < 8) {
				JOptionPane.showConfirmDialog(this, "Có lỗi xảy ra thì tạo tài khoản: Mật khẩu phải có ít nhất 8 kí tự.", "Tạo tài khoản thất bại", JOptionPane.DEFAULT_OPTION);
				continue;
			}

			// Process retrieved data
//			try {
//				socket = new Socket(SOCKET_HOST, SOCKET_PORT);
//				socketIn = new ObjectInputStream(socket.getInputStream());
//				socketOut = new ObjectOutputStream(socket.getOutputStream());
//
//				RegisterBoxThread registerBoxThread = new RegisterBoxThread(socket, socketIn, socketOut, username, fName, lName, password, address, dob, isMale, email);
//				registerBoxThread.start();
//
//				registerBoxThread.join();
//				socket.close();
//				int responseCode = registerBoxThread.getResponseCode();
//
//				if (responseCode == SocketResponse.RESPONSE_CODE_SUCCESS) {
//					LocalData.setCurrentUsername(username);
//					break;
//				}
//				else {
//					JOptionPane.showConfirmDialog(this, "Có lỗi xảy ra thì tạo tài khoản. Vui lòng kiểm tra lại thông tin đăng kí.", "Tạo tài khoản thất bại", JOptionPane.DEFAULT_OPTION);
//				}
//			} catch (IOException | InterruptedException | NullPointerException e) {
//				System.out.println("Error occurred while registering a user in " + this.getClass().getSimpleName() + ": " + e.getMessage());
//			}
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

		jPanel.add(username);
		jPanel.add(password);

		return jPanel;
	}
}
