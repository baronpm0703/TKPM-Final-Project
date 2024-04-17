package com.psvm.client.views;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class ChatSection extends JPanel {
    ChatSection(){
        this.setLayout(new BorderLayout());
        int scrollbarWidth = UIManager.getLookAndFeelDefaults().getInt("ScrollBar.width");
        this.setPreferredSize(new Dimension(1120 - scrollbarWidth,820));

        //header

        //ChatScreen
        ChatBody chatBody = new ChatBody();
        JScrollPane chatBodyScroll = new JScrollPane();
        chatBodyScroll.setBorder(new EmptyBorder(0,0,0,0));
        chatBodyScroll.setViewportView(chatBody);
        this.add(chatBodyScroll, BorderLayout.CENTER);

        //Detail
        DetailOfChat detailOfChat = new DetailOfChat();
        this.add(detailOfChat,BorderLayout.EAST);
    }
}
