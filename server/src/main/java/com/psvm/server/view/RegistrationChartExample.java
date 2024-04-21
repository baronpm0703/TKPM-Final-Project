package com.psvm.server.view;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;

import javax.swing.*;
import java.awt.*;
import java.util.Random;

public class RegistrationChartExample extends JFrame {

    public RegistrationChartExample(String title) {
        super(title);

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

    private CategoryDataset createDataset() {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        // Replace this with your actual data
        Random random = new Random();
        for (int month = 1; month <= 12; month++) {
            int registrations = random.nextInt(100); // Replace with your actual data
            dataset.addValue(registrations, "Năm 2022", String.valueOf(month));
        }

        return dataset;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            RegistrationChartExample example = new RegistrationChartExample("Biểu đồ đăng ký mới");
            example.setSize(800, 600);
            example.setLocationRelativeTo(null);
            example.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
            example.setVisible(true);
        });
    }
}
