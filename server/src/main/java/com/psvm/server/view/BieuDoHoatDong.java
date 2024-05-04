package com.psvm.server.view;

import com.psvm.server.controllers.DBWrapper;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.*;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


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
        String[] itemsMonth = {"","1", "2","3", "4", "5", "6", "7", "8", "9", "10", "11", "12"}; //Thêm năm khác vào tùw database
        JComboBox<String> dropdownMonth = new JComboBox<>(itemsMonth);
        String[] itemsYear = {"","2022", "2023","2024"}; //Thêm năm khác vào tùw database
        JComboBox<String> dropdownYear = new JComboBox<>(itemsYear);
        //Filter button
        JButton filterButton = new JButton("       Xác nhận       ");
        filterButton.setFocusPainted(false);
        //Filter button logic
        filterButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String choiceYear = (String) dropdownYear.getSelectedItem();
                String choiceMonth = (String) dropdownMonth.getSelectedItem();
                if (choiceYear.isEmpty() && !choiceMonth.isEmpty()) {}
                else panel.filter(choiceYear, choiceMonth);
            }
        });
        //Add to filter Panel
        filterPanel.add(new JLabel("Tháng: "));
        filterPanel.add(dropdownMonth);
        filterPanel.add(new JLabel("Năm: "));
        filterPanel.add(dropdownYear);
        filterPanel.add(filterButton);

        JButton refreshButton = new JButton("Làm mới bộ lọc");
        refreshButton.setFocusPainted(false);
        refreshButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dropdownYear.setSelectedIndex(0);
                dropdownMonth.setSelectedIndex(0);
                panel.filter("", "");
            }
        });
        filterPanel.add(refreshButton);

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
    ScheduledExecutorService service = Executors.newScheduledThreadPool(5);

    private final DBWrapper db;
    CategoryDataset dataset;
    JFreeChart chart;
    ChartPanel chartPanel;
    static boolean isFiltering;
    private boolean isMonth = false;

    private HashMap<String, HashMap<String, Integer>> userOnlineList = new HashMap<>();


    // private HashMap<String, Object> userList = new HashMap<>();

    BieuDoHoatDongPanel() throws SQLException {
        //Panel này sẽ chưa cái jfreechart
        this.db = new DBWrapper();
        dataset = createDataset();

        // Create chart
        chart = ChartFactory.createLineChart(
                "Số lượng người đăng nhập theo năm",
                "Tháng",
                "Số lượng",
                dataset
        );
        chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new Dimension(800, 600));
        add(chartPanel);

        startNextWorker("","");
    }
    protected void startNextWorker(String selectedYear, String seletedMonth) {
        OnlineUserChartThread userWorker = new OnlineUserChartThread(selectedYear, seletedMonth, new OnlineUserChartThread.Observer() {
            @Override
            public void workerDidUpdate(HashMap<String, HashMap<String, Integer>> onlineInfo) throws SQLException {
                if (!userOnlineList.equals(onlineInfo)) {
                    // Create dataset
                    if (!seletedMonth.isEmpty()) isMonth = true; else isMonth = false;
                    dataset = createDataset(onlineInfo);

                    // Create chart
                    if (seletedMonth.isEmpty()) {
                        chart = ChartFactory.createLineChart(
                                "Số lượng người đăng nhập theo năm",
                                "Tháng",
                                "Số lượng",
                                dataset
                        );
                    } else {
                        chart = ChartFactory.createLineChart(
                                "Số lượng người đăng nhập theo tháng của năm " + selectedYear,
                                "Ngày",
                                "Số lượng",
                                dataset
                        );
                    }
                    chartPanel.setChart(chart);
                    chartPanel.revalidate();
                    chartPanel.repaint();
                    userOnlineList = onlineInfo;
                }
            }
        });

        userWorker.addPropertyChangeListener(new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                if (userWorker.getState() == SwingWorker.StateValue.DONE) {
                    userWorker.removePropertyChangeListener(this);
                    if (!isFiltering)
                        startNextWorker(selectedYear, seletedMonth);
                    else isFiltering = false;
                }
            }
        });

        service.schedule(userWorker, 1000, TimeUnit.MILLISECONDS);
    }

    private CategoryDataset createDataset() throws SQLException {

        // Get Data from server
        ResultSet queryResult = db.getOnlineDateFromUserLog("", "");
        ResultSetMetaData queryResultMeta;
        queryResultMeta = queryResult.getMetaData();

        HashMap<String, HashMap<String, Integer>> userRegData = new HashMap<>();
        while (queryResult.next()) {
            for (int i = 1; i <= queryResultMeta.getColumnCount(); i++) {
                String time = queryResult.getObject(i).toString().split(" ")[0];
                String[] arrTime = time.split("-");

                userRegData.computeIfAbsent(arrTime[0], k -> new HashMap<>())
                        .merge(arrTime[1], 1, Integer::sum);
            }
        }
        updateNonExistingMonths(userRegData);
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        System.out.println(userRegData);

        userRegData.forEach((year, monthData) -> {
            monthData.forEach((month, count) -> {
                dataset.addValue(count, "Năm " + year, month);
            });
        });

        return dataset;
    }
    private CategoryDataset createDataset(HashMap<String, HashMap<String, Integer>> userRegData) throws SQLException {

        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        if (!isMonth) {
            userRegData.forEach((year, monthData) -> {
                monthData.forEach((month, count) -> {
                    dataset.addValue(count, "Năm " + year, month);
                });
            });
        } else {
            userRegData.forEach((month, dayData) -> {
                dayData.forEach((day, count) -> {
                    dataset.addValue(count, "Tháng " + month, day);
                });
            });
        }


        return dataset;
    }
    private static void updateNonExistingMonths(HashMap<String, HashMap<String, Integer>> userRegData) {
        for (Map.Entry<String, HashMap<String, Integer>> yearEntry : userRegData.entrySet()) {
            String year = yearEntry.getKey();
            HashMap<String, Integer> monthData = yearEntry.getValue();

            // Iterate over months (assuming months are represented as two-digit strings)
            for (int month = 1; month <= 12; month++) {
                String monthStr = String.format("%02d", month);

                // Update the month with a value of 0 if it doesn't exist
                monthData.putIfAbsent(monthStr, 0);
            }
        }
        for (Map.Entry<String, HashMap<String, Integer>> yearEntry : userRegData.entrySet()) {
            HashMap<String, Integer> monthData = yearEntry.getValue();

            // Sort the entry set of the inner HashMap by month
            List<Map.Entry<String, Integer>> sortedMonths = new ArrayList<>(monthData.entrySet());
            Collections.sort(sortedMonths, Map.Entry.comparingByKey());

            // Create a new sorted HashMap for the year
            HashMap<String, Integer> sortedMonthData = new LinkedHashMap<>();
            for (Map.Entry<String, Integer> entry : sortedMonths) {
                sortedMonthData.put(entry.getKey(), entry.getValue());
            }

            // Replace the existing HashMap with the sorted one
            yearEntry.setValue(sortedMonthData);
        }
    }

    void filter(String year, String month) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                isFiltering = true;
                startNextWorker(year,month);
            }
        });
    }

}

class OnlineUserChartThread extends SwingWorker<Void, HashMap<String, HashMap<String, Integer>>> {
    private final DBWrapper db;
    private Observer observer;

    private String selectedYear;
    private String selectedMonth;


    public interface Observer {
        public void workerDidUpdate(HashMap<String, HashMap<String, Integer>> userInfo) throws SQLException;
    }


    public OnlineUserChartThread(String selectedYear, String selectedMonth, Observer observer) {
        // Connect DB
        this.db = new DBWrapper();
        this.observer = observer;
        this.selectedYear = selectedYear;
        this.selectedMonth = selectedMonth;
    }

    @Override
    protected Void doInBackground() throws Exception {
        // Get Data from server
        ResultSet queryResult = db.getOnlineDateFromUserLog(selectedYear, selectedMonth);
        ResultSetMetaData queryResultMeta;
        queryResultMeta = queryResult.getMetaData();

        HashMap<String, HashMap<String, Integer>> userRegData = new HashMap<>();
        while (queryResult.next()) {
            for (int i = 1; i <= queryResultMeta.getColumnCount(); i++) {
                String time = queryResult.getObject(i).toString().split(" ")[0];
                String[] arrTime = time.split("-");


                //Month
                if (selectedMonth.isEmpty()) {
                    userRegData.computeIfAbsent(arrTime[0], k -> new HashMap<>())
                            .merge(arrTime[1], 1, Integer::sum);
                } else {
                    //Day
                    userRegData.computeIfAbsent(arrTime[1], k -> new HashMap<>())
                            .merge(arrTime[2], 1, Integer::sum);
                }

            }
        }

        if (!selectedMonth.isEmpty()) {
            updateNonExistingDay(userRegData);
        } else updateNonExistingMonths(userRegData);


        System.out.println(userRegData);
        publish(userRegData);

        return null;
    }

    private static void updateNonExistingMonths(HashMap<String, HashMap<String, Integer>> userRegData) {
        for (Map.Entry<String, HashMap<String, Integer>> yearEntry : userRegData.entrySet()) {
            String year = yearEntry.getKey();
            HashMap<String, Integer> monthData = yearEntry.getValue();

            // Iterate over months (assuming months are represented as two-digit strings)
            for (int month = 1; month <= 12; month++) {
                String monthStr = String.format("%02d", month);

                // Update the month with a value of 0 if it doesn't exist
                monthData.putIfAbsent(monthStr, 0);
            }
        }
        for (Map.Entry<String, HashMap<String, Integer>> yearEntry : userRegData.entrySet()) {
            HashMap<String, Integer> monthData = yearEntry.getValue();

            // Sort the entry set of the inner HashMap by month
            java.util.List<Map.Entry<String, Integer>> sortedMonths = new ArrayList<>(monthData.entrySet());
            Collections.sort(sortedMonths, Map.Entry.comparingByKey());

            // Create a new sorted HashMap for the year
            HashMap<String, Integer> sortedMonthData = new LinkedHashMap<>();
            for (Map.Entry<String, Integer> entry : sortedMonths) {
                sortedMonthData.put(entry.getKey(), entry.getValue());
            }

            // Replace the existing HashMap with the sorted one
            yearEntry.setValue(sortedMonthData);
        }
    }

    private static void updateNonExistingDay(HashMap<String, HashMap<String, Integer>> userRegData) {
        for (Map.Entry<String, HashMap<String, Integer>> monthEntry : userRegData.entrySet()) {
            HashMap<String, Integer> dayData = monthEntry.getValue();

            // Iterate over days (assuming days are represented as two-digit strings)
            for (int day = 1; day <= 31; day++) {
                String dayStr = String.format("%02d", day);

                // Update the day with a value of 0 if it doesn't exist
                dayData.putIfAbsent(dayStr, 0);
            }

            // Sort the entry set of the inner HashMap by day
            java.util.List<Map.Entry<String, Integer>> sortedDays = new ArrayList<>(dayData.entrySet());
            Collections.sort(sortedDays, Map.Entry.comparingByKey());

            // Create a new sorted HashMap for the month
            HashMap<String, Integer> sortedDayData = new LinkedHashMap<>();
            for (Map.Entry<String, Integer> entry : sortedDays) {
                sortedDayData.put(entry.getKey(), entry.getValue());
            }

            // Replace the existing HashMap with the sorted one
            monthEntry.setValue(sortedDayData);
        }
    }
    @Override
    protected void process(List<HashMap<String, HashMap<String, Integer>>> chunks) {
        super.process(chunks);

        for (HashMap<String, HashMap<String, Integer>> data : chunks) {
            try {
                observer.workerDidUpdate(data);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    protected void done() {
        super.done();
        db.close();
    }

}
