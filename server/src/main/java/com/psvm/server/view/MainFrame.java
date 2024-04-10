package com.psvm.server.view;

import javax.swing.*;
import java.awt.*;
public class MainFrame {
    MainFrame(){
        // Create a new JFrame container.
        JFrame jfrm = new JFrame("hooYah");
        jfrm.setLayout(new BorderLayout());
        jfrm.setLocationRelativeTo(null);
        jfrm.getContentPane().setBackground(Color.WHITE);
        //favicon
        ImageIcon favicon = new ImageIcon("src/main/resources/icon/chat-app-logo-2.png");
        jfrm.setIconImage(favicon.getImage());
        // Give the frame an initial size.
        jfrm.setSize(1440, 900);
        // Terminate the program when the user closes
        // the application.
        jfrm.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        // Create a text-based label.
        JLabel jlab = new JLabel(" Swing means powerful GUIs.");
        // Add the label to the content pane.
        jfrm.add(jlab);

        //Header
        Header header = new Header();
        header.setSize(1440,56);
        jfrm.add(header,BorderLayout.NORTH);
        // Display the frame.
        jfrm.setVisible(true);
    }
    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new MainFrame();
            }
        });
    }
}
