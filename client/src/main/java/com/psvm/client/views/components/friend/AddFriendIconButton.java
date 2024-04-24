package com.psvm.client.views.components.friend;

import com.psvm.client.controllers.FriendMessageListRequest;
import com.psvm.client.controllers.SearchUserRequest;
import com.psvm.client.views.ListFriendInSearchDialog;
import com.psvm.client.views.ListRequestInSearchDialog;
import com.psvm.shared.socket.SocketResponse;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.List;
import java.util.Map;
import java.util.Vector;

class SearchUserThread extends Thread {
    private Socket clientSocket;
    ObjectInputStream socketIn;
    ObjectOutputStream socketOut;
    private String otherUsername;

    private int responseCode;
    private Vector<Map<String, Object>> data;

    public SearchUserThread(Socket clientSocket, ObjectInputStream socketIn, ObjectOutputStream socketOut, String otherUsername) {
        this.clientSocket = clientSocket;
        this.socketIn = socketIn;
        this.socketOut = socketOut;
        this.otherUsername = otherUsername;
    }

    @Override
    public void run() {
        SearchUserRequest request = new SearchUserRequest(clientSocket, socketIn, socketOut, otherUsername);
        SocketResponse response = request.talk();

        responseCode = response.getResponseCode();
        data = response.getData();
    }

    public int getResponseCode() { return responseCode; }
    public Vector<Map<String, Object>> getData() { return data; }
}

public class AddFriendIconButton extends JButton {
    static Icon icon = new ImageIcon("client/src/main/resources/icon/addFriend.png");

    final String SOCKET_HOST = "localhost";
    final int SOCKET_PORT = 5555;
    Socket searchUserSocket;
    ObjectInputStream searchUserSocketIn;
    ObjectOutputStream searchUserSocketOut;

    SearchUserThread searchUserThread;
    boolean isSearchFieldFocused = false;

    // Table
    ListFriendInSearchDialog listFriendInSearchDialog;
    JScrollPane scrollFriend;

    AddFriendIconButton() {
        super(icon);

        addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showOptionsPopupMenu();
            }
        });

        /* Multithreading + Socket */
        try {
            searchUserSocket = new Socket(SOCKET_HOST, SOCKET_PORT);
            searchUserSocketIn = new ObjectInputStream(searchUserSocket.getInputStream());
            searchUserSocketOut = new ObjectOutputStream(searchUserSocket.getOutputStream());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void showOptionsPopupMenu() {
        JPopupMenu popupMenu = new JPopupMenu();
        JMenuItem option1 = new JMenuItem("Thêm bạn bè");
        JMenuItem option2 = new JMenuItem("Lời mời kết bạn");

        option1.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.out.println("Thêm bạn bè selected");
                // Add your Option 1 functionality here
                addFriendDialog();
            }
        });

        option2.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Add your Option 2 functionality here
                acceptFriendRequestDialog();
            }
        });

        popupMenu.add(option1);
        popupMenu.add(option2);

        popupMenu.show(this, 0, this.getHeight());
    }

    private void addFriendDialog() {
        JDialog dialog = new JDialog((Frame) null, "Thêm bạn bè", true);
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
        listFriendInSearchDialog = new ListFriendInSearchDialog();
        scrollFriend = new JScrollPane(listFriendInSearchDialog);
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

        if (searchUserThread != null && !searchUserThread.isInterrupted()) {
            searchUserThread.interrupt();
        }
        searchUserThread = new SearchUserThread(searchUserSocket, searchUserSocketIn, searchUserSocketOut, friendName);
        searchUserThread.start();

        try {
            searchUserThread.join();

            listFriendInSearchDialog.setData(searchUserThread.getData());
            scrollFriend.revalidate();
            scrollFriend.repaint();
        } catch (InterruptedException e) {
            System.out.println("Exception thrown while search for users in " + this.getClass().getSimpleName() + ": " + e.getMessage());
        }
    }

    private void acceptFriendRequestDialog() {
        JDialog dialog = new JDialog((Frame) null, "Lời mời kết bạn", true);
        dialog.setSize(400, 300);
        dialog.setLocationRelativeTo(null);

        // Create a panel for content with padding
        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.setOpaque(false);
        contentPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        // Table
        ListRequestInSearchDialog listRequestInSearchDialog = new ListRequestInSearchDialog();
        JScrollPane scrollFriend = new JScrollPane(listRequestInSearchDialog);
        scrollFriend.setPreferredSize(new Dimension(400, 200));
        scrollFriend.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollFriend.getViewport().addChangeListener(e -> {
            scrollFriend.revalidate();
            scrollFriend.repaint();
        });
        scrollFriend.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        contentPanel.add(scrollFriend, BorderLayout.CENTER);

        // Set the content panel to the dialog
        dialog.setContentPane(contentPanel);

        dialog.setVisible(true);
    }
}
