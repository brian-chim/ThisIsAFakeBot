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

  public static boolean insertNewUser(String username, String guildId) {
    String sql = "INSERT INTO PointsTable (Username, GuildID, Points, Level) VALUES (?, ?, 0, 1)";
    try (Connection conn = connect();
        PreparedStatement pstmt = conn.prepareStatement(sql)) {
        pstmt.setString(1, username);
        pstmt.setString(2, guildId);
        pstmt.executeUpdate();
        return true;
    } catch (SQLException e) {
      System.out.println(e.getMessage());
      return false;
    }
  }

  public static int getPoints(String username, String guildId) {
    String sql = "SELECT * FROM PointsTable WHERE Username = ? AND GuildId = ?";
    int points;
    try (Connection conn = connect();
        PreparedStatement pstmt = conn.prepareStatement(sql)) {
        pstmt.setString(1, username);
        pstmt.setString(2, guildId);
        ResultSet rs = pstmt.executeQuery();
        if(rs.next()) {
          points = rs.getInt("Points");
        } else {
          insertNewUser(username, guildId);
          return getPoints(username, guildId);
        }
    } catch (SQLException e) {
      System.out.println(e.getMessage());
      points = -1;
    }
    return points;
  }

  public static int getLevel(String username, String guildId) {
    String sql = "SELECT * FROM PointsTable WHERE Username = ? AND GuildId = ?";
    int level;
    try (Connection conn = connect();
        PreparedStatement pstmt = conn.prepareStatement(sql)) {
        pstmt.setString(1, username);
        pstmt.setString(2, guildId);
        ResultSet rs = pstmt.executeQuery();
        if(rs.next()) {
          level = rs.getInt("Level");
        } else {
          insertNewUser(username, guildId);
          return getLevel(username, guildId);
        }
    } catch (SQLException e) {
      System.out.println(e.getMessage());
      level = -1;
    }
    return level;
  }
}
