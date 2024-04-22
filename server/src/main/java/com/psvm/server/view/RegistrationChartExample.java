package com.psvm.server.view;

import com.psvm.server.view.RegisterChartControl.UserRequest;
import com.psvm.shared.socket.SocketResponse;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;
import com.psvm.server.controllers.DBWrapper;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.List;



public class RegistrationChartExample extends JFrame {

    final String SOCKET_HOST = "localhost";
    final int SOCKET_PORT = 5555;
    Socket socket;
    ObjectInputStream socketIn;
    ObjectOutputStream socketOut;

    private final DBWrapper db;


    public RegistrationChartExample(String title) throws SQLException {
        super(title);

        // Connect DB
        this.db = new DBWrapper();

        // Create dataset
        CategoryDataset dataset = createDataset();

        // Create chart
        JFreeChart chart = ChartFactory.createLineChart(
                "Số lượng người đăng ký mới theo năm",
                "Tháng",
                "Số lượng",
                dataset
        );

        // Customize chart if needed
        // For example, you can customize the plot and renderer here.

        // Create Panel
        ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new Dimension(800, 600));
        setContentPane(chartPanel);
    }



    private CategoryDataset createDataset() throws SQLException {

        // Get Data from server
        ResultSet queryResult = db.getFieldUserList("CreationDate");
        ResultSetMetaData queryResultMeta;
        queryResultMeta = queryResult.getMetaData();

        Map<String, Map<String, Integer>> userRegData = new HashMap<>();
        while (queryResult.next()) {
            for (int i = 1; i <= queryResultMeta.getColumnCount(); i++) {
                String time = queryResult.getObject(i).toString().split(" ")[0];
                String[] arrTime = time.split("-");

                userRegData.computeIfAbsent(arrTime[0], k -> new HashMap<>())
                        .merge(arrTime[1], 1, Integer::sum);
            }
        }

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
            RegistrationChartExample example = null;
            try {
                example = new RegistrationChartExample("Biểu đồ đăng ký mới");
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
