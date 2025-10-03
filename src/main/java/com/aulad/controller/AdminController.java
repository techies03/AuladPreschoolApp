package com.aulad.controller;

import com.aulad.dao.UserDAO;
import com.aulad.model.User;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class AdminController {
	@FXML
	private TextField txtId, txtUsername, txtPassword, txtSearch;
	@FXML
	private ComboBox<String> cbRole;
	@FXML
	private CheckBox chkActive;
	@FXML
	private Label lblMsg;

	@FXML
	private TableView<User> table;
	@FXML
	private TableColumn<User, Number> colId;
	@FXML
	private TableColumn<User, String> colUsername, colRole, colActive;

	private final ObservableList<User> backing = FXCollections.observableArrayList();
	private FilteredList<User> filtered;
	private SortedList<User> sorted;

	@FXML
	public void initialize() {
		// Defer until scene graph exists (so we can grab the Stage)
		javafx.application.Platform.runLater(() -> {
			var user = com.aulad.util.Session.get();
			if (user == null) {
				redirectToLogin(table); // any node from this scene
				return;
			}
			if (!user.isOwner()) {
				// Not allowed here -> go to Students screen
				goStudents();
				return;
			}
			// proceed with normal setup only after authz passes
			setupTableAndBindings();
		});
	}

	// Extracted from your previous initialize() body
	private void setupTableAndBindings() {
		cbRole.getItems().addAll("OWNER", "STAFF");
		cbRole.getSelectionModel().select("STAFF");

		colId.setCellValueFactory(c -> new javafx.beans.property.SimpleIntegerProperty(c.getValue().getId()));
		colUsername
				.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getUsername()));
		colRole.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getRole()));
		colActive.setCellValueFactory(
				c -> new javafx.beans.property.SimpleStringProperty(c.getValue().isActive() ? "Yes" : "No"));

		backing.setAll(com.aulad.dao.UserDAO.all());
		filtered = new javafx.collections.transformation.FilteredList<>(backing, u -> true);
		sorted = new javafx.collections.transformation.SortedList<>(filtered);
		sorted.comparatorProperty().bind(table.comparatorProperty());
		table.setItems(sorted);

		table.getSelectionModel().selectedItemProperty().addListener((obs, old, u) -> fillForm(u));
		txtSearch.textProperty().addListener((obs, o, q) -> {
			String qq = q == null ? "" : q.toLowerCase();
			filtered.setPredicate(u -> u.getUsername().toLowerCase().contains(qq));
		});
	}

	// Reuse in multiple places
	private void redirectToLogin(javafx.scene.Node node) {
		try {
			var loader = new javafx.fxml.FXMLLoader(getClass().getResource("/login-view.fxml"));
			var scene = new javafx.scene.Scene(loader.load(), 360, 220);
			var stage = (javafx.stage.Stage) node.getScene().getWindow();
			com.aulad.util.Session.clear();
			stage.setScene(scene);
			stage.setTitle("Aulad – Login");
			stage.centerOnScreen();
			stage.setResizable(false);
		} catch (Exception e) {
			e.printStackTrace();
			new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR, "Unable to open Login.")
					.showAndWait();
		}
	}

	@FXML
	public void onLogout() {
		redirectToLogin(table);
	}

	private void fillForm(User u) {
		if (u == null) {
			onReset();
			return;
		}
		txtId.setText(String.valueOf(u.getId()));
		txtUsername.setText(u.getUsername());
		cbRole.getSelectionModel().select(u.getRole());
		chkActive.setSelected(u.isActive());
		txtPassword.clear();
		lblMsg.setText("");
	}

	@FXML
	public void onAdd() {
		String username = txtUsername.getText().trim();
		String pass = txtPassword.getText();
		String role = cbRole.getValue();
		boolean active = chkActive.isSelected();

		if (username.isBlank()) {
			alert("Please enter the username.");
			return;
		}

		if (pass.isBlank()) {
			alert("Please enter the password.");
			return;
		}

		int newId = UserDAO.add(username, pass, role, active);
		refreshAndSelect(newId);
		onReset();
		lblMsg.setText("User added.");
	}

	@FXML
	public void onUpdate() {
		var sel = table.getSelectionModel().getSelectedItem();
		if (sel == null) {
			lblMsg.setText("Select a user to update.");
			return;
		}

		String username = txtUsername.getText().trim();
		String newPass = txtPassword.getText(); // optional
		String role = cbRole.getValue();
		boolean active = chkActive.isSelected();

		if (username.isBlank()) {
			lblMsg.setText("Username is required.");
			return;
		}

		// 1) Perform update
		UserDAO.update(sel.getId(), username, role, active, newPass);

		// 2) Refresh table
		refreshAndSelect(sel.getId());
		txtPassword.clear();
		lblMsg.setText("User updated.");

		var me = com.aulad.util.Session.get();
		if (me != null && me.getId() == sel.getId()) {
			var fresh = UserDAO.findById(me.getId()); // exact method name/id
			com.aulad.util.Session.set(fresh);

			if (fresh == null || !fresh.isActive()) {
				alert("Your account is inactive. Logging out.");
				redirectToLogin(table);
				return;
			}
			if (!fresh.isOwner()) {
				alert("Role changed to STAFF. Redirecting to Students.");
				goStudents();
				return;
			}
		}

	}

	@FXML
	public void onDelete() {
		var sel = table.getSelectionModel().getSelectedItem();
		if (sel == null) {
			lblMsg.setText("Select a user to delete.");
			return;
		}

		var me = com.aulad.util.Session.get();
		boolean deletingSelf = (me != null && me.getId() == sel.getId());

		if (confirm("Delete user \"" + sel.getUsername() + "\"?")) {
			UserDAO.delete(sel.getId());
			refreshAndSelect(0);
			onReset();
			lblMsg.setText("User deleted.");

			if (deletingSelf) {
				alert("You deleted your own account. Logging out.");
				redirectToLogin(table);
			}
		}
	}

	@FXML
	public void onReset() {
		txtId.clear();
		txtUsername.clear();
		txtPassword.clear();
		cbRole.getSelectionModel().select("STAFF");
		chkActive.setSelected(true);
		table.getSelectionModel().clearSelection();
		lblMsg.setText("");
	}

	@FXML
	public void goStudents() {
		var user = com.aulad.util.Session.get();
		if (user == null) {
			redirectToLogin(table);
			return;
		}

		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource("/main-view.fxml"));
			Scene scene = new Scene(loader.load(), 900, 560);

			Stage stage = (Stage) table.getScene().getWindow();
			stage.setScene(scene);

			// Title for Students screen
			stage.setTitle("Aulad – Form • " + user.getUsername() + " [" + user.getRole() + "]");
			stage.setResizable(true);
			stage.centerOnScreen();
		} catch (Exception e) {
			e.printStackTrace();
			lblMsg.setText("Unable to open Students view.");
		}
	}

	private void refreshAndSelect(int id) {
		backing.setAll(UserDAO.all());
		if (id > 0) {
			backing.stream().filter(u -> u.getId() == id).findFirst()
					.ifPresent(u -> table.getSelectionModel().select(u));
		}
	}

	private boolean confirm(String msg) {
		return new Alert(Alert.AlertType.CONFIRMATION, msg, ButtonType.OK, ButtonType.CANCEL).showAndWait()
				.orElse(ButtonType.CANCEL) == ButtonType.OK;
	}

	private void alert(String msg) {
		new Alert(Alert.AlertType.WARNING, msg, ButtonType.OK).showAndWait();
	}
}
