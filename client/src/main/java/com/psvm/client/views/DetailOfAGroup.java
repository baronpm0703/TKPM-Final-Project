package com.psvm.client.views;

import com.psvm.client.controllers.*;
import com.psvm.client.settings.LocalData;
import com.psvm.shared.socket.SocketResponse;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

class RenameChatThread extends Thread {
    private Socket clientSocket;
    ObjectInputStream socketIn;
    ObjectOutputStream socketOut;

    private String conversationId;
    private String conversationName;

    private int responseCode;

    public RenameChatThread(Socket clientSocket, ObjectInputStream socketIn, ObjectOutputStream socketOut, String conversationId, String conversationName) {
        this.clientSocket = clientSocket;
        this.socketIn = socketIn;
        this.socketOut = socketOut;

        this.conversationId = conversationId;
        this.conversationName = conversationName;
    }

    @Override
    public void run() {
        super.run();
        RenameChatRequest request = new RenameChatRequest(clientSocket, socketIn, socketOut, conversationId, conversationName);
        SocketResponse response = request.talk();

        responseCode = response.getResponseCode();
    }

    public int getResponseCode() {
        return responseCode;
    }
}

class MemberListThread extends Thread {
    private Socket clientSocket;
    ObjectInputStream socketIn;
    ObjectOutputStream socketOut;

    private String conversationId;

    private Vector<Map<String, Object>> data;
    private int responseCode;

    public MemberListThread(Socket clientSocket, ObjectInputStream socketIn, ObjectOutputStream socketOut, String conversationId) {
        this.clientSocket = clientSocket;
        this.socketIn = socketIn;
        this.socketOut = socketOut;

        this.conversationId = conversationId;
    }

    @Override
    public void run() {
        super.run();
        GroupMemberListRequest request = new GroupMemberListRequest(clientSocket, socketIn, socketOut, conversationId);
        SocketResponse response = request.talk();

        data = response.getData();
        responseCode = response.getResponseCode();
    }

    public Vector<Map<String, Object>> getData() {
        return data;
    }
    public int getResponseCode() {
        return responseCode;
    }
}

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

class SearchNonMemberThread extends Thread {
    private Socket clientSocket;
    ObjectInputStream socketIn;
    ObjectOutputStream socketOut;
    private String searchUsername;
    private String conversationId;

    private int responseCode;
    private Vector<Map<String, Object>> data;

    public SearchNonMemberThread(Socket clientSocket, ObjectInputStream socketIn, ObjectOutputStream socketOut, String searchUsername, String conversationId) {
        this.clientSocket = clientSocket;
        this.socketIn = socketIn;
        this.socketOut = socketOut;
        this.searchUsername = searchUsername;
        this.conversationId = conversationId;
    }

    @Override
    public void run() {
        SearchNonMemberRequest request = new SearchNonMemberRequest(clientSocket, socketIn, socketOut, searchUsername, conversationId);
        SocketResponse response = request.talk();

        responseCode = response.getResponseCode();
        data = response.getData();
    }

    public int getResponseCode() { return responseCode; }
    public Vector<Map<String, Object>> getData() { return data; }
}

class SetMemberAdminStatusThread extends Thread {
    private Socket clientSocket;
    ObjectInputStream socketIn;
    ObjectOutputStream socketOut;

    private String currentUsername;
    private String conversationId;
    private String memberId;
    private boolean isAdmin;

    private int responseCode;

    public SetMemberAdminStatusThread(Socket clientSocket, ObjectInputStream socketIn, ObjectOutputStream socketOut, String currentUsername, String conversationId, String memberId, boolean isAdmin) {
        this.clientSocket = clientSocket;
        this.socketIn = socketIn;
        this.socketOut = socketOut;

        this.currentUsername = currentUsername;
        this.conversationId = conversationId;
        this.memberId = memberId;
        this.isAdmin = isAdmin;
    }

    @Override
    public void run() {
        super.run();
        SetMemberAdminStatusRequest request = new SetMemberAdminStatusRequest(clientSocket, socketIn, socketOut, currentUsername, conversationId, memberId, isAdmin);
        SocketResponse response = request.talk();

        responseCode = response.getResponseCode();
    }

    public int getResponseCode() {
        return responseCode;
    }
}

class RemoveGroupMemberThread extends Thread {
    private Socket clientSocket;
    ObjectInputStream socketIn;
    ObjectOutputStream socketOut;

    private String currentUsername;
    private String conversationId;
    private String memberId;

    private int responseCode;

    public RemoveGroupMemberThread(Socket clientSocket, ObjectInputStream socketIn, ObjectOutputStream socketOut, String currentUsername, String conversationId, String memberId) {
        this.clientSocket = clientSocket;
        this.socketIn = socketIn;
        this.socketOut = socketOut;

        this.currentUsername = currentUsername;
        this.conversationId = conversationId;
        this.memberId = memberId;
    }

    @Override
    public void run() {
        super.run();
        RemoveGroupMemberRequest request = new RemoveGroupMemberRequest(clientSocket, socketIn, socketOut, currentUsername, conversationId, memberId);
        SocketResponse response = request.talk();

        responseCode = response.getResponseCode();
    }

    public int getResponseCode() {
        return responseCode;
    }
}

public class DetailOfAGroup extends JPanel {
    // Multithreading + Socket
    ScheduledExecutorService service = Executors.newScheduledThreadPool(2);
    final String SOCKET_HOST = "localhost";
    final int SOCKET_PORT = 5555;
    Socket renameChatSocket;
    ObjectInputStream renameChatSocketIn;
    ObjectOutputStream renameChatSocketOut;
    Socket searchNonMemberSocket;
    ObjectInputStream searchNonMemberSocketIn;
    ObjectOutputStream searchNonMemberSocketOut;
    Socket leaveGroupSocket;
    ObjectInputStream leaveGroupSocketIn;
    ObjectOutputStream leaveGroupSocketOut;

    private String conversationId;
    private String avatar;
    private String name;

    SearchNonMemberThread searchNonMemberThread;
    boolean isSearchFieldFocused = false;

    ListMemberInSearchDialog listMemberInSearchDialog;
    JScrollPane scrollFriend;

    //nhớ sửa cái này
    boolean isAdmin = true;


    DetailOfAGroup(String conversationId, String avatar, String name) {
        this.setPreferredSize(new Dimension(250, 754));
        this.setBackground(Color.WHITE);
        this.conversationId = conversationId;
        this.avatar = avatar;
        this.name = name;

        initialize();

        /* Multithreading + Socket */
        try {
            renameChatSocket = new Socket(SOCKET_HOST, SOCKET_PORT);
            renameChatSocketIn = new ObjectInputStream(renameChatSocket.getInputStream());
            renameChatSocketOut = new ObjectOutputStream(renameChatSocket.getOutputStream());
            searchNonMemberSocket = new Socket(SOCKET_HOST, SOCKET_PORT);
            searchNonMemberSocketIn = new ObjectInputStream(searchNonMemberSocket.getInputStream());
            searchNonMemberSocketOut = new ObjectOutputStream(searchNonMemberSocket.getOutputStream());
            leaveGroupSocket = new Socket(SOCKET_HOST, SOCKET_PORT);
            leaveGroupSocketIn = new ObjectInputStream(leaveGroupSocket.getInputStream());
            leaveGroupSocketOut = new ObjectOutputStream(leaveGroupSocket.getOutputStream());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    void initialize() {

        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.WEST; // Align components to the left

        JPanel avatarNamePanel = new JPanel();
        avatarNamePanel.setLayout(new BoxLayout(avatarNamePanel, BoxLayout.X_AXIS));
        avatarNamePanel.setBackground(Color.WHITE);

        ImageIcon avatarIcon = createCircularAvatar("client/src/main/resources/icon/avatar_sample.jpg", 40, 40);
        JLabel avatarLabel = new JLabel(avatarIcon);
        avatarLabel.setBorder(new EmptyBorder(0, 0, 0, 10));
        avatarNamePanel.add(avatarLabel);

        JLabel nameField = new JLabel(name);
        nameField.setBackground(Color.WHITE);

        JButton editButton = new JButton(new ImageIcon("client/src/main/resources/icon/editable.png"));
        editButton.setPreferredSize(new Dimension(20, 20)); // Set the preferred size
        editButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String newName = JOptionPane.showInputDialog("Nhập tên mới:");
                if (newName != null && !newName.isEmpty()) {
                    RenameChatThread renameChatThread = new RenameChatThread(renameChatSocket, renameChatSocketIn, renameChatSocketOut, conversationId, newName);
                    renameChatThread.start();

                    try {
                        renameChatThread.join();
                        if (renameChatThread.getResponseCode() == SocketResponse.RESPONSE_CODE_SUCCESS) {
                            nameField.setText(newName);
                        }
                    } catch (InterruptedException ex) {
                        throw new RuntimeException(ex);
                    }

                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(nameField);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setBackground(Color.WHITE);
        scrollPane.setBorder(new EmptyBorder(0, 0, 0, 10));
        avatarNamePanel.add(scrollPane);
        avatarNamePanel.add(editButton);

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = new Insets(10, 10, 10, 10); // Add padding
        gbc.fill = GridBagConstraints.HORIZONTAL; // Set to expand horizontally
        add(avatarNamePanel, gbc);

        gbc.gridy++;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        add(createSeparator(), gbc);
        //Danh sách thành viên


        gbc.gridy++;
        JButton adminListButton = new JButton("<html><b><font size='4'>Danh Sách Thành Viên</font></b></html>");
        adminListButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showMemberListDialog();
            }
        });
        add(adminListButton, gbc);


        ///

        gbc.gridy++;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        add(createSeparator(), gbc);


        // Component (buttons)
        gbc.gridy++;
        JLabel searchTextLabel = new JLabel("Tìm kiếm chuỗi trong Chat:");
        searchTextLabel.setForeground(Color.BLUE);
        add(searchTextLabel, gbc);

        gbc.gridy++;
        JTextField searchField = new JTextField(20);
        gbc.weightx = 1.0;
        searchField.setBackground(Color.decode("#EEF1F4"));
        searchField.setBorder(new LineBorder(new Color(0, 0, 0, 0), 1, true));
        add(searchField, gbc);

        gbc.gridy++;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        add(createSeparator(), gbc);

        //cái nút này giống với bên thêm bạn bè (nhớ gọi addRight ở chỗ này để thông báo đã thêm 1 thành viên vào)
        gbc.gridy++;
        JButton addMember = new JButton("Thêm thành viên");
        addMember.setForeground(Color.BLUE);
        addMember.setFocusPainted(false);
        add(addMember, gbc);

        gbc.gridy++;
        JButton outGroup = new JButton("Rời khỏi nhóm");
        outGroup.setForeground(Color.BLUE);
        outGroup.setFocusPainted(false);
        add(outGroup, gbc);

//        gbc.gridy++;
//        JButton encodeChat = new JButton("Mã Hoá");
//        encodeChat.setForeground(Color.BLUE);
//        encodeChat.setFocusPainted(false);
//        add(encodeChat, gbc);

        searchField.addActionListener(new AbstractAction()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                LocalData.setConversationScrollSearch(searchField.getText());
            }
        });

        addMember.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                addFriendDialog();
            }
        });
        outGroup.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int response = JOptionPane.showConfirmDialog(null,
                        "Bạn có chắc muốn rời khỏi nhóm?",
                        "Xác nhận", JOptionPane.YES_NO_OPTION);
                if (response == JOptionPane.YES_OPTION) {
                    LeaveGroupThread leaveGroupThread = new LeaveGroupThread(leaveGroupSocket, leaveGroupSocketIn, leaveGroupSocketOut, LocalData.getCurrentUsername(), conversationId);
                    leaveGroupThread.start();

                    try {
                        leaveGroupThread.join();
                        if (leaveGroupThread.getResponseCode() == SocketResponse.RESPONSE_CODE_SUCCESS) {

                        }
                    } catch (InterruptedException ex) {
                        throw new RuntimeException(ex);
                    }
                }
            }
        });
//        encodeChat.addActionListener(new ActionListener() {
//            @Override
//            public void actionPerformed(ActionEvent e) {
//                int response = JOptionPane.showConfirmDialog(null,
//                        "Bạn có muốn mã hoá nhóm chat?",
//                        "Xác nhận", JOptionPane.YES_NO_OPTION);
//                if (response == JOptionPane.YES_OPTION) {
//
//                    // giữ hay bỏ gì tuỳ cái dialog này tuỳ ko quan trọng
//                    JOptionPane.showMessageDialog(null, "Mã hoá...");
//                }
//            }
//        });
    }

    //cái nút này giống với bên thêm bạn bè (nhớ gọi addRight ở chỗ này để thông báo đã thêm 1 thành viên vào)
     void addFriendDialog() {
        JDialog dialog = new JDialog((Frame) null, "Thêm thành viên", true);
        dialog.setSize(400, 300);
        dialog.setLocationRelativeTo(null);

        // Create a panel for content with padding
        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.setOpaque(false);
        contentPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        String placeHolder = "\uD83D\uDD0E Tìm kiếm";
        // Search field
        JTextField searchFriendField = new JTextField();
        searchFriendField.setText(placeHolder);

        searchFriendField.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
                if (searchFriendField.getText().equals(placeHolder)) {
                    searchFriendField.setText("");
                }
                isSearchFieldFocused = true;
            }

            @Override
            public void focusLost(FocusEvent e) {
                if (searchFriendField.getText().isEmpty()) {
                    searchFriendField.setText(placeHolder);
                }
                isSearchFieldFocused = false;
            }
        });

        searchFriendField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                getFriendField(searchFriendField.getText());
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                getFriendField(searchFriendField.getText());
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                getFriendField(searchFriendField.getText());
            }
        });

        contentPanel.add(searchFriendField, BorderLayout.CENTER);

        // Table
        listMemberInSearchDialog = new ListMemberInSearchDialog(conversationId);
        scrollFriend = new JScrollPane(listMemberInSearchDialog);
        scrollFriend.setPreferredSize(new Dimension(300, 200));
        scrollFriend.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollFriend.getViewport().addChangeListener(e -> {
            scrollFriend.revalidate();
            scrollFriend.repaint();
        });
        scrollFriend.setBorder(BorderFactory.createEmptyBorder(30, 0, 0, 0));
        contentPanel.add(scrollFriend, BorderLayout.SOUTH);

        // Set the content panel to the dialog
        dialog.setContentPane(contentPanel);

        dialog.setVisible(true);
    }
    private void getFriendField(String friendName) {
        // Xử lí cái *username đc ghi ở đây
        System.out.println("Text changed: " + friendName);

        // If the client is not actively typing in the search field then the list is not updated
        if (!isSearchFieldFocused) return;

        if (searchNonMemberThread != null && !searchNonMemberThread.isInterrupted()) {
            searchNonMemberThread.interrupt();
        }
        searchNonMemberThread = new SearchNonMemberThread(searchNonMemberSocket, searchNonMemberSocketIn, searchNonMemberSocketOut, friendName, conversationId);
        searchNonMemberThread.start();

        try {
            searchNonMemberThread.join();

            listMemberInSearchDialog.setData(searchNonMemberThread.getData());
            scrollFriend.revalidate();
            scrollFriend.repaint();
        } catch (InterruptedException e) {
            System.out.println("Exception thrown while search for users in " + this.getClass().getSimpleName() + ": " + e.getMessage());
        }
    }
    static ImageIcon createCircularAvatar(String imagePath, int width, int height) {
        try {
            BufferedImage originalImage = ImageIO.read(new File(imagePath));
            BufferedImage resizedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2d = resizedImage.createGraphics();

            Shape clip = new Ellipse2D.Float(0, 0, width, height);
            g2d.setClip(clip);
            g2d.drawImage(originalImage, 0, 0, width, height, null);
            g2d.dispose();

            return new ImageIcon(resizedImage);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }



    private void showMemberListDialog() {
        try (Socket memberListSocket = new Socket(SOCKET_HOST, SOCKET_PORT)) {
            ObjectInputStream memberListSocketIn = new ObjectInputStream(memberListSocket.getInputStream());
            ObjectOutputStream memberListSocketOut = new ObjectOutputStream(memberListSocket.getOutputStream());
            MemberListThread memberListThread = new MemberListThread(memberListSocket, memberListSocketIn, memberListSocketOut, conversationId);
            memberListThread.start();
            memberListThread.join();

            JDialog memberListDialog = new JDialog((Frame) null, "Danh Sách Thành Viên", true);
            memberListDialog.setSize(400, 300);
            memberListDialog.setLocationRelativeTo(null);

            // Create content for the Thành viên tab
            JPanel memberPanel = new JPanel();
            memberPanel.setLayout(new GridLayout(0, 1)); // One column, multiple rows

            Vector<Map<String, Object>> data = memberListThread.getData();
            //Chỗ này để hiện data member
            for (Map<String, Object> datum: data) {
                if (!datum.get("MemberId").toString().equals(LocalData.getCurrentUsername()))
                    memberPanel.add(memberPanel(conversationId, datum.get("MemberId").toString(), (Boolean) datum.get("IsAdmin")));
                else
                    memberPanel.add(memberPanel(conversationId, datum.get("MemberId").toString(), (Boolean) datum.get("IsAdmin")), 0);
            }

            JScrollPane memberScrollPane = new JScrollPane(memberPanel);
            memberListDialog.add(memberScrollPane);

            // Show the dialog
            memberListDialog.setVisible(true);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
	}

    JPanel memberPanel(String conversationId, String labelText, boolean isAdmin) {
        JPanel memberPanel = new JPanel();
        memberPanel.setBackground(Color.WHITE);
        memberPanel.setPreferredSize(new Dimension(300, 50));
        memberPanel.setLayout(new BorderLayout());

        // Add avatar to the left
        ImageIcon avatarIcon = createCircularAvatar("client/src/main/resources/icon/avatar_sample.jpg", 30, 30);
        JLabel avatarLabel = new JLabel(avatarIcon);
        avatarLabel.setBorder(new EmptyBorder(10, 10, 10, 10));
        memberPanel.add(avatarLabel, BorderLayout.WEST);

        // Create JLabel
        JLabel label = new JLabel(labelText);
        label.setBorder(new EmptyBorder(20, 0, 20, 20)); // Increase the right inset for more spacing
        memberPanel.add(label, BorderLayout.CENTER);

        ImageIcon adminIcon = new ImageIcon("client/src/main/resources/icon/acceptFriendRequest.png");
        Image adminImage = adminIcon.getImage().getScaledInstance(30, 30, Image.SCALE_SMOOTH);
        ImageIcon adminResizedIcon = new ImageIcon(adminImage);
        ImageIcon deleteIcon = new ImageIcon("client/src/main/resources/icon/cancelled.png");
        Image deleteImage = deleteIcon.getImage().getScaledInstance(30, 30, Image.SCALE_SMOOTH);
        ImageIcon deleteResizedIcon = new ImageIcon(deleteImage);

        if (!labelText.equals(LocalData.getCurrentUsername())) {
            JPanel buttons = new JPanel(new FlowLayout());

            // Add admin toggle button
            JToggleButton adminSwitch = new JToggleButton(adminResizedIcon);
            adminSwitch.setSelected(isAdmin);
            adminSwitch.addItemListener(new ItemListener() {
                public void itemStateChanged(ItemEvent itemEvent)
                {
                    // event is generated in button
                    int state = itemEvent.getStateChange();

                    boolean isAdmin;
                    // if selected print selected in console
                    if (state == ItemEvent.SELECTED) {
                        System.out.println("Selected");
                        isAdmin = true;
                    }
                    else {
                        // else print deselected in console
                        System.out.println("Deselected");
                        isAdmin = false;
                    }

                    try (Socket setAdminStatusSocket = new Socket(SOCKET_HOST, SOCKET_PORT)) {
                        ObjectInputStream setAdminStatusSocketIn = new ObjectInputStream(setAdminStatusSocket.getInputStream());
                        ObjectOutputStream setAdminStatusSocketOut = new ObjectOutputStream(setAdminStatusSocket.getOutputStream());

                        SetMemberAdminStatusThread setMemberAdminStatusThread = new SetMemberAdminStatusThread(setAdminStatusSocket, setAdminStatusSocketIn, setAdminStatusSocketOut, LocalData.getCurrentUsername(), conversationId, labelText, isAdmin);
                        setMemberAdminStatusThread.start();
                        setMemberAdminStatusThread.join();

                        if (setMemberAdminStatusThread.getResponseCode() == SocketResponse.RESPONSE_CODE_FAILURE) {
                            JOptionPane.showConfirmDialog(null, "Hành động thất bại", "Thất bại", JOptionPane.DEFAULT_OPTION);
                        }
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    } catch (InterruptedException e) {
						throw new RuntimeException(e);
					}
				}
            });
            buttons.add(adminSwitch);

            // Add delete member button
            JButton deleteButton = new JButton(deleteResizedIcon);
            deleteButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    int response = JOptionPane.showConfirmDialog(null,
                            "Bạn có xoá thành viên này khỏi nhóm không?",
                            "Xác nhận", JOptionPane.YES_NO_OPTION);
                    if (response == JOptionPane.YES_OPTION) {
                        try (Socket setAdminStatusSocket = new Socket(SOCKET_HOST, SOCKET_PORT)) {
                            ObjectInputStream setAdminStatusSocketIn = new ObjectInputStream(setAdminStatusSocket.getInputStream());
                            ObjectOutputStream setAdminStatusSocketOut = new ObjectOutputStream(setAdminStatusSocket.getOutputStream());

                            RemoveGroupMemberThread removeGroupMemberThread = new RemoveGroupMemberThread(setAdminStatusSocket, setAdminStatusSocketIn, setAdminStatusSocketOut, LocalData.getCurrentUsername(), conversationId, labelText);
                            removeGroupMemberThread.start();
                            removeGroupMemberThread.join();

                            if (removeGroupMemberThread.getResponseCode() == SocketResponse.RESPONSE_CODE_FAILURE) {
                                JOptionPane.showConfirmDialog(null, "Hành động thất bại", "Thất bại", JOptionPane.DEFAULT_OPTION);
                            }
                            else {
                                JPanel parent = (JPanel) memberPanel.getParent();
                                parent.remove(memberPanel);
                                parent.revalidate();
                                parent.repaint();
                            }
                        } catch (IOException | InterruptedException ie) {
                            throw new RuntimeException(ie);
                        }
                    }
				}
            });
            buttons.add(deleteButton);

            memberPanel.add(buttons, BorderLayout.EAST);
        }
        return memberPanel;
    }
    private JSeparator createSeparator() {
        JSeparator separator = new JSeparator(SwingConstants.HORIZONTAL);
        separator.setForeground(Color.CYAN);
        return separator;
    }
}
