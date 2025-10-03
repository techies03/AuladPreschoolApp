package com.aulad.util;

import com.aulad.model.User;

public class Session {
	private static User current;

	public static void set(User u) {
		current = u;
	}

	public static User get() {
		return current;
	}

	public static boolean isLoggedIn() {
		return current != null;
	}

	public static void clear() {
		current = null;
	}
}
