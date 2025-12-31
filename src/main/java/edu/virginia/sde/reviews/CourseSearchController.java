package edu.virginia.sde.reviews;


import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.TableCell;
import java.util.List;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import java.io.IOException;

public class CourseSearchController {

        //Header components
        @FXML private Label welcomeLabel;
        @FXML private Button myReviewsButton;
        @FXML private Button logOutButton;

        //Search
        @FXML private TextField searchSubjectField;
        @FXML private TextField searchNumberField;
        @FXML private TextField searchTitleField;
        @FXML private Button searchButton;
        @FXML private Button clearSearchButton;

        //Course Table
        @FXML private TableView<Course> courseTable;
        @FXML private TableColumn<Course, String> subjectColumn;
        @FXML private TableColumn<Course, Integer> numberColumn;
        @FXML private TableColumn<Course, String> titleColumn;
        @FXML private TableColumn<Course, Double> ratingColumn;
        @FXML private TableColumn<Course, Void> actionColumn;


        //course section
        @FXML private TextField addSubjectField;
        @FXML private TextField addNumberField;
        @FXML private TextField addTitleField;
        @FXML private Button addCourseButton;
        @FXML private Label messageLabel;

        private String currentUsername;
        private CourseDAO courseDAO;

        @FXML
        private void initialize() {

            courseDAO = new CourseDAO();

            setUpTableColumns();

            addActionButtonsToTable();

            loadAllCourses();
        }

        public void initData(String username){
            this.currentUsername = username;
            welcomeLabel.setText("Welcome " + username);
        }

        public void setUpTableColumns() {
            subjectColumn.setCellValueFactory(new PropertyValueFactory<>("subject"));
            numberColumn.setCellValueFactory(new PropertyValueFactory<>("number"));
            titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
            ratingColumn.setCellValueFactory(new PropertyValueFactory<>("avgRating"));

            ratingColumn.setCellFactory(column -> new TableCell<Course, Double>() {
                @Override
                protected void updateItem(Double item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null || item < 0) {
                        setText("");
                    } else {
                        setText(String.format("%.2f", item));
                    }
                }
            });
        }

        private void addActionButtonsToTable() {
            actionColumn.setCellFactory(param -> new TableCell<>() {
                private final Button viewButton = new Button("View Reviews");

                {
                    viewButton.setStyle(
                            "-fx-background-color: #3498db;" +
                            "-fx-text-fill: white;" +
                            "-fx-font-size: 12px;"+
                            "-fx-padding: 5 10 5 10;" +
                            "-fx-background-radius: 3;"
                    );

                    viewButton.setOnAction(event -> {
                        Course course = getTableView().getItems().get(getIndex());
                        navigateToCourseReviews(course);
                    });
                }
                @Override
                protected void updateItem(Void item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty) {
                        setGraphic(null);
                    } else  {
                        setGraphic(viewButton);
                    }
                }
            });
        }

        private void loadAllCourses() {
            try{
                List<Course> courses = courseDAO.getAllCourses();

                ObservableList<Course> courseList = FXCollections.observableArrayList(courses);
                courseTable.setItems(courseList);

                messageLabel.setText(courses.size() + " course(s) loaded.");
                messageLabel.setStyle("-fx-text-fill: #27ae60;");

            } catch (Exception e) {
                messageLabel.setText("Error loading courses: " + e.getMessage());
                messageLabel.setStyle("-fx-text-fill: #e74c3c;");
                e.printStackTrace();
            }
        }

        @FXML
        private void handleSearch() {
            String subject = searchSubjectField.getText().trim();
            String numberStr = searchNumberField.getText().trim();
            String title = searchTitleField.getText().trim();

            subject = subject.toUpperCase();

            if (subject.isEmpty() && numberStr.isEmpty() && title.isEmpty()) {
                loadAllCourses();
                return;
            }

            try {
                Integer number = null;
                if (!numberStr.isEmpty()) {
                    try {
                        number = Integer.parseInt(numberStr);
                    } catch (NumberFormatException e) {
                        messageLabel.setText("Please enter a valid number");
                        messageLabel.setStyle("-fx-text-fill: #e74c3c;");
                        return;
                    }
                }
                List<Course> results = courseDAO.searchCourses(
                        subject.isEmpty() ? null : subject,
                        number,
                        title.isEmpty() ? null : title
                );

                ObservableList<Course> courseList = FXCollections.observableArrayList(results);
                courseTable.setItems(courseList);

                if (results.isEmpty()) {
                    messageLabel.setText("No courses found.");
                    messageLabel.setStyle("-fx-text-fill: #e67e22;");
                } else  {
                    messageLabel.setText(results.size() + " course(s) found.");
                    messageLabel.setStyle("-fx-text-fill: #27ae60;");
                }

            } catch (Exception e) {
                messageLabel.setText("Error searching courses: " + e.getMessage());
                messageLabel.setStyle("-fx-text-fill: #e67e22;");
                e.printStackTrace();
            }
        }


    @FXML
    private void handleClearSearch() {
        searchSubjectField.clear();
        searchNumberField.clear();
        searchTitleField.clear();

        loadAllCourses();
    }

    @FXML
    private void handleAddCourse() {
        String subject = addSubjectField.getText().trim();
        String numberStr = addNumberField.getText().trim();
        String title = addTitleField.getText().trim();

        if (!validateCourseInput(subject, numberStr, title)){
            return;
        }


        subject = subject.toUpperCase();
        int number = Integer.parseInt(numberStr);

        try {
            if (courseDAO.courseExists(subject, number, title)) {
                messageLabel.setText("Error: Course already exists.");
                messageLabel.setStyle("-fx-text-fill: #e67e22;");
                return;
            }

            boolean success = courseDAO.addCourse(subject, number, title);

            if (success) {
            messageLabel.setText("Course added successfully.");
            messageLabel.setStyle("-fx-text-fill: #27ae60;");

                addSubjectField.clear();
                addNumberField.clear();
                addTitleField.clear();

                loadAllCourses();

            } else  {
                messageLabel.setText("Error adding course to database.");
                messageLabel.setStyle("-fx-text-fill: #e67e22;");
            }


        } catch (Exception e) {
            messageLabel.setText("Error adding course to database.");
            messageLabel.setStyle("-fx-text-fill: #e74c3c;");
            e.printStackTrace();
        }
    }
    private boolean validateCourseInput(String subject, String numberStr, String title) {
            if (!subject.matches("[A-Za-z]{2,4}")) {
                messageLabel.setText("Error: Subject must be 2–4 letters");
                messageLabel.setStyle("-fx-text-fill: #e74c3c;");
                return false;
            }

            if (!numberStr.matches("\\d{4}")) {
                messageLabel.setText("Error: Number must be exactly 4 digits.");
                messageLabel.setStyle("-fx-text-fill: #e74c3c;");
                return false;
            }


            if (title.isEmpty() ||  title.length() > 50) {
                messageLabel.setText("Error: Title must be 1-50 characters.");
                messageLabel.setStyle("-fx-text-fill: #e74c3c;");
                return false;
            }

            return true;
    }

    private void navigateToCourseReviews(Course course) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("courseReviews.fxml"));
            Parent root = loader.load();

            CourseReviewsController controller = loader.getController();
            controller.initData(course, currentUsername);

            Stage stage = (Stage) courseTable.getScene().getWindow();
            stage.setScene(new Scene(root, 1280, 720));
            stage.show();

        } catch (IOException e) {
            messageLabel.setText("Error: Could not load course reviews page.");
            messageLabel.setStyle("-fx-text-fill: #e74c3c;");
            e.printStackTrace();
        }
    }

    @FXML
    private void handleMyReviews() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("myReviews.fxml"));
            Parent root = loader.load();

            MyReviewsController controller = loader.getController();
            controller.initData(currentUsername);

            Stage stage = (Stage) myReviewsButton.getScene().getWindow();
            stage.setScene(new Scene(root, 1280, 720));
            stage.show();

        } catch (IOException e) {
            messageLabel.setText("Error: Could not load My Reviews page.");
            messageLabel.setStyle("-fx-text-fill: #e74c3c;");
            e.printStackTrace();
        }
    }

    @FXML
        private  void handleLogOut() {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("loginScene.fxml"));
                Parent root = loader.load();

                Stage stage = (Stage) logOutButton.getScene().getWindow();
                stage.setScene(new Scene(root, 1280, 720));
                stage.show();

            } catch (IOException e) {
                messageLabel.setText("Error: Could not log out. ");
                messageLabel.setStyle("-fx-text-fill: #e74c3c;");
                e.printStackTrace();
            }
        }
}
