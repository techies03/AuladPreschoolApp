package com.aulad.controller;

import com.aulad.dao.AuthDAO;
import com.aulad.model.User;
import com.aulad.util.Session;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class LoginController {
	@FXML
	private TextField txtUser;
	@FXML
	private PasswordField txtPass;
	@FXML
	private Label lblMsg;

	@FXML
	public void onLogin() {
		String u = txtUser.getText().trim();
		String p = txtPass.getText();

		User user = AuthDAO.login(u, p);
		if (user == null) {
			lblMsg.setText("Invalid username or password.");
			return;
		}
		Session.set(user);

		String fxml = user.isOwner() ? "/admin-view.fxml" : "/main-view.fxml";
		String title = user.isOwner() ? "Aulad – Administrator • " + user.getUsername()
				: "Aulad – Form • " + user.getUsername() + " [" + user.getRole() + "]";

		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource(fxml));
			Scene scene = new Scene(loader.load(), user.isOwner() ? 820 : 900, 560);
			Stage stage = (Stage) txtUser.getScene().getWindow();
			stage.setScene(scene);
			stage.setTitle(title);
			stage.centerOnScreen();
			stage.setResizable(true);
		} catch (Exception ex) {
			ex.printStackTrace();
			lblMsg.setText("Unable to open next screen.");
		}
	}
}
