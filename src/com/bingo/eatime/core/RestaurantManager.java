package com.bingo.eatime.core;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;

public class RestaurantManager {

	public static final String KIND_RESTAURANT = "restaurant";

	private DatastoreService mDatastoreService;

	public RestaurantManager() {
		mDatastoreService = DatastoreServiceFactory.getDatastoreService();
	}

	public void addRestaurant(Restaurant restaurant) {
		Entity restaurantEntity = new Entity(KIND_RESTAURANT);
		restaurantEntity.setProperty(Restaurant.PROPERTY_NAME,
				restaurant.getName());
		restaurantEntity.setProperty(Restaurant.PROPERTY_ADDRESS,
				restaurant.getLocation());
		restaurantEntity.setProperty(Restaurant.PROPERTY_PHONENUMBER,
				restaurant.getPhoneNumber());

	}

}
