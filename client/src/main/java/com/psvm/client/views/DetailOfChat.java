package com.psvm.client.views;

import com.psvm.client.controllers.GetFriendRequestRequest;
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

class DetailOfChatThread extends SwingWorker<Void, Map<String, Object>> {
    private Socket clientSocket;
    ObjectInputStream socketIn;
    ObjectOutputStream socketOut;
    private String username;
    private Observer observer;

    public interface Observer {
        public void workerDidUpdate(Vector<Map<String, Object>> data);
    }

    public DetailOfChatThread(Socket clientSocket, ObjectInputStream socketIn, ObjectOutputStream socketOut, Observer observer) {
        this.clientSocket = clientSocket;
        this.socketIn = socketIn;
        this.socketOut = socketOut;
        this.username = username;

        this.observer = observer;
    }

    @Override
    protected Void doInBackground() throws Exception {
        GetUserInfoRequest request = new GetUserInfoRequest(clientSocket, socketIn, socketOut, username);
        SocketResponse response = request.talk();

        for (Map<String, Object> datum: response.getData()) {
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
    Socket socket;
    ObjectInputStream socketIn;
    ObjectOutputStream socketOut;

    //Cái này thì tốt nhất là mày truyền một class của người dùng vào cái Constructor để lấy thông tin
    //Mấy cái bên dưới t làm đại thôi, truyền sao mày thấy tiện là được

    DetailOfChat(){
        setPreferredSize(new Dimension(250,764));
        this.setBackground((Color.WHITE));
        renderFriend();
    }
    void removeComponent(){
        Component[] componentList = this.getComponents();
        for(Component c : componentList){
            //Find the components you want to remove
            this.remove(c);
        }
    }
    void renderFriend(){
        removeComponent();
        DetailOfAFriend detailOfAFriend = new DetailOfAFriend("test","Nguyễn Lâm Hải","kizark");
        this.add(detailOfAFriend);
        this.revalidate();
        this.repaint();
    }

    void renderGroup(){
        removeComponent();
        //call function here
        DetailOfAGroup detailOfAGroup = new DetailOfAGroup("test","Hehefdsfbsdfnsdfn");
        this.add(detailOfAGroup);
        this.revalidate();
        this.repaint();
    }

    protected void startNextWorker() {
        JPanel thisPanel = this;

        // Thread to update chat body
        DetailOfChatThread detailOfChatThreadWorker = new DetailOfChatThread(socket, socketIn, socketOut, new DetailOfChatThread.Observer() {
            @Override
            public void workerDidUpdate(Vector<Map<String, Object>> messages) {
                // Update GUI
                SwingUtilities.invokeLater(() -> {


                    thisPanel.revalidate();
                    thisPanel.repaint();
                });
            }
        });
        detailOfChatThreadWorker.addPropertyChangeListener(new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                if (detailOfChatThreadWorker.getState() == SwingWorker.StateValue.DONE) {
                    detailOfChatThreadWorker.removePropertyChangeListener(this);
                    startNextWorker();
                }
            }
        });

        // Scheduling
        service.schedule(detailOfChatThreadWorker, 250, TimeUnit.MILLISECONDS);
    }
}

