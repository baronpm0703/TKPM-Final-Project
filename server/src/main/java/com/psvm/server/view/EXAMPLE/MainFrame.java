package com.psvm.server.view.EXAMPLE;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class MainFrame {
    MySQLData mySQLData;
    MainFrame(){
        //Frame
        JFrame jfrm = new JFrame();
        jfrm.setLayout(new BorderLayout());

        jfrm.setLocationRelativeTo(null);
        jfrm.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        //Connect to database
        mySQLData = new MySQLData();

        //Bar for function
        JPanel utilitiesBar = new JPanel();
        utilitiesBar.setLayout(new BorderLayout());

        //Add button
        JButton add = new JButton("Add");
        add.setFocusPainted(false);



        //Search field
        JPanel searchPanel = new JPanel();
        JTextField searchID = new SearchField("Student ID", 10);
        JTextField searchFirstName = new SearchField("First Name",20);
        JTextField searchLastName = new SearchField("Last Name",20);
        JButton searchButton = new JButton("Search");
        searchButton.setFocusPainted(false);

        searchPanel.add(searchID);
        searchPanel.add(searchFirstName);
        searchPanel.add(searchLastName);
        searchPanel.add(searchButton);
        //add to bar
        utilitiesBar.add(searchPanel,BorderLayout.WEST);
        utilitiesBar.add(add,BorderLayout.EAST);

        //Table
        String[] columnNames = {"Student ID", "First Name", "Last Name", "Date of Birth", "Address", "Update","Delete"};
        JTable table = new Table(columnNames,mySQLData);

        JScrollPane scrollPane = new JScrollPane(table);


        //Add to frame
        jfrm.add(utilitiesBar,BorderLayout.NORTH);
        jfrm.add(scrollPane,BorderLayout.CENTER);
        jfrm.setSize(1080,800);
        jfrm.setVisible(true);

    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new MainFrame();
            }
        });
    }
}
