package com.psvm.client.views;

import com.psvm.client.controllers.DeleteChatHistoryRequest;
import com.psvm.client.controllers.SendMessageRequest;
import com.psvm.client.settings.LocalData;
import com.psvm.shared.socket.SocketResponse;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.rmi.ConnectIOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

class DeleteChatHistoryButtonThread extends Thread {
    private Socket clientSocket;
    ObjectInputStream socketIn;
    ObjectOutputStream socketOut;

    private String conversationId;

    private int responseCode;

    public DeleteChatHistoryButtonThread(Socket clientSocket, ObjectInputStream socketIn, ObjectOutputStream socketOut, String conversationId) {
        this.clientSocket = clientSocket;
        this.socketIn = socketIn;
        this.socketOut = socketOut;

        this.conversationId = conversationId;
    }

    @Override
    public void run() {
        super.run();
        DeleteChatHistoryRequest request = new DeleteChatHistoryRequest(clientSocket, socketIn, socketOut, conversationId);
        SocketResponse response = request.talk();

        responseCode = response.getResponseCode();
    }

    public int getResponseCode() {
        return responseCode;
    }
}

public class DetailOfAFriend extends JPanel {
    // Multithreading + Socket
    ScheduledExecutorService service = Executors.newScheduledThreadPool(2);
    final String SOCKET_HOST = "localhost";
    final int SOCKET_PORT = 5555;
    Socket deleteChatHistorySocket;
    ObjectInputStream deleteChatHistorySocketIn;
    ObjectOutputStream deleteChatHistorySocketOut;

    private String conversationId;
    private String avatar;
    private String name;
    private String username;
    private boolean blocked = false;

    DetailOfAFriend(String conversationId, String avatar, String name, String username) {
        this.setPreferredSize(new Dimension(250, 754));
        this.setBackground(Color.WHITE);
        this.conversationId = conversationId;
        this.avatar = avatar;
        this.name = name;
        this.username = username;

        initialize();

        /* Multithreading + Socket */
        try {
            deleteChatHistorySocket = new Socket(SOCKET_HOST, SOCKET_PORT);
            deleteChatHistorySocketIn = new ObjectInputStream(deleteChatHistorySocket.getInputStream());
            deleteChatHistorySocketOut = new ObjectOutputStream(deleteChatHistorySocket.getOutputStream());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    void initialize() {

        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.WEST; // Align components to the left

        // Component 1 (avatar and name)
        JPanel avatarNamePanel = new JPanel();
        avatarNamePanel.setLayout(new BoxLayout(avatarNamePanel, BoxLayout.X_AXIS));
        avatarNamePanel.setBackground(Color.WHITE);
        avatarNamePanel.setPreferredSize(new Dimension(250, 50));

        // Component 1.1 (avatar)
        ImageIcon avatarIcon = createCircularAvatar("client/src/main/resources/icon/avatar_sample.jpg", 40, 40);
        JLabel avatarLabel = new JLabel(avatarIcon);
        avatarLabel.setBorder(new EmptyBorder(0, 0, 0, 10));
        avatarNamePanel.add(avatarLabel);

        // Component 1.2 (name)
        JLabel nameLabel = new JLabel(name);
        avatarNamePanel.add(nameLabel);

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = new Insets(10, 10, 10, 10); // Add padding
        add(avatarNamePanel, gbc);

        gbc.gridy++;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        add(createSeparator(), gbc);

        gbc.gridy++;
        JLabel usernameLabel = new JLabel("Username: @" + username);
        add(usernameLabel, gbc);

        gbc.gridy++;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        add(createSeparator(), gbc);

        // Component 5 (buttons)
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

        gbc.gridy++;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        JButton blockUser = new JButton("Chặn người này");
        blockUser.setForeground(Color.BLUE);
        blockUser.setFocusPainted(false);
        add(blockUser, gbc);

        gbc.gridy++;
        JButton clearChat = new JButton("Xoá lịch sử chat");
        clearChat.setForeground(Color.BLUE);
        clearChat.setFocusPainted(false);
        add(clearChat, gbc);


        searchField.addActionListener(new AbstractAction()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                LocalData.setConversationScrollSearch(searchField.getText());
            }
        });

        blockUser.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int response = JOptionPane.showConfirmDialog(null,
                        "Bạn có chắc muốn chặn người này?",
                        "Xác nhận", JOptionPane.YES_NO_OPTION);
                if (response == JOptionPane.YES_OPTION) {
                    blocked = true;
                    // giữ hay bỏ gì tuỳ cái dialog này tuỳ ko quan trọng
                    JOptionPane.showMessageDialog(null, "Chặn người này...");
                }
            }
        });
        clearChat.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int response = JOptionPane.showConfirmDialog(null,
                        "Bạn có chắc muốn xoá lịch sử chat?",
                        "Xác nhận", JOptionPane.YES_NO_OPTION);
                if (response == JOptionPane.YES_OPTION) {
                    DeleteChatHistoryButtonThread deleteChatHistoryButtonThread = new DeleteChatHistoryButtonThread(deleteChatHistorySocket, deleteChatHistorySocketIn, deleteChatHistorySocketOut, conversationId);
                    deleteChatHistoryButtonThread.start();

                    try {
                        deleteChatHistoryButtonThread.join();
                        if (deleteChatHistoryButtonThread.getResponseCode() == SocketResponse.RESPONSE_CODE_SUCCESS) {
                            LocalData.setToReloadChat(true);
                        }
                    } catch (InterruptedException ex) {
                        ex.printStackTrace();
                    }

                }
            }
        });
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

    private JSeparator createSeparator() {
        JSeparator separator = new JSeparator(SwingConstants.HORIZONTAL);
        separator.setForeground(Color.CYAN);
        return separator;
    }
}
