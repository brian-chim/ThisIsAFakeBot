package com.thisisafakecom.thisisafakebot.commands.points.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import net.dv8tion.jda.core.entities.User;

public class DBHandler {

  // taken from http://www.sqlitetutorial.net/sqlite-java/select/
  private static Connection connect() {
    // SQLite connection string
    String url = "jdbc:sqlite:sqlite/db/points.db";
    Connection conn = null;
    try {
        conn = DriverManager.getConnection(url);
    } catch (SQLException e) {
        System.out.println(e.getMessage());
    }
    return conn;
  }

  public static boolean insertNewUser(User user) {
    String sql = "INSERT INTO PointsTable (ID, CanonicalName, Points) VALUES (?, ?, 0)";
    try (Connection conn = connect();
        PreparedStatement pstmt = conn.prepareStatement(sql)) {
        pstmt.setString(1, user.getId());
        pstmt.setString(2, user.getName() + "#" + user.getDiscriminator());
        if (pstmt.executeUpdate() == 0) {
          return false;
        } else {
          return true;
        }
    } catch (SQLException e) {
      System.out.println(e.getMessage());
      return false;
    }
  }

  public static int getPoints(User user) {
    String sql = "SELECT * FROM PointsTable WHERE ID = ?";
    int points;
    try (Connection conn = connect();
        PreparedStatement pstmt = conn.prepareStatement(sql)) {
        pstmt.setString(1, user.getId());
        ResultSet rs = pstmt.executeQuery();
        if(rs.next()) {
          points = rs.getInt("Points");
        } else {
          insertNewUser(user);
          return getPoints(user);
        }
    } catch (SQLException e) {
      System.out.println(e.getMessage());
      points = -1;
    }
    return points;
  }

  public static boolean addPoints(User user, int points) {
    String sql = "UPDATE PointsTable SET Points = Points + ? WHERE ID = ?";
    try (Connection conn = connect();
        PreparedStatement pstmt = conn.prepareStatement(sql)) {
        pstmt.setString(2, user.getId());
        pstmt.setInt(1, points);
        // check if one record was updated
        if(pstmt.executeUpdate() == 0) {
          // if it wasnt then insert a new user and try again
          if (insertNewUser(user)) {
            // new user inserted, return the next try;
            return addPoints(user, points);
          }
          // if user wasnt inserted and pstmt failed to update then return false
           return false;
        }
        // return true if statement was executed as wanted
        return true;
    } catch (SQLException e) {
      System.out.println(e.getMessage());
    }
    return false;
  }
}
