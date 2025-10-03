package com.aulad.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.mindrot.jbcrypt.BCrypt;

import com.aulad.model.User;
import com.aulad.util.DB;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class UserDAO {

	public static ObservableList<User> all() {
		ObservableList<User> out = FXCollections.observableArrayList();
		String sql = "SELECT id, username, role, is_active FROM users ORDER BY username";
		try (Connection c = DB.get();
				PreparedStatement ps = c.prepareStatement(sql);
				ResultSet rs = ps.executeQuery()) {
			while (rs.next()) {
				out.add(new User(rs.getInt("id"), rs.getString("username"), rs.getString("role"),
						rs.getBoolean("is_active")));
			}
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
		return out;
	}

	public static int add(String username, String rawPassword, String role, boolean active) {
		String sql = "INSERT INTO users(username, password_hash, role, is_active) VALUES(?,?,?,?)";
		try (Connection c = DB.get(); PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
			ps.setString(1, username);
			ps.setString(2, BCrypt.hashpw(rawPassword, BCrypt.gensalt(12)));
			ps.setString(3, role);
			ps.setBoolean(4, active);
			ps.executeUpdate();
			try (ResultSet keys = ps.getGeneratedKeys()) {
				if (keys.next())
					return keys.getInt(1);
			}
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
		return 0;
	}

	public static void update(int id, String username, String role, boolean active, String newPassword) {
		boolean changePw = newPassword != null && !newPassword.isBlank();
		String sql = changePw ? "UPDATE users SET username=?, role=?, is_active=?, password_hash=? WHERE id=?"
				: "UPDATE users SET username=?, role=?, is_active=? WHERE id=?";
		try (Connection c = DB.get(); PreparedStatement ps = c.prepareStatement(sql)) {
			ps.setString(1, username);
			ps.setString(2, role);
			ps.setBoolean(3, active);
			if (changePw) {
				ps.setString(4, BCrypt.hashpw(newPassword, BCrypt.gensalt(12)));
				ps.setInt(5, id);
			} else {
				ps.setInt(4, id);
			}
			ps.executeUpdate();
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

	public static void delete(int id) {
		try (Connection c = DB.get(); PreparedStatement ps = c.prepareStatement("DELETE FROM users WHERE id=?")) {
			ps.setInt(1, id);
			ps.executeUpdate();
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

	public static User findById(int id) {
		String sql = "SELECT id, username, role, is_active FROM users WHERE id=?";
		try (Connection c = DB.get(); PreparedStatement ps = c.prepareStatement(sql)) {
			ps.setInt(1, id);
			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next()) {
					return new User(rs.getInt("id"), rs.getString("username"), rs.getString("role"),
							rs.getBoolean("is_active"));
				}
			}
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
		return null;
	}

}
