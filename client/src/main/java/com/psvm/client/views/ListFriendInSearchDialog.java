package com.psvm.client.views;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Vector;

public class ListFriendInSearchDialog extends JPanel {
    public ListFriendInSearchDialog(){
        this.setBackground(Color.white);
        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        //friendList.add(friendSearchAndAdd);
    }

    public void setData(Vector<Map<String, Object>> data) {
        this.removeAll();
        for (Map<String, Object> datum: data) {
            if (datum.get("TargetId") == null) {
                FriendInSearchDialog friendInSearchDialog = new FriendInSearchDialog("asdasd",datum.get("Username").toString(), datum.get("Username").toString(), false);
                this.add(friendInSearchDialog);
            }
            else {
                FriendInSearchDialog friendInSearchDialog = new FriendInSearchDialog("asdasd",datum.get("Username").toString(), datum.get("Username").toString(), true);
                this.add(friendInSearchDialog);
            }

        }
        this.add(Box.createVerticalGlue());
    }
}
