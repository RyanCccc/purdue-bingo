package com.bingo.eatime.test;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.TreeSet;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.bingo.eatime.core.Category;
import com.bingo.eatime.core.CategoryManager;
import com.google.appengine.api.datastore.*;

public class DatabaseForFrontEndTest extends HttpServlet {

	private static final long serialVersionUID = 2639400166713434665L;

	private static final String TAG = "EaTimeDatabaseTestServlet";
	private static final String TAG_SPLITTER = ": ";

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) {
		try {
			resp.setContentType("text/html");
			PrintWriter writer = resp.getWriter();
			writer.println("<html><head></head><body><p>Testing Database...</p></body></html>");
		} catch (IOException e) {
			e.printStackTrace();
		}

		testDb();
	}

	private void testDb() {
		// TODO Auto-generated method stub
		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		boolean result = false;
		String cats[] = { "Chinese", "Japanese", "Korean", "Western" };
		for (String cat : cats) {
			Category categoryChinese = Category.createCategory(cat);
			result = CategoryManager.addCategory(categoryChinese);

			System.out.println(TAG + TAG_SPLITTER + "Add category " + categoryChinese + " " + result);
		}
	}
}