package com.psvm.server.view;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

class SideMenu extends JPanel{
    private final ContentPane contentPane;
    private JButton selectedButton;
    //private JButton currentButton;

    SideMenu(ContentPane contentPane){
        this.contentPane = contentPane;
        //left sideMenu
        this.setLayout(new GridLayout(9,1,0,0));
        this.setBackground(null);
        this.setSize(280,680);
        this.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEmptyBorder(0, 0, 0, 0),
                BorderFactory.createMatteBorder(0, 0, 0, 1, Color.decode("#CDD5DE"))
        ));
        //Label describe which tag
        //Too lazy to do
        //Menu options
        JButton dsNguoiDung = createButton("Danh sách người dùng","src/main/resources/icon/dsnguoidung.png");
        JButton dsNhomChat = createButton("Danh sách nhóm chat","src/main/resources/icon/dsnhomchat.png");
        JButton dsLienLac = createButton("Danh sách liên lạc của người dùng","src/main/resources/icon/danhsachlienlac.png");
        JButton dsSpam = createButton("Báo cáo spam","src/main/resources/icon/spam.png");
        JButton dsDangNhap = createButton("Danh sách người dùng đăng nhập","src/main/resources/icon/danhsachdangnhap.png");
        JButton dsDangKy = createButton("Danh sách người dùng đăng ký","src/main/resources/icon/danhsachdangky.png");
        JButton bieuDoDangKy = createButton("Biểu đồ người dùng đăng ký","src/main/resources/icon/bieudodangky.png");
        JButton dsHoatDong = createButton("Danh sách hoạt động của người dùng","src/main/resources/icon/dshoatdong.png");
        JButton bieuDoHoatDong = createButton("Biểu đồ hoạt động của người dùng","src/main/resources/icon/bieudohoatdong.png");

        // Add action listeners to change button color
        addColorChangeActionListener(dsNguoiDung);
        dsNguoiDung.doClick();
        addColorChangeActionListener(dsNhomChat);
        addColorChangeActionListener(dsLienLac);
        addColorChangeActionListener(dsSpam);
        addColorChangeActionListener(dsDangNhap);
        addColorChangeActionListener(dsDangKy);
        addColorChangeActionListener(bieuDoDangKy);
        addColorChangeActionListener(dsHoatDong);
        addColorChangeActionListener(bieuDoHoatDong);

        // Add on hover
        addHoverEffect(dsNguoiDung);
        addHoverEffect(dsNhomChat);
        addHoverEffect(dsLienLac);
        addHoverEffect(dsSpam);
        addHoverEffect(dsDangNhap);
        addHoverEffect(dsDangKy);
        addHoverEffect(bieuDoDangKy);
        addHoverEffect(dsHoatDong);
        addHoverEffect(bieuDoHoatDong);
        //Add to side menu
//        this.add(taskLabel);
        this.add(dsNguoiDung);
        this.add(dsNhomChat);
        this.add(dsLienLac);
        this.add(dsSpam);
        this.add(dsDangNhap);
        this.add(dsDangKy);
        this.add(bieuDoDangKy);
        this.add(dsHoatDong);
        this.add(bieuDoHoatDong);
    }
    private JButton createButton(String text, String imagePath) {
        text = "  " + text;
        ImageIcon originalLogoIcon = new ImageIcon(imagePath);
        Image scaledLogo = originalLogoIcon.getImage().getScaledInstance(35, 35, Image.SCALE_SMOOTH);
        ImageIcon logoIcon = new ImageIcon(scaledLogo);
        JButton button = new JButton(text);
        button.setPreferredSize(new Dimension(280, 76));
        button.setBackground(null);
        button.setBorder(new EmptyBorder(0,10,0,10));
        button.setFocusPainted(false);
        button.setFont(new Font("Arial",Font.BOLD, 12));
        button.setIcon(logoIcon);
        button.setHorizontalAlignment(SwingConstants.LEFT);
        button.setVerticalAlignment(SwingConstants.CENTER);
        button.setVerticalTextPosition(SwingConstants.CENTER);
        button.setHorizontalTextPosition(SwingConstants.RIGHT);
        return button;
    }
    private void addColorChangeActionListener(JButton button) {
        button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (selectedButton != null) {
                    // Reset the color of the previously selected button
                    selectedButton.setBackground(null);
                }

                if (button.getText().trim().equals("Danh sách người dùng")){
                    contentPane.renderDSNguoiDung();
                }
                if (button.getText().trim().equals("Danh sách nhóm chat")){
                    //contentPane.updateContent("Content for Option 2");
                }
                // Set the color of the current button
                button.setBackground(Color.decode("#2FB7F1"));

                // Update the reference to the currently selected button
                selectedButton = button;
            }
        });
    }
    private void addHoverEffect(JButton button) {
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                if (button != selectedButton) {
                    button.setBackground(Color.CYAN);
                }
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                if (button != selectedButton) {
                    button.setBackground(null);
                }
            }
        });
    }
}