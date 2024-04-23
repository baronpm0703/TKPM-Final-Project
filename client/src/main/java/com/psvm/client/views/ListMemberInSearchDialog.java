package com.psvm.client.views;

import javax.swing.*;
import java.awt.*;
import java.util.Map;
import java.util.Vector;

public class ListMemberInSearchDialog extends JPanel {
    private String conversationId;
    public ListMemberInSearchDialog(String conversationId){
        this.conversationId = conversationId;

        this.setBackground(Color.white);
        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        //friendList.add(friendSearchAndAdd);
    }

    public void setData(Vector<Map<String, Object>> data) {
        this.removeAll();
        for (Map<String, Object> datum: data) {
            if (datum.get("TargetId") == null) {
                MemberInSearchDialog memberInSearchDialog = new MemberInSearchDialog("asdasd",datum.get("Username").toString(), datum.get("Username").toString(), conversationId);
                this.add(memberInSearchDialog);
            }
            else {
                MemberInSearchDialog memberInSearchDialog = new MemberInSearchDialog("asdasd",datum.get("Username").toString(), datum.get("Username").toString(), conversationId);
                this.add(memberInSearchDialog);
            }

        }
        this.add(Box.createVerticalGlue());
    }
}
