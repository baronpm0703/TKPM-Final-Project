package com.psvm.client.views;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

public class AddFriendIconButton extends JButton {
    static Icon icon = new ImageIcon("client/src/main/resources/icon/addFriend.png");

    AddFriendIconButton() {
        super(icon);

        addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showOptionsPopupMenu();
            }
        });
    }

    private void showOptionsPopupMenu() {
        JPopupMenu popupMenu = new JPopupMenu();
        JMenuItem option1 = new JMenuItem("Thêm bạn bè");
        JMenuItem option2 = new JMenuItem("Tạo nhóm");

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
                System.out.println("Tạo nhóm selected");
                // Add your Option 2 functionality here
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
}
