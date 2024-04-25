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

class NewRegisterListThread extends SwingWorker<Void, HashMap<String, Object>> {
    private final DBWrapper db;
    private Observer observer;

    public interface Observer {
        public void workerDidUpdate(HashMap<String, Object> userInfo);
    }

    public NewRegisterListThread(Observer observer) {
        // Connect DB
        this.db = new DBWrapper();
        this.observer = observer;
    }

    @Override
    protected Void doInBackground() throws Exception {
        // Get User List With given DateTime
        ResultSet userNameQueryRes = db.getUserListInfo();

        HashMap<String, Object> newRegisterListInfo = new HashMap<>();
        while (userNameQueryRes.next()) {
            // Get UserId First
            String userId = (String) userNameQueryRes.getObject(1);

            HashMap<String, Object> newRegisterInfo = new HashMap<>();


            String Hoten = (String) userNameQueryRes.getString("Hoten");
            String dateString = (String) userNameQueryRes.getString("CreationDate");

            // Define the format of the input string
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            // Parse the string into a LocalDate object
            LocalDate createDate = LocalDate.parse(dateString, formatter);

            newRegisterInfo.put("Hoten", Hoten);
            newRegisterInfo.put("CreateDate", createDate);


            newRegisterListInfo.put(userId, newRegisterInfo);
        }
//        System.out.println(newRegisterListInfo);

        publish(newRegisterListInfo);

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

public class NewRegisterList extends JFrame {
    ScheduledExecutorService service = Executors.newScheduledThreadPool(10);

    JPanel jPanel;
    JList<String> jList;

    public NewRegisterList(String title) throws SQLException {
        super(title);

        jPanel = new JPanel();
        jList = new JList();
        jPanel.add(jList);
        add(jPanel);
        startNextWorker();
    }


    protected void startNextWorker() {
        NewRegisterListThread userWorker = new NewRegisterListThread(new NewRegisterListThread.Observer() {
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
                    startNextWorker();
                }
            }
        });

        service.schedule(userWorker, 1000, TimeUnit.MILLISECONDS);
    }
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            NewRegisterList example = null;
            try {
                example = new NewRegisterList("Biểu đồ đăng ký mới");
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