package com.psvm.server.view;

import javax.swing.*;
import java.awt.*;
public class Header extends JPanel {
    Header(){

        setLayout(new BorderLayout());

        //Logo on the left
        ImageIcon originalLogoIcon = new ImageIcon("src/main/resources/icon/logo-with-name.png");
        Image scaledLogo = originalLogoIcon.getImage().getScaledInstance(150, 56, Image.SCALE_SMOOTH);
        ImageIcon logoIcon = new ImageIcon(scaledLogo);

        JLabel logoLabel = new JLabel(logoIcon);
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

        //border color
        this.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        this.setOpaque(false);
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
