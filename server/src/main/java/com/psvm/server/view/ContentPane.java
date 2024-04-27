package com.psvm.server.view;

import javax.swing.*;
import java.awt.*;
import java.sql.SQLException;

public class ContentPane extends JPanel {
    ContentPane() {
        JLabel jLabel = new JLabel("Initial Content");
        this.setOpaque(false);
        this.add(jLabel);
    }
    void removeComponent(){
        this.removeAll();
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
    void renderDSLienLacNguoiDung(){
        removeComponent();
        String[] columnNames = {"STT","Tên", "Số lượng bạn bè"};
        this.setLayout(new BorderLayout());
        DSLienLacNguoiDungTable table = new DSLienLacNguoiDungTable(columnNames);
        OptionPanelDSLienLacNguoiDung optionPanel = new OptionPanelDSLienLacNguoiDung(table);
        this.add(optionPanel,BorderLayout.NORTH);
        JScrollPane scrollPane = new JScrollPane();
        scrollPane.setViewportView(table);
        this.add(scrollPane,BorderLayout.CENTER);
        this.revalidate();
        this.repaint();
    }
    void renderDSNguoiDungDangKyMoi(){
        removeComponent();
        String[] columnNames = {"STT","Tên", "Thời gian"};
        this.setLayout(new BorderLayout());
        DSNguoiDungDangKyMoiTable table = new DSNguoiDungDangKyMoiTable(columnNames);
        OptionPanelDSNguoiDungDangKyMoi optionPanel = new OptionPanelDSNguoiDungDangKyMoi(table);
        this.add(optionPanel,BorderLayout.NORTH);
        JScrollPane scrollPane = new JScrollPane();
        scrollPane.setViewportView(table);
        this.add(scrollPane,BorderLayout.CENTER);
        this.revalidate();
        this.repaint();
    }
    void renderDSNguoiDungHoatDong(){
        removeComponent();
        String[] columnNames = {"STT","Tên", "Thời gian hoạt lần cuối","Số lần mở ứng dụng","Số lần chat với người","Số lần chat với nhóm"};
        this.setLayout(new BorderLayout());
        DSNguoiDungHoatDongTable table = new DSNguoiDungHoatDongTable(columnNames);
        OptionPanelDSNguoiDungHoatDong optionPanel = new OptionPanelDSNguoiDungHoatDong(table);
        this.add(optionPanel,BorderLayout.NORTH);
        JScrollPane scrollPane = new JScrollPane();
        scrollPane.setViewportView(table);
        this.add(scrollPane,BorderLayout.CENTER);
        this.revalidate();
        this.repaint();
    }
    void renderBieuDoSoLuongDangKy() throws SQLException {
        removeComponent();
        BieuDoSoLuongDangKyPanel bieuDoSoLuongDangKyPanel = new BieuDoSoLuongDangKyPanel();
        OptionPanelBieuDoSoLuongDangKy optionPanel = new OptionPanelBieuDoSoLuongDangKy(bieuDoSoLuongDangKyPanel);
        this.add(optionPanel,BorderLayout.NORTH);
        this.add(bieuDoSoLuongDangKyPanel,BorderLayout.CENTER);
        this.revalidate();
        this.repaint();
    }
    void renderBieuDoHoatDong() throws SQLException {
        removeComponent();
        BieuDoHoatDongPanel bieuDoHoatDongPanel = new BieuDoHoatDongPanel();
        OptionPanelBieuDoHoatDong optionPanel = new OptionPanelBieuDoHoatDong(bieuDoHoatDongPanel);
        this.add(optionPanel,BorderLayout.NORTH);
        this.add(bieuDoHoatDongPanel,BorderLayout.CENTER);
        this.revalidate();
        this.repaint();
    }
}

