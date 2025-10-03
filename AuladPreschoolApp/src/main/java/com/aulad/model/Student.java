package com.aulad.model;

import java.time.LocalDate;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class Student {
	private final IntegerProperty id = new SimpleIntegerProperty();
	private final StringProperty name = new SimpleStringProperty();
	private final ObjectProperty<LocalDate> birthDate = new SimpleObjectProperty<>();
	private final IntegerProperty age = new SimpleIntegerProperty();
	private final StringProperty address = new SimpleStringProperty();
	private final StringProperty allergy = new SimpleStringProperty();

	public Student() {
	}

	public Student(int id, String name, LocalDate birth, int age, String address, String allergy) {
		setId(id);
		setName(name);
		setBirthDate(birth);
		setAge(age);
		setAddress(address);
		setAllergy(allergy);
	}

	public int getId() {
		return id.get();
	}

	public void setId(int v) {
		id.set(v);
	}

	public IntegerProperty idProperty() {
		return id;
	}

	public String getName() {
		return name.get();
	}

	public void setName(String v) {
		name.set(v);
	}

	public StringProperty nameProperty() {
		return name;
	}

	public LocalDate getBirthDate() {
		return birthDate.get();
	}

	public void setBirthDate(LocalDate v) {
		birthDate.set(v);
	}

	public ObjectProperty<LocalDate> birthDateProperty() {
		return birthDate;
	}

	public int getAge() {
		return age.get();
	}

	public void setAge(int v) {
		age.set(v);
	}

	public IntegerProperty ageProperty() {
		return age;
	}

	public String getAddress() {
		return address.get();
	}

	public void setAddress(String v) {
		address.set(v);
	}

	public StringProperty addressProperty() {
		return address;
	}

	public String getAllergy() {
		return allergy.get();
	}

	public void setAllergy(String v) {
		allergy.set(v);
	}

	public StringProperty allergyProperty() {
		return allergy;
	}
}
