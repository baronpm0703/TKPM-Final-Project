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
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


class OptionPanelBieuDoSoLuongDangKy extends JPanel{
    BieuDoSoLuongDangKyPanel panel;
    OptionPanelBieuDoSoLuongDangKy(BieuDoSoLuongDangKyPanel panel){
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
                panel.filter(choice);
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
class BieuDoSoLuongDangKyPanel extends JPanel{
    //private MySQLData mySQLData; //Biến để lưu database, coi cách gọi database của tao trong Example/Table
    // Cần dòng này để gọi dữ liệu từ database
    // Khi trả dữ về thì trả với dạng List<Object[]> (ArrayList)
    // Coi trong folder EXAMPLE, MySQLData
    ScheduledExecutorService service = Executors.newScheduledThreadPool(5);

    private final DBWrapper db;
    CategoryDataset dataset;
    JFreeChart chart;
    ChartPanel chartPanel;
    static boolean isFiltering;

    private HashMap<String, HashMap<String, Integer>> userRegsiterList = new HashMap<>();

    BieuDoSoLuongDangKyPanel() throws SQLException {
        //Panel này sẽ chưa cái jfreechart
        this.db = new DBWrapper();
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
        add(chartPanel);

        startNextWorker("");
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

        db.close();
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


    void filter(String year) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                if (!year.isEmpty()){
                    //vẽ ra biểu đồ
                    isFiltering = true;
                    startNextWorker(year);
                }

            }
        });
    }
}

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
