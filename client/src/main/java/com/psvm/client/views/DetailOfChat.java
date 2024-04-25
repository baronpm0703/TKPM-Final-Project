package com.psvm.client.views;

import com.psvm.client.controllers.GetFriendRequestRequest;
import com.psvm.client.controllers.GetGroupInfoRequest;
import com.psvm.client.controllers.GetUserInfoRequest;
import com.psvm.client.settings.LocalData;
import com.psvm.shared.socket.SocketResponse;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.sql.Timestamp;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

class FriendChatDetailThread extends SwingWorker<Void, Map<String, Object>> {
    private Socket clientSocket;
    ObjectInputStream socketIn;
    ObjectOutputStream socketOut;
    private String username;
    private String conversationId;
    private Observer observer;

    public interface Observer {
        public void workerDidUpdate(Vector<Map<String, Object>> data);
    }

    public FriendChatDetailThread(Socket clientSocket, ObjectInputStream socketIn, ObjectOutputStream socketOut, String username, String conversationId, Observer observer) {
        this.clientSocket = clientSocket;
        this.socketIn = socketIn;
        this.socketOut = socketOut;
        this.username = username;
        this.conversationId = conversationId;

        this.observer = observer;
    }

    @Override
    protected Void doInBackground() throws Exception {
        GetUserInfoRequest getUserInfoRequest = new GetUserInfoRequest(clientSocket, socketIn, socketOut, username, conversationId);
        SocketResponse getUserInfoResponse = getUserInfoRequest.talk();
        for (Map<String, Object> datum: getUserInfoResponse.getData()) {
            publish(datum);
        }

        return null;
    }

    @Override
    protected void process(List<Map<String, Object>> chunks) {
        super.process(chunks);

        Vector<Map<String, Object>> data = new Vector<>(chunks);
        observer.workerDidUpdate(data);
    }
}

class GroupChatDetailThread extends SwingWorker<Void, Map<String, Object>> {
    private Socket clientSocket;
    ObjectInputStream socketIn;
    ObjectOutputStream socketOut;
    private String conversationId;
    private Observer observer;

    public interface Observer {
        public void workerDidUpdate(Vector<Map<String, Object>> data);
    }

    public GroupChatDetailThread(Socket clientSocket, ObjectInputStream socketIn, ObjectOutputStream socketOut, String conversationId, Observer observer) {
        this.clientSocket = clientSocket;
        this.socketIn = socketIn;
        this.socketOut = socketOut;
        this.conversationId = conversationId;

        this.observer = observer;
    }

    @Override
    protected Void doInBackground() throws Exception {
        GetGroupInfoRequest getGroupInfoRequest = new GetGroupInfoRequest(clientSocket, socketIn, socketOut, conversationId);
        SocketResponse getGroupInfoResponse = getGroupInfoRequest.talk();
        for (Map<String, Object> datum: getGroupInfoResponse.getData()) {
            publish(datum);
        }

        return null;
    }

    @Override
    protected void process(List<Map<String, Object>> chunks) {
        super.process(chunks);

        Vector<Map<String, Object>> data = new Vector<>(chunks);
        observer.workerDidUpdate(data);
    }
}

public class DetailOfChat extends JPanel {
    // Multithreading + Socket
    ScheduledExecutorService service = Executors.newScheduledThreadPool(2);
    final String SOCKET_HOST = "localhost";
    final int SOCKET_PORT = 5555;
    Socket friendChatDetailSocket;
    ObjectInputStream friendChatDetailSocketIn;
    ObjectOutputStream friendChatDetailSocketOut;
    Socket groupChatDetailSocket;
    ObjectInputStream groupChatDetailSocketIn;
    ObjectOutputStream groupChatDetailSocketOut;

    private String currentConversationId;
    private String previousUsername;
    private String previousGroupConvoId;

    //Cái này thì tốt nhất là mày truyền một class của người dùng vào cái Constructor để lấy thông tin
    //Mấy cái bên dưới t làm đại thôi, truyền sao mày thấy tiện là được

    DetailOfChat(){
        setPreferredSize(new Dimension(250,764));
        this.setBackground((Color.WHITE));

        /* Multithreading + Socket */
        try {
            friendChatDetailSocket = new Socket(SOCKET_HOST, SOCKET_PORT);
            friendChatDetailSocketIn = new ObjectInputStream(friendChatDetailSocket.getInputStream());
            friendChatDetailSocketOut = new ObjectOutputStream(friendChatDetailSocket.getOutputStream());
            startFriendChatDetailWorker();

            groupChatDetailSocket = new Socket(SOCKET_HOST, SOCKET_PORT);
            groupChatDetailSocketIn = new ObjectInputStream(groupChatDetailSocket.getInputStream());
            groupChatDetailSocketOut = new ObjectOutputStream(groupChatDetailSocket.getOutputStream());
            startGroupChatDetailWorker();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    void removeComponent(){
        Component[] componentList = this.getComponents();
        for(Component c : componentList){
            //Find the components you want to remove
            this.remove(c);
        }
    }
    void renderFriend(String conversationId, String fName, String lName, String username){
        this.removeAll();
        DetailOfAFriend detailOfAFriend = new DetailOfAFriend(conversationId, "test", fName + " " + lName, username);
        this.add(detailOfAFriend);
        this.revalidate();
        this.repaint();
    }

    void renderGroup(String conversationId, String conversationName){
        this.removeAll();
        DetailOfAGroup detailOfAGroup = new DetailOfAGroup(conversationId, "test", conversationName);
        this.add(detailOfAGroup);
        this.revalidate();
        this.repaint();
    }

    protected void startFriendChatDetailWorker() {
        JPanel thisPanel = this;
        // Thread to update chat body
        FriendChatDetailThread friendChatDetailThread = new FriendChatDetailThread(friendChatDetailSocket, friendChatDetailSocketIn, friendChatDetailSocketOut, LocalData.getCurrentUsername(), LocalData.getSelectedConversation(), new FriendChatDetailThread.Observer() {
            @Override
            public void workerDidUpdate(Vector<Map<String, Object>> messages) {
                if (LocalData.getToRemoveChatDetail()) previousGroupConvoId = null;
                // Update GUI
                SwingUtilities.invokeLater(() -> {
                    // Turn off chat detail on command
                    if (LocalData.getToRemoveChatDetail()) {
                        thisPanel.removeAll();
                        thisPanel.revalidate();
                        thisPanel.repaint();
                        LocalData.setToRemoveChatDetail(false);
                    }

                    boolean isGroup = (boolean) messages.get(0).get("IsGroup");
                    if (!isGroup) {
                        String username = messages.get(1).get("Username").toString();
                        if (!username.equals(previousUsername)) {
                            String fName = messages.get(2).get("FirstName").toString();
                            String lName = messages.get(3).get("LastName").toString();

                            renderFriend(LocalData.getSelectedConversation(), fName, lName, username);
                        }

                        // Update variables
                        previousUsername = username;
                    }
                });
            }
        });
        friendChatDetailThread.addPropertyChangeListener(new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                if (friendChatDetailThread.getState() == SwingWorker.StateValue.DONE) {
                    friendChatDetailThread.removePropertyChangeListener(this);
                    startFriendChatDetailWorker();
                }
            }
        });

        // Scheduling
        service.schedule(friendChatDetailThread, 250, TimeUnit.MILLISECONDS);
    }

    protected void startGroupChatDetailWorker() {
        JPanel thisPanel = this;

        // Thread to update chat body
        GroupChatDetailThread groupChatDetailThread = new GroupChatDetailThread(groupChatDetailSocket, groupChatDetailSocketIn, groupChatDetailSocketOut, LocalData.getSelectedConversation(), new GroupChatDetailThread.Observer() {
            @Override
            public void workerDidUpdate(Vector<Map<String, Object>> messages) {
                if (LocalData.getToRemoveChatDetail()) previousUsername = null;
                // Update GUI
                SwingUtilities.invokeLater(() -> {
                    // Turn off chat detail on command
                    if (LocalData.getToRemoveChatDetail()) {
                        thisPanel.removeAll();
                        thisPanel.revalidate();
                        thisPanel.repaint();
                        LocalData.setToRemoveChatDetail(false);
                    }

                    boolean isGroup = (boolean) messages.get(0).get("IsGroup");
                    if (isGroup) {
                        String conversationId = messages.get(1).get("ConversationId").toString();
                        System.out.println(conversationId + " - " + previousGroupConvoId + " - " + !conversationId.equals(previousGroupConvoId));
                        if (!conversationId.equals(previousGroupConvoId)) {
                            String conversationName = messages.get(2).get("ConversationName").toString();

                            renderGroup(conversationId, conversationName);
                        }

                        previousGroupConvoId = conversationId;
                    }
                });
            }
        });
        groupChatDetailThread.addPropertyChangeListener(new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                if (groupChatDetailThread.getState() == SwingWorker.StateValue.DONE) {
                    groupChatDetailThread.removePropertyChangeListener(this);
                    startGroupChatDetailWorker();
                }
            }
        });

        // Scheduling
        service.schedule(groupChatDetailThread, 250, TimeUnit.MILLISECONDS);
    }
}

