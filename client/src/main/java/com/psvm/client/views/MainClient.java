package com.psvm.client.views;

import com.psvm.client.settings.LocalData;
import com.psvm.client.views.components.account.LoginBox;
import com.psvm.client.views.components.account.RegisterBox;
import com.psvm.client.views.components.friend.FriendListBar;
import javax.swing.*;
import javax.swing.border.MatteBorder;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class MainClient {
    private final JFrame jfrm;
    MainClient(){
        // Spawn login box
        LoginBox loginBox = new LoginBox();
        int loginResult = loginBox.display();

        if (loginResult != 0) {
            jfrm = null;
            return;
        }

        jfrm = new JFrame("hooYah");
        jfrm.setLayout(new BorderLayout());
        jfrm.setLocationRelativeTo(null);
        jfrm.getContentPane().setBackground(Color.decode("#FDFDFD"));
        jfrm.getRootPane().setBorder(new MatteBorder(1,1,1,1,Color.decode("#CDD5DE")));
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

        //Chat Section
        ChatSection chatSection = new ChatSection();
        jfrm.add(chatSection,BorderLayout.CENTER);

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

                try {
                    int width = mainFrame.getFrameWidth();
                    int height = mainFrame.getFrameHeight();

                    System.out.println("Current Frame Width: " + width);
                    System.out.println("Current Frame Height: " + height);
                }
                catch (NullPointerException e) {
                    System.out.println("Program exited");
                }
            }
        });
    }
}
