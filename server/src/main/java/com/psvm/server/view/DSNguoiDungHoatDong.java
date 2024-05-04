package com.psvm.server.view;

import com.psvm.server.controllers.DBWrapper;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.Serial;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


class OptionPanelDSNguoiDungHoatDong extends JPanel{
    DSNguoiDungHoatDongTable table;
    OptionPanelDSNguoiDungHoatDong(DSNguoiDungHoatDongTable table){
        this.table = table;
        this.setLayout(new BorderLayout());
        this.setOpaque(false);

        //this.setBorder(new EmptyBorder(10,10,10,10));

        //Filter panel
        JPanel filterPanel1 = new JPanel();
        filterPanel1.setLayout(new FlowLayout(FlowLayout.LEFT));
        filterPanel1.setOpaque(false);
        //filterPanel.setBackground(Color.RED);
        filterPanel1.setBorder(new EmptyBorder(0,0,0,100));
        //filterPanel.setSize(990,180);
        //Filter field
        String[] dateField = new String[32];
        dateField[0] = "";
        for (int i = 1; i <= 31; i++){
            dateField[i] = String.valueOf(i);
        }
        String[] monthField = {"","1","2","3","4","5","6","7","8","9","10","11","12"};

        JComboBox<String> dayChoice = new JComboBox<>(dateField);
        JComboBox<String> monthChoice = new JComboBox<>(monthField);
        JTextField yearChoice = new JTextField(5);

        JComboBox<String> dayChoice2 = new JComboBox<>(dateField);
        JComboBox<String> monthChoice2 = new JComboBox<>(monthField);
        JTextField yearChoice2 = new JTextField(5);

        //name field

        JPanel filterPanel2 = new JPanel();
        filterPanel2.setLayout(new FlowLayout(FlowLayout.LEFT));
        filterPanel2.setOpaque(false);
        //filterPanel.setBackground(Color.RED);
        filterPanel2.setBorder(new EmptyBorder(0,0,0,100));
        JTextField nameField = new JTextField();
        nameField.setColumns(20);


        //Login field
        String[] items = {"Bằng", "Lớn hơn", "Nhỏ hơn"};
        JComboBox<String> dropdownLogin = new JComboBox<>(items);
        JTextField loginTime = new JTextField();
        loginTime.setColumns(3);

        //chatWithFriend field
        JComboBox<String> dropdownWithFriend = new JComboBox<>(items);
        JTextField chatWithFriend = new JTextField();
        chatWithFriend.setColumns(3);

        //chatWithGroup field
        JComboBox<String> dropdownWithGroup = new JComboBox<>(items);
        JTextField chatWithGroup = new JTextField();
        chatWithGroup.setColumns(3);

        //Filter button
        JButton filterButton = new JButton("       Lọc       ");
        filterButton.setFocusPainted(false);
        //Filter button logic
        filterButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String dayStart = (String) dayChoice.getSelectedItem();
                String monthStart = (String) monthChoice.getSelectedItem();
                String yearStart = yearChoice.getText();
                String dayEnd = (String) dayChoice2.getSelectedItem();
                String monthEnd = (String) monthChoice2.getSelectedItem();
                String yearEnd =  yearChoice2.getText();
                String nameFilter = nameField.getText();

                String choiceLogin = (String) dropdownLogin.getSelectedItem();
                String logTime = loginTime.getText();

                String choiceWithFriend = (String) dropdownWithFriend.getSelectedItem();
                String chatWFriendTime = chatWithFriend.getText();

                String choiceWithGroup = (String) dropdownWithGroup.getSelectedItem();
                String chatWGroup = chatWithGroup.getText();
                table.filterTable(nameFilter, dayStart, monthStart,yearStart,dayEnd,monthEnd,yearEnd, choiceLogin, logTime, choiceWithFriend, chatWFriendTime, choiceWithGroup, chatWGroup);
            }
        });
        //Add to filter Panel
        filterPanel1.add(new JLabel("Từ Ngày: "));
        filterPanel1.add(dayChoice);
        filterPanel1.add(new JLabel("Tháng: "));
        filterPanel1.add(monthChoice);
        filterPanel1.add(new JLabel("Năm: "));
        filterPanel1.add(yearChoice);
        filterPanel1.add(new JLabel("Đến Ngày: "));
        filterPanel1.add(dayChoice2);
        filterPanel1.add(new JLabel("Tháng: "));
        filterPanel1.add(monthChoice2);
        filterPanel1.add(new JLabel("Năm: "));
        filterPanel1.add(yearChoice2);
        filterPanel1.add(new JLabel("Tên: "));
        filterPanel1.add(nameField);
        filterPanel2.add(new JLabel("Số lần mở ứng dụng: "));
        filterPanel2.add(dropdownLogin);
        filterPanel2.add(loginTime);
        filterPanel2.add(new JLabel("Số lần chat với người: "));
        filterPanel2.add(dropdownWithFriend);
        filterPanel2.add(chatWithFriend);
        filterPanel2.add(new JLabel("Số lần chat với nhóm: "));
        filterPanel2.add(dropdownWithGroup);
        filterPanel2.add(chatWithGroup);
        filterPanel2.add(filterButton);

        JButton refreshButton = new JButton("Làm mới bộ lọc");
        refreshButton.setFocusPainted(false);
        refreshButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                nameField.setText("");
                yearChoice.setText("");
                dayChoice.setSelectedIndex(0);
                monthChoice.setSelectedIndex(0);
                yearChoice2.setText("");
                dayChoice2.setSelectedIndex(0);
                monthChoice2.setSelectedIndex(0);
                dropdownLogin.setSelectedIndex(0);
                loginTime.setText("");

                dropdownWithFriend.setSelectedIndex(0);
                chatWithFriend.setText("");

                dropdownWithGroup.setSelectedIndex(0);
                chatWithGroup.setText("");
                table.filterTable("","","","","","","","","","","","","");
            }
        });
        filterPanel2.add(refreshButton);


        //Add to Option Panel
        this.add(filterPanel1,BorderLayout.WEST);
        this.add(filterPanel2,BorderLayout.SOUTH);


    }
}
class DSNguoiDungHoatDongTable extends JTable{
    //private MySQLData mySQLData; //Biến để lưu database, coi cách gọi database của tao trong Example/Table
    // Cần dòng này để gọi dữ liệu từ database
    // Khi trả dữ về thì trả với dạng List<Object[]> (ArrayList)
    // Coi trong folder EXAMPLE, MySQLData
    ScheduledExecutorService service = Executors.newScheduledThreadPool(5);
    private final DefaultTableModel model;
    private final int columnCount;

    // private HashMap<String, Object> userList = new HashMap<>();\
    private HashMap<String, Object> OnlineList = new HashMap<>();
    List<Object[]> OnlineListData; // Store conID
    static boolean isFiltering;
    int index = 1;
    DefaultTableModel getTableModel(){
        return model;
    }
    DSNguoiDungHoatDongTable(String[] columnNames){
        super(new DefaultTableModel(columnNames,0));
        //Formating the table
        this.model = (DefaultTableModel) this.getModel();
        this.setDefaultEditor(Object.class,null);
        int iconHeight = new ImageIcon("server/src/main/resources/icon/more_vert.png").getIconHeight();
        this.setRowHeight(iconHeight);
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        this.setDefaultRenderer(Object.class, centerRenderer);
        // Enable sorting
        this.setAutoCreateRowSorter(true);
        //Example data
        // CHỖ NÀY ĐỌC KĨ VÀO CHO TAO
        // TẤT cả ngày ở mySQL phải là SQL date, khi gọi hàm ở database, nhớ chuyển nó về LocalDate để add vào data
        //startNextWorker(); // riel data
//        model.addRow(new Object[]{0,"hải", LocalDate.of(2023,5,30),5,6,7});
//        model.addRow(new Object[]{1,"khoa",LocalDate.of(2023,6,2),8,4,3});
//        model.addRow(new Object[]{2,"bảo1",LocalDate.of(2022,11,4),6,3,5});
//        model.addRow(new Object[]{3,"bảo2",LocalDate.of(2022,5,4),2,3,7});
        startNextWorker("","");
        //formatting table
        setColumnWidthToFitContent();
        //add columnCount for later use
        this.columnCount = this.getColumnCount();

        DateTimeFormatter myFormatObj = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        DefaultTableCellRenderer dateTimeRenderer = new DefaultTableCellRenderer() {
            @Serial
            private static final long serialVersionUID = 1L;
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                if (value instanceof LocalDate) {
                    value = ((LocalDate) value).format(myFormatObj);
                }
                setHorizontalAlignment(SwingConstants.CENTER);
                return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            }
        };
        getColumnModel().getColumn(2).setCellRenderer(dateTimeRenderer);


    }
    protected void startNextWorker(String startDate, String endDate) {
        OnlineListThread userWorker = new OnlineListThread(startDate,endDate,new OnlineListThread.Observer() {
            @Override
            public void workerDidUpdate(HashMap<String, Object> OnlineInfo) {
                if (!OnlineList.equals(OnlineInfo)) {
                    OnlineListData = new ArrayList<>();
                    OnlineInfo.forEach((userId, detail) -> {
                        Object[] obj = new Object[5];
                        HashMap<String, Object> castedDetail = (HashMap<String, Object>) detail;

                        obj[0] = (String) castedDetail.get("Hoten");
                        obj[1] = (LocalDate) castedDetail.get("LastAccessTime");
                        obj[2] = (int) castedDetail.get("AccessCount");
                        obj[3] = (int) castedDetail.get("ChatWithFriend");
                        obj[4] = (int) castedDetail.get("ChatWithGroup");

                        OnlineListData.add(obj);
                    });

                    resetModelRow();
                    index = 1;
                    for (Object[] row: OnlineListData){
                        Object[] newRow = new Object[row.length + 1];
                        newRow[0] = index++;
                        System.arraycopy(row,0,newRow,1,row.length);
                        model.addRow(newRow);
                    }
                    OnlineList = OnlineInfo;
                }
            }

        });

        userWorker.addPropertyChangeListener(new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                if (userWorker.getState() == SwingWorker.StateValue.DONE) {
                    userWorker.removePropertyChangeListener(this);
                    if (!isFiltering)
                        startNextWorker(startDate, endDate);
                    else isFiltering = false;

                }
            }
        });

        service.schedule(userWorker, 1000, TimeUnit.MILLISECONDS);
    }
    void filterTable(String name, String dayStart, String monthStart, String yearStart, String dayEnd, String monthEnd, String yearEnd,
                     String choiceLogin, String logTime, String choiceWithFriend, String chatWFriendTime,
                     String choiceWithGroup, String chatWGroup) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(model);
                List<RowFilter<Object, Object>> filters = new ArrayList<>();

                if (!name.isEmpty()) {
                    filters.add(RowFilter.regexFilter(name, 1));
                }


                String startDateS = "";
                if (!monthStart.isEmpty() && !yearStart.isEmpty()) {
                    LocalDate startDate = LocalDate.of(Integer.parseInt(yearStart), Integer.parseInt(monthStart), Integer.parseInt(dayStart));
                    startDateS = startDate.toString();
                }

                String endDateS = "";
                if (!monthEnd.isEmpty() && !yearEnd.isEmpty()) {
                    LocalDate endDate = LocalDate.of(Integer.parseInt(yearEnd), Integer.parseInt(monthEnd), Integer.parseInt(dayEnd));
                    endDateS = endDate.toString();
                }

                String finalStartDateS = startDateS;
                String finalEndDateS = endDateS;
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        isFiltering = true;
                        startNextWorker(finalStartDateS, finalEndDateS);
                    }
                });

                if (!logTime.isEmpty()) {
                    try {
                        int logTimeValue = Integer.parseInt(logTime);
                        int columnIndexOfLogTime = 3;

                        switch (choiceLogin) {
                            case "Bằng":
                                filters.add(RowFilter.numberFilter(RowFilter.ComparisonType.EQUAL, logTimeValue, columnIndexOfLogTime));
                                break;
                            case "Lớn hơn":
                                filters.add(RowFilter.numberFilter(RowFilter.ComparisonType.AFTER, logTimeValue, columnIndexOfLogTime));
                                break;
                            case "Nhỏ hơn":
                                filters.add(RowFilter.numberFilter(RowFilter.ComparisonType.BEFORE, logTimeValue, columnIndexOfLogTime));
                                break;
                            default:
                                break;
                        }
                    } catch (NumberFormatException e) {
                        e.printStackTrace();
                    }
                }

                if (!chatWFriendTime.isEmpty()) {
                    try {
                        int chatWFriendTimeValue = Integer.parseInt(chatWFriendTime);
                        int columnIndexOfChatWFriendTime = 4;

                        switch (choiceWithFriend) {
                            case "Bằng":
                                filters.add(RowFilter.numberFilter(RowFilter.ComparisonType.EQUAL, chatWFriendTimeValue, columnIndexOfChatWFriendTime));
                                break;
                            case "Lớn hơn":
                                filters.add(RowFilter.numberFilter(RowFilter.ComparisonType.AFTER, chatWFriendTimeValue, columnIndexOfChatWFriendTime));
                                break;
                            case "Nhỏ hơn":
                                filters.add(RowFilter.numberFilter(RowFilter.ComparisonType.BEFORE, chatWFriendTimeValue, columnIndexOfChatWFriendTime));
                                break;
                            default:
                                break;
                        }
                    } catch (NumberFormatException e) {
                        e.printStackTrace();
                    }
                }

                if (!chatWGroup.isEmpty()) {
                    try {
                        int chatWGroupTimeValue = Integer.parseInt(chatWGroup);
                        int columnIndexOfChatWGroupTime = 5;

                        switch (choiceWithGroup) {
                            case "Bằng":
                                filters.add(RowFilter.numberFilter(RowFilter.ComparisonType.EQUAL, chatWGroupTimeValue, columnIndexOfChatWGroupTime));
                                break;
                            case "Lớn hơn":
                                filters.add(RowFilter.numberFilter(RowFilter.ComparisonType.AFTER, chatWGroupTimeValue, columnIndexOfChatWGroupTime));
                                break;
                            case "Nhỏ hơn":
                                filters.add(RowFilter.numberFilter(RowFilter.ComparisonType.BEFORE, chatWGroupTimeValue, columnIndexOfChatWGroupTime));
                                break;
                            default:
                                break;
                        }
                    } catch (NumberFormatException e) {
                        e.printStackTrace();
                    }
                }

                sorter.setRowFilter(RowFilter.andFilter(filters));
                setRowSorter(sorter);
            }
        });
    }


    void resetModelRow() {
        while (model.getRowCount() > 0) {
            model.removeRow(0);
        }
    }

    private void setColumnWidthToFitContent() {
        for (int column = 0; column < this.getColumnCount(); column++) {
            int maxwidth = 0;
            for (int row = 0; row < this.getRowCount(); row++) {
                TableCellRenderer renderer = this.getCellRenderer(row, column);
                Component comp = prepareRenderer(renderer, row, column);
                maxwidth = Math.max(comp.getPreferredSize().width, maxwidth);
            }
            TableColumn tableColumn = getColumnModel().getColumn(column);
            tableColumn.setPreferredWidth(maxwidth);
        }
    }
    private void updateIndexColumn() {
        for (int i = 0; i < this.getRowCount(); i++) {
            this.setValueAt(i + 1, i, 0); // Update the index column
        }
        index = getRowCount() + 1;
    }

}

class OnlineListThread extends SwingWorker<Void, HashMap<String, Object>> {
    private final DBWrapper db;
    private Observer observer;

    String startDate;
    String endDate;

    public interface Observer {
        public void workerDidUpdate(HashMap<String, Object> userInfo);
    }

    public OnlineListThread(String startDate, String endDate, OnlineListThread.Observer observer) {
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
                String dateString =  getDateTimeUserId.getString("Datetime");

                // Define the format of the input string
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                // Parse the string into a LocalDate object
                LastAccessDatetime = LocalDate.parse(dateString, formatter);
            }
            if (LastAccessDatetime == null) continue;
//            System.out.println(LastAccessDatetime);

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



    @Override
    protected void process(List<HashMap<String, Object>> chunks) {
        super.process(chunks);

        for (HashMap<String, Object> OnlineInfo : chunks) {
            observer.workerDidUpdate(OnlineInfo);
        }
    }

    @Override
    protected void done() {
        super.done();
        db.close();
    }

}

