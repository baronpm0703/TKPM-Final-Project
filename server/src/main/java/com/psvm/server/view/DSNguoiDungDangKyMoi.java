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


class OptionPanelDSNguoiDungDangKyMoi extends JPanel{
    DSNguoiDungDangKyMoiTable table;
    OptionPanelDSNguoiDungDangKyMoi(DSNguoiDungDangKyMoiTable table){
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
        JTextField nameField = new JTextField();
        nameField.setColumns(20);

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
                System.out.println(nameFilter);
                table.filterTable(nameFilter, dayStart, monthStart,yearStart,dayEnd,monthEnd,yearEnd);
            }
        });
        //Add to filter Panel
        filterPanel.add(new JLabel("Từ Ngày: "));
        filterPanel.add(dayChoice);
        filterPanel.add(new JLabel("Tháng: "));
        filterPanel.add(monthChoice);
        filterPanel.add(new JLabel("Năm: "));
        filterPanel.add(yearChoice);
        filterPanel.add(new JLabel("Đến Ngày: "));
        filterPanel.add(dayChoice2);
        filterPanel.add(new JLabel("Tháng: "));
        filterPanel.add(monthChoice2);
        filterPanel.add(new JLabel("Năm: "));
        filterPanel.add(yearChoice2);
        filterPanel.add(new JLabel("Tên: "));
        filterPanel.add(nameField);
        filterPanel.add(filterButton);

        JButton refreshButton = new JButton("Làm mới bộ lọc");
        refreshButton.setFocusPainted(false);
        refreshButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dayChoice.setSelectedIndex(0);
                monthChoice.setSelectedIndex(0);
                yearChoice.setText("");
                dayChoice2.setSelectedIndex(0);
                monthChoice2.setSelectedIndex(0);
                yearChoice2.setText("");
                nameField.setText("");

                table.filterTable("","","","","","","");
            }
        });
        filterPanel.add(refreshButton);

        //Add to Option Panel
        this.add(filterPanel,BorderLayout.WEST);


    }
}
class DSNguoiDungDangKyMoiTable extends JTable{
    //private MySQLData mySQLData; //Biến để lưu database, coi cách gọi database của tao trong Example/Table
    // Cần dòng này để gọi dữ liệu từ database
    // Khi trả dữ về thì trả với dạng List<Object[]> (ArrayList)
    // Coi trong folder EXAMPLE, MySQLData
    ScheduledExecutorService service = Executors.newScheduledThreadPool(5);
    private final DefaultTableModel model;
    private final int columnCount;

    // private HashMap<String, Object> userList = new HashMap<>();
    private HashMap<String, Object> newRegisterList = new HashMap<>();
    List<Object[]> newRegisterListData; // Store conID
    int index = 1;
    DefaultTableModel getTableModel(){
        return model;
    }
    DSNguoiDungDangKyMoiTable(String[] columnNames){
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
//        model.addRow(new Object[]{0,"hải", LocalDate.of(2023,5,30)});
//        model.addRow(new Object[]{1,"khoa",LocalDate.of(2023,6,2)});
//        model.addRow(new Object[]{2,"bảo1",LocalDate.of(2022,11,4)});
//        model.addRow(new Object[]{3,"bảo2",LocalDate.of(2022,5,4)});
        startNextWorker();
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

    protected void startNextWorker() {
        NewRegisterListThread userWorker = new NewRegisterListThread(new NewRegisterListThread.Observer() {
            @Override
            public void workerDidUpdate(HashMap<String, Object> newRegisterInfo) {
                if (!newRegisterList.equals(newRegisterInfo)) {

                    newRegisterListData = new ArrayList<>();
                    newRegisterInfo.forEach((userId, detail) -> {
                        Object[] row = new Object[2];
                        HashMap<String, Object> castedDetail = (HashMap<String, Object>) detail;
                        row[0] = (String) castedDetail.get("Hoten");
                        row[1] = (LocalDate) castedDetail.get("CreateDate");

                        newRegisterListData.add(row);
                    });

                    resetModelRow(); // Reset new Data
                    index = 1;
                    for (Object[] row: newRegisterListData){
                        Object[] newRow = new Object[row.length + 1];
                        newRow[0] = index++;
                        System.arraycopy(row,0,newRow,1,row.length);
                        model.addRow(newRow);
                    }

                    newRegisterList = newRegisterInfo;
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
    void filterTable(String name, String dayStart, String monthStart, String yearStart, String dayEnd, String monthEnd, String yearEnd) {
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
