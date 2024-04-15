package com.psvm.client.views;

import javax.swing.*;
import java.awt.*;

public class FriendListHeader extends JPanel {

    FriendListHeader(){
        this.setPreferredSize(new Dimension(320,820));
        this.setOpaque(false);
        //Logo and setting wheel

        JPanel logoWheel = logoAndSettingWheel();


        //Search And add Friend
        JPanel utilitiesPanel = utilitiesPanel();

        //Add
        this.add(logoWheel,BorderLayout.NORTH);
        this.add(utilitiesPanel,BorderLayout.SOUTH);

    }
    JPanel logoAndSettingWheel(){
        JPanel logoWheel = new JPanel();
        logoWheel.setOpaque(false);
        ImageIcon originalLogoIcon = new ImageIcon("client/src/main/resources/icon/logo-with-name.png");
        Image scaledLogo = originalLogoIcon.getImage().getScaledInstance(135, 56, Image.SCALE_SMOOTH);
        ImageIcon logoIcon = new ImageIcon(scaledLogo);
        JLabel logoLabel = new JLabel(logoIcon);
        logoLabel.setOpaque(false);
        logoWheel.add(logoLabel,BorderLayout.WEST);

        ImageIcon originalWheelIcon = new ImageIcon("client/src/main/resources/icon/setting.png");
        Image scaledWheel = originalWheelIcon.getImage().getScaledInstance(56, 56, Image.SCALE_SMOOTH);
        ImageIcon wheelIcon = new ImageIcon(scaledWheel);
        JLabel wheelLabel = new JLabel(wheelIcon);
        wheelLabel.setOpaque(false);

        logoWheel.add(wheelLabel,BorderLayout.EAST);
        return logoWheel;
    }
    JPanel utilitiesPanel(){
        JPanel utilitiesPanel = new JPanel();
        //Search field
        SearchFriendField searchFriendField = new SearchFriendField();
        utilitiesPanel.add(searchFriendField, BorderLayout.WEST);

        //Friend iconButton

        return utilitiesPanel;
    }
}
