package com.psvm.client.views.components.friend;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.Array;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

public class ListFriendOfUser extends JPanel {
    // Global collections of messages of the 3 types: Unseen while online, Unseen while offline, Seen
    private Vector<Map<String, Object>> totalUnseenOnlineMessages = new Vector<>();
    private Vector<Map<String, Object>> totalUnseenOfflineMessages = new Vector<>();
    private Vector<Map<String, Object>> totalSeenMessages = new Vector<>();

    // Separate JPanels to display these messages
    private int unseenOnlineMessagesIndex = 0;
    private int unseenOfflineMessagesIndex = 0;
    private int seenMessagesIndex = 0;
    private ArrayList<String> messageIndexer = new ArrayList<>();

    private JPanel currentSelectedFriend;
    private String currentSelectedFriendId;
    ListFriendOfUser(){
        // Initialize this GUI component
        setBackground(new Color(255,255,255,255));
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
    }
    private void addHoverEffect(UserEachFriend friend) {
        friend.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent evt) {
                if (friend != currentSelectedFriend) {
                    friend.setBackground(Color.LIGHT_GRAY);
                }
            }
            public void mouseExited(MouseEvent evt) {
                if (friend != currentSelectedFriend) {
                    friend.setBackground(Color.WHITE);
                }

            }
            public void mouseClicked(MouseEvent evt) {
                if (currentSelectedFriend != null){
                    currentSelectedFriend.setBackground(Color.WHITE);
                }
                // do sth

                friend.setBackground(Color.decode("#ADD8E6"));
                currentSelectedFriend = friend;
                currentSelectedFriendId = friend.getConversationId();
            }
        });
    }

    // Move message component at childIndex to 3 message type areas, then return the new index of the component
    int moveMessage(int childIndex, int newMessageType) {
        System.out.println(childIndex + " " + newMessageType);
        UserEachFriend conversation = (UserEachFriend) getComponent(childIndex);

        remove(conversation);

        switch (newMessageType) {
            case 1: {
                String conversationId = messageIndexer.get(childIndex);
                messageIndexer.remove(conversationId);
                messageIndexer.add(0, conversationId);

                add(conversation, 0);
                return 0;
            }
            case 2: {
                String conversationId = messageIndexer.get(childIndex);
                messageIndexer.remove(conversationId);

                if (unseenOnlineMessagesIndex <= messageIndexer.size() - 1)
                    messageIndexer.add(unseenOnlineMessagesIndex, conversationId);
                else messageIndexer.add(conversationId);

                add(conversation, unseenOnlineMessagesIndex);
                return unseenOnlineMessagesIndex;
            }
            case 3: {
                String conversationId = messageIndexer.get(childIndex);
                messageIndexer.remove(conversationId);

                if (unseenOnlineMessagesIndex + unseenOfflineMessagesIndex <= messageIndexer.size() - 1)
                    messageIndexer.add(unseenOnlineMessagesIndex + unseenOfflineMessagesIndex, conversationId);
                else messageIndexer.add(conversationId);

                add(conversation, unseenOnlineMessagesIndex + unseenOfflineMessagesIndex);
                return unseenOnlineMessagesIndex + unseenOfflineMessagesIndex;
            }
        }

        return -1;
    }

    public void resetList() {
        JPanel thisPanel = this;
        totalUnseenOnlineMessages.clear();
        totalUnseenOfflineMessages.clear();
        totalSeenMessages.clear();
        unseenOnlineMessagesIndex = 0;
        unseenOfflineMessagesIndex = 0;
        seenMessagesIndex = 0;
        messageIndexer.clear();
        SwingUtilities.invokeLater(() -> {
            thisPanel.removeAll();
            thisPanel.revalidate();
            thisPanel.repaint();
        });
    }

    public void manuallySelectMessage(String conversationId) {
        UserEachFriend selectMessage = (UserEachFriend) getComponent(messageIndexer.indexOf(conversationId));

        selectMessage.setBackground(Color.decode("#ADD8E6"));
        currentSelectedFriend = selectMessage;
    }

    public void setData(Vector<Map<String, Object>> friends) {
        JPanel thisPanel = this;
        SwingUtilities.invokeLater(() -> {
            /* Add unseen messages while online */
            Vector<Map<String, Object>> unseenOnlineMessages = (Vector<Map<String, Object>>) friends.get(0).get("data");
            // If unseenOnlineMessages is empty after this line then the data is the same
            unseenOnlineMessages.removeAll(totalUnseenOnlineMessages);
            totalUnseenOnlineMessages.addAll(unseenOnlineMessages);
            for (Map<String, Object> friend: unseenOnlineMessages)
            {
                String convoName = (Boolean.parseBoolean(friend.get("IsGroup").toString())) ? friend.get("ConversationName").toString() : friend.get("MemberId").toString();
                if (!messageIndexer.contains(friend.get("ConversationId").toString())) {
                    UserEachFriend userEachFriend = new UserEachFriend(friend.get("ConversationId").toString(), "af", convoName, friend.get("Content").toString(), ((Timestamp) friend.get("Datetime")).toLocalDateTime(),"Online");
                    thisPanel.add(userEachFriend, unseenOnlineMessagesIndex);
                    messageIndexer.add(unseenOnlineMessagesIndex, friend.get("ConversationId").toString());
                    addHoverEffect(userEachFriend);
                }
                else {
                    int newIndex = moveMessage(messageIndexer.indexOf(friend.get("ConversationId").toString()), 1);
                    System.out.println(newIndex);
                    UserEachFriend userEachFriend = (UserEachFriend) thisPanel.getComponent(newIndex);
                    userEachFriend.setData(convoName, friend.get("Content").toString(), ((Timestamp) friend.get("Datetime")).toLocalDateTime(),"Online");
                }

                // Manually set the selected effect
                if (currentSelectedFriendId != null && currentSelectedFriendId.equals(friend.get("ConversationId").toString()))
                    manuallySelectMessage(currentSelectedFriendId);
                unseenOnlineMessagesIndex++;
            }

            /* Add unseen messages while offline */
            Vector<Map<String, Object>> unseenOfflineMessages = (Vector<Map<String, Object>>) friends.get(1).get("data");
            // If seenMessages is empty after this line then the data is the same
            unseenOfflineMessages.removeAll(totalUnseenOfflineMessages);
            totalUnseenOfflineMessages.addAll(unseenOfflineMessages);
            for (Map<String, Object> friend: unseenOfflineMessages) {
                System.out.println(friend.get("ConversationId").toString());
                System.out.println(messageIndexer);
                String convoName = (Boolean.parseBoolean(friend.get("IsGroup").toString())) ? friend.get("ConversationName").toString() : friend.get("MemberId").toString();
                if (!messageIndexer.contains(friend.get("ConversationId").toString())) {
                    UserEachFriend userEachFriend = new UserEachFriend(friend.get("ConversationId").toString(), "af", convoName, friend.get("Content").toString(), ((Timestamp) friend.get("Datetime")).toLocalDateTime(),"Offline");
                    thisPanel.add(userEachFriend, unseenOnlineMessagesIndex + unseenOfflineMessagesIndex);
                    messageIndexer.add(unseenOnlineMessagesIndex + unseenOfflineMessagesIndex, friend.get("ConversationId").toString());
                    addHoverEffect(userEachFriend);
                }
                else {
                    int newIndex = moveMessage(messageIndexer.indexOf(friend.get("ConversationId").toString()), 2);
                    UserEachFriend userEachFriend = (UserEachFriend) thisPanel.getComponent(newIndex);
                    userEachFriend.setData(convoName, friend.get("Content").toString(), ((Timestamp) friend.get("Datetime")).toLocalDateTime(),"Offline");
                }

                // Manually set the selected effect
                if (currentSelectedFriendId != null && currentSelectedFriendId.equals(friend.get("ConversationId").toString()))
                    manuallySelectMessage(currentSelectedFriendId);
                unseenOfflineMessagesIndex++;
            }

            /* Add seen messages */
            Vector<Map<String, Object>> seenMessages = (Vector<Map<String, Object>>) friends.get(2).get("data");
            // If seenMessages is empty after this line then the data is the same
            seenMessages.removeAll(totalSeenMessages);
            totalSeenMessages.addAll(seenMessages);
            for (Map<String, Object> friend: seenMessages) {
                String convoName = (Boolean.parseBoolean(friend.get("IsGroup").toString())) ? friend.get("ConversationName").toString() : friend.get("MemberId").toString();
                if (!messageIndexer.contains(friend.get("ConversationId").toString())) {
                    UserEachFriend userEachFriend = new UserEachFriend(friend.get("ConversationId").toString(), "af", convoName, friend.get("Content").toString(), ((Timestamp) friend.get("Datetime")).toLocalDateTime(),"");
                    thisPanel.add(userEachFriend, unseenOnlineMessagesIndex + unseenOfflineMessagesIndex + seenMessagesIndex);
                    messageIndexer.add(unseenOnlineMessagesIndex + unseenOfflineMessagesIndex + seenMessagesIndex, friend.get("ConversationId").toString());
                    addHoverEffect(userEachFriend);
                }
                else {
                    int newIndex = moveMessage(messageIndexer.indexOf(friend.get("ConversationId").toString()), 3);
                    UserEachFriend userEachFriend = (UserEachFriend) thisPanel.getComponent(newIndex);
                    userEachFriend.setData(convoName, friend.get("Content").toString(), ((Timestamp) friend.get("Datetime")).toLocalDateTime(),"");
                }

                // Manually set the selected effect
                if (currentSelectedFriendId != null && currentSelectedFriendId.equals(friend.get("ConversationId").toString()))
                    manuallySelectMessage(currentSelectedFriendId);
                seenMessagesIndex++;
            }
            thisPanel.add(Box.createVerticalGlue());
            thisPanel.revalidate();
        });
    }
}
