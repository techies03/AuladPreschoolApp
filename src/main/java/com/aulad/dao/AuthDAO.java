package com.aulad.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.mindrot.jbcrypt.BCrypt;

import com.aulad.model.User;
import com.aulad.util.DB;

public class AuthDAO {
	public static User login(String username, String password) {
		String sql = "SELECT id, username, password_hash, role, is_active FROM users WHERE username=?";
		try (Connection c = DB.get(); PreparedStatement ps = c.prepareStatement(sql)) {
			ps.setString(1, username);
			try (ResultSet rs = ps.executeQuery()) {
				if (!rs.next())
					return null;
				String hash = rs.getString("password_hash");
				if (!BCrypt.checkpw(password, hash))
					return null;
				if (rs.getInt("is_active") != 1)
					return null;
				return new User(rs.getInt("id"), rs.getString("username"), rs.getString("role"), true);
			}
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

	// Utility to generate a hash once for seeding
	public static String hash(String raw) {
		return BCrypt.hashpw(raw, BCrypt.gensalt(12));
	}
}
