package com.bingo.eatime.core;

import java.util.ArrayList;
import java.util.List;

import com.google.appengine.api.datastore.PhoneNumber;
import com.google.appengine.api.datastore.PostalAddress;

public class Restaurant {

	public static final String PROPERTY_NAME = "name";
	public static final String PROPERTY_CATEGORIES = "categories";
	public static final String PROPERTY_LOCATION = "address";

	private String name;
	private List<Category> categories;
	private PostalAddress location;
	private PhoneNumber phoneNumber;

	private Restaurant() {

	}

	public String getName() {
		return name;
	}

	private Restaurant setName(String name) {
		this.name = name;

		return this;
	}

	public List<Category> getCategories() {
		return categories;
	}

	private Restaurant setCategories(List<Category> categories) {
		this.categories = categories;

		return this;
	}

	private Restaurant addCategory(Category category) {
		if (this.categories == null) {
			this.categories = new ArrayList<Category>();
		}

		this.categories.add(category);

		return this;
	}

	public PostalAddress getLocation() {
		return location;
	}

	private Restaurant setLocation(PostalAddress location) {
		this.location = location;

		return this;
	}

	public PhoneNumber getPhoneNumber() {
		return phoneNumber;
	}

	private Restaurant setPhoneNumber(PhoneNumber phoneNumber) {
		this.phoneNumber = phoneNumber;

		return this;
	}

	public static Restaurant createRestaurant(String name,
			List<Category> categories, PostalAddress address,
			PhoneNumber phoneNumber) {
		Restaurant restaurant = new Restaurant().setName(name)
				.setCategories(categories).setLocation(address)
				.setPhoneNumber(phoneNumber);

		return restaurant;
	}

	public static Restaurant createRestaurant(String name, Category category,
			PostalAddress address, PhoneNumber phoneNumber) {
		Restaurant restaurant = new Restaurant().setName(name)
				.addCategory(category).setLocation(address)
				.setPhoneNumber(phoneNumber);

		return restaurant;
	}

}
