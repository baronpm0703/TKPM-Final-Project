package com.psvm.client.views.components.friend;

import com.psvm.client.controllers.FriendMessageListRequest;
import com.psvm.shared.socket.SocketResponse;

import javax.swing.*;
import javax.swing.border.MatteBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
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

class FriendListBarThread extends SwingWorker<Void, Map<String, Object>> {
    private Socket clientSocket;
    ObjectInputStream socketIn;
    ObjectOutputStream socketOut;
    private String friendSearch;
    private String chatSearch;
    private Observer observer;

    public interface Observer {
        public void workerDidUpdate(Vector<Map<String, Object>> message);
    }

    public FriendListBarThread(Socket clientSocket, ObjectInputStream socketIn, ObjectOutputStream socketOut, String friendSearch, String chatSearch, Observer observer) {
        this.clientSocket = clientSocket;
        this.socketIn = socketIn;
        this.socketOut = socketOut;
        this.friendSearch = friendSearch;
        this.chatSearch = chatSearch;

        this.observer = observer;
    }

    @Override
    protected Void doInBackground() throws Exception {
        FriendMessageListRequest request = new FriendMessageListRequest(clientSocket, socketIn, socketOut, friendSearch, chatSearch);
        SocketResponse response = request.talk();

        for (Map<String, Object> datum: response.getData()) {
            publish(datum);
        }

        return null;
    }

    @Override
    protected void process(List<Map<String, Object>> chunks) {
        super.process(chunks);

        Vector<Map<String, Object>> messages = new Vector<>(chunks);
        observer.workerDidUpdate(messages);
    }
}

public class FriendListBar extends JPanel{
    // Multithreading + Socket
    ScheduledExecutorService service = Executors.newScheduledThreadPool(2);
    final String SOCKET_HOST = "localhost";
    final int SOCKET_PORT = 5555;
    Socket socket;
    ObjectInputStream socketIn;
    ObjectOutputStream socketOut;

    //Chat of selected Friend panel
    //private final ContentPane contentPane;
    private JButton selectedButton;
    //private JButton currentButton;

    private ListFriendOfUser listFriendOfUser;
    private FriendSearchOptions friendSearchOptions;
    private SearchFriendField searchFriendField;
    private String previousFriendSearch = "";
    private String previousSearchContent = "";

    //later for chat of selected Friend
    //FriendList(ContentPane contentPane)
    public FriendListBar() {
        /* Initialize this GUI component */
        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        this.setOpaque(false);
        this.setPreferredSize(new Dimension(320, 820));
        this.setBorder(new MatteBorder(0,0,0,1,Color.decode("#CDD5DE")));
        // Add header
        FriendListHeader friendListHeader = new FriendListHeader();
        this.add(friendListHeader);
        // Add search option
        friendSearchOptions = new FriendSearchOptions();
        this.add(friendSearchOptions);
        // friend search and add friend
        searchFriendField = new SearchFriendField();
        AddFriendIconButton addFriendIconButton = new AddFriendIconButton();
        JPanel friendSearchAndAdd = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        friendSearchAndAdd.setBackground(Color.WHITE);
        friendSearchAndAdd.add(searchFriendField);
        friendSearchAndAdd.add(addFriendIconButton);
        this.add(friendSearchAndAdd);
        // Friend list
        listFriendOfUser = new ListFriendOfUser();
        JScrollPane scrollFriend = new JScrollPane(listFriendOfUser);
        scrollFriend.setBorder(null);
        scrollFriend.setPreferredSize(new Dimension(320, 630));
        scrollFriend.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollFriend.getViewport().addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                scrollFriend.revalidate();
                scrollFriend.repaint();
            }
        });

        this.add(scrollFriend);


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
        String searchOption = friendSearchOptions.getCurrentOption();
        String chatSearch = "";
        String friendSearch = "";
        switch (searchOption) {
            case "Tìm người theo tên đăng nhập": {
                chatSearch = "";
                friendSearch = searchFriendField.isFocused() ? searchFriendField.getText() : "";
                break;
            }
            case "Tìm người theo tên": {
                chatSearch = "";
                break;
            }
            case "Tìm đoạn chat": {
                chatSearch = searchFriendField.isFocused() ? searchFriendField.getText() : "";
                break;
            }
        }

        String finalChatSearch = chatSearch;
        String finalFriendSearch = friendSearch;
        FriendListBarThread worker = new FriendListBarThread(socket, socketIn, socketOut, friendSearch, chatSearch, new FriendListBarThread.Observer() {
            @Override
            public void workerDidUpdate(Vector<Map<String, Object>> friends) {
                SwingUtilities.invokeLater(() -> {
                    if (!finalChatSearch.equals(previousSearchContent) || !finalFriendSearch.equals(previousFriendSearch))
                        listFriendOfUser.resetList();

                    listFriendOfUser.setData(friends);

                    previousFriendSearch = finalFriendSearch;
                    previousSearchContent = finalChatSearch;
                });
            }
        });
        worker.addPropertyChangeListener(new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                if (worker.getState() == SwingWorker.StateValue.DONE) {
                    worker.removePropertyChangeListener(this);
                    startNextWorker();
                }
            }
        });
        service.schedule(worker, 250, TimeUnit.MILLISECONDS);
    }
}