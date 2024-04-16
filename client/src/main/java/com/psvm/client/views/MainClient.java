package com.psvm.client.views;

import javax.swing.*;
import java.awt.*;

public class MainClient {
    private final JFrame jfrm;
    MainClient(){
        jfrm = new JFrame("hooYah");
        jfrm.setLayout(new BorderLayout());
        jfrm.setLocationRelativeTo(null);
        jfrm.getContentPane().setBackground(Color.decode("#FDFDFD"));
        //favicon
        ImageIcon favicon = new ImageIcon("client/src/main/resources/icon/chat-app-logo-2.png");

        jfrm.setIconImage(favicon.getImage());
        // Give the frame an initial size.
        jfrm.setSize(1440, 820);
        // Terminate the program when the user closes
        // the application.
        jfrm.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        //FriendList
        FriendListBar friendListBar = new FriendListBar();
        jfrm.add(friendListBar,BorderLayout.WEST);


        //Set visible
        jfrm.setVisible(true);
    }

    public int getFrameWidth() {
        return jfrm.getWidth();
    }

    // Method to get the current frame height
    public int getFrameHeight() {
        return jfrm.getHeight();
    }
    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                MainClient mainFrame = new MainClient();

                int width = mainFrame.getFrameWidth();
                int height = mainFrame.getFrameHeight();

                System.out.println("Current Frame Width: " + width);
                System.out.println("Current Frame Height: " + height);
            }
        });
    }
}
