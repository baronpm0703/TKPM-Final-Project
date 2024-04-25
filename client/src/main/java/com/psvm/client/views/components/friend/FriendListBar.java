package com.psvm.client.views.components.friend;

import com.psvm.client.controllers.FriendMessageListRequest;
import com.psvm.client.settings.LocalData;
import com.psvm.client.views.FriendListChoosingCategory;
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
    private String searchOption;
    private Observer observer;

    public interface Observer {
        public void workerDidUpdate(Vector<Map<String, Object>> message);
    }

    public FriendListBarThread(Socket clientSocket, ObjectInputStream socketIn, ObjectOutputStream socketOut, String friendSearch, String chatSearch, String searchOption, Observer observer) {
        this.clientSocket = clientSocket;
        this.socketIn = socketIn;
        this.socketOut = socketOut;
        this.friendSearch = friendSearch;
        this.chatSearch = chatSearch;
        this.searchOption = searchOption;

        this.observer = observer;
    }

    @Override
    protected Void doInBackground() throws Exception {
        FriendMessageListRequest request = new FriendMessageListRequest(clientSocket, socketIn, socketOut, friendSearch, chatSearch, searchOption);
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

    private JScrollPane scrollFriend;
    private ListFriendOfUser listFriendOfUser;
    private ListFriendOfUser listOnlineFriendOfUser;
    private ListGroupOfUser listGroupOfUser;
    private ListFriendOfUser listBlockedFriendOfUser;
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
        scrollFriend = new JScrollPane();
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


        // Setup choosing panel
        FriendListChoosingCategory friendListChoosingCategory = new FriendListChoosingCategory(scrollFriend);
        this.add(friendListChoosingCategory);

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
        String searchOption = LocalData.getSelectedSearchOption();
        String searchSubOption = friendSearchOptions.getCurrentOption();
        String chatSearch = "";
        String friendSearch = "";

        switch (searchOption) {
            case "friend": {
                //Remove current list being displayed (if any)
                if (listOnlineFriendOfUser != null) listOnlineFriendOfUser = null;
                if (listGroupOfUser != null) listGroupOfUser = null;
                if (listBlockedFriendOfUser != null) listBlockedFriendOfUser = null;

                if (listFriendOfUser == null) {
                    listFriendOfUser = new ListFriendOfUser();
                    scrollFriend.setViewportView(listFriendOfUser);
                }

                break;
            }
            case "friendOnline": {
                //Remove current list being displayed (if any)
                if (listFriendOfUser != null) listFriendOfUser = null;
                if (listGroupOfUser != null) listGroupOfUser = null;
                if (listBlockedFriendOfUser != null) listBlockedFriendOfUser = null;

                if (listOnlineFriendOfUser == null) {
                    listOnlineFriendOfUser = new ListFriendOfUser();
                    scrollFriend.setViewportView(listOnlineFriendOfUser);
                }

                break;
            }
            case "group": {
                //Remove current list being displayed (if any)
                if (listFriendOfUser != null) listFriendOfUser = null;
                if (listOnlineFriendOfUser != null) listOnlineFriendOfUser = null;
                if (listBlockedFriendOfUser != null) listBlockedFriendOfUser = null;

                if (listGroupOfUser == null) {
                    listGroupOfUser = new ListGroupOfUser();
                    scrollFriend.setViewportView(listGroupOfUser);
                }

                break;
            }
            case "friendBlocked": {
                if (listFriendOfUser != null) listFriendOfUser = null;
                if (listOnlineFriendOfUser != null) listOnlineFriendOfUser = null;
                if (listGroupOfUser != null) listGroupOfUser = null;

                if (listBlockedFriendOfUser == null) {
                    listBlockedFriendOfUser = new ListFriendOfUser();
                    scrollFriend.setViewportView(listBlockedFriendOfUser);
                }

                break;
            }
        }

        switch (searchSubOption) {
            case "Tìm người/đoạn chat": {
                chatSearch = "";
                friendSearch = searchFriendField.isFocused() ? searchFriendField.getText() : "";
                break;
            }
            case "Tìm theo nội dung đoạn chat": {
                chatSearch = searchFriendField.isFocused() ? searchFriendField.getText() : "";
                break;
            }
        }

        String finalChatSearch = chatSearch;
        String finalFriendSearch = friendSearch;
        FriendListBarThread worker = new FriendListBarThread(socket, socketIn, socketOut, friendSearch, chatSearch, searchOption, new FriendListBarThread.Observer() {
            @Override
            public void workerDidUpdate(Vector<Map<String, Object>> result) {
                SwingUtilities.invokeLater(() -> {
                    try {
                        switch (searchOption) {
                            case "friend": {
                                if (!finalChatSearch.equals(previousSearchContent) || !finalFriendSearch.equals(previousFriendSearch))
                                    listFriendOfUser.resetList();

                                listFriendOfUser.setData(result);

                                previousFriendSearch = finalFriendSearch;
                                previousSearchContent = finalChatSearch;

                                break;
                            }
                            case "friendOnline": {
                                if (!finalChatSearch.equals(previousSearchContent) || !finalFriendSearch.equals(previousFriendSearch))
                                    listOnlineFriendOfUser.resetList();

                                listOnlineFriendOfUser.setData(result);

                                previousFriendSearch = finalFriendSearch;
                                previousSearchContent = finalChatSearch;

                                break;
                            }
                            case "group": {
                                if (!finalChatSearch.equals(previousSearchContent) || !finalFriendSearch.equals(previousFriendSearch))
                                    listGroupOfUser.resetList();

                                listGroupOfUser.setData(result);

                                previousFriendSearch = finalFriendSearch;
                                previousSearchContent = finalChatSearch;

                                break;
                            }
                            case "friendBlocked": {
                                if (!finalChatSearch.equals(previousSearchContent) || !finalFriendSearch.equals(previousFriendSearch))
                                    listBlockedFriendOfUser.resetList();

                                listBlockedFriendOfUser.setData(result);

                                previousFriendSearch = finalFriendSearch;
                                previousSearchContent = finalChatSearch;

                                break;
                            }
                        }
                    }
                    catch (NullPointerException nullPointerException) {
                        System.out.println("List type changed. Aborting setting data...");
                    }

//                    JViewport viewport = scrollFriend.getViewport();
//                    // Remove all components from the viewport's view
//                    viewport.removeAll();
                    // Revalidate and repaint to reflect the changes
                    scrollFriend.revalidate();
                    scrollFriend.repaint();
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