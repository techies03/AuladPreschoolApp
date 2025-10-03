package com.aulad.dao;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;

import com.aulad.model.Student;
import com.aulad.util.DB;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class StudentDAO {

	public static ObservableList<Student> all() {
		ObservableList<Student> list = FXCollections.observableArrayList();
		String sql = "SELECT id, name, birth_date, age, address, COALESCE(allergy,'') allergy FROM student ORDER BY name";
		try (Connection c = DB.get();
				PreparedStatement ps = c.prepareStatement(sql);
				ResultSet rs = ps.executeQuery()) {
			while (rs.next()) {
				list.add(new Student(rs.getInt("id"), rs.getString("name"), rs.getDate("birth_date").toLocalDate(),
						rs.getInt("age"), rs.getString("address"), rs.getString("allergy")));
			}
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
		return list;
	}

	public static int insert(Student s) {
		String sql = "INSERT INTO student(name, birth_date, age, address, allergy) VALUES(?,?,?,?,?)";
		try (Connection c = DB.get(); PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
			ps.setString(1, s.getName());
			ps.setDate(2, Date.valueOf(s.getBirthDate()));
			ps.setInt(3, s.getAge());
			ps.setString(4, s.getAddress());
			if (s.getAllergy() == null || s.getAllergy().isBlank())
				ps.setNull(5, Types.VARCHAR);
			else
				ps.setString(5, s.getAllergy());
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

	public static void update(Student s) {
		String sql = "UPDATE student SET name=?, birth_date=?, age=?, address=?, allergy=? WHERE id=?";
		try (Connection c = DB.get(); PreparedStatement ps = c.prepareStatement(sql)) {
			ps.setString(1, s.getName());
			ps.setDate(2, Date.valueOf(s.getBirthDate()));
			ps.setInt(3, s.getAge());
			ps.setString(4, s.getAddress());
			if (s.getAllergy() == null || s.getAllergy().isBlank())
				ps.setNull(5, Types.VARCHAR);
			else
				ps.setString(5, s.getAllergy());
			ps.setInt(6, s.getId());
			ps.executeUpdate();
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

	public static void delete(int id) {
		try (Connection c = DB.get(); PreparedStatement ps = c.prepareStatement("DELETE FROM student WHERE id=?")) {
			ps.setInt(1, id);
			ps.executeUpdate();
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}
}
