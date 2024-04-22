package com.psvm.client.views;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.LocalDateTime;

public class ListFriendInSearchDialog extends JPanel {
    public ListFriendInSearchDialog(){
        this.setBackground(Color.white);
        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        //friendList.add(friendSearchAndAdd);

        for (int i = 1; i <= 10; i++) {
            FriendInSearchDialog friendInSearchDialog = new FriendInSearchDialog("asdasd","asd","Kizark");
            this.add(friendInSearchDialog);
        }
        this.add(Box.createVerticalGlue());
    }

}
