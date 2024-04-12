package com.psvm.server.view.EXAMPLE;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

class MySQLData {

    static final String JDBC_Driver = "com.mysql.cj.jdbc.Driver";
    static final String DB_URL = "jdbc:mysql://localhost/student";

    static final String USER = "root";
    static final String PASS = "admin";

    Connection conn;
    MySQLData(){
        try {
            Class.forName(JDBC_Driver);
            System.out.println("Connecting to DB....");
            conn = DriverManager.getConnection(DB_URL,USER,PASS);
            conn.setAutoCommit(false);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Connection error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
        //System.out.println("GOODBYE!!!!");
    }
    List<Object[]> getAllStudent() {
        List<Object[]> resultList = new ArrayList<>();
        String sql = "SELECT * FROM student_list";

        try {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);

            while (rs.next()) {
                Object[] rowData = new Object[5];  // Adjust the array size to match the number of columns
                rowData[0] = rs.getString("studentID");
                rowData[1] = rs.getString("firstName");
                rowData[2] = rs.getString("lastName");
                rowData[3] = rs.getDate("dob");
                rowData[4] = rs.getString("address");
                resultList.add(rowData);
            }
        } catch (SQLException e) {
            //e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error retrieving data: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
        return resultList;
    }
    List<Object[]> getStudent(String studentID, String firstName, String lastName) {
        List<Object[]> resultList = new ArrayList<>();
        try {
            String sql = "SELECT * FROM student_list WHERE (studentID = ? OR ? = '') AND (firstName = ? OR ? = '') AND (lastname = ? OR ? = '');";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            // Set parameters based on the input values or set NULL if the input is null
            pstmt.setString(1, studentID);
            pstmt.setString(2, studentID);
            pstmt.setString(3, firstName);
            pstmt.setString(4, firstName);
            pstmt.setString(5, lastName);
            pstmt.setString(6, lastName);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                Object[] rowData = new Object[5];
                rowData[0] = rs.getString("studentID");
                rowData[1] = rs.getString("firstName");
                rowData[2] = rs.getString("lastName");
                rowData[3] = rs.getDate("dob");
                rowData[4] = rs.getString("address");
                resultList.add(rowData);
            }
            rs.close();
            pstmt.close();
        }
        catch (SQLException e) {
            if (e instanceof SQLIntegrityConstraintViolationException)
                JOptionPane.showMessageDialog(null, "Student ID must be unique.", "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
        return resultList;
    }
    void addStudent(Object[] newData){
        if (newData[0].equals("")){
            JOptionPane.showMessageDialog(null, "Student ID must not be empty.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        String sql = "INSERT INTO student_list Values(?,?,?,?,?)";
        try {
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, (String) newData[0]);
            pstmt.setString(2, (String) newData[1]);
            pstmt.setString(3, (String) newData[2]);
            pstmt.setDate(4, (java.sql.Date) newData[3]);
            pstmt.setString(5, (String) newData[4]);
            pstmt.executeUpdate();
            conn.commit();
            System.out.println("Add row to DB");
            pstmt.close();

        } catch (SQLException e) {
            if (e instanceof SQLIntegrityConstraintViolationException)
                JOptionPane.showMessageDialog(null, "Student ID must be unique.", "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }
    void updateStudent(String oldStudentID, Object[] newData){
        if (newData[0].equals("")){
            JOptionPane.showMessageDialog(null, "Student ID must not be empty.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        String sql = "UPDATE student_list set studentID = ?, firstName = ?, lastName = ?, dob = ?, address = ? WHERE studentID = ?";
        try {
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, (String) newData[0]);
            pstmt.setString(2, (String) newData[1]);
            pstmt.setString(3, (String) newData[2]);
            pstmt.setDate(4, (java.sql.Date) newData[3]);
            pstmt.setString(5, (String) newData[4]);
            pstmt.setString(6,oldStudentID);
            pstmt.executeUpdate();
            conn.commit();
            System.out.println("Update row to DB");
            pstmt.close();

        } catch (SQLException e) {
            if (e instanceof SQLIntegrityConstraintViolationException)
                JOptionPane.showMessageDialog(null, "Student ID must be unique.", "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }
    void deleteStudent(String studentID) {
        String sql = "DELETE FROM student_list WHERE studentID = ?";
        try {
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1,studentID);
            pstmt.executeUpdate();
            conn.commit();
            System.out.println("Delete row from DB");
            pstmt.close();
        }
        catch (SQLException e){
            try{
                conn.rollback();
            }
            catch (SQLException se){
                se.printStackTrace();
            }
            e.printStackTrace();
        }
    }
    void close(){

        try{
            if (conn != null){
                System.out.println("GOODBYE!!!!");
                conn.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}