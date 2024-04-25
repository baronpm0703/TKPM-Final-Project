package com.psvm.server.view;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.Serial;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;


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
    //ScheduledExecutorService service = Executors.newScheduledThreadPool(5);
    private final DefaultTableModel model;
    private final int columnCount;

    // private HashMap<String, Object> userList = new HashMap<>();
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
        model.addRow(new Object[]{0,"hải", LocalDate.of(2023,5,30),5,6,7});
        model.addRow(new Object[]{1,"khoa",LocalDate.of(2023,6,2),8,4,3});
        model.addRow(new Object[]{2,"bảo1",LocalDate.of(2022,11,4),6,3,5});
        model.addRow(new Object[]{3,"bảo2",LocalDate.of(2022,5,4),2,3,7});
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

    //    protected void startNextWorker() {
//        UserListThread userWorker = new UserListThread(new UserListThread.Observer() {
//            @Override
//            public void workerDidUpdate(HashMap<String, Object> userInfo) {
//                if (!userList.equals(userInfo)) {
//                    List<Object[]> userAccountData = new ArrayList<>();
//                    userInfo.forEach((userId, detail) -> {
//                        Object[] acc = new Object[8];
//                        acc[0] = userId; // Assign row
//                        HashMap<String, Object> castedDetail = (HashMap<String, Object>) detail;
//                        // Loop Through to get value
//                        castedDetail.forEach((field, value) -> {
//                            Object obj = value;
//                            if (obj instanceof LocalDate) {
//                                LocalDate dateValue = (LocalDate) obj;
//                                acc[getIndex(field)] = dateValue;
//                            }
//                            else if (obj instanceof String) {
//                                String stringValue = (String) obj;
//                                acc[getIndex(field)] = stringValue;
//                            } else {
//                                String gender = (Boolean) obj ? "Female" : "Male";
//                                acc[getIndex(field)] = gender;
//                            }
//                        });
//                        userAccountData.add(acc);
//                    });
//
//                    resetModelRow(); // Reset new Data
//                    index = 1;
//                    for (Object[] row: userAccountData){
//                        Object[] newRow = new Object[row.length + 1];
//                        newRow[0] = index++;
//                        System.arraycopy(row,0,newRow,1,row.length);
//                        model.addRow(newRow);
//                    }
//                    userList = userInfo;
//                }
//            }
//        });
//        userWorker.addPropertyChangeListener(new PropertyChangeListener() {
//            @Override
//            public void propertyChange(PropertyChangeEvent evt) {
//                if (userWorker.getState() == SwingWorker.StateValue.DONE) {
//                    userWorker.removePropertyChangeListener(this);
//                    startNextWorker();
//                }
//
//            }
//        });
//        service.schedule(userWorker, 1000, TimeUnit.MILLISECONDS);
//    }
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

                if (!monthStart.isEmpty() && !yearStart.isEmpty() && !monthEnd.isEmpty() && !yearEnd.isEmpty()) {
                    LocalDate startDate = LocalDate.of(Integer.parseInt(yearStart), Integer.parseInt(monthStart), Integer.parseInt(dayStart));
                    LocalDate endDate = LocalDate.of(Integer.parseInt(yearEnd), Integer.parseInt(monthEnd), Integer.parseInt(dayEnd));

                    filters.add(new RowFilter<Object, Object>() {
                        @Override
                        public boolean include(Entry<? extends Object, ? extends Object> entry) {
                            LocalDate date = (LocalDate) entry.getValue(2);

                            return !date.isBefore(startDate) && !date.isAfter(endDate);
                        }
                    });
                }

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




    void refreshTable() {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
//                List<Object[]> updatedData = mySQLData.getAllStudent();
//                model.setRowCount(0);
//                for (Object[] row : updatedData) {
//                    model.addRow(row);
//                }
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

//    private int getIndex(String value) {
//        switch (value) {
//            case "fullName":
//                return 1;
//
//            case "addrs":
//                return 2;
//
//            case "dob":
//                return 3;
//
//            case "gender":
//                return 4;
//
//            case "email":
//                return 5;
//
//            case "creationDate":
//                return 6;
//            default:
//                return -1;
//        }
//    }
//


}
