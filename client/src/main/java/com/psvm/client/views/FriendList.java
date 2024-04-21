package com.psvm.client.views;

import com.psvm.server.view.ContentPane;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

class FriendList extends JPanel{
    //Chat of selected Friend panel
    //private final ContentPane contentPane;
    private JButton selectedButton;
    //private JButton currentButton;

    //later for chat of selected Friend
    //FriendList(ContentPane contentPane)
    FriendList(){
        //Set chat of friend later
        //this.contentPane = contentPane;
        //left sideMenu
        this.setLayout(new BorderLayout());
        this.setOpaque(false);
        //set size for panel is kinda useless
        this.setPreferredSize(new Dimension(320,820));



        //add header
        FriendListHeader friendListHeader = new FriendListHeader();
        this.add(friendListHeader,BorderLayout.NORTH);

        //add footer


        //add friend
        JPanel friendList = new JPanel();
        int columns = 1; // Single column for vertical expanding
        friendList.setLayout(new GridLayout(0, columns, 10, 10));

        for (int i = 1; i <= 20; i++) {
            JLabel button = new JLabel("Button " + i);
            friendList.add(button);
        }

        JScrollPane scrollFriend = new JScrollPane(friendList);
        this.add(scrollFriend, BorderLayout.SOUTH);
    }
}