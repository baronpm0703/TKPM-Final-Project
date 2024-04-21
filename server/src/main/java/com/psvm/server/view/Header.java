package com.psvm.server.view;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class Header extends JPanel {
    Header(){
        this.setLayout(new BorderLayout());
        this.setSize(1440,56);
        //border color
        this.setBorder(BorderFactory.createLineBorder(Color.decode("#CDD5DE")));
        this.setOpaque(false);
        //Logo on the left
        ImageIcon originalLogoIcon = new ImageIcon("src/main/resources/icon/logo-with-name.png");
        Image scaledLogo = originalLogoIcon.getImage().getScaledInstance(150, 56, Image.SCALE_SMOOTH);
        ImageIcon logoIcon = new ImageIcon(scaledLogo);

        JLabel logoLabel = new JLabel(logoIcon);
        logoLabel.setBorder(new EmptyBorder(0,10,0,0));
        add(logoLabel, BorderLayout.WEST);

        //Button and avatar on the right
        JButton setting = createImageButton("src/main/resources/icon/setting.png");
        JButton more = createImageButton("src/main/resources/icon/more.png");
        JButton avatar = createImageButton("src/main/resources/icon/avatar_sample.jpg");
        JPanel buttonsPanel = new JPanel();
        buttonsPanel.setOpaque(false);
        buttonsPanel.add(setting);
        buttonsPanel.add(more);
        buttonsPanel.add(avatar);
        add(buttonsPanel,BorderLayout.EAST);

        //Set up button actions
        setting.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                //JOptionPane.showMessageDialog(null,"setting");
            }
        });
        more.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

            }
        });
        avatar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

            }
        });
    }

    private JButton createImageButton(String imagePath){
        ImageIcon imageIcon = new ImageIcon(imagePath);
        Image scaledImage = imageIcon.getImage().getScaledInstance(56, 56, Image.SCALE_SMOOTH);
        ImageIcon scaledIcon = new ImageIcon(scaledImage);
        JButton imageButton = new JButton(scaledIcon);
        imageButton.setBorderPainted(false);
        imageButton.setContentAreaFilled(false);
        imageButton.setFocusPainted(false);
        imageButton.setOpaque(false);
        return imageButton;
    }
}
