package com.psvm.server.view;

import javax.swing.*;
import java.awt.*;

public class ContentPane extends JPanel {
    ContentPane() {
        JLabel jLabel = new JLabel("Initial Content");
        this.setOpaque(false);
        this.add(jLabel);
    }
    void removeComponent(){
        Component[] componentList = this.getComponents();
        for(Component c : componentList){
            //Find the components you want to remove
            this.remove(c);
        }
    }
    void renderDSNguoiDung() {
        removeComponent();
        String[] columnNames = {"STT","Tên đăng nhập","Họ tên","Địa chỉ","Ngày sinh","Giới tính","Email","Ngày tạo TK","Trạng thái","Khác"};
        this.setLayout(new BorderLayout());
        DSNguoiDungTable table = new DSNguoiDungTable(columnNames);
        OptionPanel optionPanel = new OptionPanel(table);
        this.add(optionPanel,BorderLayout.NORTH);
        JScrollPane scrollPane = new JScrollPane();
        scrollPane.setViewportView(table);
        this.add(scrollPane,BorderLayout.CENTER);
        this.revalidate();
        this.repaint();
    }
}

