package com.psvm.client.views;

import com.psvm.client.controllers.AddGroupMemberRequest;
import com.psvm.client.controllers.FriendRequestSendRequest;
import com.psvm.shared.socket.SocketResponse;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
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

public class MemberInSearchDialog extends JPanel {
    final String SOCKET_HOST = "localhost";
    final int SOCKET_PORT = 5555;
    Socket addGroupMemberSocket;
    ObjectInputStream addGroupMemberSocketIn;
    ObjectOutputStream addGroupMemberSocketOut;

    //chỗ này cần phải có 1 cái xử lí để lấy danh sách pending add friend request của thằng user mà muốn add để có thể check. (Khi tắt cái dialog đi là xác nhận
    private String avatar;
    private String conversationId;
    private String username;
    private String name;

    MemberInSearchDialog(String avatar, String username, String name, String conversationId){
        this.avatar = avatar;
        this.username = username;
        this.name = name;
        this.conversationId = conversationId;

        this.setPreferredSize(new Dimension(200,50));
        this.setBorder(new EmptyBorder(0,0,0,0));
        this.setBackground(Color.WHITE);
        initialize();

        /* Multithreading + Socket */
        try {
            addGroupMemberSocket = new Socket(SOCKET_HOST, SOCKET_PORT);
            addGroupMemberSocketIn = new ObjectInputStream(addGroupMemberSocket.getInputStream());
            addGroupMemberSocketOut = new ObjectOutputStream(addGroupMemberSocket.getOutputStream());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    String getUsername(){
        return username;
    }
    String getUserAppName(){
        return name;
    }
    void initialize() {
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();

        // Component 1 (avatar, spanning 2 rows)
        ImageIcon avatarIcon = createCircularAvatar("client/src/main/resources/icon/avatar_sample.jpg", 40, 40);
        JLabel avatarLabel = new JLabel(avatarIcon);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridheight = 2;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(0, 0, 0, 10); // Add a 10 pixel gap to the right
        this.add(avatarLabel, gbc);

        // Component 2 (button in the second column)
        JLabel friendName = new JLabel(name);
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.gridheight = 1; // Reset grid height
        gbc.insets = new Insets(0, 0, 0, 150); // Add a 150 pixel gap to the right
        this.add(friendName, gbc);

        // Component 3 (button in the third column)
        JButton addFriendButton = createToggleButton();
        gbc.gridx = 2;
        gbc.gridy = 0;
        gbc.gridheight = 1; // Reset grid height
        gbc.insets = new Insets(0, 0, 0, 0); // Reset insets
        this.add(addFriendButton, gbc);

    }

    private JButton createToggleButton() {
        String addFriendIconPath = "client/src/main/resources/icon/addFriend.png";
        String doneIconPath = "client/src/main/resources/icon/done.png";

        ImageIcon addFriendIcon = new ImageIcon(addFriendIconPath);
        ImageIcon doneIcon = new ImageIcon(doneIconPath);

        // Scale icons to 30x30
        Image scaledAddFriendImage = addFriendIcon.getImage().getScaledInstance(30, 30, Image.SCALE_SMOOTH);
        Image scaledDoneImage = doneIcon.getImage().getScaledInstance(30, 30, Image.SCALE_SMOOTH);

        JButton toggleButton;
        toggleButton = new JButton(new ImageIcon(scaledAddFriendImage));
        toggleButton.setBorderPainted(false);
        toggleButton.setContentAreaFilled(false);
        toggleButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Thread to create friend request
                AddGroupMemberRequest addGroupMemberRequest = new AddGroupMemberRequest(addGroupMemberSocket, addGroupMemberSocketIn, addGroupMemberSocketOut, conversationId, username);
                System.out.println("responseCode");
                SocketResponse response = addGroupMemberRequest.talk();

                int responseCode = response.getResponseCode();
                if (responseCode == SocketResponse.RESPONSE_CODE_SUCCESS) {
                    // Implement sau khi có cách xử lí (Huỷ lời mời kết bạn, nếu có thể làm kịp sẽ làm sau)
    //                if (toggleButton.getIcon().toString().equals(new ImageIcon(addFriendIcon).toString())) {
    //                    toggleButton.setIcon(new ImageIcon(doneIcon));
    //                } else {
    //                    toggleButton.setIcon(new ImageIcon(addFriendIcon));
    //                }
                    toggleButton.setIcon(new ImageIcon(scaledDoneImage));
                }
            }
        });
        return toggleButton;
    }

    private static ImageIcon createCircularAvatar(String imagePath, int width, int height) {
        try {
            BufferedImage originalImage = ImageIO.read(new File(imagePath));
            BufferedImage resizedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2d = resizedImage.createGraphics();

            // Create a circular clip
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

}
