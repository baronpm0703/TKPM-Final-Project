package com.psvm.server.view.EXAMPLE;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

public class Table extends JTable {
    private MySQLData mySQLData;
    Table(String[] columnNames, MySQLData mySQLData){
        super(new DefaultTableModel(columnNames,0));
        this.mySQLData = mySQLData;
        List<Object[]> studentsData = mySQLData.getAllStudent();
        DefaultTableModel model = (DefaultTableModel) this.getModel();
        for (Object[] row: studentsData){
            Object[] newRow = new Object[row.length + 2];
            for (int i = 0; i < row.length; i++)
                newRow[i] = row[i];
            newRow[newRow.length-2] = "Update";
            newRow[newRow.length-1] = "Delete";
            model.addRow(newRow);
        }
        this.getColumn("Update").setCellRenderer(new ButtonRenderer());
        this.getColumn("Update").setCellEditor(new ButtonEditorUpdate(new JCheckBox()));
        this.getColumn("Delete").setCellRenderer(new ButtonRenderer());
        this.getColumn("Delete").setCellEditor(new ButtonEditorDelete(new JCheckBox(),this,mySQLData));
    }
}
//https://stackoverflow.com/questions/13833688/adding-jbutton-to-jtable
class ButtonRenderer extends JButton implements TableCellRenderer {

    public ButtonRenderer() {
        setOpaque(true);
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value,
                                                   boolean isSelected, boolean hasFocus, int row, int column) {
        if (isSelected) {
            setForeground(table.getSelectionForeground());
            setBackground(table.getSelectionBackground());
        } else {
            setForeground(table.getForeground());
            setBackground(UIManager.getColor("Button.background"));
        }
        setText((value == null) ? "" : value.toString());
        return this;
    }
}

class ButtonEditorUpdate extends DefaultCellEditor {

    protected JButton button;
    private String label;
    private boolean isPushed;

    public ButtonEditorUpdate(JCheckBox checkBox) {
        super(checkBox);
        button = new JButton();
        button.setOpaque(true);
        button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                fireEditingStopped();
            }
        });
    }

    @Override
    public Component getTableCellEditorComponent(JTable table, Object value,
                                                 boolean isSelected, int row, int column) {
        if (isSelected) {
            button.setForeground(table.getSelectionForeground());
            button.setBackground(table.getSelectionBackground());
        } else {
            button.setForeground(table.getForeground());
            button.setBackground(table.getBackground());
        }
        label = (value == null) ? "" : value.toString();
        button.setText(label);
        isPushed = true;
        return button;
    }

    @Override
    public Object getCellEditorValue() {
        if (isPushed) {

        }
        isPushed = false;
        return label;
    }

    @Override
    public boolean stopCellEditing() {
        isPushed = false;
        return super.stopCellEditing();
    }

}
class ButtonEditorDelete extends DefaultCellEditor {

    protected JButton button;
    private String label;
    private boolean isPushed;

    private JTable table;

    private MySQLData mySQLData;
    public ButtonEditorDelete(JCheckBox checkBox, JTable table, MySQLData mySQLData) {
        super(checkBox);
        this.table = table;
        this.mySQLData = mySQLData;
        button = new JButton();
        button.setOpaque(true);
        button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                fireEditingStopped();
            }
        });
    }

    @Override
    public Component getTableCellEditorComponent(JTable table, Object value,
                                                 boolean isSelected, int row, int column) {
        if (isSelected) {
            button.setForeground(table.getSelectionForeground());
            button.setBackground(table.getSelectionBackground());
        } else {
            button.setForeground(table.getForeground());
            button.setBackground(table.getBackground());
        }
        label = (value == null) ? "" : value.toString();
        button.setText(label);
        isPushed = true;
        return button;
    }

    @Override
    public Object getCellEditorValue() {
        if (isPushed) {
            // Handle deletion here
            int selectedRow = table.getSelectedRow();
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    DefaultTableModel model = (DefaultTableModel) table.getModel();
                    String studentID = (String) model.getValueAt(selectedRow,0);
                    mySQLData.deleteStudent(studentID);
                    model.removeRow(selectedRow);
                }
            });
        }
        isPushed = false;
        return label;
    }

    @Override
    public boolean stopCellEditing() {
        isPushed = false;
        return super.stopCellEditing();
    }
}