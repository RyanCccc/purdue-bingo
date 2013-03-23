package com.bingo.eatime.core;

import java.util.TreeSet;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.PhoneNumber;
import com.google.appengine.api.datastore.PostalAddress;

public class Restaurant {

	public static final String KIND_RESTAURANT = "restaurant";

	public static final String PROPERTY_NAME = "name";
	// public static final String PROPERTY_CATEGORIES = "categories";
	public static final String PROPERTY_ADDRESS = "address";
	public static final String PROPERTY_PHONENUMBER = "phonenumber";

	private Key key;
	private String name;
	private TreeSet<Category> categories;
	private PostalAddress address;
	private PhoneNumber phoneNumber;

	private Restaurant() {

	}

	public Key getKey() {
		return key;
	}

	private Restaurant setKey(Key key) {
		this.key = key;

		return this;
	}

	private Restaurant setKey(String name) {
		this.key = KeyFactory.createKey(KIND_RESTAURANT,
				Utilities.getKeyFromName(name));

		return this;
	}

	public String getName() {
		return name;
	}

	private Restaurant setName(String name) {
		this.name = name;

		return this;
	}

	public TreeSet<Category> getCategories() {
		return categories;
	}

	private Restaurant setCategories(TreeSet<Category> categories) {
		this.categories = categories;

		return this;
	}

	private Restaurant addCategories(Iterable<Category> categories) {
		if (this.categories == null) {
			this.categories = Category.newCategories();
		}

		for (Category category : categories) {
			this.categories.add(category);
		}

		return this;
	}

	private Restaurant addCategory(Category category) {
		if (this.categories == null) {
			this.categories = Category.newCategories();
		}

		this.categories.add(category);

		return this;
	}

	public PostalAddress getLocation() {
		return address;
	}

	private Restaurant setLocation(PostalAddress address) {
		this.address = address;

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
			Iterable<Category> categories, PostalAddress address,
			PhoneNumber phoneNumber) {
		Restaurant restaurant = new Restaurant();
		restaurant.setName(name).setKey(name).addCategories(categories)
				.setLocation(address).setPhoneNumber(phoneNumber);

		return restaurant;
	}

	public static Restaurant createRestaurant(String name, Category category,
			PostalAddress address, PhoneNumber phoneNumber) {
		Restaurant restaurant = new Restaurant();
		restaurant.setName(name).setKey(name).addCategory(category)
				.setLocation(address).setPhoneNumber(phoneNumber);

		return restaurant;
	}

	/**
	 * Create Restaurant object using DataStore Entity object.
	 * 
	 * @param entity
	 *            Entity object of Restaurant kind.
	 * @return Restaurant object if successful reconstructed. Null if
	 *         reconstruct failed or entity is not Restaurant kind.
	 */
	public static Restaurant createRestaurant(Entity entity) {
		if (entity.getKind().equals(KIND_RESTAURANT)) {
			try {
				Restaurant restaurant = new Restaurant();
				restaurant.setKey(entity.getKey());
				restaurant.setName((String) entity.getProperty(PROPERTY_NAME));
				restaurant.setLocation((PostalAddress) entity
						.getProperty(PROPERTY_ADDRESS));
				restaurant.setPhoneNumber((PhoneNumber) entity
						.getProperty(PROPERTY_PHONENUMBER));

				TreeSet<Category> categories = CategoryManager
						.getRestaurantCategories(entity.getKey());
				restaurant.setCategories(categories);

				return restaurant;
			} catch (ClassCastException e) {
				return null;
			}
		} else {
			return null;
		}
	}

}
