package edu.virginia.sde.reviews;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.List;

public class CourseReviewsController {

    @FXML
    private Label courseTitleLabel;
    @FXML
    private Label avgRatingLabel;
    @FXML
    private ListView<Review> reviewListView;
    @FXML
    private TextField ratingField;
    @FXML
    private TextArea commentArea;
    @FXML
    private Button submitUpdateButton;
    @FXML
    private Button deleteButton;
    @FXML
    private Button backButton;
    @FXML
    private Label messageLabel;

    private Course currentCourse;
    private String currentUsername;
    private int currentUserId = -1;
    private Review myReview;

    @FXML
    private void initialize() {
       reviewListView.setCellFactory(listView -> new ListCell<>() {
            @Override
            protected void updateItem(Review review, boolean empty) {
                super.updateItem(review, empty);
                if (empty || review == null) {
                    setText(null);
                } else {
                    StringBuilder sb = new StringBuilder();
                    sb.append("Rating: ").append(review.getRating());
                    sb.append(" | Time: ").append(review.getTimestamp());

                    String comment = review.getComment();
                    if (comment != null && !comment.isBlank()) {
                        sb.append("\nComment: ").append(comment);
                    }

                    setText(sb.toString());
                    setWrapText(true);
                }
            }
        });

        submitUpdateButton.setText("Submit Review");
        deleteButton.setDisable(true);
        messageLabel.setText("");
    }

    public void initData(Course course, String username) {
        this.currentCourse = course;
        this.currentUsername = username;

        if (!Database.initDatabase()) {
            messageLabel.setText("Error: could not initialize database.");
            messageLabel.setStyle("-fx-text-fill: #e74c3c;");
            return;
        }

        currentUserId = Database.getUserID(username);
        if (currentUserId <= 0) {
            messageLabel.setText("Error: current user not found. Please log in again.");
            messageLabel.setStyle("-fx-text-fill: #e74c3c;");
        }

        String header = String.format("%s %d: %s",
                currentCourse.getSubject(),
                currentCourse.getNumber(),
                currentCourse.getTitle());
        courseTitleLabel.setText(header);

        refreshReviews();
    }

    private void refreshReviews() {
        if (currentCourse == null) {
            return;
        }

        List<Review> reviews = Database.getReviewsForCourse(currentCourse.getId());
        ObservableList<Review> observableReviews = FXCollections.observableArrayList(reviews);
        reviewListView.setItems(observableReviews);

        double avg = Database.getAvgRating(currentCourse.getId());
        if (avg < 0 || reviews.isEmpty()) {
            avgRatingLabel.setText("Average rating: N/A");
        } else {
            avgRatingLabel.setText(String.format("Average rating: %.2f", avg));
        }

        myReview = null;
        if (currentUserId > 0) {
            for (Review r : reviews) {
                if (r.getUserID() == currentUserId) {
                    myReview = r;
                    break;
                }
            }
        }

        if (myReview == null) {
            ratingField.clear();
            commentArea.clear();
            submitUpdateButton.setText("Submit Review");
            deleteButton.setDisable(true);
        } else {
            ratingField.setText(String.valueOf(myReview.getRating()));
            String comment = myReview.getComment();
            commentArea.setText(comment == null ? "" : comment);
            submitUpdateButton.setText("Update Review");
            deleteButton.setDisable(false);
        }

        messageLabel.setText("");
    }

    @FXML
    private void handleSubmitOrUpdate() {
        messageLabel.setText("");

        if (currentUserId <= 0) {
            messageLabel.setText("Error: current user not found.");
            messageLabel.setStyle("-fx-text-fill: #e74c3c;");
            return;
        }

        String ratingText = ratingField.getText();
        int rating;
        try {
            rating = Integer.parseInt(ratingText.trim());
        } catch (NumberFormatException e) {
            messageLabel.setText("Rating must be an integer between 1 and 5.");
            messageLabel.setStyle("-fx-text-fill: #e74c3c;");
            return;
        }
        if (rating < 1 || rating > 5) {
            messageLabel.setText("Rating must be an integer between 1 and 5.");
            messageLabel.setStyle("-fx-text-fill: #e74c3c;");
            return;
        }

        String comment = commentArea.getText();
        if (comment != null) {
            comment = comment.trim();
        }

        Timestamp timestamp = new Timestamp(System.currentTimeMillis());

        boolean ok = Database.insertReview(
                (comment == null || comment.isBlank()) ? null : comment,
                currentCourse.getId(),
                currentUserId,
                rating,
                timestamp
        );

        if (!ok) {
            messageLabel.setText("Error saving review. Please try again.");
            messageLabel.setStyle("-fx-text-fill: #e74c3c;");
        } else {
            messageLabel.setText("Review saved.");
            messageLabel.setStyle("-fx-text-fill: #27ae60;");
            refreshReviews();
        }
    }

    @FXML
    private void handleDelete() {
        messageLabel.setText("");

        if (currentUserId <= 0 || myReview == null) {
            return;
        }

        boolean ok = Database.deleteReview(currentCourse.getId(), currentUserId);
        if (!ok) {
            messageLabel.setText("Error deleting review. Please try again.");
            messageLabel.setStyle("-fx-text-fill: #e74c3c;");
        } else {
            messageLabel.setText("Review deleted.");
            messageLabel.setStyle("-fx-text-fill: #27ae60;");
            refreshReviews();
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
