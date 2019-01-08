package com.thisisafakecom.thisisafakebot.commands.points.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

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

  public static boolean insertNewUser(String userId) {
    String sql = "INSERT INTO PointsTable (ID, Points) VALUES (?, 0)";
    try (Connection conn = connect();
        PreparedStatement pstmt = conn.prepareStatement(sql)) {
        pstmt.setString(1, userId);
        pstmt.executeUpdate();
        return true;
    } catch (SQLException e) {
      System.out.println(e.getMessage());
      return false;
    }
  }

  public static int getPoints(String userId) {
    String sql = "SELECT * FROM PointsTable WHERE ID = ?";
    int points;
    try (Connection conn = connect();
        PreparedStatement pstmt = conn.prepareStatement(sql)) {
        pstmt.setString(1, userId);
        ResultSet rs = pstmt.executeQuery();
        if(rs.next()) {
          points = rs.getInt("Points");
        } else {
          insertNewUser(userId);
          return getPoints(userId);
        }
    } catch (SQLException e) {
      System.out.println(e.getMessage());
      points = -1;
    }
    return points;
  }

}
