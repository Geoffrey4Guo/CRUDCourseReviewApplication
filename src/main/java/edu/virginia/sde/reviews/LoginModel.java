package edu.virginia.sde.reviews;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class LoginModel {
/*
    public LoginModel() {
        try {


        } catch (RuntimeException e) {
            throw new RuntimeException("Failed to connect to database", e);
        }
    }
    */

    public void addNewUser(String newUsername, String newPassword) {

        try{
            if (newPassword.length() < 8) {
                throw new RuntimeException("Password must be at least 8 characters");
            }

            if (Database.userExists(newUsername)) {
                throw new SQLException("Username already exists");
            }

            Database.register(newUsername, newPassword);

            /*
            PreparedStatement addUserStatement = conn.prepareStatement("INSERT INTO users (username, password) VALUES (?, ?)");

            addUserStatement.setString(1, newUsername);
            addUserStatement.setString(2, newPassword);

            addUserStatement.executeUpdate();
            addUserStatement.close();

             */
        } catch (SQLException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public void loginUser(String givenUsername, String givenPassword) {

        try {
            if (!Database.userExists(givenUsername)) {
                throw new RuntimeException("User does not exist");
            }

            Database.login(givenUsername, givenPassword);
            /*
            PreparedStatement searchStatement = conn.prepareStatement("SELECT password FROM users WHERE username = ?");
            searchStatement.setString(1, givenUsername);
            ResultSet resultSet = searchStatement.executeQuery();

            if (!resultSet.next()) {
                throw new RuntimeException("User does not exist");
            }
            String storedstring = resultSet.getString("password");
            if (!storedstring.equals(givenPassword)) {
                throw new RuntimeException("Wrong password");
            }
            */
            //completed run through now switch scenes in controller

        } catch (RuntimeException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public void quitDatabase() {
        Database.disconnect();
    }

/*
    private boolean checkForUsername(String username) {
        boolean found = false;
        try {
            PreparedStatement searchStatement = conn.prepareStatement("SELECT id FROM users WHERE username = ?");
            searchStatement.setString(1, username);

            ResultSet resultSets = searchStatement.executeQuery();

            if (resultSets.next()) {
                found = true;
            }

            return found;
        } catch (SQLException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

 */
}
