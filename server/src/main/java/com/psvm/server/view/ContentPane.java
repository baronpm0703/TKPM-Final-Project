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
        OptionPanelDSNguoiDung optionPanel = new OptionPanelDSNguoiDung(table);
        this.add(optionPanel,BorderLayout.NORTH);
        JScrollPane scrollPane = new JScrollPane();
        scrollPane.setViewportView(table);
        this.add(scrollPane,BorderLayout.CENTER);
        this.revalidate();
        this.repaint();
    }
    void renderDSNguoiDungDangNhap(){
        removeComponent();
        String[] columnNames = {"STT","Tên đăng nhập","Họ tên","Thời gian"};
        this.setLayout(new BorderLayout());
        DSNguoiDungDangNhapHeader header = new DSNguoiDungDangNhapHeader();
        this.add(header,BorderLayout.NORTH);
        DSNguoiDungDangNhap table = new DSNguoiDungDangNhap(columnNames);
        JScrollPane scrollPane = new JScrollPane();
        scrollPane.setViewportView(table);
        this.add(scrollPane,BorderLayout.CENTER);
        this.revalidate();
        this.repaint();
    }
    void renderDSNhomChat(){
        removeComponent();
        String[] columnNames = {"STT","Tên","Thời gian tạo","Xem thêm"};
        this.setLayout(new BorderLayout());
        DSNhomChatTable table = new DSNhomChatTable(columnNames);
        OptionPanelDSNhomChat optionPanel = new OptionPanelDSNhomChat(table);
        this.add(optionPanel,BorderLayout.NORTH);
        JScrollPane scrollPane = new JScrollPane();
        scrollPane.setViewportView(table);
        this.add(scrollPane,BorderLayout.CENTER);
        this.revalidate();
        this.repaint();
    }
    void renderDSBaoCaoSpam(){
        removeComponent();
        String[] columnNames = {"STT","Người Gửi", "Người Bị Báo Cáo","Thời gian tạo", "Chi Tiết"};
        this.setLayout(new BorderLayout());
        DSBaoCaoSpamTable table = new DSBaoCaoSpamTable(columnNames);
        OptionPanelDSSpam optionPanel = new OptionPanelDSSpam(table);
        this.add(optionPanel,BorderLayout.NORTH);
        JScrollPane scrollPane = new JScrollPane();
        scrollPane.setViewportView(table);
        this.add(scrollPane,BorderLayout.CENTER);
        this.revalidate();
        this.repaint();
    }
}

