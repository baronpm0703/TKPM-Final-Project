package com.psvm.client.views;

import javax.swing.*;
import java.awt.*;

public class ChatSection extends JPanel {
    ChatSection(){
        this.setLayout(new BorderLayout());
        int scrollbarWidth = UIManager.getLookAndFeelDefaults().getInt("ScrollBar.width");
        this.setPreferredSize(new Dimension(1120 - scrollbarWidth,820));

        //header

        //ChatScreen
        ChatBody chatBody = new ChatBody();

        this.add(chatBody, BorderLayout.CENTER);
    }
}
