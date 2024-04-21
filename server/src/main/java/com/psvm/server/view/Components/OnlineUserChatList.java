package com.psvm.server.view.Components;

import com.psvm.server.controllers.DBWrapper;

import javax.swing.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

class OnlineUserList extends SwingWorker<Void, HashMap<String, Object>> {
    private final DBWrapper db;
    private Observer observer;

    public interface Observer {
        public void workerDidUpdate(HashMap<String, Object> userInfo);
    }

    public OnlineUserList(Observer observer) {
        // Connect DB
        this.db = new DBWrapper();
        this.observer = observer;
    }

    @Override
    protected Void doInBackground() throws Exception {
        // Get User List With given DateTime
        ResultSet userQueryRes = db.getLogInUserIdListWithDateTime("2023");

        HashMap<String, Object> userInfo = new HashMap<>();
        while (userQueryRes.next()) {
            // Get User Id match with date
            String userId = (String) userQueryRes.getObject(1);

            // With above User Id find the Suitable Conversation that macth the date
            ResultSet queryResult = db.getSuitableConversationId(userId, "2023");
            System.out.println("UserID: " + userQueryRes.getObject(1) + " ") ;

            Vector<String> SuitableConversationId = new Vector<>();
            HashMap<String, Integer> userChatCount = new HashMap<>();
            System.out.print("Convers: ");
            while (queryResult.next()) {
                // Each suitable will throw back to Conversation to filter the Conversation Info which is a group or not
                SuitableConversationId.add((String) queryResult.getObject(1));
                ResultSet innerQueryResult = db.getInfoConversationUserParticipateIn((String) queryResult.getObject(1));

                System.out.print(queryResult.getObject(1) + " ");
                while (innerQueryResult.next()) {
                    System.out.print( (Boolean) innerQueryResult.getObject(1) + " ");
                    if ((innerQueryResult.getObject(1) != null || !innerQueryResult.equals("")) && !(Boolean) innerQueryResult.getObject(1) ) {
                        userChatCount.merge("individual", 1, Integer::sum);
                    } else userChatCount.merge("group", 1, Integer::sum);
                }
            }

            System.out.println();
            userInfo.put(userId, userChatCount);
        }

        System.out.println(userInfo);
        publish(userInfo);

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

public class OnlineUserChatList extends JFrame {

    ScheduledExecutorService service = Executors.newScheduledThreadPool(10);
    private final DBWrapper db;
    JPanel jPanel;
    JList<String> jList;

    public OnlineUserChatList(String title) throws SQLException {
        super(title);
        // Connect DB
        this.db = new DBWrapper();
        jPanel = new JPanel();
        jList = new JList();
        jPanel.add(jList);
        add(jPanel);
        startNextWorker();
    }


    protected void startNextWorker() {
        OnlineUserList userWorker = new OnlineUserList(new OnlineUserList.Observer() {
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
            OnlineUserChatList example = null;
            try {
                example = new OnlineUserChatList("Biểu đồ đăng ký mới");
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


class HashMapListModel extends AbstractListModel<String> {
    private final HashMap<String, Object> userInfo;

    public HashMapListModel(HashMap<String, Object> userInfo) {
        this.userInfo = userInfo;
    }

    @Override
    public int getSize() {
        return userInfo.size();
    }

    @Override
    public String getElementAt(int index) {
        Map.Entry<String, Object> entry = (Map.Entry<String, Object>) userInfo.entrySet().toArray()[index];
        String userId = entry.getKey();
        Object userChatCountObj = entry.getValue();

        // Convert userChatCountObj to a String representation or as needed
        String userChatCountStr = String.valueOf(userChatCountObj);

        // Create a string to represent the user info
        return userId + ": " + userChatCountStr;
    }
}