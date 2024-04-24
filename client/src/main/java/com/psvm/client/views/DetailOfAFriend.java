package com.psvm.client.views;

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
import java.rmi.ConnectIOException;

public class DetailOfAFriend extends JPanel {
    private String avatar;
    private String name;
    private String username;
    private boolean blocked = false;

    DetailOfAFriend(String avatar, String name, String username) {
        this.setPreferredSize(new Dimension(250, 754));
        this.setBackground(Color.WHITE);
        this.avatar = avatar;
        this.name = name;
        this.username = username;

        initialize();
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
        JTextArea searchField = new JTextArea(5, 20);
        gbc.weightx = 1.0;
        searchField.setLineWrap(true);
        searchField.setWrapStyleWord(true);
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


        searchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                // Handle text insertion
                System.out.println("Text inserted: " + searchField.getText());
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                // Handle text removal
                System.out.println("Text removed: " + searchField.getText());
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
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

                    // giữ hay bỏ gì tuỳ cái dialog này tuỳ ko quan trọng
                    JOptionPane.showMessageDialog(null, "Xoá lịch sử chat...");
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
