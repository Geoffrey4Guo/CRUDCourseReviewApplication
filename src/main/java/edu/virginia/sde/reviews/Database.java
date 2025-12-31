package edu.virginia.sde.reviews;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
public class Database {
    private static final String DATABASE_FILE = "DBCOURSEREVIEWS.sqlite3";
    private static Connection connection;

    //open connection to SQLite database
    public static void connect() {
        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:" + DATABASE_FILE);
        } catch (ClassNotFoundException | SQLException e) {
            throw new RuntimeException("Database Connection Failed", e);
        }
    }

    //close connection
    public static void disconnect() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException ignored) {
            //ignore
        }
    }

    //initialize database
    public static boolean initDatabase() {
        try (Statement s = connection.createStatement()) {
            //enable foreign key constraints
            s.execute("PRAGMA foreign_keys = ON;");
            //user table
            s.executeUpdate("""
            CREATE TABLE IF NOT EXISTS users (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            username TEXT NOT NULL UNIQUE,
            password TEXT NOT NULL);
        """);
            //courses table
            s.executeUpdate("""
                CREATE TABLE IF NOT EXISTS courses (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    subject TEXT NOT NULL,
                    number TEXT NOT NULL,
                    title TEXT NOT NULL,
                    UNIQUE(subject, number, title));
            """);
            //reviews table
            s.executeUpdate("""
                CREATE TABLE IF NOT EXISTS reviews (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    user_id INTEGER NOT NULL,
                    course_id INTEGER NOT NULL,
                    rating INTEGER,
                    text TEXT,
                    timestamp TIMESTAMP,
                    UNIQUE(user_id, course_id),
                    FOREIGN KEY (user_id) REFERENCES users(id),
                    FOREIGN KEY (course_id) REFERENCES courses(id));
            """);

            return true;
        } catch (SQLException e) {
            return false;
        }
    }

    //check user login
    public static void login(String username, String password) {
        String sql = "SELECT password FROM users WHERE username = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                String storedPassword = rs.getString("password");
                if (!storedPassword.equals(password)) {
                    throw new RuntimeException("Wrong password");
                } //else login works
            } else {
                throw new RuntimeException("Username does not exist");
            }
        } catch (SQLException e) {
            throw new RuntimeException("Login Failed: Database issue", e);
        }
    }

    //check if user already exists
    public static boolean userExists(String username) {
        String sql = "SELECT 1 FROM users WHERE username = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            return false;
        }
    }

    //register new user
    public static boolean register(String username, String password) {
        //user already exists
        if (userExists(username)) {
            return false;
        }
        String sql = "INSERT INTO users (username, password) VALUES (?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setString(2, password);
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            return false;
        }
    }

    //get user id by username
    public static int getUserID(String username) {
        String sql = "SELECT id FROM users WHERE username = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt("id");
            }
        } catch (SQLException ignored) {

        }
        return -1;
    }

    //search for courses by subject, number, or title
    public static List<Course> getCourses(String subjectFilter, String numberFilter, String titleFilter) {
        String sql = "SELECT * FROM courses WHERE subject LIKE ? AND number LIKE ? AND title LIKE ?;";
        List<Course> courses = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, "%" + subjectFilter + "%");
            ps.setString(2, "%" + numberFilter + "%");
            ps.setString(3, "%" + numberFilter + "%");
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                int id = rs.getInt("id");
                String subject = rs.getString("subject");
                String number = rs.getString("number");
                String title = rs.getString("title");
                int num;

                try {
                    num = Integer.parseInt(number);
                } catch (NumberFormatException e) {
                    num = 0;
                }
                double avgRating = getAvgRating(id);
                Course course = new Course(id, subject, num, title);
                if(avgRating >=0){
                    course.setAvgRating(avgRating);
                }else{
                    course.setAvgRating(null);
                }
                courses.add(course);

            }
        } catch (SQLException ignored) {

        }
        return courses;
    }

    //insert a new course
    public static boolean insertCourses(String subject, String number, String title) {

        if (subject == null || subject.isEmpty() || subject.length() > 4) {
            return false;
        }
        if (title == null || title.isEmpty() || title.length() > 50) {
            return false;
        }
        int num;
        try {
            num = Integer.parseInt(number);
        } catch (NumberFormatException e) {
            return false;
        }
        if (number.length() != 4) {
            return false;
        }

        String sql = "INSERT INTO courses (subject, number, title) VALUES (?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, subject);
            ps.setString(2, number);
            ps.setString(3, title);
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            return false;
        }
    }

    //get average rating of course
    public static double getAvgRating(int courseID) {

        String sql = "SELECT AVG(rating) AS avg FROM reviews WHERE course_id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, courseID);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                double avg = rs.getDouble("avg");
                if (rs.wasNull()) {
                    return -1.0;
                }
                return avg;
            }
        } catch (SQLException ignored) {

        }
        return -1.0;
    }

    //get course info by ID
    public static String getCoursebyID(int courseID) {

        String sql = "SELECT subject, number, title FROM courses WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, courseID);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                String subj = rs.getString("subject");
                String num = rs.getString("number");
                String title = rs.getString("title");
                return subj + ", " + ", " + title;
            }
        } catch (SQLException ignored) {

        }
        return null;
    }

    //check if review exists for given user and course
    public static boolean findReview(int courseId, int userId) {

        String sql = "SELECT 1 FROM reviews WHERE course_id = ? AND user_id = ?";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, courseId);
            ps.setInt(2, userId);
            ResultSet rs = ps.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            return false;
        }
    }

    //add new review of update existing reciew for a course by a user
    public static boolean insertReview(String text, int courseId, int userId, int rating, Timestamp timestamp) {

        try {
            if (findReview(courseId, userId)) {
                String sqlUpdate = "UPDATE reviews SET rating = ?, text = ?, timestamp = ? " + "WHERE course_id = ? AND user_id = ?";
                try (PreparedStatement ps = connection.prepareStatement(sqlUpdate)) {
                    ps.setInt(1, rating);
                    ps.setString(2, text);
                    ps.setTimestamp(3, timestamp);
                    ps.setInt(4, courseId);
                    ps.setInt(5, userId);
                    ps.executeUpdate();
                }
            } else {
                String sqlInsert = "INSERT INTO reviews (user_id, course_id, rating, text, timestamp) VALUES (?, ?, ?, ?, ?)";
                try (PreparedStatement ps = connection.prepareStatement(sqlInsert)) {
                    ps.setInt(1, userId);
                    ps.setInt(2, courseId);
                    ps.setInt(3, rating);
                    ps.setString(4, text);
                    ps.setTimestamp(5, timestamp);
                    ps.executeUpdate();
                }
            }
            return true;
        } catch (SQLException e) {
            throw new RuntimeException("review failed", e);
        }
    }

    //delete review
    public static boolean deleteReview(int courseId, int userId) {

        String sql = "DELETE FROM reviews WHERE course_id = ? AND user_id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, courseId);
            ps.setInt(2, userId);
            int affected = ps.executeUpdate();
            return (affected > 0);
        } catch (SQLException e) {
            return false;
        }
    }

    //get all reviews of a course
    public static List<Review> getReviewsForCourse(int courseId) {

        String sql = "SELECT * FROM reviews WHERE course_id = ? ORDER BY timestamp DESC";
        List<Review> reviews = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, courseId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                int userId = rs.getInt("user_id");
                int rating = rs.getInt("rating");
                String text = rs.getString("text");
                Timestamp ts = rs.getTimestamp("timestamp");
                Review rev = new Review(userId, courseId, rating, text, ts);
                reviews.add(rev);
            }
        } catch (SQLException ignored) {

        }
        return reviews;

    }
    //get all courses reviewed by specific user
    public static List<Course> getCoursesReviewedByUser(int userId){

        String sql = """
        SELECT c.id, c.subject, c.number, c.title, r.rating, r.text
        FROM courses c JOIN reviews r ON c.id = r.course_id
        WHERE r.user_id = ?;
        """;
        List<Course> courseList = new ArrayList<>();
        try(PreparedStatement ps = connection.prepareStatement(sql)){
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            while(rs.next()){
                int cid = rs.getInt("id");
                String subj = rs.getString("subject");
                String numStr = rs.getString("number");
                String title = rs.getString("title");
                int rating = rs.getInt("rating");
                String text = rs.getString("text");
                int num;
                try{
                    num = Integer.parseInt(numStr);
                }catch(NumberFormatException e){
                    num = 0;
                }
                Course course = new Course(cid, subj, num, title);
                course.setAvgRating((double) rating);
                course.setComment(text);
                courseList.add(course);
            }
        }catch(SQLException ignored){

        }
        return courseList;
    }

}


