package com.bingo.eatime.core;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.Transaction;
import com.google.appengine.api.datastore.TransactionOptions;

public class RestaurantManager {

	/**
	 * Add restaurant to database. It is necessary to check whether the
	 * operation is successful or not.
	 * 
	 * @param restaurant
	 *            Restaurant object, can be created by calling
	 *            Restaurant.createRestaurant.
	 * @return Key of added Restaurant. Null if failed.
	 */
	public static Key addRestaurant(Restaurant restaurant) {
		DatastoreService datastore = DatastoreServiceFactory
				.getDatastoreService();

		TransactionOptions options = TransactionOptions.Builder.withXG(true);
		Transaction txn = datastore.beginTransaction(options);
		Key restaurantKey;
		try {
			Entity restaurantEntity = new Entity(Restaurant.KIND_RESTAURANT,
					restaurant.getKey().getName());
			restaurantEntity.setProperty(Restaurant.PROPERTY_NAME,
					restaurant.getName());
			restaurantEntity.setProperty(Restaurant.PROPERTY_ADDRESS,
					restaurant.getLocation());
			restaurantEntity.setProperty(Restaurant.PROPERTY_PHONENUMBER,
					restaurant.getPhoneNumber());

			restaurantKey = datastore.put(restaurantEntity);

			for (Category category : restaurant.getCategories()) {
				Entity restaurantKeyEntity = CategoryManager
						.createRestaurantKeyEntity(restaurantKey, restaurant
								.getKey().getName(), category.getKey());
				datastore.put(restaurantKeyEntity);
			}

			txn.commit();
		} finally {
			if (txn.isActive()) {
				txn.rollback();

				return null;
			}
		}

		return restaurantKey;
	}
}
