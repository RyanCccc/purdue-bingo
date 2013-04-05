package com.bingo.eatime.admin;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.bingo.eatime.core.Event;
import com.bingo.eatime.core.EventManager;
import com.bingo.eatime.core.Person;
import com.bingo.eatime.core.PersonAlreadyJoinException;
import com.bingo.eatime.core.PersonManager;
import com.bingo.eatime.core.Restaurant;
import com.bingo.eatime.core.RestaurantManager;
import com.google.appengine.api.datastore.Key;

public class EventAdminServlet extends HttpServlet {

	private static final long serialVersionUID = 8208066016833205326L;
	
	private static final Logger log = Logger.getLogger(EventAdminServlet.class.getName());
	
	// Status succeed
	private static final int STATUS_SUCCEED = 0;
	
	// Level 100 error - low priority error
	private static final int ERROR_UNKNOWN = 100;
	private static final int ERROR_ALREADY_JOINED = 101;
	
	// Level 200 error - argument error
	private static final int ERROR_MISSING_ARGUMENT = 200;
	private static final int ERROR_MALFORMED_ARGUMENT = 201;
	private static final int ERROR_USERNAME_NOT_FOUND = 202;
	private static final int ERROR_RESTAURANT_NOT_FOUND = 203;
	private static final int ERROR_CURRENT_USER_NOT_FOUND = 204;
	private static final int ERROR_UNKNOWN_ACTION = 299;
	
	// Level 300 error - database error
	private static final int ERROR_DATABASE_FAILED = 300;

	public void doGet(HttpServletRequest req, HttpServletResponse resp) {
		int status = STATUS_SUCCEED;
		List<String> usernameList = new ArrayList<String>();
		
		// Get arguments strings from parameters
		String action = req.getParameter("action");
		String eventName = req.getParameter("name");
		String restaurantKeyName = req.getParameter("restaurant");
		String creatorUsername = req.getParameter("username");
		String dateString = req.getParameter("date");
		String[] invitesUsername = req.getParameterValues("invite");
		String eventIdString = req.getParameter("id");
		
		// Parsing argument strings
		Date time = null;
		if (dateString != null) {
			try {
				long timeMillisecond = Long.valueOf(dateString);
				time = new Date(timeMillisecond);
			} catch (IllegalArgumentException e) {
				if (ERROR_MALFORMED_ARGUMENT > status) {
					status = ERROR_MALFORMED_ARGUMENT;
				}
				log.warning("Cannot parse date to long.");
			}
		}
		
		Person creator = null;
		if (creatorUsername != null) {
			creator = PersonManager.getPersonByUsername(creatorUsername);
			if (creator == null && ERROR_CURRENT_USER_NOT_FOUND > status) {
				status = ERROR_CURRENT_USER_NOT_FOUND;
			}
		}
		
		List<Person> invites = null;
		if (invitesUsername != null) {
			invites = new ArrayList<Person>();
			for (String inviteUsername : invitesUsername) {
				Person invite = PersonManager.getPersonByUsername(inviteUsername);
				if (invite == null && ERROR_USERNAME_NOT_FOUND > status) {
					status = ERROR_USERNAME_NOT_FOUND;
					usernameList.add(inviteUsername);
				} else {
					invites.add(invite);
				}
			}
		}
		
		Restaurant restaurant = null;
		if (restaurantKeyName != null) {
			log.info("Restaurant Key Name: " + restaurantKeyName);
			restaurant = RestaurantManager.getRestaurant(restaurantKeyName);
			if (restaurant == null && ERROR_RESTAURANT_NOT_FOUND > status) {
				status = ERROR_RESTAURANT_NOT_FOUND;
			}
		}
		
		Long eventId = null;
		if (eventIdString != null) {
			try {
				eventId = Long.valueOf(eventIdString);
			} catch (IllegalArgumentException e) {
				if (ERROR_MALFORMED_ARGUMENT > status) {
					status =  ERROR_MALFORMED_ARGUMENT;
				}
				log.warning("Cannot parse event id to interger.");
			}
		}
		
		// Conduct action
		Key eventKey = null;
		if (action != null) {
			if (action.equals("add")) {
				// Create new event
				if (status == STATUS_SUCCEED && eventName != null && 
						restaurant != null && creator != null && time != null) {
					Event event = Event.createEvent(eventName, restaurant, creator, time, invites);
					eventKey = EventManager.addEvent(event);
					if (eventKey == null && ERROR_DATABASE_FAILED > status) {
						status = ERROR_DATABASE_FAILED;
					}
				} else if (ERROR_MISSING_ARGUMENT > status) {
					status = ERROR_MISSING_ARGUMENT;
				}
			} else if (action.equals("invite")) {
				// Add new invites
				if (status == STATUS_SUCCEED && eventId != null && invites != null 
						&& restaurantKeyName != null) {
					for (Person invite : invites) {
						try {
							boolean result = EventManager.addInvite(invite, eventId, restaurantKeyName);
							if (result) {
								eventKey = Event.createKey(eventId, restaurantKeyName);
							} else if (ERROR_DATABASE_FAILED > status) {
								status = ERROR_DATABASE_FAILED;
							}
						} catch (PersonAlreadyJoinException e) {
							if (ERROR_ALREADY_JOINED > status) {
								status = ERROR_ALREADY_JOINED;
								usernameList.add(invite.getUsername());
							}
						}
					}
				} else if (ERROR_MISSING_ARGUMENT > status) {
					status = ERROR_MISSING_ARGUMENT;
				}
			} else if (action.equals("join")) {
				// Join to event
				if (status == STATUS_SUCCEED && creator != null && eventId != null
						&& restaurantKeyName != null) {
					Key joinEventKey = Event.createKey(eventId, restaurantKeyName);
					boolean result = EventManager.joinEvent(creator.getKey(), joinEventKey);
					if (result) {
						eventKey = joinEventKey;
					} else if (ERROR_DATABASE_FAILED > status) {
						status = ERROR_DATABASE_FAILED;
					}
				} else if (ERROR_MISSING_ARGUMENT > status) {
					status = ERROR_MISSING_ARGUMENT;
				}
			} else if (ERROR_UNKNOWN_ACTION > status) {
				// Unknown action
				status = ERROR_UNKNOWN_ACTION;
			}
		}
		
		// Write to client
		try {
			resp.setContentType("application/json");
			PrintWriter writer = resp.getWriter();
			if (status == STATUS_SUCCEED && eventKey != null) {
				String json = generateResponseJson(status, eventKey.getId(), null);
				log.info("Writing response: " + json);
				writer.print(json);
			} else {
				String json = generateResponseJson(status, 0, usernameList);
				log.info("Writing response: " + json);
				writer.print(json);
			}
		} catch (IOException e) {
			log.log(Level.SEVERE, "Cannot get print writer.", e);
		}
		
	}
	
	private String generateResponseJson(int status, long eventId, List<String> bundle) {
		if (status == STATUS_SUCCEED) {
			return "{ \"status\": " + status + ", \"id\": " + eventId + " }";
		} else {
			String reason = "";
			StringBuilder sb;
			switch(status) {
			case ERROR_UNKNOWN:
				reason = "Unknown error.";
				break;
			case ERROR_MISSING_ARGUMENT:
				reason = "Missing argument.";
				break;
			case ERROR_UNKNOWN_ACTION:
				reason = "Unknown action.";
				break;
			case ERROR_DATABASE_FAILED:
				reason = "Update database failed.";
				break;
			case ERROR_USERNAME_NOT_FOUND:
				sb = new StringBuilder();
				sb.append("Following username not found: ");
				for (String username : bundle) {
					sb.append(username);
					sb.append(", ");
				}
				sb.delete(sb.length() - 2, sb.length());
				sb.append(".");
				reason = sb.toString();
				break;
			case ERROR_CURRENT_USER_NOT_FOUND:
				reason = "Current user not found.";
				break;
			case ERROR_MALFORMED_ARGUMENT:
				reason = "Malformed argument.";
				break;
			case ERROR_RESTAURANT_NOT_FOUND:
				reason = "Restaurant not found.";
				break;
			case ERROR_ALREADY_JOINED:
				sb = new StringBuilder();
				sb.append("Following people already joined this event: ");
				for (String username : bundle) {
					sb.append(username);
					sb.append(", ");
				}
				sb.delete(sb.length() - 2, sb.length());
				sb.append(".");
				reason = sb.toString();
				break;
			default:
				break;
			}
			
			return "{ \"status\": " + status + ", \"reason\": \"" + reason + "\" }";
		}
	}

}
