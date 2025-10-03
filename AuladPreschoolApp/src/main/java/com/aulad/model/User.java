package com.aulad.model;

public class User {
	private final int id;
	private final String username;
	private final String role;
	private final boolean active;

	public User(int id, String username, String role, boolean active) {
		this.id = id;
		this.username = username;
		this.role = role;
		this.active = active;
	}

	// Getters
	public int getId() {
		return id;
	}

	public String getUsername() {
		return username;
	}

	public String getRole() {
		return role;
	}

	public boolean isActive() {
		return active;
	}

	// Role helpers
	public boolean isOwner() {
		return "OWNER".equalsIgnoreCase(role);
	}

	public boolean isStaff() {
		return "STAFF".equalsIgnoreCase(role);
	}
}
