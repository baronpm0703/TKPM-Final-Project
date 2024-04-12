package com.psvm.server.view.EXAMPLE;

import javax.swing.*;
import java.awt.*;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

public class SearchField extends JTextField {
    SearchField(String placeholder, int column){
        super(placeholder,column);
        this.setForeground(Color.GRAY);

        this.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
                if (getText().equals(placeholder)){
                    setText("");
                    setForeground(Color.BLACK);
                }
            }

            @Override
            public void focusLost(FocusEvent e) {
                if (getText().isEmpty()){
                    setText(placeholder);
                    setForeground(Color.GRAY);
                }
            }
        });
    }
}
