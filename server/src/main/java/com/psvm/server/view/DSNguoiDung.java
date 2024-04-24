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
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


class OptionPanelDSNguoiDung extends JPanel{
    DSNguoiDungTable table;
    OptionPanelDSNguoiDung(DSNguoiDungTable table){
        this.table = table;
        this.setLayout(new BorderLayout());
        this.setOpaque(false);

        //this.setBorder(new EmptyBorder(10,10,10,10));

        //Filter panel
        JPanel filterPanel = new JPanel();
        filterPanel.setLayout(new FlowLayout());
        filterPanel.setOpaque(false);
        //filterPanel.setBackground(Color.RED);
        filterPanel.setBorder(new EmptyBorder(0,0,0,100));
        //filterPanel.setSize(990,180);
        //Filter field
        JTextField nameField = new JTextField();
        nameField.setColumns(20);
        JTextField usernameField = new JTextField();
        usernameField.setColumns(10);
        String[] statuses = {"","Online","Offline","Banned"};
        JComboBox<String> statusField = new JComboBox<>(statuses);

        //Filter button
        JButton filterButton = new JButton("       Lọc       ");
        filterButton.setFocusPainted(false);
        //Filter button logic
        filterButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String nameFilter = nameField.getText();
                String usernameFilter = usernameField.getText();
                String statusFilter = (String) statusField.getSelectedItem();
                System.out.println(nameFilter);
                System.out.println(usernameFilter);
                System.out.println(statusFilter);
                table.filterTable(usernameFilter,nameFilter,statusFilter);
            }
        });
        //Add to filter Panel
        filterPanel.add(new JLabel("Tên Đăng nhập: "));
        filterPanel.add(usernameField);
        filterPanel.add(new JLabel("Tên: "));
        filterPanel.add(nameField);
        filterPanel.add(new JLabel("Trạng thái: "));
        filterPanel.add(statusField);
        filterPanel.add(filterButton);

        //Utilities Panel
        JPanel utilities = new JPanel();
        utilities.setOpaque(false);
        //Add new user
        JButton addUser = new JButton("Thêm người dùng");
        addUser.setFocusPainted(false);
        addUser.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                table.add();
            }
        });
        utilities.add(addUser);

        //Add to Option Panel
        this.add(filterPanel,BorderLayout.WEST);
        this.add(utilities,BorderLayout.EAST);
    }
}
class DSNguoiDungTable extends JTable{
    //private MySQLData mySQLData; //Biến để lưu database, coi cách gọi database của tao trong Example/Table
    // Cần dòng này để gọi dữ liệu từ database
    // Khi trả dữ về thì trả với dạng List<Object[]> (ArrayList)
    // Coi trong folder EXAMPLE, MySQLData
    ScheduledExecutorService service = Executors.newScheduledThreadPool(5);
    private final DefaultTableModel model;
    private final int columnCount;

    private HashMap<String, Object> userList = new HashMap<>();
    List<Object[]> userListData; // Store conID
    int index = 1;
    DefaultTableModel getTableModel(){
        return model;
    }
    DSNguoiDungTable(String[] columnNames){
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
//        List<Object[]> userAccountData = new ArrayList<>();
//        //LocalDate mới sort được, string ko sort đc
//        Object[] acc1 = {"haimen","Nguyễn Anh Khoa","123 Nguyen Thi Phuong, HA noi","21/10/2002","Nam","2men@gmail.com","21/02/2003","Online"};
//        Object[] acc2 = {"aaimen","Nguyễn Phú Minh Bảo","723 Nguyen Thi Phuong, HA noi","11/10/2002","Nam","2men@gmail.com","23/01/2003","Online"};
//        Object[] acc3 = {"aaiasdfasdfmen","Nguyễn Phú Minh Bảo","723 Nguyen Thi Phuong, HA noi","11/10/2002","Nam","2men@gmail.com","24/10/2003","Online"};
//        LocalDate date1 = LocalDate.of(2004,11, 5);
//        LocalDate date2 = LocalDate.of(2004,12, 3);
//        LocalDate date3 = LocalDate.of(2004,12, 4);
//
//        acc1[3] = date1;
//        acc2[3] = date2;
//        acc3[3] = date3;
//        acc1[6] = date1;
//        acc2[6] = date2;
//        acc3[6] = date3;
//        userAccountData.add(acc1);
//        userAccountData.add(acc2);
//        userAccountData.add(acc3);
//
//
//        //Add data to table
//        for (Object[] row: userAccountData){
//            Object[] newRow = new Object[row.length + 1];
//            newRow[0] = index++;
//            System.arraycopy(row,0,newRow,1,row.length);
//            this.model.addRow(newRow);
//        }
        startNextWorker(); // riel data

        // Add a custom renderer and editor for the last column
        this.getColumnModel().getColumn(columnNames.length - 1).setCellRenderer(new ButtonRenderer());
        this.getColumnModel().getColumn(columnNames.length - 1).setCellEditor(new ButtonEditor(new JCheckBox()));
        //Renderer for "time" column
        DateTimeFormatter myFormatObj = DateTimeFormatter.ofPattern("dd/MM/yyyy");
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
        getColumnModel().getColumn(4).setCellRenderer(dateTimeRenderer);
        getColumnModel().getColumn(7).setCellRenderer(dateTimeRenderer);


        //formatting table
        setColumnWidthToFitContent();
        //add columnCount for later use
        this.columnCount = this.getColumnCount();

    }

    protected void startNextWorker() {
        UserListThread userWorker = new UserListThread(new UserListThread.Observer() {
            @Override
            public void workerDidUpdate(HashMap<String, Object> userInfo) {
                if (!userList.equals(userInfo)) {
                    userListData = new ArrayList<>();
                    userInfo.forEach((userId, detail) -> {
                        Object[] acc = new Object[8];
                        acc[0] = userId; // Assign row
                        HashMap<String, Object> castedDetail = (HashMap<String, Object>) detail;
                        // Loop Through to get value
                        castedDetail.forEach((field, value) -> {
                            Object obj = value;
                            if (obj instanceof LocalDate) {
                                LocalDate dateValue = (LocalDate) obj;
                                acc[getIndex(field)] = dateValue;
                            }
                            else if (obj instanceof String) {
                                String stringValue = (String) obj;
                                acc[getIndex(field)] = stringValue;
                            } else {
                                String gender = (Boolean) obj ? "Female" : "Male";
                                acc[getIndex(field)] = gender;
                            }
                        });
                        userListData.add(acc);
                    });

                    resetModelRow(); // Reset new Data
                    index = 1;
                    for (Object[] row: userListData){
                        Object[] newRow = new Object[row.length + 1];
                        newRow[0] = index++;
                        System.arraycopy(row,0,newRow,1,row.length);
                        model.addRow(newRow);
                    }
                    userList = userInfo;
                }
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
    void filterTable(String username, String name , String status){
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(model);
                List<RowFilter<Object, Object>> filters = new ArrayList<>();
                if (!username.isEmpty()) filters.add(RowFilter.regexFilter(username,1));
                if (!name.isEmpty()) filters.add(RowFilter.regexFilter(name,2));
                if (!status.isEmpty()) filters.add(RowFilter.regexFilter(status,columnCount-2));
                if (!filters.isEmpty()){
                    RowFilter<Object,Object> combinedFilter = RowFilter.andFilter(filters);
                    sorter.setRowFilter(combinedFilter);
                }
                else sorter.setRowFilter(null);
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
    void add() {
        JDialog addDialog = new JDialog();
        addDialog.setTitle("Thêm người dùng");
        addDialog.setLayout(new GridLayout(8, 2, 10, 10));
        addDialog.getRootPane().setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Components for the dialog
        JTextField usernameField = new JTextField();
        JTextField fullNameField = new JTextField();
        JTextField addressField = new JTextField();
        JTextField dobField = new JTextField();
        JTextField genderField = new JTextField();
        JTextField emailField = new JTextField();
        JButton addButton = new JButton("Thêm");

        addDialog.add(new JLabel("Username:"));
        addDialog.add(usernameField);
        addDialog.add(new JLabel("Họ Tên:"));
        addDialog.add(fullNameField);
        addDialog.add(new JLabel("Địa chỉ:"));
        addDialog.add(addressField);
        addDialog.add(new JLabel("Ngày sinh (DD/MM/YYYY):"));
        addDialog.add(dobField);
        addDialog.add(new JLabel("Giới tính:"));
        addDialog.add(genderField);
        addDialog.add(new JLabel("Email:"));
        addDialog.add(emailField);
        //Trạng thái thì không tạo vì mới tạo làm gì đã online.
//        String[] statuses = {"","Online","Offline","Banned"};
//        JComboBox<String> statusField = new JComboBox<>(statuses);
//        addDialog.add(new JLabel("Trạng thái:"));
//        addDialog.add(statusField);
        addDialog.add(new JLabel());
        addDialog.add(addButton);

        addButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Get data
                String username = usernameField.getText();
                String fullName = fullNameField.getText();
                String address = addressField.getText();
                String dobString = dobField.getText();
                Boolean gender = genderField.getText().equalsIgnoreCase("Male") ? true : false;
                String email = emailField.getText();

                DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                LocalDateTime dob;

                try {
                    Timestamp timestamp = Timestamp.valueOf(LocalDate.parse(dobString, dateFormat).atStartOfDay());
                    dob =  timestamp.toLocalDateTime();
                } catch (DateTimeParseException ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(null, "Sai format ngày", "Lỗi", JOptionPane.ERROR_MESSAGE);
                    return;  // Exit the method if there's an error
                }
                try {
                    createUser(username, fullName, address, dob, gender, email);
                } catch (SQLException ex) {
                    throw new RuntimeException(ex);
                }
                addDialog.dispose();
            }
        });

        addDialog.pack();
        addDialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        addDialog.setLocationRelativeTo(null);
        addDialog.setVisible(true);
    }

    void createUser(String username, String fullName, String address, LocalDateTime dob, Boolean gender, String email ) throws SQLException {
        DBWrapper db = new DBWrapper();
        String[] name_part = fullName.split(" ");
        db.createUser(username, name_part[0], name_part[1], " ",address, dob, gender, email);
        System.out.println("Add successfully");
        db.close();
    }
    void updateUser(String id, String username, String fullName, String address, LocalDateTime dob, Boolean gender, String email, LocalDateTime creationDate ) throws SQLException {
        DBWrapper db = new DBWrapper();
        String[] name_part = fullName.split(" ");
        db.updateUser(id, username, name_part[0], name_part[1], address, dob, gender, email, creationDate);
        System.out.println("Update successfully");
        db.close();
    }
    void deleteUser(String id) throws SQLException {
        DBWrapper db = new DBWrapper();
        db.deleteUser(id);
        System.out.println("Update successfully");
        db.close();
    }
    void unBanorBanUser(String id, int type) throws SQLException {
        DBWrapper db = new DBWrapper();
        if (type == 1) {
            db.BanUser(id);
            System.out.println("Ban successfully");
        } else {
            db.UnBanUser(id);
            System.out.println("Ban successfully");
        }
        db.close();
    }

    void resetModelRow() {
        while (model.getRowCount() > 0) {
            model.removeRow(0);
        }
    }

    void showPopupMenu(JButton button) {
        JPopupMenu popupMenu = new JPopupMenu();

        JMenuItem update = new JMenuItem("Cập nhật thông tin");
        JMenuItem delete = new JMenuItem("Xoá");
        JMenuItem lockUnlocked = new JMenuItem("Khoá/Mở khoá");
        JMenuItem loginHistory = new JMenuItem("Lịch sử đăng nhập");
        JMenuItem friendList = new JMenuItem("Danh sách bạn bè");
        update.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Create a new JDialog for updating the selected row
                JDialog updateDialog = new JDialog();
                updateDialog.setTitle("Cập nhật người dùng");
                updateDialog.setLayout(new GridLayout(8, 2, 10, 10));
                updateDialog.getRootPane().setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

                // Components for the dialog
                JTextField usernameField = new JTextField();
                JTextField fullNameField = new JTextField();
                JTextField addressField = new JTextField();
                JTextField dobField = new JTextField();
                JTextField genderField = new JTextField();
                JTextField emailField = new JTextField();
                JTextField creationDateField = new JTextField();
                JButton updateButton = new JButton("Cập nhật");

                // Get the selected row index
                int selectedRow = getSelectedRow();

                // Check if a row is selected
                if (selectedRow == -1) {
                    JOptionPane.showMessageDialog(null, "Chọn một dòng để cập nhật", "Lỗi", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                // Populate text fields with data from the selected row
                usernameField.setText((String) getValueAt(selectedRow, 1));
                fullNameField.setText((String) getValueAt(selectedRow, 2));
                addressField.setText((String) getValueAt(selectedRow, 3));
                LocalDate dob = (LocalDate) getValueAt(selectedRow, 4);
                dobField.setText(dob.format(dateFormat));
                genderField.setText((String) getValueAt(selectedRow, 5));
                emailField.setText((String) getValueAt(selectedRow, 6));
                LocalDate creationDate = (LocalDate) getValueAt(selectedRow, 7);
                creationDateField.setText(creationDate.format(dateFormat));

                // Add components to the dialog
                updateDialog.add(new JLabel("Username:"));
                updateDialog.add(usernameField);
                updateDialog.add(new JLabel("Họ Tên:"));
                updateDialog.add(fullNameField);
                updateDialog.add(new JLabel("Địa chỉ:"));
                updateDialog.add(addressField);
                updateDialog.add(new JLabel("Ngày sinh (DD/MM/YYYY):"));
                updateDialog.add(dobField);
                updateDialog.add(new JLabel("Giới tính:"));
                updateDialog.add(genderField);
                updateDialog.add(new JLabel("Email:"));
                updateDialog.add(emailField);
                updateDialog.add(new JLabel("Ngày tạo TK (DD/MM/YYYY):"));
                updateDialog.add(creationDateField);
                // Trạng thái thì không tạo vì mới tạo làm gì đã online.
                // String[] statuses = {"","Online","Offline","Banned"};
                // JComboBox<String> statusField = new JComboBox<>(statuses);
                // updateDialog.add(new JLabel("Trạng thái:"));
                // updateDialog.add(statusField);
                updateDialog.add(new JLabel());
                updateDialog.add(updateButton);

                // Add action listener for the update button
                updateButton.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        // Get data from the text fields
                        String updatedUsername = usernameField.getText();
                        String updatedFullName = fullNameField.getText();
                        String updatedAddress = addressField.getText();
                        String updatedDob = dobField.getText();
                        boolean updatedGender = genderField.getText().equalsIgnoreCase("Male") ? true : false;
                        String updatedEmail = emailField.getText();
                        String updatedCreationDate = creationDateField.getText();

                        LocalDateTime dob;
                        LocalDateTime creationDate;

                        try {
                            Timestamp timestamp = Timestamp.valueOf(LocalDate.parse(updatedDob, dateFormat).atStartOfDay());
                            dob =  timestamp.toLocalDateTime();
                            timestamp = Timestamp.valueOf(LocalDate.parse(updatedCreationDate, dateFormat).atStartOfDay());
                            creationDate = timestamp.toLocalDateTime();
                        } catch (DateTimeParseException ex) {
                            ex.printStackTrace();
                            JOptionPane.showMessageDialog(null, "Sai format ngày", "Lỗi", JOptionPane.ERROR_MESSAGE);
                            return;  // Exit the method if there's an error
                        }
//
                        // Update data in the selected row
                        getModel().setValueAt(updatedUsername, selectedRow, 1);
                        getModel().setValueAt(updatedFullName, selectedRow, 2);
                        getModel().setValueAt(updatedAddress, selectedRow, 3);
                        getModel().setValueAt(dob, selectedRow, 4);
                        getModel().setValueAt(updatedGender, selectedRow, 5);
                        getModel().setValueAt(updatedEmail, selectedRow, 6);
                        getModel().setValueAt(creationDate, selectedRow, 7);

                        Object[] selectedUser = userListData.get(selectedRow);
                        try {
                            updateUser((String) selectedUser[0], updatedUsername, updatedFullName, updatedAddress, dob, updatedGender, updatedEmail, creationDate);
                        } catch (SQLException ex) {
                            throw new RuntimeException(ex);
                        }
                        int stop = 1;
                        // Gọi database ở đây
//                        Object[] newData = {studentID,firstName,lastName,finalDob,address};
//                        mySQLData.updateStudent(oldDataPrimaryKey,newData);
                        updateDialog.dispose();
                    }
                });

                updateDialog.pack();
                updateDialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
                updateDialog.setLocationRelativeTo(null);
                updateDialog.setVisible(true);
            }
        });
        delete.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int selectedRow = getSelectedRow();

                //Cái này t dùng để gọi khoá chính, thay bằng cái khác đi
                //String selectedStudentID = (String) model.getValueAt(selectedRow, 0);

                int confirm = JOptionPane.showConfirmDialog(
                        null,
                        "Bạn có chắc là xoá người dùng này?",
                        "Xác nhận xoá",
                        JOptionPane.YES_NO_OPTION
                );

                if (confirm == JOptionPane.YES_OPTION) {
                    //gọi database ở đây
                    Object[] selectedUser = userListData.get(selectedRow);
                    try {
                        deleteUser((String) selectedUser[0]);
                    } catch (SQLException ex) {
                        throw new RuntimeException(ex);
                    }
                    model.removeRow(selectedRow);
                    updateIndexColumn();
                }
            }
        });
        //cái này tao chỉnh từ Online/Offline sang Banned và ngược lại. Logic khác thì nói tao
        lockUnlocked.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int selectedRow = getSelectedRow();
                String curStatus = (String) getValueAt(selectedRow,8);
                if (!Objects.equals(curStatus, "Banned")){
                    int confirm = JOptionPane.showConfirmDialog(
                            null,
                            "Bạn có chắc là khoá người dùng này?",
                            "Xác nhận khoá",
                            JOptionPane.YES_NO_OPTION
                    );
                    if (confirm == JOptionPane.YES_OPTION) {
                        //gọi database ở đây
                        //mySQLData.deleteStudent(selectedStudentID);
                        Object[] selectedUser = userListData.get(selectedRow);
                        try {
                            unBanorBanUser((String) selectedUser[0], 1);
                        } catch (SQLException ex) {
                            throw new RuntimeException(ex);
                        }
                        setValueAt("Banned",selectedRow,8);
                    }
                }
                else{
                    int confirm = JOptionPane.showConfirmDialog(
                            null,
                            "Bạn có chắc là mở khoá người dùng này?",
                            "Xác nhận mở khoá",
                            JOptionPane.YES_NO_OPTION
                    );
                    if (confirm == JOptionPane.YES_OPTION) {
                        //gọi database ở đây
                        //mySQLData.deleteStudent(selectedStudentID);
                        Object[] selectedUser = userListData.get(selectedRow);
                        try {
                            unBanorBanUser((String) selectedUser[0], 0);
                        } catch (SQLException ex) {
                            throw new RuntimeException(ex);
                        }
                        setValueAt("Offline",selectedRow,8);
                    }
                }

            }
        });
        loginHistory.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int selectedRow = getSelectedRow();

                JDialog historyDialog = new JDialog();
                historyDialog.setTitle("Lịch sử đăng nhập");
                //Login history table
                String[] historyColumns = {"Ngày","Giờ"};
                JTable historyTable = new JTable(new DefaultTableModel(historyColumns,0));
                DefaultTableModel historyModel = (DefaultTableModel) historyTable.getModel();
                //data mẫu, cần hàm gọi data từ db;
                List<Object[]>historyData = new ArrayList<>();
                //SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");


                //Đống này để đổi từ ngày qua string như bên trên (add, update cac thu)
//                Date date = null;
//
//                try {
//                    dob = dateFormat.parse(updatedDob);
//                } catch (ParseException ex) {
//                    ex.printStackTrace();
//                    JOptionPane.showMessageDialog(null, "Sai format ngày", "Lỗi", JOptionPane.ERROR_MESSAGE);
//                    return;  // Exit the method if there's an error
//                }

//                Object[] data1 = {"01/01/2004","15:04"};
//                Object[] data2 = {"01/01/2005","21:04"};
//
//                historyData.add(data1);
//                historyData.add(data2);
//
//                for (Object[] row: historyData){
//                    historyModel.addRow(row);
//                }

                Object[] selectedUser = userListData.get(selectedRow);
                DBWrapper db = new DBWrapper();
                ResultSet userHisRes;
                // Get DateTime With Id
                try {
                    userHisRes = db.DiplayUserLogWithType((String) selectedUser[0], 0);
                } catch (SQLException ex) {
                    throw new RuntimeException(ex);
                }
                // Add to model
                while (true) {
                    try {
                        if (!userHisRes.next()) break;
                        String[] datetimeArr = userHisRes.getObject(1).toString().split(" ");

                        String date = datetimeArr[0];
                        String time = datetimeArr[1];
                        Object[] rowData = {date, time};

                        historyData.add(rowData);
                    } catch (SQLException ex) {
                        throw new RuntimeException(ex);
                    }

                }
                for (Object[] row: historyData){
                    historyModel.addRow(row);
                }
                db.close();
                DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
                centerRenderer.setHorizontalAlignment(JLabel.CENTER);
                historyTable.setDefaultRenderer(Object.class, centerRenderer);
                // Add to scrollPane
                JScrollPane historyScrollPane = new JScrollPane();
                historyScrollPane.setViewportView(historyTable); // Set the JTable as the view for JScrollPane

                // Add to dialog
                historyDialog.add(historyScrollPane);

                historyDialog.pack();
                historyDialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
                historyDialog.setLocationRelativeTo(null);
                historyDialog.setVisible(true);
            }
        });
        friendList.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int selectedRow = getSelectedRow();

                JDialog friendDialog = new JDialog();
                friendDialog.setTitle("Dach sách bạn bè");
                //Login history table
                String[] friendColumns = {"Họ tên","Trạng thái"};
                JTable friendsTable = new JTable(new DefaultTableModel(friendColumns,0));
                DefaultTableModel friendModel = (DefaultTableModel) friendsTable.getModel();
                //data mẫu, cần hàm gọi data từ db;
                List<Object[]>friendData = new ArrayList<>();

//                Object[] data1 = {"evilwomenmakemeactunwise","banned"};
//                Object[] data2 = {"ifevilwhyhot?","online"};
//
//                friendData.add(data1);
//                friendData.add(data2);
//
//                for (Object[] row: friendData){
//                    friendModel.addRow(row);
//                }

                Object[] selectedUser = userListData.get(selectedRow);
                DBWrapper db = new DBWrapper();
                ResultSet userFriendListRes;
                // Get DateTime With Id
                try {
                    userFriendListRes = db.FriendListOfUser((String) selectedUser[0]);
                } catch (SQLException ex) {
                    throw new RuntimeException(ex);
                }
                // Add to model
                while (true) {
                    try {
                        if (!userFriendListRes.next()) break;
                        String fullName = (String) userFriendListRes.getObject(1);
                        int status = (int) userFriendListRes.getObject(2);
                        String Detail = "";
                        switch (status) {
                            case 0:
                                Detail = "Offline";
                                break;
                            case 1:
                                Detail = "Online";
                                break;
                            case 2:
                                Detail = "Banned";
                                break;

                        }
                        Object[] rowData = {fullName, Detail};

                        friendData.add(rowData);
                    } catch (SQLException ex) {
                        throw new RuntimeException(ex);
                    }

                }
                for (Object[] row: friendData){
                    friendModel.addRow(row);
                }
                db.close();
                DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
                centerRenderer.setHorizontalAlignment(JLabel.CENTER);
                friendsTable.setDefaultRenderer(Object.class, centerRenderer);
                // Add to scrollPane
                JScrollPane friendScrollPane = new JScrollPane();
                friendScrollPane.setViewportView(friendsTable); // Set the JTable as the view for JScrollPane

                // Add to dialog
                friendDialog.add(friendScrollPane);

                friendDialog.pack();
                friendDialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
                friendDialog.setLocationRelativeTo(null);
                friendDialog.setVisible(true);
            }
        });
        // add to popup menu
        popupMenu.add(update);
        popupMenu.add(delete);
        popupMenu.add(lockUnlocked);
        popupMenu.add(loginHistory);
        popupMenu.add(friendList);

        // Show the popup menu at the cell's location
        popupMenu.show(this, button.getX(), button.getY());
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

            case "status":
                return 7;
            default:
                return -1;
        }
    }
    // Custom renderer for the button column
    // Custom editor for the button column
    private class ButtonEditor extends DefaultCellEditor {

        private JButton button;
        public ButtonEditor(JCheckBox checkBox) {
            super(checkBox);
            button = new JButton();
            // Add action listener to handle button clicks
            button.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    fireEditingStopped();
                    // Handle button click action
                    // ...
                    //Popup menu when clicked
                    showPopupMenu(button);
                }
            });
        }
        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            // Set the button text and icon (replace "Icon" with your actual icon)
            button.setIcon(new ImageIcon("server/src/main/resources/icon/more_vert.png"));
            // Customize the button as needed
            // ...
            return button;
        }

    }
    private static class ButtonRenderer extends JButton implements TableCellRenderer {
        public ButtonRenderer() {
            setOpaque(false);
            setBorderPainted(false);
            setContentAreaFilled(false);
            setFocusPainted(false);

        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            // Set the button text and icon (replace "Icon" with your actual icon)
            setIcon(new ImageIcon("server/src/main/resources/icon/more_vert.png"));
            // Customize the button as needed
            // ...
//            if (isSelected) {
////                setForeground(table.getSelectionForeground());
////                setBackground(table.getSelectionBackground());
//            } else {
//                // setForeground(table.getForeground());
//                // setBackground(UIManager.getColor("Button.background"));
//                //setContentAreaFilled(false);
//            }
            return this;
        }
    }

}

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
            String gender = !((Boolean) userNameQueryRes.getObject(5)) ? "Male" : "Female";

            // Email
            String email = (String) userNameQueryRes.getObject(6);

            // Creation Date
            dateString =  userNameQueryRes.getString("CreationDate");
            // Parse the string into a LocalDate object
            LocalDate creationDate = LocalDate.parse(dateString, formatter);

            // Status
            int status = (int) userNameQueryRes.getInt("Status");

            HashMap<String, Object> userDetailInfo = new HashMap<>();
            userDetailInfo.put("fullName", fullName);
            userDetailInfo.put("addrs", addrs);
            userDetailInfo.put("dob", dob);
            userDetailInfo.put("gender", gender);
            userDetailInfo.put("email", email);
            userDetailInfo.put("creationDate", creationDate);
            switch (status){
                case 0:
                    userDetailInfo.put("status", "Offline");
                    break;
                case 1:
                    userDetailInfo.put("status", "Online");
                    break;
                case 2:
                    userDetailInfo.put("status", "Banned");
                    break;
            }
            userListInfo.put(userId, userDetailInfo);
        }

        String stopHere = "Stop";
        publish(userListInfo);

        userNameQueryRes.close();
        return null;
    }

    @Override
    protected void process(List<HashMap<String, Object>> chunks) {
        super.process(chunks);

        for (HashMap<String, Object> obj : chunks)
            observer.workerDidUpdate(obj);
    }

    @Override
    protected void done() {
        super.done();
        db.close();
    }

}
