package com.psvm.server.view.Components;

import com.psvm.server.controllers.DBWrapper;

import javax.swing.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

class ChatRoomListThread extends SwingWorker<Void, HashMap<String, Object>> {
    private final DBWrapper db;
    private Observer observer;

    public interface Observer {
        public void workerDidUpdate(HashMap<String, Object> userInfo);
    }

    public ChatRoomListThread(Observer observer) {
        // Connect DB
        this.db = new DBWrapper();
        this.observer = observer;
    }

    @Override
    protected Void doInBackground() throws Exception {
        // Get Conversation
        ResultSet conversationQueryRes = db.getConversationInfo();


        HashMap<String, Object> userLogListInfo = new HashMap<>();
        while (conversationQueryRes.next()) {
            // Get Conversation Id
            String conID = (String) conversationQueryRes.getObject(1);
            // Get conName
            String conName = (String) conversationQueryRes.getObject(2);
            // IsGroup
            Boolean conIsGroup = (Boolean) conversationQueryRes.getObject(3);

            // Detail about Conversation
            HashMap<String, Object> conDetailInfo = new HashMap<>();
            conDetailInfo.put("conversationName", conName);
            conDetailInfo.put("isGroup", conIsGroup);

            ResultSet conMemberInfo = db.getConversationMemberInfo(conID);
            HashMap<String, Object> conMember = new HashMap<>();
            while(conMemberInfo.next()) {
                // MemID
                String memId = (String) conMemberInfo.getObject(2);
                // Ho Ten
                String hoten = (String) conMemberInfo.getObject(3);
                // isAdmin
                Boolean isAdmin = (Boolean) conMemberInfo.getObject(4);
                // Meminfo
                HashMap<String, Object> memInfo = new HashMap<>();
                memInfo.put("hoten", hoten);
                memInfo.put("isAdmin", isAdmin);
                conMember.put(memId, memInfo);
            }
            conDetailInfo.put("memInfo", conMember);
            // Put in the collection
            userLogListInfo.put(conID, conDetailInfo);
        }
        publish(userLogListInfo);

        return null;
    }

    @Override
    protected void process(List<HashMap<String, Object>> chunks) {
        super.process(chunks);

        for (HashMap<String, Object> userInfo : chunks) {
            observer.workerDidUpdate(userInfo);
        }
    }

}

public class ChatRoomList extends JFrame {

    ScheduledExecutorService service = Executors.newScheduledThreadPool(10);

    JPanel jPanel;
    JList<String> jList;

    public ChatRoomList(String title) throws SQLException {
        super(title);

        jPanel = new JPanel();
        jList = new JList();
        jPanel.add(jList);
        add(jPanel);
        startNextWorker();
    }


    protected void startNextWorker() {
        ChatRoomListThread userWorker = new ChatRoomListThread(new ChatRoomListThread.Observer() {
            @Override
            public void workerDidUpdate(HashMap<String, Object> userInfo) {
                HashMapListModel listModel = new HashMapListModel(userInfo);
                jList.setModel(listModel);
            }

        });

        userWorker.addPropertyChangeListener(new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                if (userWorker.getState() == SwingWorker.StateValue.DONE) {
                    userWorker.removePropertyChangeListener(this);
                    startNextWorker();
                }
            }
        });

        service.schedule(userWorker, 1000, TimeUnit.MILLISECONDS);
    }
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            ChatRoomList example = null;
            try {
                example = new ChatRoomList("Biểu đồ đăng ký mới");
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
            example.setSize(800, 600);
            example.setLocationRelativeTo(null);
            example.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
            example.setVisible(true);
        });
    }
}
