package com.psvm.server.view;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

class OptionPanel extends JPanel{
    DSDangNhapTable table;
    OptionPanel(DSDangNhapTable table){
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
        String[] statuses = {"Online","Offline","Banned"};
        JComboBox<String> statusField = new JComboBox<>(statuses);

        //Filter button
        JButton filterButton = new JButton("       Lọc       ");
        filterButton.setFocusPainted(false);
        //Add to filter Panel
        filterPanel.add(new JLabel("Tên: "));
        filterPanel.add(nameField);
        filterPanel.add(new JLabel("Tên Đăng nhập: "));
        filterPanel.add(usernameField);
        filterPanel.add(new JLabel("Trạng thái: "));
        filterPanel.add(statusField);
        filterPanel.add(filterButton);

        //Utilities Panel
        JPanel utilities = new JPanel();
        utilities.setOpaque(false);
        //Add new user
        JButton addUser = new JButton("Thêm người dùng");
        addUser.setFocusPainted(false);
        utilities.add(addUser);

        //Add to Option Panel
        this.add(filterPanel,BorderLayout.WEST);
        this.add(utilities,BorderLayout.EAST);
    }
}
class DSDangNhapTable extends JTable{
    //private MySQLData mySQLData;
    // Cần dòng này để gọi dữ liệu từ database
    // Khi trả dữ về thì trả với dạng List<Object[]> (ArrayList)
    // Coi trong folder EXAMPLE, MySQLData
    private DefaultTableModel model;
    DSDangNhapTable(String[] columnNames){
        super(new DefaultTableModel(columnNames,0));
        this.model = (DefaultTableModel) this.getModel();
        int iconHeight = new ImageIcon("src/main/resources/icon/more_vert.png").getIconHeight();
        this.setRowHeight(iconHeight);

        //Example data
        List<Object[]> userAccountData = new ArrayList<>();
        Object[] acc1 = {"1","haimen","Nguyễn ANH KHOA","123 Nguyen Thi Phuong, HA noi","21/10/2002","Nam","2men@gmail.com","21/02/2003","Chưa Kích hoạt"};
        Object[] acc2 = {"1","haimen","Nguyễn ANH KHOA","123 Nguyen Thi Phuong, HA noi","21/10/2002","Nam","2men@gmail.com","21/02/2003","Chưa Kích hoạt"};
        userAccountData.add(acc1);
        userAccountData.add(acc2);

        for (Object[] row: userAccountData){
//            Icon moreIcon = new ImageIcon("src/main/resources/icon/more_vert.png");
//            Object[] newRow = new Object[row.length + 1];
            this.model.addRow(row);
        }
        // Add a custom renderer and editor for the last column
        this.getColumnModel().getColumn(columnNames.length - 1).setCellRenderer(new ButtonRenderer());
        this.getColumnModel().getColumn(columnNames.length - 1).setCellEditor(new ButtonEditor(new JCheckBox()));
        setColumnWidthToFitContent();
    }
    private void setColumnWidthToFitContent() {
        for (int column = 0; column < this.getColumnCount(); column++) {
            int maxwidth = 0;
            for (int row = 0; row < this.getRowCount(); row++) {
                TableCellRenderer renderer = getCellRenderer(row, column);
                Component comp = prepareRenderer(renderer, row, column);
                maxwidth = Math.max(comp.getPreferredSize().width, maxwidth);
            }
            TableColumn tableColumn = getColumnModel().getColumn(column);
            tableColumn.setPreferredWidth(maxwidth);
        }
    }
    // Custom renderer for the button column
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
            setIcon(new ImageIcon("src/main/resources/icon/more_vert.png"));

            // Customize the button as needed
            // ...
            if (isSelected) {
                setForeground(table.getSelectionForeground());
                setBackground(table.getSelectionBackground());
            } else {
                setForeground(table.getForeground());
                setBackground(UIManager.getColor("Button.background"));
            }
            return this;
        }
    }

    // Custom editor for the button column
    private static class ButtonEditor extends DefaultCellEditor {
        private JButton button;

        public ButtonEditor(JCheckBox checkBox) {
            super(checkBox);
            button = new JButton();
//            button.setOpaque(false);
//            button.setBorderPainted(false);
//            button.setBackground(null);
            // Add action listener to handle button clicks
            button.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    fireEditingStopped();
                    // Handle button click action
                    // ...
                }
            });
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            // Set the button text and icon (replace "Icon" with your actual icon)
            button.setIcon(new ImageIcon("src/main/resources/icon/more_vert.png"));
            // Customize the button as needed
            // ...

            return button;
        }
    }
}

