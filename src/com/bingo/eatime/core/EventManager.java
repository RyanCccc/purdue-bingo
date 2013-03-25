package com.bingo.eatime.core;

import java.util.HashSet;
import java.util.TreeSet;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Transaction;
import com.google.appengine.api.datastore.TransactionOptions;

public class EventManager {

	public static Key addEvent(Event event) {
		DatastoreService datastore = DatastoreServiceFactory
				.getDatastoreService();

		TransactionOptions options = TransactionOptions.Builder.withXG(true);
		Transaction txn = datastore.beginTransaction(options);
		Key eventKey;
		try {
			Key restaurantKey = event.getRestaurantKey();
			if (restaurantKey == null) {
				txn.rollback();
				throw new NullKeyException("Restaurant Key is null.");
			}

			Entity eventEntity = new Entity(Event.KIND_EVENT, restaurantKey);
			eventEntity.setProperty(Event.PROPERTY_NAME, event.getName());
			eventEntity.setProperty(Event.PROPERTY_RESTAURANTKEY,
					event.getRestaurantKey());
			eventEntity.setProperty(Event.PROPERTY_TIME, event.getTime());

			Key creatorKey = event.getCreator().getKey();
			if (creatorKey == null) {
				txn.rollback();
				throw new NullKeyException("Creator Key is null.");
			}
			eventEntity.setProperty(Event.PROPERTY_CREATOR, creatorKey);

			eventKey = datastore.put(eventEntity);

			if (event.getInvites() != null) {
				for (Person person : event.getInvites()) {
					Key personKey = person.getKey();
					if (personKey == null) {
						txn.rollback();
						throw new NullKeyException("Person Key is null.");
					}

					Entity personKeyEntity = createPersonKeyEntity(personKey,
							eventKey);
					datastore.put(personKeyEntity);
				}
			}

			txn.commit();
		} finally {
			if (txn.isActive()) {
				txn.rollback();

				return null;
			}
		}

		return eventKey;
	}

	public static boolean addInvite(Person person, Key eventKey) {
		HashSet<Person> people = new HashSet<Person>();
		people.add(person);
		return addInvites(people, eventKey);
	}

	public static boolean addInvites(Iterable<Person> people, Key eventKey) {
		DatastoreService datastore = DatastoreServiceFactory
				.getDatastoreService();

		Transaction txn = datastore.beginTransaction();
		try {
			for (Person person : people) {
				Key personKey = person.getKey();
				if (personKey == null) {
					txn.rollback();
					throw new NullKeyException("Person Key is null.");
				}

				Entity personKeyEntity = createPersonKeyEntity(personKey,
						eventKey);
				datastore.put(personKeyEntity);
			}
			txn.commit();
		} finally {
			if (txn.isActive()) {
				txn.rollback();

				return false;
			}
		}

		return true;
	}

	/**
	 * Get invite Person entities from an event key.
	 * 
	 * @param eventKey
	 *            Event key.
	 * @return A Iterable of Entity. Null if failed.
	 */
	public static Iterable<Entity> getInviteEntities(Key eventKey) {
		DatastoreService datastore = DatastoreServiceFactory
				.getDatastoreService();

		Query q = new Query(Event.KIND_PERSONKEY, eventKey);

		PreparedQuery pq = datastore.prepare(q);

		HashSet<Entity> inviteEntities = new HashSet<Entity>();
		try {
			for (Entity entity : pq.asIterable()) {
				Key personKey = (Key) entity
						.getProperty(Event.PROPERTY_PERSONKEY);

				Entity personEntity = datastore.get(personKey);
				inviteEntities.add(personEntity);
			}
		} catch (EntityNotFoundException e) {
			return null;
		}

		return inviteEntities;
	}

	/**
	 * Get an ordered set of invite Person objects from an event key.
	 * 
	 * @param eventKey
	 *            Event key.
	 * @return A set of invite Person ordered by the name of the person. Null if
	 *         failed.
	 */
	public static TreeSet<Person> getInvitePeople(Key eventKey) {
		Iterable<Entity> inviteEntities = getInviteEntities(eventKey);
		if (inviteEntities != null) {
			TreeSet<Person> invitePeople = Person.createPeople(inviteEntities);

			return invitePeople;
		} else {
			return null;
		}
	}

	protected static Entity createPersonKeyEntity(Key personKey, Key eventKey) {
		Entity personKeyEntity = new Entity(Event.KIND_PERSONKEY, eventKey);
		personKeyEntity.setProperty(Event.PROPERTY_PERSONKEY, personKey);

		return personKeyEntity;
	}

}
