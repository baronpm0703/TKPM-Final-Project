package com.psvm.client.views;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class FriendSearchOptions extends JPanel {
    FriendSearchOptions(){
        setOpaque(false);
        String[] options = {"Tìm người theo tên đăng nhập", "Tìm người theo tên", "Tìm đoạn chat"};

        JComboBox<String> comboBox = new JComboBox<>(options);
        comboBox.setSelectedIndex(0);

        add(comboBox);

        comboBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String selectedOption = (String) comboBox.getSelectedItem();
                //Do sth with this selected option
                System.out.println("Selected option: " + selectedOption);
            }
        });
    }
}
