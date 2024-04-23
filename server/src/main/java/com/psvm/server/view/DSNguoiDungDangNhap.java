package com.psvm.server.view;

import com.psvm.server.controllers.DBWrapper;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.Serial;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

class DSNguoiDungDangNhapHeader extends JPanel{
    DSNguoiDungDangNhapHeader() {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        JLabel header = new JLabel("Danh sách đăng nhập theo thứ tự thời gian");
        JLabel instruct = new JLabel("Mặc định sắp xếp thời gian theo thứ tự tăng dần, ấn vào đầu cột 'Thời gian' để có thể đổi thứ tự sắp xếp");
        header.setAlignmentX(Component.CENTER_ALIGNMENT);
        instruct.setAlignmentX(Component.CENTER_ALIGNMENT);
        add(Box.createVerticalGlue());
        add(header);
        add(Box.createVerticalStrut(5));
        add(instruct);
        add(Box.createVerticalGlue());
        add(Box.createVerticalStrut(5));
        setBackground(Color.decode("#FDFDFD"));
    }
}
public class DSNguoiDungDangNhap extends JTable {
    private final DefaultTableModel model;
    private final TableRowSorter<DefaultTableModel> sorter;
    ScheduledExecutorService service = Executors.newScheduledThreadPool(5);

    private HashMap<String, Object> userLogList = new HashMap<>();

    List<Object[]> userLogListData; // Store reportID
    int index = 1;
    DSNguoiDungDangNhap(String[] columnNames){
        super(new DefaultTableModel(columnNames,0));
        this.setDefaultEditor(Object.class,null);
        this.model = (DefaultTableModel) this.getModel();
        this.sorter = new TableRowSorter<>(model);
        int iconHeight = new ImageIcon("server/src/main/resources/icon/more_vert.png").getIconHeight();
        this.setRowHeight(iconHeight);
        this.setRowSorter(sorter);
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        this.setDefaultRenderer(Object.class, centerRenderer);
        //this.setAutoCreateRowSorter(true);

        //Renderer for "time" column
        DateTimeFormatter myFormatObj = DateTimeFormatter.ofPattern("HH:mm:ss dd-MM-yyyy");
        DefaultTableCellRenderer dateTimeRenderer = new DefaultTableCellRenderer() {
            @Serial
            private static final long serialVersionUID = 1L;
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                if (value instanceof LocalDateTime) {
                    value = ((LocalDateTime) value).format(myFormatObj);
                }
                setHorizontalAlignment(SwingConstants.CENTER);
                return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            }
        };

        getColumnModel().getColumn(3).setCellRenderer(dateTimeRenderer);
        setColumnWidthToFitContent();

        //Lúc gọi này trong mySQL phải có dạng DATETIME, sau đso nhớ chuyển về LocalDateTime
        //Nếu chỉ có ngày thì xem trong file DSNguoiDung của tao (Nói tao sửa lại neu ko biet lam)
        //Data example
        List<Object[]> userAccountData = new ArrayList<>();
        LocalDateTime dateTime1 = LocalDateTime.of(2023, 12, 1, 14, 30, 15);
        LocalDateTime dateTime2 = LocalDateTime.of(2023, 12, 1, 12, 45, 30);
        LocalDateTime dateTime3 = LocalDateTime.of(2023, 12, 2, 8, 15, 45);
        LocalDateTime dateTime4 = LocalDateTime.of(2023, 11, 2, 8, 15, 45);
        Object[] acc1 = {"ge", "Nguyễn Anh Khoa", dateTime1};
        Object[] acc2 = {"23", "Nguyễn Phú Minh Bảo", dateTime2};
        Object[] acc3 = {"asdf", "Nguyễn Phú Minh Bảo", dateTime3};

        userAccountData.add(acc1);
        userAccountData.add(acc2);
        userAccountData.add(acc3);

        //Add data to table
        for (Object[] row: userAccountData){
            addRowAndSort(row);
        }
        startNextWorker();
        addRowAndSort(new Object[]{"aaiasdfasdfmen", "Nguyễn Phú Minh Bảo", dateTime4});
    }

    protected void startNextWorker() {
        UserLogListThread userWorker = new UserLogListThread(new UserLogListThread.Observer() {
            @Override
            public void workerDidUpdate(HashMap<String, Object> userLogInfo) {
                if (!userLogList.equals(userLogInfo)) {
                    userLogListData = new ArrayList<>();

                    userLogInfo.forEach((index, detail) -> {
                        Object[] userLog = new Object[3];
                        HashMap<String, Object> castedDetail = (HashMap<String, Object>) detail;
                        userLog[0] = (String) castedDetail.get("userId");
                        userLog[1] = (String) castedDetail.get("fullName");
                        userLog[2] = (LocalDate) castedDetail.get("date");

                        userLogListData.add(userLog);
                    });

                    resetModelRow(); // reset table
                    index = 1;
                    for (Object[] row: userLogListData){
                        Object[] newRow = new Object[row.length + 1];
                        newRow[0] = index++;
                        System.arraycopy(row,0,newRow,1,row.length);
                        model.addRow(newRow);
                    }
                    userLogList = userLogInfo;
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
    public void addRowAndSort(Object[] rowData) {
        Object[] newRow = new Object[rowData.length + 1];
        newRow[0] = index++;
        System.arraycopy(rowData, 0, newRow, 1, rowData.length);
        model.addRow(newRow);
        //adding the data and sorting
        List<RowSorter.SortKey> sortKeys = new ArrayList<>();
        sortKeys.add(new RowSorter.SortKey(3, SortOrder.ASCENDING));
        sorter.setSortKeys(sortKeys);
        sorter.sort();
        updateIndexColumn();
    }
}

class UserLogListThread extends SwingWorker<Void, HashMap<String, Object>> {
    private final DBWrapper db;
    private Observer observer;

    public interface Observer {
        public void workerDidUpdate(HashMap<String, Object> userInfo);
    }

    public UserLogListThread(Observer observer) {
        // Connect DB
        this.db = new DBWrapper();
        this.observer = observer;
    }

    @Override
    protected Void doInBackground() throws Exception {
        // Get User List With given DateTime
        ResultSet userNameQueryRes = db.getUserLogListWithDetailInfo();
        int index = 1;
        HashMap<String, Object> userLogListInfo = new HashMap<>();
        while (userNameQueryRes.next()) {
            // Get UserName First
            String userId = (String) userNameQueryRes.getObject(1);
            // Get Fullname
            String fullName = (String) userNameQueryRes.getObject(2);
            // Date of birth
            String dateString =  userNameQueryRes.getString("Datetime");
            // Define the format of the input string
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            // Parse the string into a LocalDate object
            LocalDate localDate = LocalDate.parse(dateString, formatter);

            HashMap<String, Object> userDetailInfo = new HashMap<>();
            userDetailInfo.put("userId", userId);
            userDetailInfo.put("fullName", fullName);
            userDetailInfo.put("date", localDate);

            userLogListInfo.put(String.valueOf(index), userDetailInfo);
            index  ++;
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
