package com.psvm.server.view;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.io.Serial;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

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
    int index = 1;
    DSNguoiDungDangNhap(String[] columnNames){
        super(new DefaultTableModel(columnNames,0));
        this.setDefaultEditor(Object.class,null);
        this.model = (DefaultTableModel) this.getModel();
        this.sorter = new TableRowSorter<>(model);
        int iconHeight = new ImageIcon("src/main/resources/icon/more_vert.png").getIconHeight();
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
        addRowAndSort(new Object[]{"aaiasdfasdfmen", "Nguyễn Phú Minh Bảo", dateTime4});
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
