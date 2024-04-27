package com.psvm.server.view.Components;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;
import com.psvm.server.controllers.DBWrapper;

import javax.swing.*;
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
            List<Map.Entry<String, Integer>> sortedDays = new ArrayList<>(dayData.entrySet());
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

public class OnlineUserChart extends JFrame {

    ScheduledExecutorService service = Executors.newScheduledThreadPool(10);
    private final DBWrapper db;
    CategoryDataset dataset;
    JFreeChart chart;
    ChartPanel chartPanel;
    static boolean isFiltering;
    private HashMap<String, HashMap<String, Integer>> userRegsiterList = new HashMap<>();


    public OnlineUserChart(String title) throws SQLException {
        super(title);

        // Connect DB
        this.db = new DBWrapper();
        // Customize chart if needed
        // For example, you can customize the plot and renderer here.
        dataset = createDataset();

        // Create chart
        chart = ChartFactory.createLineChart(
                "Số lượng người đăng ký mới theo năm",
                "Tháng",
                "Số lượng",
                dataset
        );
        // Create Panel
        chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new Dimension(800, 600));
        setContentPane(chartPanel);

        JButton addBtn = new JButton("Add");
        addBtn.setFocusPainted(false);
        addBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                isFiltering = true;
                startNextWorker("2022", "10");
                //startNextWorker("2022", "");
            }
        });

        // Start
        startNextWorker("","");
        add(addBtn);
    }

    protected void startNextWorker(String selectedYear, String seletedMonth) {
        OnlineUserChartThread userWorker = new OnlineUserChartThread(selectedYear, seletedMonth, new OnlineUserChartThread.Observer() {
            @Override
            public void workerDidUpdate(HashMap<String, HashMap<String, Integer>> regInfo) throws SQLException {
                if (!userRegsiterList.equals(regInfo)) {
                    // Create dataset
                    dataset = createDataset(regInfo);

                    // Create chart
                    chart = ChartFactory.createLineChart(
                            "Số lượng người đăng nhập mới theo năm",
                            "Tháng",
                            "Số lượng",
                            dataset
                    );

                    chartPanel.setChart(chart);
                    chartPanel.revalidate();
                    chartPanel.repaint();
                    userRegsiterList = regInfo;
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

        userRegData.forEach((year, monthData) -> {
            monthData.forEach((month, count) -> {
                dataset.addValue(count, "Năm " + year, month);
            });
        });

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
            List<Map.Entry<String, Integer>> sortedDays = new ArrayList<>(dayData.entrySet());
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
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            OnlineUserChart example = null;
            try {
                example = new OnlineUserChart("Biểu đồ Hoạt động mới");
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
            example.setSize(800, 600);
            example.setLocationRelativeTo(null);
            example.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
            example.setVisible(true);
        });
    }
}
