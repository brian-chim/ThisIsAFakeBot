package com.thisisafakecom.thisisafakebot.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DBHandler {

  public static void initDb() {
	  createPointsTable();
  }
  // taken from http://www.sqlitetutorial.net/sqlite-java/select/
  public static Connection connect() {
    // SQLite connection string
    String url = "jdbc:sqlite:sqlite/db/thisisafakebot.db";
    Connection conn = null;
    try {
        conn = DriverManager.getConnection(url);
    } catch (SQLException e) {
        System.out.println(e.getMessage());
    }
    return conn;
  }
  
  public static boolean createTable(String tableName, String[] columns, String[] dataTypes) {
	  int numCol = columns.length;
	  String sql = "CREATE TABLE IF NOT EXISTS " + tableName + "(";
	  for (int i = 0; i < numCol; i++) {
		  sql += columns[i] + " " + dataTypes[i] + ", ";
	  }
	  sql = sql.substring(0, sql.length() - 2) + ");";
	  try (Connection conn = connect();
		  PreparedStatement pstmt = conn.prepareStatement(sql)) {
		  boolean success = pstmt.execute();
		  close(conn, pstmt);
		  return success;
	  } catch (SQLException e) {
		System.out.println(e.getMessage());
		return false;
	}
  }

  private static void createPointsTable() {
	  String[] pointsColumns = {"ID", "CanonicalName","Points"};
	  String[] pointsDataTypes = {"INTEGER NOT NULL UNIQUE", "TEXT NOT NULL", "INTEGER NOT NULL"};
	  createTable("PointsTable", pointsColumns, pointsDataTypes);
  }
 
  public static void close(Connection conn, PreparedStatement stmt) {
	  try {
		  conn.close();
		  stmt.close();  
	  } catch (Exception e) {
		  System.out.println("Failed to close connections.");
	  }
  }

  public static void close(Connection conn, PreparedStatement stmt, ResultSet rs) {
	  try {
		  conn.close();
		  stmt.close();
		  rs.close();
	  } catch (Exception e) {
		  System.out.println("Failed to close.");
	  }
  }
}
