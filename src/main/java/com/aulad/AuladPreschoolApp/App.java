package com.aulad.AuladPreschoolApp;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class App extends Application {
	@Override
	public void start(Stage stage) throws Exception {
		Parent root = FXMLLoader.load(getClass().getResource("/login-view.fxml"));
		Scene scene = new Scene(root);

		// scene.getStylesheets().add(getClass().getResource("/NewFile.css").toExternalForm());
		String css = this.getClass().getResource("/app.css").toExternalForm();
		scene.getStylesheets().add(css);

		stage.setTitle("Login");
		stage.setScene(scene);
		stage.centerOnScreen();
		stage.getIcons().add(new Image(getClass().getResourceAsStream("/auladLogo.png")));
		stage.show();
	}

	public static void main(String[] args) {
		launch();
	}
}
