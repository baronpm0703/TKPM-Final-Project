package com.psvm.client.views;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.LocalDateTime;

public class ListFriendOfUser extends JPanel {
    private UserEachFriend currentSelectedFriend;
    ListFriendOfUser(){
        this.setBackground(Color.white);
        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        //friendList.add(friendSearchAndAdd);

        for (int i = 1; i <= 10; i++) {
            UserEachFriend userEachFriend = new UserEachFriend("af","tenne","hehe", LocalDateTime.of(2005,12,4, 1,40),"Offline");
            addHoverEffect(userEachFriend);
            this.add(userEachFriend);
        }
        this.add(Box.createVerticalGlue());
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
            }
        });
    }
}
