package com.aulad.controller;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

import com.aulad.dao.StudentDAO;
import com.aulad.model.Student;
import com.aulad.model.User;
import com.aulad.util.Session;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class MainController {
	@FXML
	private TextField txtName, txtAge, txtSearch;
	@FXML
	private TextArea taAddress, taAllergy;
	@FXML
	private DatePicker dpBirth;
	@FXML
	private Button btnManageUsers;

	@FXML
	private TableView<Student> table;
	@FXML
	private TableColumn<Student, Number> colId, colAge;
	@FXML
	private TableColumn<Student, String> colName, colBirth, colAddr, colAllg;

	private final ObservableList<Student> backing = FXCollections.observableArrayList();
	private FilteredList<Student> filtered;
	private SortedList<Student> sorted;

	@FXML
	public void initialize() {
		javafx.application.Platform.runLater(() -> {
			User user = Session.get();
			if (user == null) {
				redirectToLogin(table);
				return;
			}
			// show button only for OWNER
			if (btnManageUsers != null) {
				btnManageUsers.setVisible(user.isOwner());
			}

			setupTableAndBindings();
		});
	}

	private void setupTableAndBindings() {
		colId.setCellValueFactory(c -> c.getValue().idProperty());
		colName.setCellValueFactory(c -> c.getValue().nameProperty());
		colBirth.setCellValueFactory(
				c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getBirthDate().toString()));
		colAge.setCellValueFactory(c -> c.getValue().ageProperty());
		colAddr.setCellValueFactory(c -> c.getValue().addressProperty());
		colAllg.setCellValueFactory(c -> c.getValue().allergyProperty());

		backing.setAll(com.aulad.dao.StudentDAO.all());
		filtered = new javafx.collections.transformation.FilteredList<>(backing, s -> true);
		sorted = new javafx.collections.transformation.SortedList<>(filtered);
		sorted.comparatorProperty().bind(table.comparatorProperty());
		table.setItems(sorted);

		table.getSelectionModel().selectedItemProperty().addListener((obs, old, s) -> fillForm(s));
		txtSearch.textProperty().addListener((obs, o, q) -> {
			String qq = q == null ? "" : q.toLowerCase();
			filtered.setPredicate(
					s -> s.getName().toLowerCase().contains(qq) || s.getAddress().toLowerCase().contains(qq)
							|| (s.getAllergy() != null && s.getAllergy().toLowerCase().contains(qq)));
		});

		dpBirth.valueProperty().addListener((obs, o, d) -> txtAge.setText(d == null ? "" : String.valueOf(calcAge(d))));

		javafx.application.Platform.runLater(() -> {
			var sc = table.getScene();
			sc.getAccelerators().put(new javafx.scene.input.KeyCodeCombination(javafx.scene.input.KeyCode.N,
					javafx.scene.input.KeyCombination.SHORTCUT_DOWN), this::onAdd);
			sc.getAccelerators().put(new javafx.scene.input.KeyCodeCombination(javafx.scene.input.KeyCode.S,
					javafx.scene.input.KeyCombination.SHORTCUT_DOWN), this::onUpdate);
			sc.getAccelerators().put(new javafx.scene.input.KeyCodeCombination(javafx.scene.input.KeyCode.DELETE),
					this::onDelete);
			sc.getAccelerators().put(new javafx.scene.input.KeyCodeCombination(javafx.scene.input.KeyCode.ESCAPE),
					this::onReset);
		});
	}

	private void redirectToLogin(javafx.scene.Node node) {
		try {
			var loader = new javafx.fxml.FXMLLoader(getClass().getResource("/login-view.fxml"));
			Scene scene = new Scene(loader.load()); // don’t fix width/height
			var stage = (javafx.stage.Stage) node.getScene().getWindow();
			com.aulad.util.Session.clear();
			stage.setScene(scene);
			stage.setTitle("Aulad – Login");
			stage.centerOnScreen();
			stage.setResizable(false);
			stage.sizeToScene(); // sizes the stage to the scene’s preferred size
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

	@FXML
	public void goAdmin() {
		User user = Session.get();
		if (user == null) {
			redirectToLogin(table);
			return;
		}
		if (!user.isOwner()) {
			new Alert(Alert.AlertType.WARNING, "Only OWNER can access User Management").showAndWait();
			return;
		}

		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource("/admin-view.fxml"));
			Scene scene = new Scene(loader.load(), 820, 560);

			Stage stage = (Stage) table.getScene().getWindow();
			stage.setScene(scene);
			stage.setTitle("Aulad – Administrator • " + user.getUsername());
			stage.setResizable(true);
		} catch (Exception e) {
			e.printStackTrace();
			new Alert(Alert.AlertType.ERROR, "Unable to open Admin view").showAndWait();
		}
	}

	private void fillForm(Student s) {
		if (s == null) {
			onReset();
			return;
		}
		txtName.setText(s.getName());
		dpBirth.setValue(s.getBirthDate());
		txtAge.setText(String.valueOf(s.getAge()));
		taAddress.setText(s.getAddress());
		taAllergy.setText(s.getAllergy());
	}

	private int calcAge(LocalDate b) {
		return (int) ChronoUnit.YEARS.between(b, LocalDate.now());
	}

	@FXML
	public void onAdd() {
		String name = txtName.getText().trim();
		LocalDate birth = dpBirth.getValue();
		String address = taAddress.getText().trim();
		String allergy = taAllergy.getText().trim();

		if (name.isBlank()) {
			alert("Please enter the student's name.");
			return;
		} else if (birth == null) {
			alert("Please select the birth date.");
			return;
		} else if (address.isBlank()) {
			alert("Please enter the address.");
			return;
		}

		int age = calcAge(birth);
		if (age < 4 || age > 6) {
			alert("Age must be 4–6. Current: " + age);
			return;
		}

		int newId = StudentDAO.insert(new Student(0, name, birth, age, address, allergy));
		refreshAndSelect(newId);
		onReset();
	}

	@FXML
	public void onUpdate() {
		var sel = table.getSelectionModel().getSelectedItem();
		if (sel == null) {
			alert("Select a row to update.");
			return;
		}

		String name = txtName.getText().trim();
		LocalDate birth = dpBirth.getValue();
		String address = taAddress.getText().trim();
		String allergy = taAllergy.getText().trim();
		if (name.isBlank() || birth == null || address.isBlank()) {
			alert("Fill Name, Birth, Address.");
			return;
		}

		int age = calcAge(birth);
		if (age < 4 || age > 6) {
			alert("Age must be 4–6. Current: " + age);
			return;
		}

		sel.setName(name);
		sel.setBirthDate(birth);
		sel.setAge(age);
		sel.setAddress(address);
		sel.setAllergy(allergy);
		StudentDAO.update(sel);
		refreshAndSelect(sel.getId());
	}

	@FXML
	public void onDelete() {
		var sel = table.getSelectionModel().getSelectedItem();
		if (sel == null) {
			alert("Select a row to delete.");
			return;
		}
		if (confirm("Delete \"" + sel.getName() + "\"?")) {
			StudentDAO.delete(sel.getId());
			refreshAndSelect(0);
			onReset();
		}
	}

	@FXML
	public void onReset() {
		txtName.clear();
		dpBirth.setValue(null);
		txtAge.clear();
		taAddress.clear();
		taAllergy.clear();
		table.getSelectionModel().clearSelection();
	}

	private void refreshAndSelect(int id) {
		backing.setAll(StudentDAO.all());
		if (id > 0) {
			backing.stream().filter(s -> s.getId() == id).findFirst()
					.ifPresent(s -> table.getSelectionModel().select(s));
		}
	}

	private void alert(String msg) {
		new Alert(Alert.AlertType.WARNING, msg, ButtonType.OK).showAndWait();
	}

	private boolean confirm(String msg) {
		return new Alert(Alert.AlertType.CONFIRMATION, msg, ButtonType.OK, ButtonType.CANCEL).showAndWait()
				.orElse(ButtonType.CANCEL) == ButtonType.OK;
	}
}
