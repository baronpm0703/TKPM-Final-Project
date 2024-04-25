package com.psvm.server.view;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;


class OptionPanelBieuDoHoatDong extends JPanel{
    BieuDoHoatDongPanel panel;
    OptionPanelBieuDoHoatDong(BieuDoHoatDongPanel panel){
        this.panel = panel;
        this.setLayout(new BorderLayout());
        this.setOpaque(false);

        //this.setBorder(new EmptyBorder(10,10,10,10));

        //Filter panel
        JPanel filterPanel = new JPanel();
        filterPanel.setLayout(new FlowLayout());
        filterPanel.setOpaque(false);
        //filterPanel.setBackground(Color.RED);
        filterPanel.setBorder(new EmptyBorder(0,0,0,100));


        //number of friend field
        String[] items = {"2022", "2023"}; //Thêm năm khác vào tùw database
        JComboBox<String> dropdown = new JComboBox<>(items);
        //Filter button
        JButton filterButton = new JButton("       Xác nhận       ");
        filterButton.setFocusPainted(false);
        //Filter button logic
        filterButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String choice = (String) dropdown.getSelectedItem();
                panel.filter( choice);
            }
        });
        //Add to filter Panel
        filterPanel.add(new JLabel("Năm: "));
        filterPanel.add(dropdown);
        filterPanel.add(filterButton);
        //Add to Option Panel
        this.add(filterPanel,BorderLayout.WEST);
    }
}
class BieuDoHoatDongPanel extends JPanel{
    //private MySQLData mySQLData; //Biến để lưu database, coi cách gọi database của tao trong Example/Table
    // Cần dòng này để gọi dữ liệu từ database
    // Khi trả dữ về thì trả với dạng List<Object[]> (ArrayList)
    // Coi trong folder EXAMPLE, MySQLData
    //ScheduledExecutorService service = Executors.newScheduledThreadPool(5);



    // private HashMap<String, Object> userList = new HashMap<>();

    BieuDoHoatDongPanel(){
        //Panel này sẽ chưa cái jfreechart
    }

    //    protected void startNextWorker() {
//        UserListThread userWorker = new UserListThread(new UserListThread.Observer() {
//            @Override
//            public void workerDidUpdate(HashMap<String, Object> userInfo) {
//                if (!userList.equals(userInfo)) {
//                    List<Object[]> userAccountData = new ArrayList<>();
//                    userInfo.forEach((userId, detail) -> {
//                        Object[] acc = new Object[8];
//                        acc[0] = userId; // Assign row
//                        HashMap<String, Object> castedDetail = (HashMap<String, Object>) detail;
//                        // Loop Through to get value
//                        castedDetail.forEach((field, value) -> {
//                            Object obj = value;
//                            if (obj instanceof LocalDate) {
//                                LocalDate dateValue = (LocalDate) obj;
//                                acc[getIndex(field)] = dateValue;
//                            }
//                            else if (obj instanceof String) {
//                                String stringValue = (String) obj;
//                                acc[getIndex(field)] = stringValue;
//                            } else {
//                                String gender = (Boolean) obj ? "Female" : "Male";
//                                acc[getIndex(field)] = gender;
//                            }
//                        });
//                        userAccountData.add(acc);
//                    });
//
//                    resetModelRow(); // Reset new Data
//                    index = 1;
//                    for (Object[] row: userAccountData){
//                        Object[] newRow = new Object[row.length + 1];
//                        newRow[0] = index++;
//                        System.arraycopy(row,0,newRow,1,row.length);
//                        model.addRow(newRow);
//                    }
//                    userList = userInfo;
//                }
//            }
//        });
//        userWorker.addPropertyChangeListener(new PropertyChangeListener() {
//            @Override
//            public void propertyChange(PropertyChangeEvent evt) {
//                if (userWorker.getState() == SwingWorker.StateValue.DONE) {
//                    userWorker.removePropertyChangeListener(this);
//                    startNextWorker();
//                }
//
//            }
//        });
//        service.schedule(userWorker, 1000, TimeUnit.MILLISECONDS);
//    }
    void filter(String year) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                if (!year.isEmpty()){
                    //vẽ ra biểu đồ
                }

            }
        });
    }


}
