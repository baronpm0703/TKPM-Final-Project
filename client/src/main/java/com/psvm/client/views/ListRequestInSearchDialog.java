package com.psvm.client.views;

import com.psvm.client.controllers.GetFriendRequestRequest;
import com.psvm.client.settings.LocalData;
import com.psvm.shared.socket.SocketResponse;

import javax.swing.*;
import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

class ListRequestInSearchDialogThread extends SwingWorker<Void, Map<String, Object>> {
    private Socket clientSocket;
    ObjectInputStream socketIn;
    ObjectOutputStream socketOut;
    private String username;
    private Observer observer;

    public interface Observer {
        public void workerDidUpdate(Vector<Map<String, Object>> data);
    }

    public ListRequestInSearchDialogThread(Socket clientSocket, ObjectInputStream socketIn, ObjectOutputStream socketOut, String username, Observer observer) {
        this.clientSocket = clientSocket;
        this.socketIn = socketIn;
        this.socketOut = socketOut;
        this.username = username;

        this.observer = observer;
    }

    @Override
    protected Void doInBackground() throws Exception {
        GetFriendRequestRequest request = new GetFriendRequestRequest(clientSocket, socketIn, socketOut, username);
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

public class ListRequestInSearchDialog extends JPanel {
    // Multithreading + Socket
    ScheduledExecutorService service = Executors.newScheduledThreadPool(2);
    final String SOCKET_HOST = "localhost";
    final int SOCKET_PORT = 5555;
    Socket socket;
    ObjectInputStream socketIn;
    ObjectOutputStream socketOut;

    Vector<String> totalFriendRequest = new Vector<>();

    public ListRequestInSearchDialog(){
        this.setBackground(Color.white);
        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        //friendList.add(friendSearchAndAdd);

        /* Multithreading + Socket */
        try {
            socket = new Socket(SOCKET_HOST, SOCKET_PORT);
            socketIn = new ObjectInputStream(socket.getInputStream());
            socketOut = new ObjectOutputStream(socket.getOutputStream());
            startNextWorker();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    protected void startNextWorker() {
        JPanel thisPanel = this;

        // Thread to update chat body
        ListRequestInSearchDialogThread listRequestInSearchDialogThread = new ListRequestInSearchDialogThread(socket, socketIn, socketOut, LocalData.getCurrentUsername(), new ListRequestInSearchDialogThread.Observer() {
            @Override
            public void workerDidUpdate(Vector<Map<String, Object>> data) {
                // Update GUI
                SwingUtilities.invokeLater(() -> {
                    for (Map<String, Object> request: data) {
                        String currentSenderId = request.get("SenderId").toString();

                        if (!totalFriendRequest.contains(currentSenderId)) {
                            if (Integer.parseInt(request.get("Status").toString()) == 0) {
                                FriendRequest friendRequestInSearchDialog = new FriendRequest("asdasd", request.get("SenderId").toString(), request.get("SenderId").toString());

                                thisPanel.add(friendRequestInSearchDialog);
                                totalFriendRequest.add(currentSenderId);
                            }
                        }
                        else {
                            int tempIndex = totalFriendRequest.indexOf(currentSenderId);

                            if (Integer.parseInt(request.get("Status").toString()) != 0)
                                thisPanel.remove(tempIndex);
                        }
                    }

                    thisPanel.revalidate();
                    thisPanel.repaint();
                });
            }
        });
        listRequestInSearchDialogThread.addPropertyChangeListener(new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                if (listRequestInSearchDialogThread.getState() == SwingWorker.StateValue.DONE) {
                    listRequestInSearchDialogThread.removePropertyChangeListener(this);
                    startNextWorker();
                }
            }
        });

        // Scheduling
        service.schedule(listRequestInSearchDialogThread, 750, TimeUnit.MILLISECONDS);
    }
}
