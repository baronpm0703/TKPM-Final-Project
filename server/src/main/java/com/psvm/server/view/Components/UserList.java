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

class UserListThread extends SwingWorker<Void, HashMap<String, Object>> {
    private final DBWrapper db;
    private Observer observer;

    public interface Observer {
        public void workerDidUpdate(HashMap<String, Object> userInfo);
    }

    public UserListThread(Observer observer) {
        // Connect DB
        this.db = new DBWrapper();
        this.observer = observer;
    }

    @Override
    protected Void doInBackground() throws Exception {
        // Get User List With given DateTime
        ResultSet userNameQueryRes = db.getUserListInfo();

        HashMap<String, Object> userListInfo = new HashMap<>();
        while (userNameQueryRes.next()) {
            // Get UserName First
            String userId = (String) userNameQueryRes.getObject(1);
            // Get Fullname
            String fullName = (String) userNameQueryRes.getObject(2);
            // Address
            String addrs = (String) userNameQueryRes.getObject(3);
            // Date of birth
            String dateString =  userNameQueryRes.getString("DoB");
                // Define the format of the input string
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                // Parse the string into a LocalDate object
            LocalDate dob = LocalDate.parse(dateString, formatter);

            // Gender
            String gender = (Boolean) userNameQueryRes.getObject(5) == false ? "Male" : "Female";

            // Email
            String email = (String) userNameQueryRes.getObject(6);

            // Creation Date
            dateString =  userNameQueryRes.getString("CreationDate");
            // Parse the string into a LocalDate object
            LocalDate creationDate = LocalDate.parse(dateString, formatter);

            HashMap<String, Object> userDetailInfo = new HashMap<>();
            userDetailInfo.put("fullName", fullName);
            userDetailInfo.put("addrs", addrs);
            userDetailInfo.put("dob", dob);
            userDetailInfo.put("gender", gender);
            userDetailInfo.put("email", email);
            userDetailInfo.put("creationDate", creationDate);

            userListInfo.put(userId, userDetailInfo);
        }
        Object[] acc = new Object[8];
        userListInfo.forEach((userId, detail) -> {
            acc[0] = userId; // Assign row
//            System.out.println(userId + ": ");
            HashMap<String, Object> castedDetail = (HashMap<String, Object>) detail;
            // Loop Through to get value
            castedDetail.forEach((field, value) -> {
//                System.out.print(getIndex(field));
                Object obj = value;
                if (obj instanceof LocalDate) {
                    LocalDate dateValue = (LocalDate) obj;
                    acc[getIndex(field)] = dateValue;
//                    System.out.print(field + ": " + dateValue + " ");
                }
                else if (obj instanceof String) {
                    String stringValue = (String) obj;
                    acc[getIndex(field)] = stringValue;
//                    System.out.print(field + ": " + stringValue + " ");
                } else {
                    String gender = (Boolean) obj ? "Female" : "Male";
                    acc[getIndex(field)] = gender;
                }
            });
//            System.out.println();
        });


        publish(userListInfo);

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

}

public class UserList extends JFrame {
    ScheduledExecutorService service = Executors.newScheduledThreadPool(10);

    JPanel jPanel;
    JList<String> jList;

    public UserList(String title) throws SQLException {
        super(title);

        jPanel = new JPanel();
        jList = new JList();
        jPanel.add(jList);
        add(jPanel);
        startNextWorker();
    }


    protected void startNextWorker() {
        UserListThread userWorker = new UserListThread(new UserListThread.Observer() {
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
            UserList example = null;
            try {
                example = new UserList("Biểu đồ đăng ký mới");
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