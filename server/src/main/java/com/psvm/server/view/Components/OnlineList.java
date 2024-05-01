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

class OnlineListThread extends SwingWorker<Void, HashMap<String, Object>> {
    private final DBWrapper db;
    private Observer observer;

    String startDate;
    String endDate;

    public interface Observer {
        public void workerDidUpdate(HashMap<String, Object> userInfo);
    }

    public OnlineListThread(String startDate, String endDate,Observer observer) {
        // Connect DB
        this.db = new DBWrapper();
        this.observer = observer;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    @Override
    protected Void doInBackground() throws Exception {
        // Get User List With given DateTime
        ResultSet userNameQueryRes = db.getUserListInfo();

        HashMap<String, Object> OnlineListInfo = new HashMap<>();
        while (userNameQueryRes.next()) {
            // Get UserId First
            String userId = (String) userNameQueryRes.getObject(1);

            HashMap<String, Object> OnlineInfo = new HashMap<>();

            String Hoten = (String) userNameQueryRes.getString("Hoten");
            LocalDate LastAccessDatetime = null;
            int OnlineCount = 0;
            int ChatWithFriend = 0;
            int ChatWithGroup = 0;

            // Last access
            ResultSet getDateTimeUserId = db.getOnlineDateTimeFromUserLog(userId, startDate, endDate);
            while (getDateTimeUserId.next()) {
                String dateString = (String) getDateTimeUserId.getString("Datetime");
                if (dateString.isEmpty()) continue;
                // Define the format of the input string
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                // Parse the string into a LocalDate object
                LastAccessDatetime = LocalDate.parse(dateString, formatter);
            }

            // Online Count
            ResultSet getOnlineCountOfUserId = db.getOnlineInfoFromUserLogWithSDateEDate(userId, startDate, endDate);
            while (getOnlineCountOfUserId.next()) {
                OnlineCount = (int)  getOnlineCountOfUserId.getInt("AccessCount");
            }

            // Chat with friend
            ResultSet getChatFriendCount = db.getHowManyTimeUserChatWithFriend(userId, startDate, endDate);
            while (getChatFriendCount.next()) {
                ChatWithFriend = (int)  getChatFriendCount.getInt("ChatFCount");
            }

            // Chat with Group
            ResultSet getChatGroupCount = db.getHowManyTimeUserChatWithGroup(userId, startDate, endDate);
            while (getChatGroupCount.next()) {
                ChatWithGroup = (int)  getChatGroupCount.getInt("ChatGCount");
            }

            OnlineInfo.put("Hoten", Hoten);
            OnlineInfo.put("LastAccessTime", LastAccessDatetime);
            OnlineInfo.put("AccessCount", OnlineCount);
            OnlineInfo.put("ChatWithFriend", ChatWithFriend);
            OnlineInfo.put("ChatWithGroup", ChatWithGroup);

            OnlineListInfo.put(userId, OnlineInfo);
        }
//        System.out.println(OnlineListInfo);

        publish(OnlineListInfo);

        return null;
    }

    private int getIndex(String value) {
        switch (value) {
            case "fullName":
                return 1;

            case "addrs":
                return 2;

            case "dob":
                return 3;

            case "gender":
                return 4;

            case "email":
                return 5;

            case "creationDate":
                return 6;
            default:
                return -1;
        }
    }

    @Override
    protected void process(List<HashMap<String, Object>> chunks) {
        super.process(chunks);

        for (HashMap<String, Object> userInfo : chunks) {
            observer.workerDidUpdate(userInfo);
        }
    }

    @Override
    protected void done() {
        super.done();
        db.close();
    }

}

public class OnlineList extends JFrame {
    ScheduledExecutorService service = Executors.newScheduledThreadPool(10);

    JPanel jPanel;
    JList<String> jList;

    public OnlineList(String title) throws SQLException {
        super(title);

        jPanel = new JPanel();
        jList = new JList();
        jPanel.add(jList);
        add(jPanel);
        startNextWorker("","");
    }


    protected void startNextWorker(String startDate, String endDate) {
        OnlineListThread userWorker = new OnlineListThread(startDate,endDate,new OnlineListThread.Observer() {
            @Override
            public void workerDidUpdate(HashMap<String, Object> userFriendInfo) {
                HashMapListModel listModel = new HashMapListModel(userFriendInfo);
                jList.setModel(listModel);
            }

        });

        userWorker.addPropertyChangeListener(new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                if (userWorker.getState() == SwingWorker.StateValue.DONE) {
                    userWorker.removePropertyChangeListener(this);
                    startNextWorker(startDate, endDate);
                }
            }
        });

        service.schedule(userWorker, 1000, TimeUnit.MILLISECONDS);
    }
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            OnlineList example = null;
            try {
                example = new OnlineList("Biểu đồ đăng ký mới");
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