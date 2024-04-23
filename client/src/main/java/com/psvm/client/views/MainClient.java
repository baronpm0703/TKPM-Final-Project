package com.psvm.client.views;

import com.psvm.client.controllers.LogActivityLoginRequest;
import com.psvm.client.controllers.LogActivityLogoutRequest;
import com.psvm.client.settings.LocalData;
import com.psvm.client.views.components.account.LoginBox;
import com.psvm.client.views.components.account.RegisterBox;
import com.psvm.client.views.components.friend.FriendListBar;
import com.psvm.shared.socket.SocketResponse;

import javax.swing.*;
import javax.swing.border.MatteBorder;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;



public class MainClient {
    // Socket for processing registering logging ins and outs
    final String SOCKET_HOST = "localhost";
    final int SOCKET_PORT = 5555;
    private Socket socket;
    ObjectInputStream socketIn;
    ObjectOutputStream socketOut;

    private JFrame jfrm = null;

    MainClient(){
        /* Multithreading + Socket */
        try {
            socket = new Socket(SOCKET_HOST, SOCKET_PORT);
            socketIn = new ObjectInputStream(socket.getInputStream());
            socketOut = new ObjectOutputStream(socket.getOutputStream());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        System.out.println("1");
        // Spawn login box
        LoginBox loginBox = new LoginBox();
        int loginResult = loginBox.display();

        if (loginResult != 0) {
            return;
        }
        System.out.println("2");
        LogActivityLoginRequest logActivityLoginRequest = new LogActivityLoginRequest(socket, socketIn, socketOut, LocalData.getCurrentUsername());
        SocketResponse logActivityLoginRequestResponse = logActivityLoginRequest.talk();
        // If logging fails then stop the program
        if (logActivityLoginRequestResponse.getResponseCode() == SocketResponse.RESPONSE_CODE_FAILURE) return;

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
        // the application + do sth other stuff
        jfrm.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        jfrm.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                LogActivityLogoutRequest logActivityLogoutRequest = new LogActivityLogoutRequest(socket, socketIn, socketOut, LocalData.getCurrentUsername());
                SocketResponse logActivityLogoutRequestResponse = logActivityLogoutRequest.talk();
                // If logging fails then not allow the program to exit
                if (logActivityLogoutRequestResponse.getResponseCode() == SocketResponse.RESPONSE_CODE_SUCCESS) System.exit(0);
            }
        });

        //FriendList
        FriendListBar friendListBar = new FriendListBar();
        jfrm.add(friendListBar,BorderLayout.WEST);

        //Chat Section
        ChatSection chatSection = new ChatSection();
        jfrm.add(chatSection,BorderLayout.CENTER);

        jfrm.setLocationRelativeTo(null);
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
