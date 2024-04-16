package com.psvm.client.views;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDateTime;

class FriendListBar extends JPanel{
    //Chat of selected Friend panel
    //private final ContentPane contentPane;
    private JButton selectedButton;
    //private JButton currentButton;

    //later for chat of selected Friend
    //FriendList(ContentPane contentPane)
    FriendListBar() {
        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        this.setOpaque(false);
        this.setPreferredSize(new Dimension(320, 820));

        // Add header
        FriendListHeader friendListHeader = new FriendListHeader();
        this.add(friendListHeader);
        // friend search and add friend
        SearchFriendField searchFriendField = new SearchFriendField();
        AddFriendIconButton addFriendIconButton = new AddFriendIconButton();
        JPanel friendSearchAndAdd = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        friendSearchAndAdd.setBackground(Color.WHITE);
        friendSearchAndAdd.add(searchFriendField);
        friendSearchAndAdd.add(addFriendIconButton);
        this.add(friendSearchAndAdd);
        // Friend list
        ListFriendOfUser listFriendOfUser = new ListFriendOfUser();
        JScrollPane scrollFriend = new JScrollPane(listFriendOfUser);
        scrollFriend.setBorder(null);
        scrollFriend.setPreferredSize(new Dimension(320, 630));
        scrollFriend.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);

        this.add(scrollFriend);
    }
}