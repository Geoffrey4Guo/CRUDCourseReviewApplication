package edu.virginia.sde.reviews;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.List;

public class MyReviewsController {

    @FXML
    private Label headerLabel;
    @FXML
    private TableView<Course> myReviewsTable;
    @FXML
    private TableColumn<Course, String> subjectColumn;
    @FXML
    private TableColumn<Course, Integer> numberColumn;
    @FXML
    private TableColumn<Course, String> titleColumn;
    @FXML
    private TableColumn<Course, Double> ratingColumn;
    @FXML
    private TableColumn<Course, String> commentColumn;
    @FXML
    private Label messageLabel;
    @FXML
    private Button backButton;
    private String currentUsername;
    private int currentUserId = -1;

    @FXML
    private void initialize() {
        subjectColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getSubject()));

        numberColumn.setCellValueFactory(cellData ->
                new SimpleIntegerProperty(cellData.getValue().getNumber()).asObject());

        titleColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getTitle()));

        ratingColumn.setCellValueFactory(cellData -> {
            Double rating = cellData.getValue().getAvgRating();
            if (rating == null) {
                rating = 0.0;
            }
            return new SimpleDoubleProperty(rating).asObject();
        });

        myReviewsTable.setRowFactory(tableView -> {
            TableRow<Course> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (!row.isEmpty() && event.getClickCount() == 2) {
                    Course course = row.getItem();
                    openCourseReviews(course);
                }
            });
            return row;
        });

        commentColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(
                        cellData.getValue().getComment() == null
                                ? ""
                                : cellData.getValue().getComment()
                ));
    }

    public void initData(String username) {
        this.currentUsername = username;
        headerLabel.setText("My Reviews for " + username);

        Database.initDatabase();
        currentUserId = Database.getUserID(username);
        if (currentUserId <= 0) {
            messageLabel.setText("Error: current user not found.");
            messageLabel.setStyle("-fx-text-fill: #e74c3c;");
            return;
        }

        loadMyReviews();
    }

    private void loadMyReviews() {
        List<Course> courses = Database.getCoursesReviewedByUser(currentUserId);
        ObservableList<Course> observableCourses = FXCollections.observableArrayList(courses);
        myReviewsTable.setItems(observableCourses);

        if (courses.isEmpty()) {
            messageLabel.setText("You have not written any reviews yet.");
            messageLabel.setStyle("-fx-text-fill: #7f8c8d;");
        } else {
            messageLabel.setText("");
        }
    }

    private void openCourseReviews(Course course) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("courseReviews.fxml"));
            Parent root = loader.load();

            CourseReviewsController controller = loader.getController();
            controller.initData(course, currentUsername);

            Stage stage = (Stage) myReviewsTable.getScene().getWindow();
            stage.setScene(new Scene(root, 1280, 720));
            stage.show();
        } catch (IOException e) {
            messageLabel.setText("Error: Could not open course reviews.");
            messageLabel.setStyle("-fx-text-fill: #e74c3c;");
            e.printStackTrace();
        }
    }

    @FXML
    private void handleBack() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("course_search.fxml"));
            Parent root = loader.load();

            CourseSearchController controller = loader.getController();
            controller.initData(currentUsername);

            Stage stage = (Stage) backButton.getScene().getWindow();
            stage.setScene(new Scene(root, 1280, 720));
            stage.show();
        } catch (IOException e) {
            messageLabel.setText("Error: Could not return to course search.");
            messageLabel.setStyle("-fx-text-fill: #e74c3c;");
            e.printStackTrace();
        }
    }
}
