package com.psvm.client.views;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class DetailOfAGroup extends JPanel {
    private String avatar;
    private String name;

    //nhớ sửa cái này
    boolean isAdmin = true;


    DetailOfAGroup(String avatar, String name) {
        this.setPreferredSize(new Dimension(250, 754));
        this.setBackground(Color.WHITE);
        this.avatar = avatar;
        this.name = name;

        initialize();
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
                    nameField.setText(newName);
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
        JTextArea searchField = new JTextArea(0, 20);
        gbc.weightx = 1.0;
        searchField.setLineWrap(true);
        searchField.setWrapStyleWord(true);
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

        gbc.gridy++;
        JButton encodeChat = new JButton("Mã Hoá");
        encodeChat.setForeground(Color.BLUE);
        encodeChat.setFocusPainted(false);
        add(encodeChat, gbc);

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
                    // giữ hay bỏ gì tuỳ cái dialog này tuỳ ko quan trọng
                    JOptionPane.showMessageDialog(null, "Rời khỏi nhóm...");
                }
            }
        });
        encodeChat.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int response = JOptionPane.showConfirmDialog(null,
                        "Bạn có muốn mã hoá nhóm chat?",
                        "Xác nhận", JOptionPane.YES_NO_OPTION);
                if (response == JOptionPane.YES_OPTION) {

                    // giữ hay bỏ gì tuỳ cái dialog này tuỳ ko quan trọng
                    JOptionPane.showMessageDialog(null, "Mã hoá...");
                }
            }
        });
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
            }

            @Override
            public void focusLost(FocusEvent e) {
                if (searchFriendField.getText().isEmpty()) {
                    searchFriendField.setText(placeHolder);
                }
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
        ListFriendInSearchDialog listFriendInSearchDialog = new ListFriendInSearchDialog();
        JScrollPane scrollFriend = new JScrollPane(listFriendInSearchDialog);
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
        JDialog memberListDialog = new JDialog((Frame) null, "Danh Sách Thành Viên", true);
        memberListDialog.setSize(400, 300);
        memberListDialog.setLocationRelativeTo(null);

        // Create a tabbed pane
        JTabbedPane tabbedPane = new JTabbedPane();

        // Create content for the Thành viên tab
        JPanel memberPanel = new JPanel();
        memberPanel.setLayout(new GridLayout(0, 1)); // One column, multiple rows

        //Chỗ này để hiện data member
        for (int i = 0; i < 4; i++) {
            memberPanel.add(memberPanel("Member " + (i + 1))); // Adjust label text accordingly
        }

        JScrollPane memberScrollPane = new JScrollPane(memberPanel);
        tabbedPane.addTab("Thành viên", memberScrollPane);


        JPanel adminPanel = new JPanel();
        adminPanel.setLayout(new GridLayout(0, 1)); // One column, multiple rows

        //Chỗ này để hiện admin
        for (int i = 0; i < 10; i++) {
            adminPanel.add(adminPanel("Admin " + (i + 1))); // Adjust label text accordingly
        }

        JScrollPane adminScrollPane = new JScrollPane(adminPanel);
        tabbedPane.addTab("Admin", adminScrollPane);

        // Add the tabbed pane to the content panel
        memberListDialog.add(tabbedPane);

        // Show the dialog
        memberListDialog.setVisible(true);
    }

    JPanel adminPanel(String labelText) {
        JPanel adminPanel = new JPanel();
        adminPanel.setBackground(Color.WHITE);
        adminPanel.setPreferredSize(new Dimension(300, 50));
        adminPanel.setLayout(new BorderLayout());

        // Add avatar to the left
        ImageIcon avatarIcon = createCircularAvatar("client/src/main/resources/icon/avatar_sample.jpg", 30, 30);
        JLabel avatarLabel = new JLabel(avatarIcon);
        avatarLabel.setBorder(new EmptyBorder(10, 10, 10, 10));
        adminPanel.add(avatarLabel, BorderLayout.WEST);

        // Create JLabel
        JLabel label = new JLabel(labelText);
        label.setBorder(new EmptyBorder(20, 0, 20, 20)); // Increase the right inset for more spacing
        adminPanel.add(label, BorderLayout.CENTER);

        ImageIcon icon;
        Image image;
        ImageIcon resizedIcon;
        // Create JButton
        if (isAdmin) {
            icon = new ImageIcon("client/src/main/resources/icon/cancelled.png");
            image = icon.getImage().getScaledInstance(30, 30, Image.SCALE_SMOOTH);
            resizedIcon = new ImageIcon(image);
            JButton button = new JButton(resizedIcon);
            button.setFocusPainted(false);
            button.setBackground(Color.WHITE);
            button.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    int response = JOptionPane.showConfirmDialog(null,
                            "Bạn có chắc giáng chức người này xuống thành thành viên?",
                            "Xác nhận", JOptionPane.YES_NO_OPTION);
                    if (response == JOptionPane.YES_OPTION) {
                        // giữ hay bỏ gì tuỳ cái dialog này tuỳ ko quan trọng
                        JOptionPane.showMessageDialog(null, "Giáng chức xuống member...");
                    }
                }
            });
            adminPanel.add(button, BorderLayout.EAST);
        }
        return adminPanel;
    }

    JPanel memberPanel(String labelText) {
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

        ImageIcon icon;
        Image image;
        ImageIcon resizedIcon;
        // Create JButton
        if (isAdmin) {
            icon = new ImageIcon("client/src/main/resources/icon/acceptFriendRequest.png");
            image = icon.getImage().getScaledInstance(30, 30, Image.SCALE_SMOOTH);
            resizedIcon = new ImageIcon(image);
            JButton button = new JButton(resizedIcon);
            button.setFocusPainted(false);
            button.setBackground(Color.WHITE);
            button.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    int response = JOptionPane.showConfirmDialog(null,
                            "Bạn có chắc muốn bổ nhiệm người này thành admin?",
                            "Xác nhận", JOptionPane.YES_NO_OPTION);
                    if (response == JOptionPane.YES_OPTION) {
                        // giữ hay bỏ gì tuỳ cái dialog này tuỳ ko quan trọng
                        JOptionPane.showMessageDialog(null, "Bổ nhiệm thành admin...");
                    }
                }
            });
            memberPanel.add(button, BorderLayout.EAST);
        }
        return memberPanel;
    }
    private JSeparator createSeparator() {
        JSeparator separator = new JSeparator(SwingConstants.HORIZONTAL);
        separator.setForeground(Color.CYAN);
        return separator;
    }
}
