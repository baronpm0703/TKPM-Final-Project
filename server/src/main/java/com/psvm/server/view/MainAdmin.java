package com.psvm.server.view;

import javax.swing.*;
import java.awt.*;

public class MainAdmin {
    private final JFrame jfrm;
    MainAdmin(){
        // Create a new JFrame container.
        jfrm = new JFrame("hooYah");
        jfrm.setLayout(new BorderLayout());
        jfrm.setLocationRelativeTo(null);
        jfrm.getContentPane().setBackground(Color.decode("#FDFDFD"));
        //favicon
        ImageIcon favicon = new ImageIcon("server/src/main/resources/icon/chat-app-logo-2.png");

        jfrm.setIconImage(favicon.getImage());
        // Give the frame an initial size.
        jfrm.setSize(1440, 820);
        // Terminate the program when the user closes
        // the application.
        jfrm.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);


        //Header
        Header header = new Header();
        //header.setSize(1440,56);
        jfrm.add(header,BorderLayout.NORTH);

        //Content
        ContentPane contentPane = new ContentPane();
        jfrm.add(contentPane,BorderLayout.CENTER);
        //SideMenu
        SideMenu sideMenu = new SideMenu(contentPane);
        jfrm.add(sideMenu,BorderLayout.WEST);


        // Display the frame.
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
                MainAdmin mainAdmin = new MainAdmin();

                int width = mainAdmin.getFrameWidth();
                int height = mainAdmin.getFrameHeight();

                System.out.println("Current Frame Width: " + width);
                System.out.println("Current Frame Height: " + height);
            }
        });
    }
}
