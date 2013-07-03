package teammates.test.driver;

import static org.junit.Assert.assertEquals;

import javax.servlet.http.HttpServletRequest;

import teammates.logic.api.Logic;
import teammates.storage.datastore.Datastore;
import teammates.ui.controller.Action;
import teammates.ui.controller.ActionFactory;

import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalMailServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.google.appengine.tools.development.testing.LocalTaskQueueTestConfig;
import com.google.appengine.tools.development.testing.LocalUserServiceTestConfig;
import com.meterware.httpunit.PostMethodWebRequest;
import com.meterware.httpunit.WebRequest;
import com.meterware.servletunit.InvocationContext;
import com.meterware.servletunit.ServletRunner;
import com.meterware.servletunit.ServletUnitClient;

/** Provides a Singleton in-memory simulation of the GAE for unit testing.
 * This is not the same as testing against the dev server. When testing
 * against the GAE simulation, there is no need for the dev server to be up.
 * However, the GAE simulation does not support JSP and can only be used to
 * test up to Servlets level.
 */
public class GaeSimulation {
	

	public static GaeSimulation inst() {
		return instance;
	}

	private static final GaeSimulation instance = new GaeSimulation();
	
	
	/** This is used only to generate an HttpServletRequest for given parameters */
	protected  ServletUnitClient sc;
	
	protected  LocalServiceTestHelper helper;
	
	public synchronized void setup() {
		System.out.println("Setting up GAE simulation");
		
		LocalTaskQueueTestConfig localTasks = new LocalTaskQueueTestConfig();
		LocalUserServiceTestConfig localUserServices = new LocalUserServiceTestConfig();
		LocalDatastoreServiceTestConfig localDatastore = new LocalDatastoreServiceTestConfig();
		LocalMailServiceTestConfig localMail = new LocalMailServiceTestConfig();
		helper = new LocalServiceTestHelper(localDatastore, localMail,	localUserServices, localTasks);
		helper.setUp();
		
		Datastore.initialize();
		
		sc = new ServletRunner().newClient();
	}
	
	
	/**Logs in the user to the GAE simulation environment without admin rights.
	 */
	public void loginUser(String userId) {
		helper.setEnvIsLoggedIn(true);
		helper.setEnvEmail(userId);
		helper.setEnvAuthDomain("gmail.com");
		helper.setEnvIsAdmin(false);
	}

	/**Logs the current user out of the GAE simulation environment. 
	 */
	public void logoutUser() {
		helper.setEnvIsLoggedIn(false);
		helper.setEnvIsAdmin(false);
	}

	/**Logs in the user to the GAE simulation environment as an admin.
	 */
	public void loginAsAdmin(String userId) {
		loginUser(userId);
		helper.setEnvIsAdmin(true);
	}

	/**Logs in the user to the GAE simulation environment as an instructor
	 * (without admin rights). 
	 */
	public void loginAsInstructor(String userId) {
		loginUser(userId);
		Logic logic = new Logic();
		assertEquals(true, logic.getCurrentUser().isInstructor);
		assertEquals(false, logic.getCurrentUser().isAdmin);
	}

	/**Logs in the user to the GAE simulation environment as a student 
	 * (without admin rights or instructor rights).
	 */
	public void loginAsStudent(String userId) {
		loginUser(userId);
		Logic logic = new Logic();
		assertEquals(true, logic.getCurrentUser().isStudent);
		assertEquals(false, logic.getCurrentUser().isInstructor);
		assertEquals(false, logic.getCurrentUser().isAdmin);
	}
	
	/** 
	 * @param parameters Parameters that appear in a HttpServletRequest 
	 * received by the app.
	 * @return an {@link Action} object that matches the parameters given.
	 */
	public Action getActionObject(String uri, String... parameters) {
		HttpServletRequest req = createWebRequest(uri, parameters);
		return ActionFactory.getAction(req);
	}

	/** Refreshes the datastore by recreating it from scratch. */
	public void resetDatastore() {
		if(helper!=null){
			helper.tearDown();
		}
		helper.setUp();
	}

	public void tearDown() {
		helper.tearDown();
	}


	private HttpServletRequest createWebRequest(String uri, String... parameters){
		
		WebRequest request = new PostMethodWebRequest("http://localhost:8888" + uri);
		
		for (int i = 0; i < parameters.length; i = i + 2) {
			request.setParameter(parameters[i], parameters[i + 1]);
		}
	
		try {
			InvocationContext ic = sc.newInvocation(request);
			HttpServletRequest req = ic.getRequest();
			return req;
		} catch (Exception e) {
			throw new RuntimeException(e);
		} 
	}


}