package com.psvm.client.views;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.text.DefaultCaret;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.time.LocalDateTime;

public class ChatBody extends JPanel {
    private JPanel chatBody;
    private JScrollPane scrollPane;
    private int currentRow = 0;

    public ChatBody() {
        this.setBackground(Color.WHITE);
        this.setPreferredSize(new Dimension(840, 764));
        this.setBorder(new EmptyBorder(0, 0, 0, 0));
        scrollPane = initUIChatBody();

        this.add(scrollPane, BorderLayout.CENTER);
        JPanel inputTextArea = initUIInputTextArea();
        this.add(inputTextArea, BorderLayout.SOUTH);
        addLeft("heheheheh");
        addLeft("hehehehehehehehehe");
        addRight("hahahahahaahahaahahahahahaha");
        addLeft("hahhahahahahahahahahahahahahahhahahahahahahahahahahahahahaahahahhahahahahahahahahahahahahahaahahahhahahahahahahahahahahahahahaahahahaaha");
        addRight("hahhahahahahaha");
        addLeft("hehehehehehehehehheehehehe");
        addRight("hahhahahahahahahahahahahahahahhahahahahahahahahahahahahahaahahahhahahahahahahahahahahahahahaahahahhahahahahahahahahahahahahahaahahahaaha");
        addRight("hahhahahahahaha");
    }
    void scrollToTheEnd() {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                JScrollBar verticalScrollBar = scrollPane.getVerticalScrollBar();
                verticalScrollBar.setValue(verticalScrollBar.getMaximum());
            }
        });
    }

    private JScrollPane initUIChatBody() {
        chatBody = new JPanel(new GridBagLayout());
        chatBody.setBackground(Color.WHITE);

        JScrollPane chatScrollPane = new JScrollPane(chatBody);
        chatScrollPane.setPreferredSize(new Dimension(840,700));
        chatScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        chatScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

        return chatScrollPane;
    }

    private JPanel initUIInputTextArea() {
        JPanel inputTextArea = new JPanel(new GridBagLayout());
        inputTextArea.setPreferredSize(new Dimension(840, 64));
        inputTextArea.setBorder(new EmptyBorder(0, 0, 0, 0));
        inputTextArea.setBackground(Color.WHITE);

        JTextArea chatInput = new JTextArea(1, 50);
        chatInput.setLineWrap(true);
        chatInput.setWrapStyleWord(true);
        chatInput.setBorder(new LineBorder(new Color(0, 0, 0, 0), 1, true)); // Rounded border

        JScrollPane chatScrollPane = new JScrollPane(chatInput);
        chatScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        GridBagConstraints gbcChatInput = new GridBagConstraints();
        gbcChatInput.gridx = 0;
        gbcChatInput.gridy = 0;
        gbcChatInput.weightx = 1.0; // Take 100% of the width
        gbcChatInput.weighty = 1.0; // Allow vertical expansion
        gbcChatInput.fill = GridBagConstraints.BOTH; // Allow both horizontal and vertical expansion
        inputTextArea.add(chatScrollPane, gbcChatInput);

        // send
        ImageIcon sendIcon = new ImageIcon(new ImageIcon("client/src/main/resources/icon/send-button.png").getImage().getScaledInstance(40, 40, Image.SCALE_SMOOTH));
        JButton submitButton = userSendMessage(sendIcon, chatInput);

        GridBagConstraints gbcSubmitButton = new GridBagConstraints();
        gbcSubmitButton.gridx = 1;
        gbcSubmitButton.gridy = 0;
        gbcSubmitButton.weighty = 1.0; // Allow vertical expansion
        inputTextArea.add(submitButton, gbcSubmitButton);

        return inputTextArea;
    }
    private JButton userSendMessage(ImageIcon sendIcon, JTextArea chatInput) {
        JButton submitButton = new JButton(sendIcon);
        submitButton.setBackground(Color.WHITE);
        submitButton.setBorder(new EmptyBorder(0, 0, 0, 0));
        submitButton.setFocusPainted(false);
        submitButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String text = chatInput.getText();
                if (!text.isEmpty()) {
                    //Them cac parameter khac
                    addRight(text);
                    chatInput.setText("");
                }
            }
        });
        return submitButton;
    }

    private void addLeft(String msg) {
        FriendsChat friendsChat = new FriendsChat("avatar_path", "Kizark", msg, LocalDateTime.now());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = currentRow++;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(0, 0, 10, 30);
        chatBody.add(friendsChat, gbc);

        scrollPane.revalidate();
        scrollPane.repaint();
        scrollToTheEnd();
    }

    private void addRight(String msg) {
        UsersChat friendsChat = new UsersChat (msg, LocalDateTime.now());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = currentRow++;
        gbc.anchor = GridBagConstraints.EAST;
        gbc.insets = new Insets(0, 30, 10, 0);
        chatBody.add(friendsChat, gbc);

        scrollPane.revalidate();
        scrollPane.repaint();
        scrollToTheEnd();
    }

}