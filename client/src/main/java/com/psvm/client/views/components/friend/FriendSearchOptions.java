package com.psvm.client.views.components.friend;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class FriendSearchOptions extends JPanel {
    private String currentOption;
    FriendSearchOptions(){
        setOpaque(false);
        String[] options = {"Tìm người/đoạn chat", "Tìm theo nội dung đoạn chat"};

        JComboBox<String> comboBox = new JComboBox<>(options);
        comboBox.setSelectedIndex(0);
        currentOption = options[0];

        add(comboBox);

        comboBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String selectedOption = (String) comboBox.getSelectedItem();
                //Do sth with this selected option
                System.out.println("Selected option: " + selectedOption);
                currentOption = selectedOption;
            }
        });
    }

    public String getCurrentOption() {return currentOption;}
}
