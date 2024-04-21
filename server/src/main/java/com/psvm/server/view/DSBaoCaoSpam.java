package com.psvm.server.view;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.*;
import javax.swing.text.MaskFormatter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.ParseException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

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
        filterButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
//                System.out.println(nameFilter);
//                System.out.println(usernameFilter);
//              table.filterTable(usernameFilter,nameFilter,statusFilter);
            }
        });
        //Add to filter Panel
        filterPanel.add(new JLabel("Giờ: "));
        filterPanel.add(timeField);
        filterPanel.add(new JLabel("Ngày (DD/MM/YYYY): "));
        filterPanel.add(dateField);
        filterPanel.add(filterButton);

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
    private final int columnCount;
    int index = 1;
    DefaultTableModel getTableModel(){
        return model;
    }
    DSBaoCaoSpamTable(String[] columnNames){
        super(new DefaultTableModel(columnNames,0));
        //Formating the table
        this.model = (DefaultTableModel) this.getModel();
        this.setDefaultEditor(Object.class,null);
        int iconHeight = new ImageIcon("src/main/resources/icon/more_vert.png").getIconHeight();
        this.setRowHeight(iconHeight);
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        this.setDefaultRenderer(Object.class, centerRenderer);
        // Enable sorting
        this.setAutoCreateRowSorter(true);
        //Example data
        java.util.List<Object[]> userAccountData = new ArrayList<>();
        Object[] acc1 = {"nhoma","sdfg"};
        Object[] acc2 = {"nhomb","sdfg"};
        Object[] acc3 = {"nhoma","sdfg"};
        //Đống bên dưới này sửa sao cho nó hiện từ cái SQL date sang Date là được, không cần phải là String (Ngày ko phải là String mà là Date, còn lại String hết (hay sao đó tuỳ) ), vì mảng là Object[]
        LocalDate date1 = LocalDate.of(2004,11, 5);
        LocalDate date2 = LocalDate.of(2004,12, 3);
        LocalDate date3 = LocalDate.of(2004,12, 4);
        acc1[1] = date1;
        acc2[1] = date2;
        acc3[1] = date3;
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

        // Add a custom renderer and editor for the last column
        this.getColumnModel().getColumn(columnNames.length - 1).setCellRenderer(new ButtonRenderer());
        this.getColumnModel().getColumn(columnNames.length - 1).setCellEditor(new ButtonEditor(new JCheckBox()));

        //formatting table
        setColumnWidthToFitContent();
        //add columnCount for later use
        this.columnCount = this.getColumnCount();
    }
    void filterTable(String name){
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(model);
                List<RowFilter<Object, Object>> filters = new ArrayList<>();
                if (!name.isEmpty()) filters.add(RowFilter.regexFilter(name,1));
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
        JPopupMenu popupMenu = new JPopupMenu();
        JMenuItem memberItem = new JMenuItem("Danh sách thành viên");
        JMenuItem adminItem = new JMenuItem("Danh sách admin");

        memberItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showTableMember();
            }
        });

        adminItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showTableAdmin();
            }
        });
        popupMenu.add(memberItem);
        popupMenu.add(adminItem);
        // Show the popup menu at the cell's location
        popupMenu.show(this, button.getX(), button.getY());
    }
    void showTableMember() {
        int selectedRow = getSelectedRow();
        //Cái này t dùng để gọi khoá chính, thay bằng cái khác đi
        //String chatName = (String) model.getValueAt(selectedRow, 1);

        JDialog tableDialog = new JDialog();
        tableDialog.setTitle("Danh sách thành viên");
        tableDialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

        //Thêm data vào ở đây (ko có ID thì bỏ)
        String[] columnNames = {"ID", "Tên thành viên"};
        Object[][] data = {
                {1, "John"},
                {2, "Alice"},
                {3, "Bob"},
        };
        JTable table = new JTable(new DefaultTableModel(data, columnNames));
        table.setDefaultEditor(Object.class,null);
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        table.setDefaultRenderer(Object.class, centerRenderer);
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        JScrollPane scrollPane = new JScrollPane(table);
        tableDialog.add(scrollPane);

        tableDialog.pack();
        tableDialog.setLocationRelativeTo(null);
        tableDialog.setVisible(true);
    }
    void showTableAdmin() {
        int selectedRow = getSelectedRow();
        //Cái này t dùng để gọi khoá chính, thay bằng cái khác đi
        //String chatName = (String) model.getValueAt(selectedRow, 1);

        JDialog tableDialog = new JDialog();
        tableDialog.setTitle("Danh sách admin");
        tableDialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        //Thêm data vào ở đây
        String[] columnNames = {"ID", "Tên Admin"};
        Object[][] data = {
                {1, "John"},
                {2, "Alice"},
                {3, "Bob"},
        };
        JTable table = new JTable(new DefaultTableModel(data, columnNames));
        table.setDefaultEditor(Object.class,null);
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        table.setDefaultRenderer(Object.class, centerRenderer);
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);

        JScrollPane scrollPane = new JScrollPane(table);
        tableDialog.add(scrollPane);

        tableDialog.pack();
        tableDialog.setLocationRelativeTo(null);
        tableDialog.setVisible(true);
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
            button.setIcon(new ImageIcon("src/main/resources/icon/more_vert.png"));
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
            setIcon(new ImageIcon("src/main/resources/icon/more_vert.png"));
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