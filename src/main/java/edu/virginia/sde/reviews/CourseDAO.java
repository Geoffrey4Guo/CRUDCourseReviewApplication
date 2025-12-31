package edu.virginia.sde.reviews;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CourseDAO {

    // in CourseDAO.java
    private static final String DB_URL = "jdbc:sqlite:DBCOURSEREVIEWS.sqlite3";

    public CourseDAO() {

        createTableIfNotExists();
    }

    private void createTableIfNotExists() {
        String sql = """
            CREATE TABLE IF NOT EXISTS Courses (
                            id INTEGER PRIMARY KEY AUTOINCREMENT,
                            subject TEXT NOT NULL,
                            number INTEGER NOT NULL,
                            title TEXT NOT NULL,
                            UNIQUE(subject, number, title)
                        )
                       \s""";

        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement()) {

            stmt.execute(sql);

        } catch (SQLException e) {
            System.err.println("Error creating Courses table: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public List<Course> getAllCourses() {
        List<Course> courses = new ArrayList<>();
        String sql = " SELECT * FROM Courses ORDER BY subject,  number;";

        try(Connection conn = DriverManager.getConnection(DB_URL);
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Course course = new Course(
                        rs.getInt("id"),
                        rs.getString("subject"),
                        rs.getInt("number"),
                        rs.getString("title")
                );

                course.setAvgRating(calculateAverageRating(course.getId()));
                courses.add(course);
            }

        } catch (SQLException e) {
            System.err.println("Error getting all courses: " + e.getMessage());
            e.printStackTrace();
        }

        return courses;
    }

    public List<Course> searchCourses(String subject, Integer number,  String title) {
        List<Course> courses = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT * FROM Courses WHERE 1=1");

        if (subject != null && !subject.isEmpty()) {
            sql.append(" AND UPPER(subject) = ?");
        }
        if (number != null) {
            sql.append(" AND number = ?");
        }
        if (title != null && !title.isEmpty()) {
            sql.append(" AND UPPER(title) LIKE ?");
        }

        sql.append(" ORDER BY subject, number");

        try (Connection conn = DriverManager.getConnection(DB_URL);
        PreparedStatement pstmt = conn.prepareStatement(sql.toString())) {

            int paramIndex = 1;
            if (subject != null && !subject.isEmpty()) {
                pstmt.setString(paramIndex++, subject.toUpperCase());
            }
            if (number != null) {
                pstmt.setInt(paramIndex++, number);
            }
            if (title != null && !title.isEmpty()) {
                pstmt.setString(paramIndex++, "%" + title.toUpperCase() + "%");
            }

            ResultSet resultSet = pstmt.executeQuery();

            while (resultSet.next()) {
                Course course = new Course(
                        resultSet.getInt("id"),
                        resultSet.getString("subject"),
                        resultSet.getInt("number"),
                        resultSet.getString("title")
                );

                course.setAvgRating(calculateAverageRating(course.getId()));
                courses.add(course);
            }

            resultSet.close();

        } catch(SQLException e){
            System.err.println("Error searching courses: " + e.getMessage());
            e.printStackTrace();
        }

        return courses;
    }

    public boolean addCourse(String subject, int number, String title) {
        String sql = "INSERT INTO Courses (subject, number, title) VALUES (?, ?, ?)";

        try(Connection conn = DriverManager.getConnection(DB_URL);
            PreparedStatement pstmt = conn.prepareStatement(sql)){

            pstmt.setString(1, subject.toUpperCase());
            pstmt.setInt(2, number);
            pstmt.setString(3, title);

            int rowAffected = pstmt.executeUpdate();
            return rowAffected > 0;

        }catch(SQLException e){
            System.err.println("Error adding courses: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public  boolean courseExists(String subject, int number, String title) {
        String sql = "SELECT COUNT(*) FROM Courses WHERE UPPER(subject) = ? AND number = ? AND UPPER(title) = ?";

        try(Connection conn = DriverManager.getConnection(DB_URL);
            PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, subject.toUpperCase());
            pstmt.setInt(2, number);
            pstmt.setString(3, title.toUpperCase());

            ResultSet resultSet = pstmt.executeQuery();

            if (resultSet.next()) {
                int count = resultSet.getInt(1);
                resultSet.close();
                return count > 0;
            }

            resultSet.close();
        }catch(SQLException e){
            System.err.println("Error, Checking if course exists: " + e.getMessage());
            e.printStackTrace();
        }

        return false;
    }

    public Course getCourseById(int id) {
        String sql = "SELECT * FROM Courses WHERE id = ?";

        try(Connection conn = DriverManager.getConnection(DB_URL);
            PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            ResultSet resultSet = pstmt.executeQuery();

            if (resultSet.next()){
                Course course = new Course(
                        resultSet.getInt("id"),
                        resultSet.getString("subject"),
                        resultSet.getInt("number"),
                        resultSet.getString("title")
                );
                course.setAvgRating(calculateAverageRating(course.getId()));
                resultSet.close();
                return course;
            }

            resultSet.close();

        } catch (SQLException e){
            System.err.println("Error getting course by id: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    private Double calculateAverageRating(int courseId) {
        String sql = "SELECT AVG(rating) as avg_rating, COUNT(*) as count FROM REVIEWS WHERE course_id = ?";

        try(Connection conn = DriverManager.getConnection(DB_URL);
            PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, courseId);
            ResultSet resultSet = pstmt.executeQuery();

            if (resultSet.next()){
                int count = resultSet.getInt("count");
                if (count ==  0){
                    resultSet.close();
                    return null;
                }
                double avg = resultSet.getDouble("avg_rating");
                resultSet.close();
                return avg;
            }

            resultSet.close();

        } catch (SQLException e){

        }
        return null;
    }
}
