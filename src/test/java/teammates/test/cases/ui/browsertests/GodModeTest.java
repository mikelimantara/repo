package teammates.test.cases.ui.browsertests;

import static org.testng.AssertJUnit.assertNull;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import teammates.common.util.FileHelper;
import teammates.test.driver.HtmlHelper;
import teammates.test.driver.TestProperties;
import teammates.test.pageobjects.AppPage;
import teammates.test.pageobjects.Browser;
import teammates.test.pageobjects.BrowserPool;

public class GodModeTest extends BaseUiTestCase {
    
    private static final String PLACEHOLDER_CONTENT = "<div id=\"mainContent\">test</div>";
    private static final String OUTPUT_FILENAME = "/godmodeOutput.html";
    private static final String OUTPUT_PATH = TestProperties.TEST_PAGES_FOLDER + OUTPUT_FILENAME;
    private static final String ACTUAL_FILENAME = "/godmode.html";
    private static final String ACTUAL_PATH = TestProperties.TEST_PAGES_FOLDER + ACTUAL_FILENAME;
    private static final String EXPECTED_FILENAME = "/godmodeExpectedOutput.html";
    private static final String EXPECTED_PART_FILENAME = "/godmodeExpectedPartOutput.html";
    
    private static Browser browser;
    private static AppPage page;
    private static String initialContent;

    @BeforeClass
    public static void classSetUp() throws IOException {
        printTestClassHeader();
        TestProperties.inst().verifyReadyForGodMode();
        injectContextDependentValuesIntoActualFile();
        browser = BrowserPool.getBrowser();
        page = AppPage.getNewPageInstance(browser).navigateTo(createLocalUrl(ACTUAL_FILENAME));
    }
    
    private static void injectContextDependentValuesIntoActualFile() throws IOException {
        initialContent = FileHelper.readFile(ACTUAL_PATH);
        String changedContent = HtmlHelper.injectContextDependentValuesForTest(initialContent);
        writeToFile(ACTUAL_PATH, changedContent);
    }

    private static void writeToFile(String filePath, String content) throws IOException {
        FileWriter output = new FileWriter(new File(filePath));
        output.write(content);
        output.close();
    }

    @Test
    public void testGodMode() throws IOException {
        
        System.clearProperty("godmode");
        assertNull(System.getProperty("godmode"));
        
        ______TS("test verifyHtml");
        
        testGodMode(true);
        
        ______TS("test verifyHtmlMainContent");
        
        testGodMode(false);
        
    }
    
    private void testGodMode(boolean isPart) throws IOException {
        // run the God mode with non-existent expected file
        runGodModeRoutine(isPart);
        
        writeToFile(OUTPUT_PATH, PLACEHOLDER_CONTENT);
        
        // run the God mode with wrong content in expected file
        runGodModeRoutine(isPart);
        
        // delete the output file generated
        File file = new File(OUTPUT_PATH);
        if (!file.delete()) {
            print("Delete failed: " + file.getAbsolutePath());
            file.deleteOnExit();
        }
    }
    
    private void runGodModeRoutine(boolean isPart) throws IOException {
        
        try {
            // should fail as the expected file "godmodeOutput.html"
            // either does not exist or has the wrong content
            verifyHtml(OUTPUT_FILENAME, isPart);
            signalFailureToDetectException();
        } catch (IOException|AssertionError e) {
            ignoreExpectedException();
        }
        
        System.setProperty("godmode", "true");
        // automatically generates the file and hence passes
        verifyHtml(OUTPUT_FILENAME, isPart);
        
        System.clearProperty("godmode");
        assertNull(System.getProperty("godmode"));
        
        // should pass without need for godmode as the file has already been generated
        verifyHtml(OUTPUT_FILENAME, isPart);
        
        String expectedOutputPage = FileHelper.readFile(TestProperties.TEST_PAGES_FOLDER
                                                        + (isPart ? EXPECTED_PART_FILENAME : EXPECTED_FILENAME));
        String actualOutputPage = FileHelper.readFile(OUTPUT_PATH);
        
        // ensure that the generated file is as expected
        HtmlHelper.assertSameHtml(expectedOutputPage, actualOutputPage, isPart);
        
    }

    private void verifyHtml(String filePath, boolean isPart) throws IOException {
        if (isPart) {
            page.verifyHtmlMainContent(filePath);
        } else {
            page.verifyHtml(filePath);
        }
    }

    @AfterClass
    public static void classTearDown() throws IOException {
        BrowserPool.release(browser);
        System.clearProperty("godmode");
        writeToFile(ACTUAL_PATH, initialContent);
    }

}
