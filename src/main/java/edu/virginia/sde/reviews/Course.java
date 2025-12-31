package edu.virginia.sde.reviews;

import java.sql.Timestamp;
public class Course {
    private int id;
    private String subject;
    private int number;
    private String title;
    private Double avgRating;
    private String comment;
    //default constructor
    public Course(int id, String subject, int number, String title){
        this.id = id;
        this.subject = subject;
        this.number = number;
        this.title = title;
        this.avgRating = null;
    }
    //accessor methods
    public int getId() {
        return id;
    }
    public String getSubject(){
        return subject;
    }
    public int getNumber(){
        return number;
    }
    public String getTitle(){
        return title;
    }
    public Double getAvgRating(){
        return avgRating;
    }
    //mutator methods
    public void setAvgRating(Double avg){
        this.avgRating = avg;
    }
    public String getComment() {return comment;}
    public void setComment(String comment) {this.comment = comment;}
}
