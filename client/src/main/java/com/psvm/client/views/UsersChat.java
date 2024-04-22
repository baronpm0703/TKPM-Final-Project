package com.psvm.client.views;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class UsersChat extends JPanel {

    private String senderText;
    private LocalDateTime initialTime;

    public UsersChat(String senderText, LocalDateTime initialTime) {
        this.senderText = senderText;
        this.initialTime = initialTime;
        setLayout(new GridBagLayout());
        this.setBackground(Color.WHITE);
        initialize();
    }

    private void initialize() {
        // ChatText
        JTextArea textArea = new JTextArea(0, Math.min(senderText.length(), 20));
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        textArea.setText(senderText);
        textArea.setEditable(false);
        textArea.setBackground(new Color(0xEEF1F4));
        textArea.setBorder(new LineBorder(new Color(0, 0, 0, 0), 1, true)); // Rounded border
        GridBagConstraints gbcTextArea = new GridBagConstraints();
        gbcTextArea.gridx = 0;
        gbcTextArea.gridy = 0;
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
        gbcTimeLabel.gridy = 1;
        gbcTimeLabel.anchor = GridBagConstraints.EAST; // Align to the right
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
            JFrame frame = new JFrame("User Chat Example");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            UsersChat userChat = new UsersChat("Initial TextInitial TextInitial TextInitial TextI" +
                    "nitial TextInitial TextInitial TextInitial " +
                    "nitial TextInitial TextInitial TextInitial Text" +
                    "nitial TextInitial TextInitial TextInitial Textnitial TextInitial TextInitial TextInitial TextTextInitial TextInitial Text", LocalDateTime.now());
            frame.getContentPane().add(userChat);
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
}
