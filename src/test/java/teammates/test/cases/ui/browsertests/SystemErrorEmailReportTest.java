package teammates.test.cases.ui.browsertests;

import java.util.logging.Level;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import teammates.common.BuildProperties;
import teammates.common.Common;
import teammates.common.exception.EntityDoesNotExistException;
import teammates.common.exception.UnauthorizedAccessException;
import teammates.logic.Emails;
import teammates.test.driver.Url;
import teammates.test.pageobjects.AppPage;
import teammates.test.pageobjects.Browser;
import teammates.test.pageobjects.BrowserPool;

import com.google.apphosting.api.DeadlineExceededException;

/**
 * Triggers various system errors that in turn triggers email error reports
 * to the admin. When run against a production server, this class triggers 
 * three emails to the admin.
 */
public class SystemErrorEmailReportTest extends BaseUiTestCase {
	private static Browser browser;
	private static AppPage page;

	@BeforeClass
	public static void classSetUp() throws Exception {
		printTestClassHeader();
		setGeneralLoggingLevel(Level.WARNING);
		setLogLevelOfClass(Emails.class, Level.FINE);
		setConsoleLoggingLevel(Level.FINE);

		browser = BrowserPool.getBrowser();
		page = loginAdmin(browser);

	}

	@Test
	public void testAssertionError() {
		
		______TS("AssertionError testing");
		
		Url url = new Url(Common.PAGE_ADMIN_EXCEPTION_TEST)
				.withParam(Common.PARAM_ERROR, AssertionError.class.getSimpleName());
		page.navigateTo(url);
		print("AssertionError triggered, please check your crash report at "
				+ BuildProperties.inst().getAppCrashReportEmail());
	}
	
	@Test
	public void testEntityDoesNotExistException() {
		
		______TS("EntityDoesNotExistException testing");
		
		Url url = new Url(Common.PAGE_ADMIN_EXCEPTION_TEST)
			.withParam(Common.PARAM_ERROR, EntityDoesNotExistException.class.getSimpleName());
		page.navigateTo(url);
		print("This exception is handled by system, make sure you don't receive any emails. ");
	}
	
	@Test
	public void testNullPointerException() {
		
		______TS("NullPointerException testing");
		
		Url url = new Url(Common.PAGE_ADMIN_EXCEPTION_TEST)
			.withParam(Common.PARAM_ERROR, NullPointerException.class.getSimpleName());
		page.navigateTo(url);
		print("NullPointerException triggered, please check your crash report at " + BuildProperties.inst().getAppCrashReportEmail());	
	}
	
	@Test
	public void testDeadlineExceededException() throws Exception {
		
		______TS("Deadline Exceeded testing");
		
		Url url = new Url(Common.PAGE_ADMIN_EXCEPTION_TEST)
			.withParam(Common.PARAM_ERROR, DeadlineExceededException.class.getSimpleName());
		page.navigateTo(url);
		print("DeadlineExceededException triggered, please check your crash report at " + BuildProperties.inst().getAppCrashReportEmail());	
		
		______TS("DeadlineExceededException error view");
		
		page.verifyHtml(Common.TEST_PAGES_FOLDER+"/deadlineExceededErrorPage.html");
		
	}
	
	//TODO: this test should be moved to the class testing access control
	@Test
	public void testUnauthorizedAccessException() {
		
		______TS("UnauthorizedAccessException testing");
		
		Url url = new Url(Common.PAGE_ADMIN_EXCEPTION_TEST)
			.withParam(Common.PARAM_ERROR, UnauthorizedAccessException.class.getSimpleName());
		page.navigateTo(url);
		print("This exception is handled by system, make sure you don't receive any emails. ");
	}


	@AfterClass()
	public static void classTearDown() throws Exception {
		printTestClassFooter();
		setLogLevelOfClass(Emails.class, Level.WARNING);
		setConsoleLoggingLevel(Level.WARNING);
		BrowserPool.release(browser);
	}
}