package org.example;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class StudentManager extends JFrame implements ActionListener {
    JTextField nameField, ageField, deptField;
    JTextArea displayArea;
    JButton addBtn, viewBtn, delBtn;

    final String URL = "jdbc:mysql://localhost:3306/";
    final String DB_NAME = "your_db_name";
    final String USER = "your_db_username";   // ← replace with your username
    final String PASS = "your_db_pass";       // ← replace with your password

    public StudentManager() {
        setTitle("Student Details Manager");
        setSize(400, 400);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new FlowLayout());

        add(new JLabel("Name:"));
        nameField = new JTextField(20);
        add(nameField);

        add(new JLabel("Age:"));
        ageField = new JTextField(5);
        add(ageField);

        add(new JLabel("Department:"));
        deptField = new JTextField(10);
        add(deptField);

        addBtn = new JButton("Add Student");
        viewBtn = new JButton("View All");
        delBtn = new JButton("Delete by Name");
        add(addBtn); add(viewBtn); add(delBtn);

        displayArea = new JTextArea(10, 30);
        displayArea.setEditable(false);
        add(new JScrollPane(displayArea));

        addBtn.addActionListener(this);
        viewBtn.addActionListener(this);
        delBtn.addActionListener(this);

        setupDatabase(); // auto-create DB and table

        setVisible(true);
    }

    private void setupDatabase() {
        try (Connection conn = DriverManager.getConnection(URL, USER, PASS);
             Statement st = conn.createStatement()) {

            st.executeUpdate("CREATE DATABASE IF NOT EXISTS " + DB_NAME);
            st.executeUpdate("USE " + DB_NAME);

            String createTable = """
                CREATE TABLE IF NOT EXISTS students (
                    id INT AUTO_INCREMENT PRIMARY KEY,
                    name VARCHAR(100),
                    age INT,
                    department VARCHAR(50)
                )
                """;
            st.executeUpdate(createTable);

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Database setup error: " + e.getMessage());
        }
    }

    public void actionPerformed(ActionEvent e) {
        try (Connection conn = DriverManager.getConnection(URL + DB_NAME, USER, PASS)) {
            if (e.getSource() == addBtn) {
                String name = nameField.getText().trim();
                String ageText = ageField.getText().trim();
                String dept = deptField.getText().trim();

                if (name.isEmpty() || ageText.isEmpty() || dept.isEmpty()) {
                    displayArea.setText("Please fill all fields.");
                    return;
                }

                int age = Integer.parseInt(ageText);
                String q = "INSERT INTO students (name, age, department) VALUES (?, ?, ?)";
                PreparedStatement ps = conn.prepareStatement(q);
                ps.setString(1, name);
                ps.setInt(2, age);
                ps.setString(3, dept);
                ps.executeUpdate();
                displayArea.setText("Student added successfully!");

            } else if (e.getSource() == viewBtn) {
                Statement st = conn.createStatement();
                ResultSet rs = st.executeQuery("SELECT * FROM students");
                StringBuilder sb = new StringBuilder();
                while (rs.next()) {
                    sb.append(rs.getInt("id")).append(". ")
                            .append(rs.getString("name")).append(" | Age: ")
                            .append(rs.getInt("age")).append(" | Dept: ")
                            .append(rs.getString("department")).append("\n");
                }
                displayArea.setText(sb.length() > 0 ? sb.toString() : "No students found.");

            } else if (e.getSource() == delBtn) {
                String name = nameField.getText().trim();
                if (name.isEmpty()) {
                    displayArea.setText("Enter a name to delete.");
                    return;
                }

                String q = "DELETE FROM students WHERE name = ?";
                PreparedStatement ps = conn.prepareStatement(q);
                ps.setString(1, name);
                int rows = ps.executeUpdate();
                displayArea.setText(rows > 0 ? "Deleted student(s) named " + name : "No match found.");
            }
        } catch (Exception ex) {
            displayArea.setText("Error: " + ex.getMessage());
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(StudentManager::new);
    }
}
