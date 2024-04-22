package com.psvm.client.views;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class FriendsChat extends JPanel {
    private String avatar;
    private final String senderName;
    private final String senderText;
    private final LocalDateTime initialTime;

    public FriendsChat(String avatar, String senderName, String senderText, LocalDateTime initialTime) {
        this.senderName = senderName;
        this.senderText = senderText;
        this.initialTime = initialTime;
        this.avatar = avatar;
        this.avatar = "client/src/main/resources/icon/avatar_sample.jpg";
        this.setBorder(new EmptyBorder(0, 0, 0, 0));
        setLayout(new GridBagLayout());
        this.setBackground(Color.WHITE);
        initialize();
    }

    public void initialize() {
        // Avatar on the left
        ImageIcon avatarIcon = createCircularAvatar(avatar, 40, 40);
        JLabel avatarLabel = new JLabel(avatarIcon);
        GridBagConstraints gbcAvatar = new GridBagConstraints();
        gbcAvatar.gridx = 0;
        gbcAvatar.gridy = 0;
        gbcAvatar.gridheight = 3;
        gbcAvatar.insets = new Insets(5, 5, 5, 5);
        add(avatarLabel, gbcAvatar);

        // Name of user
        JLabel senderNameLabel = new JLabel(senderName);
        senderNameLabel.setForeground(Color.decode("#1D9745"));
        GridBagConstraints gbcLabel = new GridBagConstraints();
        gbcLabel.gridx = 1;
        gbcLabel.gridy = 0;
        gbcLabel.anchor = GridBagConstraints.WEST;
        gbcLabel.insets = new Insets(5, 5, 5, 5);
        add(senderNameLabel, gbcLabel);

        // ChatText
        JTextArea textArea = new JTextArea(0, Math.min(senderText.length(), 20));
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        textArea.setText(senderText);
        textArea.setEditable(false);
        textArea.setBackground(new Color(0xEEF1F4));
        textArea.setBorder(new LineBorder(new Color(0, 0, 0, 0), 1, true)); // Rounded border
        GridBagConstraints gbcTextArea = new GridBagConstraints();
        gbcTextArea.gridx = 1;
        gbcTextArea.gridy = 1;
        gbcTextArea.gridwidth = 2;
        gbcTextArea.fill = GridBagConstraints.VERTICAL; // Allow vertical expansion
        gbcTextArea.insets = new Insets(5, 5, 5, 5);
        add(textArea, gbcTextArea);

        // Time
        JLabel timeLabel = new JLabel();
        timeLabel.setText(formatLocalDateTime(initialTime));
        timeLabel.setForeground(Color.decode("#616C76"));
        GridBagConstraints gbcTimeLabel = new GridBagConstraints();
        gbcTimeLabel.gridx = 1;
        gbcTimeLabel.gridy = 2;
        gbcTimeLabel.anchor = GridBagConstraints.WEST;
        gbcTimeLabel.insets = new Insets(5, 5, 5, 5);
        add(timeLabel, gbcTimeLabel);
    }

    private static String formatLocalDateTime(LocalDateTime dateTime) {
        LocalDateTime now = LocalDateTime.now();

        if (dateTime.toLocalDate().isEqual(now.toLocalDate())) {
            // Same day
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
            return dateTime.format(formatter);
        } else {
            // Different day
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm dd-MM-yyyy");
            return dateTime.format(formatter);
        }
    }
    private static ImageIcon createCircularAvatar(String imagePath, int width, int height) {
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

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Friends Chat Example");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            FriendsChat friendsChat = new FriendsChat("path_to_avatar_image.jpg", "Kizark", "Initial TextInitial TextInitial TextInitial TextI" +
                    "nitial TextInitial TextInitial TextInitial " +
                    "nitial TextInitial TextInitial TextInitial Text" +
                    "nitial TextInitial TextInitial TextInitial Textnitial TextInitial TextInitial TextInitial TextTextInitial TextInitial Text", LocalDateTime.now());
            frame.getContentPane().add(friendsChat);
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
}