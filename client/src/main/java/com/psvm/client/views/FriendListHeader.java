package com.psvm.client.views;

import javax.swing.*;
import java.awt.*;

public class FriendListHeader extends JPanel {
    FriendListHeader() {
        this.setOpaque(false);

        ImageIcon originalLogoIcon = new ImageIcon("client/src/main/resources/icon/logo-with-name.png");
        Image scaledLogo = originalLogoIcon.getImage().getScaledInstance(135, 56, Image.SCALE_SMOOTH);
        ImageIcon logoIcon = new ImageIcon(scaledLogo);
        JLabel logoLabel = new JLabel(logoIcon);
        logoLabel.setOpaque(false);

        //setting (cần thì tao bỏ vào chứ chưa cần thì thôi)
//        ImageIcon originalWheelIcon = new ImageIcon("client/src/main/resources/icon/setting.png");
//        Image scaledWheel = originalWheelIcon.getImage().getScaledInstance(56, 56, Image.SCALE_SMOOTH);
//        ImageIcon wheelIcon = new ImageIcon(scaledWheel);
//        JLabel wheelLabel = new JLabel(wheelIcon);
//        wheelLabel.setOpaque(false);

        // Use FlowLayout with gaps for spacing
        //setLayout(new FlowLayout(FlowLayout.LEADING, 50, 0)); // Adjust the horizontal gap (10 in this example)

        add(logoLabel);
//        add(wheelLabel);
    }
}