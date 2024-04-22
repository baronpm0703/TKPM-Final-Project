package com.psvm.client.views.components.account;

import com.psvm.client.controllers.CreateUser;
import com.psvm.client.controllers.FriendRequestResponseRequest;
import com.psvm.client.settings.LocalData;
import com.psvm.shared.socket.SocketResponse;
import org.jdatepicker.JDatePicker;
import org.jdatepicker.impl.JDatePanelImpl;
import org.jdatepicker.impl.JDatePickerImpl;
import org.jdatepicker.impl.UtilDateModel;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Time;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Properties;

class RegisterBoxThread extends Thread {
	private Socket clientSocket;
	ObjectInputStream socketIn;
	ObjectOutputStream socketOut;

	private String username, fName, lName, password, address, email;
	LocalDateTime dob;
	boolean isMale;

	private int responseCode;

	public RegisterBoxThread(Socket clientSocket, ObjectInputStream socketIn, ObjectOutputStream socketOut, String username, String fName, String lName, String password, String address, LocalDateTime dob, boolean isMale, String email) {
		this.clientSocket = clientSocket;
		this.socketIn = socketIn;
		this.socketOut = socketOut;

		this.username = username;
		this.fName = fName;
		this.lName = lName;
		this.password = password;
		this.address = address;
		this.dob = dob;
		this.isMale = isMale;
		this.email = email;
	}

	@Override
	public void run() {
		super.run();

		CreateUser request = new CreateUser(clientSocket, socketIn, socketOut, username, fName, lName, password, address, dob, isMale, email);
		SocketResponse response = request.talk();

		responseCode = response.getResponseCode();
	}

	public int getResponseCode() {
		return responseCode;
	}
}

public class RegisterBox extends Component {
	private final String EMAIL_PATTERN =
			"^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@"
					+ "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";
	final String SOCKET_HOST = "localhost";
	final int SOCKET_PORT = 5555;

	Socket socket;
	ObjectInputStream socketIn;
	ObjectOutputStream socketOut;

	JTextField usernameInput;
	JTextField fNameInput;
	JTextField lNameInput;
	JTextField passwordInput;
	JTextField addressInput;
	JDatePickerImpl datePicker;
	ButtonGroup buttonGroup;
	JRadioButton maleButton;
	JRadioButton femaleButton;
	JTextField emailInput;

	public RegisterBox() {}

	public int display() {
		int result = 0;

		while (result != -1 && result != 2) {
			result = JOptionPane.showConfirmDialog(null, getPanel(), "Tạo tài khoản", JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);

			// Get username
			String username = usernameInput.getText();

			// Get name
			String fName = fNameInput.getText();
			String lName = lNameInput.getText();

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

			// Get address
			String address = addressInput.getText();

			// Get DoB
			GregorianCalendar dobResult = (GregorianCalendar) datePicker.getJFormattedTextField().getValue();
			int dobDay = dobResult.get(GregorianCalendar.DAY_OF_MONTH);
			int dobMonth = dobResult.get(GregorianCalendar.MONTH) + 1;
			int dobYear = dobResult.get(GregorianCalendar.YEAR);
			LocalDateTime dob = LocalDateTime.of(dobYear, dobMonth, dobDay, 0, 0, 0);

			// Get gender
			boolean isMale = maleButton.isSelected();

			// Get email
			String email = emailInput.getText();

			// Check for data validity before processing
			if (passwordInput.getText().length() < 8) {
				JOptionPane.showConfirmDialog(this, "Có lỗi xảy ra thì tạo tài khoản: Mật khẩu phải có ít nhất 8 kí tự.", "Tạo tài khoản thất bại", JOptionPane.DEFAULT_OPTION);
				continue;
			}
			if (!dob.isBefore(LocalDateTime.now().toLocalDate().atStartOfDay())) {
				JOptionPane.showConfirmDialog(this, "Có lỗi xảy ra thì tạo tài khoản: Ngày sinh không đươc trong hoặc sau ngày hôm nay.", "Tạo tài khoản thất bại", JOptionPane.DEFAULT_OPTION);
				continue;
			}
			if (!email.matches(EMAIL_PATTERN)) {
				JOptionPane.showConfirmDialog(this, "Có lỗi xảy ra thì tạo tài khoản: Email không hợp lệ.", "Tạo tài khoản thất bại", JOptionPane.DEFAULT_OPTION);
				continue;
			}

			// Process retrieved data
			try {
				socket = new Socket(SOCKET_HOST, SOCKET_PORT);
				socketIn = new ObjectInputStream(socket.getInputStream());
				socketOut = new ObjectOutputStream(socket.getOutputStream());

				RegisterBoxThread registerBoxThread = new RegisterBoxThread(socket, socketIn, socketOut, username, fName, lName, hashedPassword.toString(), address, dob, isMale, email);
				registerBoxThread.start();

				registerBoxThread.join();
				socket.close();
				int responseCode = registerBoxThread.getResponseCode();

				if (responseCode == SocketResponse.RESPONSE_CODE_SUCCESS) {
					LocalData.setCurrentUsername(username);
					break;
				}
				else {
					JOptionPane.showConfirmDialog(this, "Có lỗi xảy ra thì tạo tài khoản. Vui lòng kiểm tra lại thông tin đăng kí.", "Tạo tài khoản thất bại", JOptionPane.DEFAULT_OPTION);
				}
			} catch (IOException | InterruptedException | NullPointerException e) {
				System.out.println("Error occurred while registering a user in " + this.getClass().getSimpleName() + ": " + e.getMessage());
			}
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

		JPanel name = new JPanel();
		JLabel fNameLabel = new JLabel("Họ: ");
		fNameInput = new JTextField(30);
		JLabel lNameLabel = new JLabel("Tên: ");
		lNameInput = new JTextField(30);
		name.add(fNameLabel); name.add(fNameInput);
		name.add(lNameLabel); name.add(lNameInput);

		JPanel password = new JPanel();
		JLabel passwordLabel = new JLabel("Mật khẩu: ");
		passwordInput = new JTextField(20);
		password.add(passwordLabel); password.add(passwordInput);

		JPanel address = new JPanel();
		JLabel addressLabel = new JLabel("Địa chỉ: ");
		addressInput = new JTextField(50);
		address.add(addressLabel); address.add(addressInput);

		JPanel dob = new JPanel();
		JLabel dobLabel = new JLabel("Ngày sinh: ");
		UtilDateModel model = new UtilDateModel();
		//model.setDate(20,04,2014);
		// Need this...
		Properties p = new Properties();
		p.put("text.today", "Today");
		p.put("text.month", "Month");
		p.put("text.year", "Year");
		JDatePanelImpl datePanel = new JDatePanelImpl(model, p);
		// Don't know about the formatter, but there it is...
		datePicker = new JDatePickerImpl(datePanel, new DateLabelFormatter());
		dob.add(dobLabel); dob.add(datePicker);

		JPanel gender = new JPanel();
		JLabel genderLabel = new JLabel("Giới tính: ");
		buttonGroup = new ButtonGroup();
		maleButton = new JRadioButton("Nam");
		femaleButton = new JRadioButton("Nữ");
		buttonGroup.add(maleButton); buttonGroup.add(femaleButton);
		gender.add(genderLabel); gender.add(maleButton); gender.add(femaleButton);

		JPanel email = new JPanel();
		JLabel emailLabel = new JLabel("Email: ");
		emailInput = new JTextField(30);
		email.add(emailLabel); email.add(emailInput);

		jPanel.add(new JLabel("* Mật khẩu phải có ít nhất 8 kí tự và ngày sinh phải trước ngày hôm nay"));
		jPanel.add(username);
		jPanel.add(name);
		jPanel.add(password);
		jPanel.add(address);
		jPanel.add(dob);
		jPanel.add(gender);
		jPanel.add(email);
		jPanel.add(new JLabel("Bạn có thể nhấn nút Cancel để quay lại màn hình đăng nhập"));

		return jPanel;
	}
}

class DateLabelFormatter extends JFormattedTextField.AbstractFormatter {

	private String datePattern = "dd-MM-yyyy";
	private SimpleDateFormat dateFormatter = new SimpleDateFormat(datePattern);

	@Override
	public Object stringToValue(String text) throws ParseException {
		return dateFormatter.parseObject(text);
	}

	@Override
	public String valueToString(Object value) throws ParseException {
		if (value != null) {
			Calendar cal = (Calendar) value;
			return dateFormatter.format(cal.getTime());
		}

		return "";
	}

}
