package com.psvm.client.views;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class AddFriendIconButton extends JButton {
    static Icon icon = new ImageIcon("client/src/main/resources/icon/addFriend.png");
    AddFriendIconButton(){
        super(icon);

        addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                addFriendDialog();
            }
        });
    }
    private void addFriendDialog(){
        JDialog dialog = new JDialog((Frame) null,"Thêm bạn bè", true);
        dialog.setLayout(new BorderLayout());
        dialog.setSize(400, 300);
        dialog.setLocationRelativeTo(null);
        dialog.setVisible(true);

    }
}
