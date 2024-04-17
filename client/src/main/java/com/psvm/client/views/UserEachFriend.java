package com.psvm.client.views;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.time.LocalDateTime;
import java.io.File;
import java.io.IOException;
import java.time.format.DateTimeFormatter;

public class UserEachFriend extends JPanel {
    private String avatar;
    //Mới thêm cái này vì nó cần thiết
    private String username;
    private String name;
    private String lastChat;
    private LocalDateTime lastTime;
    private String userStatus;
    //Mới thêm cái này vì nó cần thiết
    private String lastChatStatus;
    UserEachFriend(String avatar, String username, String name, String lastChat, LocalDateTime lastTime, String userStatus, String lastChatStatus){
        this.avatar = avatar;
        this.username = username;
        this.name = name;
        this.lastChat = lastChat;
        this.lastTime = lastTime;
        this.userStatus = userStatus;
        this.lastChatStatus = lastChatStatus;
        this.setPreferredSize(new Dimension(super.getWidth(),70));
        this.setBorder(new EmptyBorder(0,0,0,0));
        this.setBackground(Color.WHITE);
        initialize();
    }
    //thêm 2 cái hàm này để lọc ra
    String getUsername(){
        return username;
    }
    String getUserAppName(){
        return name;
    }
    void initialize(){

        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();

        // Component 1 (avatar, spanning 2 rows)//nhớ thay avatar dưới này
        ImageIcon avatarIcon = createCircularAvatar("client/src/main/resources/icon/avatar_sample.jpg", 40, 40,userStatus);
        JLabel avatarLabel = new JLabel(avatarIcon);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridheight = 2;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(0, 0, 0, 10); // Add a 10-pixel gap to the right
        this.add(avatarLabel, gbc);

        // Component 2 (button in the second column)
        JLabel friendName = new JLabel(name);
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.gridheight = 1; // Reset grid height
        gbc.insets = new Insets(0, 0, 0, 135); // Add a 150-pixel gap to the right
        this.add(friendName, gbc);

        // Component 3 (button in the third column)
        JLabel friendLastTime = new JLabel(formatLocalDateTime(lastTime));
        gbc.gridx = 2;
        gbc.gridy = 0;
        gbc.insets = new Insets(0, 0, 0, 0); // Reset insets
        this.add(friendLastTime, gbc);

        // Component 4 (button in the second row)
        JLabel lastMessage= new JLabel(lastChat);
        gbc.gridx = 1;
        gbc.gridy = 1;
        this.add(lastMessage, gbc);

        // Component 5 (button in the third row)
        JLabel statusMessage = createUserStatusDot(lastChatStatus);
        gbc.gridx = 2;
        gbc.gridy = 1;
        this.add(statusMessage, gbc);

    }

    private static ImageIcon createCircularAvatar(String imagePath, int width, int height, String userStatus) {
        try {
            //https://stackoverflow.com/questions/14731799/bufferedimage-into-circle-shape
            BufferedImage originalImage = ImageIO.read(new File(imagePath));
            BufferedImage resizedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2d = resizedImage.createGraphics();

            Shape clip = new Ellipse2D.Float(0, 0, width, height);
            g2d.setClip(clip);
            g2d.drawImage(originalImage, 0, 0, width, height, null);

            if ("Online".equals(userStatus)) {
                g2d.setColor(Color.GREEN);
                g2d.setStroke(new BasicStroke(1));
                g2d.drawOval(0, 0, width - 1, height - 1);
            }

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
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
            return dateTime.format(formatter);
        }
    }

    private static JLabel createUserStatusDot(String userStatus) {
        JLabel statusLabel = new JLabel();
        statusLabel.setPreferredSize(new Dimension(20, 20));

        ImageIcon icon = null;

        if ("Online".equals(userStatus)) {
            icon = new ImageIcon("client/src/main/resources/icon/chatWhenOnline.png");
        } else if ("Offline".equals(userStatus)) {
            icon = new ImageIcon("client/src/main/resources/icon/chatWhenOffline.png");
        }
        if (icon != null) {
            Image scaledImage = icon.getImage().getScaledInstance(10, 10, Image.SCALE_SMOOTH);
            ImageIcon scaledIcon = new ImageIcon(scaledImage);
            statusLabel.setIcon(scaledIcon);
        } else {
            statusLabel.setVisible(false);
        }
        return statusLabel;
    }

}
