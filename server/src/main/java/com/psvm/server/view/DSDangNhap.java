package com.psvm.server.view;


import javax.swing.*;
import javax.swing.border.EmptyBorder;

import javax.swing.table.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
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

            }
        });
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
    private final DefaultTableModel model;
    private int columnCount;
    DefaultTableModel getTableModel(){
        return model;
    }
    DSDangNhapTable(String[] columnNames){
        super(new DefaultTableModel(columnNames,0));
        this.model = (DefaultTableModel) this.getModel();
        int iconHeight = new ImageIcon("src/main/resources/icon/more_vert.png").getIconHeight();
        this.setRowHeight(iconHeight);
        this.setAutoCreateRowSorter(true);
        //Example data
        List<Object[]> userAccountData = new ArrayList<>();

        Object[] acc1 = {"1","haimen","Nguyễn Anh Khoa","123 Nguyen Thi Phuong, HA noi","21/10/2002","Nam","2men@gmail.com","21/02/2003","Online"};
        Object[] acc2 = {"2","aaimen","Nguyễn Phú Minh Bảo","723 Nguyen Thi Phuong, HA noi","11/10/2002","Nam","2men@gmail.com","21/10/2003","Online"};

        //Đống bên dưới này sửa sao cho nó hiện từ cái SQL date sang Date là được, không cần phải là String, vì mảng là Object[]
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        try{
            Date date1 = dateFormat.parse((String) acc1[acc1.length-2]);
            Date date2 = dateFormat.parse((String) acc2[acc2.length-2]);
            System.out.println(dateFormat.format(date1));
            System.out.println(dateFormat.format(date2));
            acc1[acc1.length-2] = dateFormat.format(date1);
            acc2[acc2.length-2] = dateFormat.format(date2);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
        userAccountData.add(acc1);
        userAccountData.add(acc2);

        //Add data to table
        for (Object[] row: userAccountData){
//            Icon moreIcon = new ImageIcon("src/main/resources/icon/more_vert.png");
//            Object[] newRow = new Object[row.length + 1];
            this.model.addRow(row);
        }
        // Add a custom renderer and editor for the last column
        this.getColumnModel().getColumn(columnNames.length - 1).setCellRenderer(new ButtonRenderer());
        this.getColumnModel().getColumn(columnNames.length - 1).setCellEditor(new ButtonEditor(new JCheckBox()));


        setColumnWidthToFitContent();
        //add columnCount for later use
        this.columnCount = this.getColumnCount();
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
//                setForeground(table.getSelectionForeground());
//                setBackground(table.getSelectionBackground());
            } else {
               // setForeground(table.getForeground());
               // setBackground(UIManager.getColor("Button.background"));
                //setContentAreaFilled(false);
            }
            return this;
        }
    }

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
            button.setIcon(new ImageIcon("src/main/resources/icon/more_vert.png"));
            // Customize the button as needed
            // ...
            return button;
        }
    }
    void showPopupMenu(JButton button) {
        JPopupMenu popupMenu = new JPopupMenu();

        JMenuItem update = new JMenuItem("Cập nhật thông tin");
        JMenuItem delete = new JMenuItem("Xoá");
        JMenuItem lockUnlocked = new JMenuItem("Khoá/Mở khoá");
        JMenuItem loginHistory = new JMenuItem("Lịch sử đăng nhập");
        JMenuItem friendList = new JMenuItem("Danh sách bạn bè");

        // add to popup menu
        popupMenu.add(update);
        popupMenu.add(delete);
        popupMenu.add(lockUnlocked);
        popupMenu.add(loginHistory);
        popupMenu.add(friendList);

        // Show the popup menu at the cell's location
        popupMenu.show(this, button.getX(), button.getY());
    }
}

