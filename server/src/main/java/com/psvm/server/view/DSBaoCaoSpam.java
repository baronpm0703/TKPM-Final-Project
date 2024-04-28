package com.psvm.server.view;

import com.psvm.server.controllers.DBWrapper;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.*;
import javax.swing.text.MaskFormatter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

class OptionPanelDSSpam extends JPanel {
    DSBaoCaoSpamTable table;
    OptionPanelDSSpam(DSBaoCaoSpamTable table) {
        this.table = table;
        this.setLayout(new BorderLayout());
        this.setOpaque(false);
        //this.setBorder(new EmptyBorder(10,10,10,10));
        //Filter panel
        JPanel filterPanel = new JPanel();
        filterPanel.setLayout(new FlowLayout());
        filterPanel.setOpaque(false);
        filterPanel.setBorder(new EmptyBorder(0, 0, 0, 100));
        //Filter fiele
        JFormattedTextField timeField = null;
        JFormattedTextField dateField = null;
        try {
            //Time text field
            MaskFormatter mask = new MaskFormatter("##:##");
            timeField = new JFormattedTextField(mask);
            timeField.setColumns(4); //size
            timeField.setHorizontalAlignment(JTextField.CENTER);
            //Date text field
            MaskFormatter dateFormatter = new MaskFormatter("##/##/####");
            dateField = new JFormattedTextField(dateFormatter);
            dateField.setColumns(10); //size
            dateField.setHorizontalAlignment(JTextField.CENTER);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        //Filter button
        JButton filterButton = new JButton("       Lọc       ");
        filterButton.setFocusPainted(false);
        //Filter button logic
        JFormattedTextField finalTimeField = timeField;
        JFormattedTextField finalDateField = dateField;
        filterButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String timeFilter = finalTimeField.getText();
                String dateFilter = finalDateField.getText();
//                System.out.println(nameFilter);
//                System.out.println(usernameFilter);
              table.filterTable(timeFilter, dateFilter);
            }
        });
        //Add to filter Panel
        filterPanel.add(new JLabel("Giờ: "));
        filterPanel.add(timeField);
        filterPanel.add(new JLabel("Ngày (DD/MM/YYYY): "));
        filterPanel.add(dateField);
        filterPanel.add(filterButton);

        // Refresh
        JButton refreshButton = new JButton("Làm mới bộ lọc");
        refreshButton.setFocusPainted(false);
        refreshButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                finalTimeField.setText("");
                finalDateField.setText("");
                table.filterTable("","");
            }
        });
        filterPanel.add(refreshButton);

        //Utilities Panel
        JPanel utilities = new JPanel();
        utilities.setOpaque(false);

        //Add to Option Panel
        this.add(filterPanel, BorderLayout.WEST);
        this.add(utilities, BorderLayout.EAST);
    }
}
class DSBaoCaoSpamTable extends JTable{
    //private MySQLData mySQLData; //Biến để lưu database, coi cách gọi database của tao trong Example/Table
    // Cần dòng này để gọi dữ liệu từ database
    // Khi trả dữ về thì trả với dạng List<Object[]> (ArrayList)
    // Coi trong folder EXAMPLE, MySQLData
    private final DefaultTableModel model;
    ScheduledExecutorService service = Executors.newScheduledThreadPool(5);
    private final int columnCount;
    int index = 1;
    private HashMap<String, Object> reportList = new HashMap<>();
    List<Object[]> reportedListData; // Store reportID
    DefaultTableModel getTableModel(){
        return model;
    }
    DSBaoCaoSpamTable(String[] columnNames){
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
        java.util.List<Object[]> userAccountData = new ArrayList<>();
        Object[] acc1 = {"nhoma","sdfg","ádda"};
        Object[] acc2 = {"nhomb","sdfg","adad"};
        Object[] acc3 = {"nhoma","sdfg","adad"};
        //Đống bên dưới này sửa sao cho nó hiện từ cái SQL date sang Date là được, không cần phải là String (Ngày ko phải là String mà là Date, còn lại String hết (hay sao đó tuỳ) ), vì mảng là Object[]
        LocalDate date1 = LocalDate.of(2004,11, 5);
        LocalDate date2 = LocalDate.of(2004,12, 3);
        LocalDate date3 = LocalDate.of(2004,12, 4);
        acc1[2] = date1;
        acc2[2] = date2;
        acc3[2] = date3;
        userAccountData.add(acc1);
        userAccountData.add(acc2);
        userAccountData.add(acc3);

        //Add data to table
        for (Object[] row: userAccountData){
            Object[] newRow = new Object[row.length + 1];
            newRow[0] = index++;
            System.arraycopy(row,0,newRow,1,row.length);
            this.model.addRow(newRow);
        }

        startNextWorker();
        // Add a custom renderer and editor for the last column
        this.getColumnModel().getColumn(columnNames.length - 1).setCellRenderer(new ButtonRenderer());
        this.getColumnModel().getColumn(columnNames.length - 1).setCellEditor(new ButtonEditor(new JCheckBox()));

        //formatting table
        setColumnWidthToFitContent();
        //add columnCount for later use
        this.columnCount = this.getColumnCount();
    }

    protected void startNextWorker() {
        ReportListThread userWorker = new ReportListThread(new ReportListThread.Observer() {
            @Override
            public void workerDidUpdate(HashMap<String, Object> reportInfo) {
                if (!reportList.equals(reportInfo)) {
                    reportedListData = new ArrayList<>();

                    reportInfo.forEach((rpindex, detail) -> {
                        Object[] report = new Object[3];
                        HashMap<String, Object> castedDetail = (HashMap<String, Object>) detail;
                        report[0] = (String) castedDetail.get("reporterId");
                        report[1] = (String) castedDetail.get("reportedId");
                        report[2] = (LocalDate) castedDetail.get("datetime");

                        reportedListData.add(report);
                        int stop = 1;
                    });

                    resetModelRow(); // Reset new Data
                    index = 1;
                    for (Object[] row: reportedListData){
                        Object[] newRow = new Object[row.length + 1];
                        newRow[0] = index++;
                        System.arraycopy(row,0,newRow,1,row.length);
                        model.addRow(newRow);
                    }
                    reportList = reportInfo;

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

    void resetModelRow() {
        while (model.getRowCount() > 0) {
            model.removeRow(0);
        }
    }
    void filterTable(String time, String date){
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(model);
                List<RowFilter<Object, Object>> filters = new ArrayList<>();
                if (!date.isEmpty()) filters.add(RowFilter.regexFilter(date, 3));
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

    void showPopupMenu(JButton button) {
        int selectedRow = getSelectedRow();
        Object[] selectedReport = reportedListData.get(selectedRow);

        JPopupMenu popupMenu = new JPopupMenu();
        JMenuItem lockAcc = new JMenuItem("Khóa tài khoản người dùng: " + selectedReport[1]);
        JMenuItem declineReq = new JMenuItem("Từ chối yêu cầu");

        // Khi Tao nhấn 1 khóa hoặc từ chối có thể đổi icon của cột chi tiết không?
        lockAcc.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Đổi Icon
                try {
                    showTableMember(selectedReport);
                } catch (SQLException ex) {
                    throw new RuntimeException(ex);
                }
            }
        });

        declineReq.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Đổi ICon
            }
        });
        popupMenu.add(lockAcc);
        popupMenu.add(declineReq);
        // Show the popup menu at the cell's location
        popupMenu.show(this, button.getX(), button.getY());
    }
    void showTableMember(Object[] selectedReport) throws SQLException {
       DBWrapper db = new DBWrapper();
       db.BanUser((String) selectedReport[1]);
       //db.UpdateUserLogBanType();
       db.close();
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

class ReportListThread extends SwingWorker<Void, HashMap<String, Object>> {
    private final DBWrapper db;
    private Observer observer;

    public interface Observer {
        public void workerDidUpdate(HashMap<String, Object> userInfo);
    }

    public ReportListThread(Observer observer) {
        // Connect DB
        this.db = new DBWrapper();
        this.observer = observer;
    }

    @Override
    protected Void doInBackground() throws Exception {
        // Get Conversation
        ResultSet reportQueryRes = db.getSpamReportInfo();


        HashMap<String, Object> userLogListInfo = new HashMap<>();
        int index = 1;
        while (reportQueryRes.next()) {
            HashMap<String, Object> reportDetail = new HashMap<>();
            // Get reportedId
            String reporterId = (String) reportQueryRes.getObject(1);
            // Get senderId
            String reportedId = (String) reportQueryRes.getObject(2);
            // Get DateTime send report
            String dateString =  reportQueryRes.getString("DateTime");
            // Define the format of the input string
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            // Parse the string into a LocalDate object
            LocalDate localDate = LocalDate.parse(dateString, formatter);
            reportDetail.put("reporterId", reporterId);
            reportDetail.put("reportedId", reportedId);
            reportDetail.put("datetime", localDate);

            userLogListInfo.put(String.valueOf(index), reportDetail);
            index ++;
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

    @Override
    protected void done() {
        super.done();
        db.close();
    }

}