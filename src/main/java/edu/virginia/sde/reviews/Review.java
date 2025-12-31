package edu.virginia.sde.reviews;
import java.sql.Timestamp;
public class Review {
    private int userID;
    private int courseID;
    private int rating;
    private String comment;
    private Timestamp timestamp;
    //default constructor
    public Review(int userId, int courseId, int rating, String comment, Timestamp timestamp){
        this.userID = userId;
        this.courseID = courseId;
        this.rating = rating;
        this.comment = comment;
        this.timestamp = timestamp;

    }
    //accessor methods

    public int getUserID() {
        return userID;
    }

    public int getCourseID() {
        return courseID;
    }

    public int getRating() {
        return rating;
    }
    public String getComment(){
        return comment;
    }
    public Timestamp getTimestamp(){
        return timestamp;
    }
}
