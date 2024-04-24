package com.psvm.client.views;

import com.psvm.client.controllers.GetUserRequest;
import com.psvm.client.controllers.MessageSeenRequest;
import com.psvm.client.controllers.SendMessageRequest;
import com.psvm.client.controllers.SingleFriendChatLogRequest;
import com.psvm.client.settings.LocalData;
import com.psvm.shared.socket.SocketResponse;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.*;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

class ChatBodyThread extends SwingWorker<Void, Map<String, Object>> {
    private Socket clientSocket;
    ObjectInputStream socketIn;
    ObjectOutputStream socketOut;
    private String conversationId;
    private Observer observer;

    public interface Observer {
        public void workerDidUpdate(String conversationId, Vector<Map<String, Object>> message);
    }

    public ChatBodyThread(Socket clientSocket, ObjectInputStream socketIn, ObjectOutputStream socketOut, Observer observer) {
        this.clientSocket = clientSocket;
        this.socketIn = socketIn;
        this.socketOut = socketOut;
        this.conversationId = LocalData.getSelectedConversation();

        this.observer = observer;
    }

    @Override
    protected Void doInBackground() throws Exception {
        SingleFriendChatLogRequest request = new SingleFriendChatLogRequest(clientSocket, socketIn, socketOut, conversationId);
        SocketResponse response = request.talk();

        for (Map<String, Object> datum: response.getData()) {
            publish(datum);
        }
        if (response.getData().isEmpty()) publish();

        return null;
    }

    @Override
    protected void process(List<Map<String, Object>> chunks) {
        super.process(chunks);

        Vector<Map<String, Object>> messages = new Vector<>(chunks);
        observer.workerDidUpdate(conversationId, messages);
    }
}

class MessageSeenThread extends SwingWorker<Void, String> {
    private Socket clientSocket;
    ObjectInputStream socketIn;
    ObjectOutputStream socketOut;
    private String conversationId;
    private Observer observer;

    public interface Observer {
        public void workerDidUpdate();
    }

    public MessageSeenThread(Socket clientSocket, ObjectInputStream socketIn, ObjectOutputStream socketOut, Observer observer) {
        this.clientSocket = clientSocket;
        this.socketIn = socketIn;
        this.socketOut = socketOut;
        this.conversationId = LocalData.getSelectedConversation();

        this.observer = observer;
    }

    @Override
    protected Void doInBackground() throws Exception {
        MessageSeenRequest request = new MessageSeenRequest(clientSocket, socketIn, socketOut, conversationId);
        SocketResponse response = request.talk();
        publish("done");

        return null;
    }

    @Override
    protected void process(List<String> chunks) {
        super.process(chunks);

        observer.workerDidUpdate();
    }
}

class SendMessageButtonThread extends Thread {
    private Socket clientSocket;
    ObjectInputStream socketIn;
    ObjectOutputStream socketOut;

    private String conversationId, content;

    private int responseCode;

    public SendMessageButtonThread(Socket clientSocket, ObjectInputStream socketIn, ObjectOutputStream socketOut, String conversationId, String content) {
        this.clientSocket = clientSocket;
        this.socketIn = socketIn;
        this.socketOut = socketOut;

        this.conversationId = conversationId;
        this.content = content;
    }

    @Override
    public void run() {
        super.run();
        SendMessageRequest request = new SendMessageRequest(clientSocket, socketIn, socketOut, conversationId, content);
        System.out.println("response.getData()");
        SocketResponse response = request.talk();

        responseCode = response.getResponseCode();
    }

    public int getResponseCode() {
        return responseCode;
    }
}

public class ChatBody extends JPanel {
    // Multithreading + Socket
    ScheduledExecutorService service = Executors.newScheduledThreadPool(2);
    final String SOCKET_HOST = "localhost";
    final int SOCKET_PORT = 5555;
    Socket chatBodySocket;
    ObjectInputStream chatBodySocketIn;
    ObjectOutputStream chatBodySocketOut;
    Socket messageSeenSocket;
    ObjectInputStream messageSeenSocketIn;
    ObjectOutputStream messageSeenSocketOut;

    private JPanel chatBody;
    private JScrollPane scrollPane;
    private int currentRow = 0;

    private Vector<Map<String, Object>> totalMessages = new Vector<>();

    private String currentConversationId;
    private String previousConversationId;

    public ChatBody() {
        /* Initialize this GUI component */
        this.setBackground(Color.WHITE);
        this.setPreferredSize(new Dimension(840, 764));
        this.setBorder(new EmptyBorder(0, 0, 0, 0));
        scrollPane = initUIChatBody();

        this.add(scrollPane, BorderLayout.CENTER);
        JPanel inputTextArea = initUIInputTextArea();
        this.add(inputTextArea, BorderLayout.SOUTH);

        /* Multithreading + Socket */
        try {
            chatBodySocket = new Socket(SOCKET_HOST, SOCKET_PORT);
            chatBodySocketIn = new ObjectInputStream(chatBodySocket.getInputStream());
            chatBodySocketOut = new ObjectOutputStream(chatBodySocket.getOutputStream());
            messageSeenSocket = new Socket(SOCKET_HOST, SOCKET_PORT);
            messageSeenSocketIn = new ObjectInputStream(messageSeenSocket.getInputStream());
            messageSeenSocketOut = new ObjectOutputStream(messageSeenSocket.getOutputStream());
            startNextChatBodyWorker();
            startNextMessageSeenWorker();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
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

        JPanel thisPanel = this;
        submitButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String text = chatInput.getText();
                if (!text.isEmpty()) {
                    SendMessageButtonThread sendMessageButtonThread = new SendMessageButtonThread(chatBodySocket, chatBodySocketIn, chatBodySocketOut, currentConversationId, text);
                    sendMessageButtonThread.start();

                    try {
                        sendMessageButtonThread.join();

                        if (sendMessageButtonThread.getResponseCode() == SocketResponse.RESPONSE_CODE_FAILURE) {
                            JOptionPane.showConfirmDialog(thisPanel, "Gửi tin nhắn thất bại", "Error", JOptionPane.DEFAULT_OPTION);
                        }
                    } catch (InterruptedException ex) {
                        System.out.println("Exception thrown while sending message in " + this.getClass().getSimpleName() + ": " + ex.getMessage());
                    }
                    finally {
                        chatInput.setText("");
                    }
                }
            }
        });
        return submitButton;
    }

    private void addLeft(String msg, String sender, LocalDateTime sendTime) {
        FriendsChat friendsChat = new FriendsChat("avatar_path", sender, msg, sendTime);
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

    private void addRight(String msg, LocalDateTime sendTime) {
        UsersChat friendsChat = new UsersChat (msg, sendTime);
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

    protected void startNextChatBodyWorker() {
        JPanel thisPanel = this;

        // Thread to update chat body
        ChatBodyThread chatBodyThreadWorker = new ChatBodyThread(chatBodySocket, chatBodySocketIn, chatBodySocketOut, new ChatBodyThread.Observer() {
            @Override
            public void workerDidUpdate(String conversationId, Vector<Map<String, Object>> messages) {
                /* Update variables */
                currentConversationId = conversationId;

                // Update GUI
                SwingUtilities.invokeLater(() -> {
                    // If conversationId changes, it means the client has selected another conversation=
                    if (!conversationId.equals(previousConversationId)) {
                        chatBody.removeAll();
                        totalMessages.clear();
                        previousConversationId = conversationId;
                    }

                    messages.removeAll(totalMessages);
                    totalMessages.addAll(messages);

                    for (Map<String, Object> message: messages) {
                        if (message.get("SenderId").equals(LocalData.getCurrentUsername()))
                            addRight((String) message.get("Content"), ((Timestamp) message.get("Datetime")).toLocalDateTime());
                        else
                            addLeft((String) message.get("Content"), (String) message.get("SenderId"), ((Timestamp) message.get("Datetime")).toLocalDateTime());
                    }

                    thisPanel.revalidate();
                    thisPanel.repaint();
                });
            }
        });
        chatBodyThreadWorker.addPropertyChangeListener(new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                if (chatBodyThreadWorker.getState() == SwingWorker.StateValue.DONE) {
                    chatBodyThreadWorker.removePropertyChangeListener(this);
                    startNextChatBodyWorker();
                }
            }
        });

        // Scheduling
        service.schedule(chatBodyThreadWorker, 250, TimeUnit.MILLISECONDS);
    }

    protected void startNextMessageSeenWorker() {
        JPanel thisPanel = this;

        // Thread to update message's seen state
        MessageSeenThread messageSeenThreadWorker = new MessageSeenThread(messageSeenSocket, messageSeenSocketIn, messageSeenSocketOut, new MessageSeenThread.Observer() {
            @Override
            public void workerDidUpdate() {}
        });
        messageSeenThreadWorker.addPropertyChangeListener(new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                if (messageSeenThreadWorker.getState() == SwingWorker.StateValue.DONE) {
                    messageSeenThreadWorker.removePropertyChangeListener(this);
                    startNextMessageSeenWorker();
                }
            }
        });

        // Scheduling
        service.schedule(messageSeenThreadWorker, 300, TimeUnit.MILLISECONDS);
    }
}