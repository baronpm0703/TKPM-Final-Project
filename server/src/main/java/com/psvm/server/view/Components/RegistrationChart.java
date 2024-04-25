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


class RegisterChartThread extends SwingWorker<Void, HashMap<String, HashMap<String, Integer>>> {
    private final DBWrapper db;
    private Observer observer;

    public interface Observer {
        public void workerDidUpdate(HashMap<String, HashMap<String, Integer>> userInfo) throws SQLException;
    }
    private String selectedYear;

    public RegisterChartThread(String selectedYear, Observer observer) {
        // Connect DB
        this.db = new DBWrapper();
        this.observer = observer;
        this.selectedYear = selectedYear;
    }

    @Override
    protected Void doInBackground() throws Exception {
        // Get Data from server
        ResultSet queryResult = db.getFieldUserList("CreationDate",selectedYear);
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

        publish(userRegData);

        return null;
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

public class RegistrationChart extends JFrame {

    ScheduledExecutorService service = Executors.newScheduledThreadPool(10);
    private final DBWrapper db;
    CategoryDataset dataset;
    JFreeChart chart;
    ChartPanel chartPanel;
    static boolean isFiltering;
    private HashMap<String, HashMap<String, Integer>> userRegsiterList = new HashMap<>();


    public RegistrationChart(String title) throws SQLException {
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
                startNextWorker("2022");
            }
        });

        // Start
        startNextWorker("");
        add(addBtn);
    }

    protected void startNextWorker(String selectedYear) {
        RegisterChartThread userWorker = new RegisterChartThread(selectedYear, new RegisterChartThread.Observer() {
            @Override
            public void workerDidUpdate(HashMap<String, HashMap<String, Integer>> regInfo) throws SQLException {
                if (!userRegsiterList.equals(regInfo)) {
                    // Create dataset
                    dataset = createDataset(regInfo);

                    // Create chart
                    chart = ChartFactory.createLineChart(
                            "Số lượng người đăng ký mới theo năm",
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
                        startNextWorker(selectedYear);
                    else isFiltering = false;
                }
            }
        });

        service.schedule(userWorker, 1000, TimeUnit.MILLISECONDS);
    }

    private CategoryDataset createDataset() throws SQLException {

        // Get Data from server
        ResultSet queryResult = db.getFieldUserList("CreationDate", "");
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

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            RegistrationChart example = null;
            try {
                example = new RegistrationChart("Biểu đồ đăng ký mới");
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
