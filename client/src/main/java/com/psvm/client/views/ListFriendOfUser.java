package com.psvm.client.views;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDateTime;

public class ListFriendOfUser extends JPanel {
    private JPanel currentSelectedFriend;
    ListFriendOfUser(){
        this.setBackground(new Color(0,0,0,0));
        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        //friendList.add(friendSearchAndAdd);

        for (int i = 1; i <= 10; i++) {
            UserEachFriend userEachFriend = new UserEachFriend("af","tenne","hehe", LocalDateTime.of(2005,12,4, 1,40),"Offline");
            this.add(userEachFriend);
            // Add vertical spacing between components
            this.add(Box.createVerticalStrut(10));
        }
        this.add(Box.createVerticalGlue());
    }
    private void addHoverEffect(){
        
    }
}
