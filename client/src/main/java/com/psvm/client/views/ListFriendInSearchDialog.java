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
            System.out.println(datum);
            if (datum.get("Status") != null && (Integer) datum.get("Status") == 0) {
                FriendInSearchDialog friendInSearchDialog = new FriendInSearchDialog("asdasd",datum.get("Username").toString(), datum.get("Username").toString(), true);
                this.add(friendInSearchDialog);
            }
            else if (datum.get("Status") != null && (Integer) datum.get("Status") == 1) {
                if (datum.get("UserId") == null && datum.get("FriendId") == null) {
                    FriendInSearchDialog friendInSearchDialog = new FriendInSearchDialog("asdasd",datum.get("Username").toString(), datum.get("Username").toString(), false);
                    this.add(friendInSearchDialog);
                }
                else {
                    FriendInSearchDialog friendInSearchDialog = new FriendInSearchDialog("asdasd",datum.get("Username").toString(), datum.get("Username").toString(), true);
                    this.add(friendInSearchDialog);
                }
            }
            else {
                FriendInSearchDialog friendInSearchDialog = new FriendInSearchDialog("asdasd",datum.get("Username").toString(), datum.get("Username").toString(), false);
                this.add(friendInSearchDialog);
            }
        }
        this.add(Box.createVerticalGlue());
    }
}
