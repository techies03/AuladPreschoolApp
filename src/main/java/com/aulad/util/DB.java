package com.aulad.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DB {
	private static final String URL = "jdbc:mysql://localhost:3306/aulad?useSSL=false&serverTimezone=UTC";
	private static final String USER = "aulad";
	private static final String PASS = "admin123";

	public static Connection get() throws SQLException {
		// Driver auto-registers (com.mysql.cj.jdbc.Driver)
		return DriverManager.getConnection(URL, USER, PASS);
	}
}
