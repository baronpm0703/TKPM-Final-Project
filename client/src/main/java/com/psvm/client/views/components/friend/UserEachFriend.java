package com.psvm.client.views.components.friend;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.time.LocalDateTime;
import java.io.File;
import java.io.IOException;
import java.time.format.DateTimeFormatter;

public class UserEachFriend extends JPanel {
    // Data
    private String id;
    private String avatar;
    private String name;
    private String lastChat;
    private LocalDateTime lastTime;
    private String userStatus;

    // GUI components to display data
    private GridBagConstraints gbc;
    private JLabel avatarLabel;
    private JLabel friendName;
    private JLabel friendLastTime;
    private JLabel lastMessage;
    private JLabel statusMessage;

    UserEachFriend(String id, String avatar, String name, String lastChat, LocalDateTime lastTime, String userStatus){
        this.id = id;
        this.avatar = avatar;
        this.name = name;
        this.lastChat = lastChat;
        this.lastTime = lastTime;
        this.userStatus = userStatus;
        this.setPreferredSize(new Dimension(super.getWidth(),70));
        this.setBorder(new EmptyBorder(0,0,0,0));
        this.setBackground(Color.WHITE);
        initialize();
    }
    void initialize(){

        setLayout(new GridBagLayout());
        gbc = new GridBagConstraints();

        // Component 1 (avatar, spanning 2 rows)//nhớ thay avatar dưới này
        ImageIcon avatarIcon = createCircularAvatar("client/src/main/resources/icon/avatar_sample.jpg", 40, 40);
        avatarLabel = new JLabel(avatarIcon);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridheight = 2;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(0, 0, 0, 10); // Add a 10-pixel gap to the right
        this.add(avatarLabel, gbc);

        // Component 2 (button in the second column)
        friendName = new JLabel(name);
        Dimension friendNameDimension = new Dimension();
        friendNameDimension.setSize(160, 1);
        friendName.setPreferredSize(friendNameDimension);
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.gridheight = 1; // Reset grid height
        gbc.insets = new Insets(0, 0, 0, 10); // Add a 10-pixel gap to the right
        this.add(friendName, gbc);

        // Component 3 (button in the third column)
        friendLastTime = new JLabel(formatLocalDateTime(lastTime));
        gbc.gridx = 2;
        gbc.gridy = 0;
        gbc.insets = new Insets(0, 0, 0, 0); // Reset insets
        this.add(friendLastTime, gbc);

        // Component 4 (button in the second row)
        lastMessage= new JLabel(lastChat);
        Dimension lastMessageDimension = new Dimension();
        lastMessageDimension.setSize(160, 1);
        lastMessage.setPreferredSize(lastMessageDimension);
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.insets = new Insets(0, 0, 0, 10); // Add a 10-pixel gap to the right
        this.add(lastMessage, gbc);

        // Component 5 (button in the third row)
        statusMessage = createUserStatusLabel(userStatus);
        gbc.gridx = 2;
        gbc.gridy = 1;
        this.add(statusMessage, gbc);

        // Add vertical spacing
        this.add(Box.createVerticalStrut(10));
    }

    public String getConversationId() { return id; }

    public void setData(String name, String lastChat, LocalDateTime lastTime, String userStatus) {
        this.name = name;
        this.lastChat = lastChat;
        this.lastTime = lastTime;
        this.userStatus = userStatus;

        friendName.setText(name);
        lastMessage.setText(lastChat);
        friendLastTime.setText(formatLocalDateTime(lastTime));
        this.remove(statusMessage);
        statusMessage = createUserStatusLabel(userStatus);
        this.add(statusMessage, gbc);
        revalidate();
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
    private static String formatLocalDateTime(LocalDateTime dateTime) {
        LocalDateTime now = LocalDateTime.now();

        if (dateTime.toLocalDate().isEqual(now.toLocalDate())) {
            // Same day
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
            return dateTime.format(formatter);
        } else {
            // Different day
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            return dateTime.format(formatter);
        }
    }

    private static JLabel createUserStatusLabel(String userStatus) {
        JLabel statusLabel = new JLabel();
        statusLabel.setPreferredSize(new Dimension(20, 20));

        if ("Online".equals(userStatus)) {
            statusLabel.setIcon(new ImageIcon("client/src/main/resources/icon/chatWhenOnline.png"));
        } else if ("Offline".equals(userStatus)) {
            statusLabel.setIcon(new ImageIcon("client/src/main/resources/icon/chatWhenOffline.png"));
        } else {
            // Set nothing if userStatus is "None"
            statusLabel.setVisible(false);
        }

        return statusLabel;
    }
}
