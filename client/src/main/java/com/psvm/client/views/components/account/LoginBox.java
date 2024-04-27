package com.psvm.client.views.components.account;

import com.psvm.client.controllers.GetUserRequest;
import com.psvm.client.controllers.LogActivityLoginRequest;
import com.psvm.client.controllers.ResetPasswordRequest;
import com.psvm.client.settings.LocalData;
import com.psvm.shared.socket.SocketResponse;
import org.apache.commons.lang3.RandomStringUtils;

import javax.mail.*;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.Properties;
import java.util.Random;
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

		GetUserRequest request = new GetUserRequest(clientSocket, socketIn, socketOut, username, hashedPassword);
		SocketResponse response = request.talk();

		responseCode = response.getResponseCode();
		data = response.getData();
	}

	public Vector<Map<String, Object>> getData() { return data; }
	public int getResponseCode() {
		return responseCode;
	}
}

class ForgetPasswordThread extends Thread {
	private Socket clientSocket;
	ObjectInputStream socketIn;
	ObjectOutputStream socketOut;

	private String username;
	private String email;
	private String password;

	private int responseCode;
	private Vector<Map<String, Object>> data;

	public ForgetPasswordThread(Socket clientSocket, ObjectInputStream socketIn, ObjectOutputStream socketOut, String username, String email, String password) {
		this.clientSocket = clientSocket;
		this.socketIn = socketIn;
		this.socketOut = socketOut;

		this.username = username;
		this.email = email;
		this.password = password;
	}

	@Override
	public void run() {
		super.run();

		ResetPasswordRequest request = new ResetPasswordRequest(clientSocket, socketIn, socketOut, username, email, password);
		SocketResponse response = request.talk();
		System.out.println(response.getResponseCode());

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
				Vector<Map<String, Object>> data = loginBoxThread.getData();

				// Check if the user is not banned, if true then allows log in
				if (responseCode == SocketResponse.RESPONSE_CODE_SUCCESS && Integer.parseInt(data.get(0).get("UserFound").toString()) == 1) {
					if (Integer.parseInt(data.get(1).get("Status").toString()) != 2) {
						if (Integer.parseInt(data.get(1).get("Status").toString()) != 1) {
							break;
						}
						else {
							JOptionPane.showConfirmDialog(this, "Có lỗi xảy ra khi đăng nhập. Tài khoản đã có người đăng nhập.", "Đăng nhập thất bại", JOptionPane.DEFAULT_OPTION);
						}
					}
					else {
						JOptionPane.showConfirmDialog(this, "Có lỗi xảy ra khi đăng nhập. Tài khoản đã bị ban.", "Đăng nhập thất bại", JOptionPane.DEFAULT_OPTION);
					}
				}
				else {
					JOptionPane.showConfirmDialog(this, "Có lỗi xảy ra khi đăng nhập. Vui lòng kiểm tra lại thông tin đăng nhập.", "Đăng nhập thất bại", JOptionPane.DEFAULT_OPTION);
				}
			} catch (IOException | InterruptedException | NullPointerException e) {
				System.out.println("Error occurred while logging in " + this.getClass().getSimpleName() + ": " + e.getMessage());
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
		forgetPasswordButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// Perform action on button click
				JPanel forgetPasswordPanel = new JPanel();
				forgetPasswordPanel.setLayout(new BoxLayout(forgetPasswordPanel, BoxLayout.Y_AXIS));

				JPanel forgetPasswordUsername = new JPanel();
				JLabel forgetPasswordUsernameLabel = new JLabel("Tên đăng nhập: ");
				JTextField forgetPasswordUsernameInput = new JTextField(20);
				forgetPasswordUsername.add(forgetPasswordUsernameLabel);
				forgetPasswordUsername.add(forgetPasswordUsernameInput);

				JPanel forgetPasswordEmail = new JPanel();
				JLabel forgetPasswordEmailLabel = new JLabel("Email: ");
				JTextField forgetPasswordEmailInput = new JTextField(20);
				forgetPasswordEmail.add(forgetPasswordEmailLabel);
				forgetPasswordEmail.add(forgetPasswordEmailInput);

				forgetPasswordPanel.add(forgetPasswordUsername);
				forgetPasswordPanel.add(forgetPasswordEmail);

				int forgetPasswordResult = JOptionPane.showConfirmDialog(null, forgetPasswordPanel, "Khởi tạo lại mật khẩu", JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
				if (forgetPasswordResult == 0) {
					// Generate new password
					int length = 10;
					boolean useLetters = true;
					boolean useNumbers = true;
					String newPassword = RandomStringUtils.random(length, useLetters, useNumbers);
					MessageDigest md = null;
					try {
						md = MessageDigest.getInstance("MD5");
					} catch (NoSuchAlgorithmException nsae) {
						System.out.println("Error occurred while registering a user in " + this.getClass().getSimpleName() + ": " + nsae.getMessage());
					}
					assert md != null;
					md.update(newPassword.getBytes());
					byte[] digest = md.digest();
					// Convert the byte array to a hexadecimal string representation
					StringBuilder newHashedPassword = new StringBuilder();
					for (byte b : digest) {
						newHashedPassword.append(String.format("%02x", b & 0xff));
					}

					// Update new password to database and send new password to email
					try {
						socket = new Socket(SOCKET_HOST, SOCKET_PORT);
						socketIn = new ObjectInputStream(socket.getInputStream());
						socketOut = new ObjectOutputStream(socket.getOutputStream());
					} catch (IOException ex) {
						throw new RuntimeException(ex);
					}

					ForgetPasswordThread forgetPasswordThread = new ForgetPasswordThread(socket, socketIn, socketOut, forgetPasswordUsernameInput.getText(), forgetPasswordEmailInput.getText(), newHashedPassword.toString());
					forgetPasswordThread.start();

					try {
						forgetPasswordThread.join();
						if (forgetPasswordThread.getResponseCode() == SocketResponse.RESPONSE_CODE_SUCCESS) {
							socket.close();
							String to = forgetPasswordEmailInput.getText();
							String from = "no-reply@hooyah.org";
							String username = "hooyah.app@gmail.com";
							String password = "tzft rpee cdcf svfg";

							// Get system properties
							Properties properties = System.getProperties();

							// Setup mail server
							properties.put("mail.smtp.starttls.enable", "true");
							properties.put("mail.smtp.auth", "true");
							properties.put("mail.smtp.host", "smtp.gmail.com");
							properties.put("mail.smtp.port", "587");

							// Get the default Session object.
							Session session = Session.getInstance(properties,
									new javax.mail.Authenticator() {
										protected PasswordAuthentication getPasswordAuthentication() {
											return new PasswordAuthentication(username, password);
										}
									});

							try {
								// Create a default MimeMessage object.
								MimeMessage message = new MimeMessage(session);

								// Set From: header field of the header.
								message.setFrom(new InternetAddress(from));

								// Set To: header field of the header.
								message.addRecipient(Message.RecipientType.TO, new InternetAddress(to));

								// Set Subject: header field
								message.setSubject("Your new password for hooYah!");

								// Now set the actual message
								String text = "Dear " + forgetPasswordUsernameInput.getText() + ",\n\n" +
										"Your account's new account is: " + newPassword;
								message.setText(text);

								// Send message
								Transport.send(message);
							} catch (MessagingException mex) {
								mex.printStackTrace();
							}
						}
					} catch (InterruptedException ex) {
						throw new RuntimeException(ex);
					} catch (IOException ex) {
						throw new RuntimeException(ex);
					}
				}
			}
		});
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
