package com.mf;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.TimeUnit;
import java.util.HashMap;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.remote.http.HttpClient;
import org.openqa.selenium.remote.HttpCommandExecutor;
import org.openqa.selenium.remote.CommandInfo;

import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.HttpHost;

public class AOSWebTest {
    private static RemoteWebDriver driver;
    private static String SUTAddress = "http://www.advantageonlineshopping.com/";
    private static final boolean hasProxy = false;
    private static boolean isUsingTunnel = false;
    private static final String AOSuserName = "Shahar";           // YOUR AOS USER NAME
    private static final String AOSpassword = "Password1";        // YOUR AOS PASSWORD (CLEAR TEXT)
    private static String clientID;            // YOUR SRF CLIENT ID
    private static String clientSecret;    // YOUR SRF CLIENT SECRET
    private static URL SeleniumURL;
    private static String browser;
    private static DesiredCapabilities capabilities;

    @BeforeClass
    public static void openBrowser() {
        boolean isCloudExecution = (System.getenv("SELENIUM_ADDRESS") != null);
        try {
            System.out.println("Entering openBrowser(), init global params");

            Environment environment = new Environment();

            // Remote execution from IDE
            if (!isCloudExecution) {
                SRFRemoteExecution(environment);

            } else { // Cloud Execution
                SRFCloudExecution(environment);
                capabilities = environment.initBrowserCapabilities(browser);
            }

            // If using tunnel, use Nimbus AOS version and set the tunnel name
            if (isUsingTunnel)
                SUTAddress = "nimbusserver.aos.com:8000/#/";

            capabilities.setCapability("SRF_CLIENT_ID", clientID);
            capabilities.setCapability("SRF_CLIENT_SECRET", clientSecret);

            System.out.println("\n=== Print all environment vars right before creating a WD object ===");
            System.out.println(environment.AllEnvironmentToString());

            /*
            When accessing the web driver over proxy
            ===== This code was not tested =====
            */
            if (hasProxy && System.getenv("SELENIUM_ADDRESS") == null) {
                //URL srfGatewayUrl = new URL("https", "ftaas.saas.hpe.com", 443, "/wd/hub/");

                System.out.println("Creating remote web driver with address: " + SeleniumURL);

                String proxyHost = ""; //use your org proxy
                int proxyPort = 80;

                HttpClientBuilder builder = HttpClientBuilder.create();
                HttpHost driverProxy = new HttpHost(proxyHost, proxyPort);

                builder.setProxy(driverProxy);

                HttpClient.Factory factory = new MyHttpClientFactory(builder);
                HttpCommandExecutor executor = new HttpCommandExecutor(new HashMap<String, CommandInfo>(), SeleniumURL, factory);

                driver = new RemoteWebDriver(executor, capabilities);
            } else
                driver = new RemoteWebDriver(SeleniumURL, capabilities);

            driver.manage().timeouts().implicitlyWait(20, TimeUnit.SECONDS);
        } catch (Exception e) {
            System.out.println("Exception message: " +e.getMessage());
            e.printStackTrace();
        }
    }

    @Test
    public void OnlineShoppingE2E() throws java.io.IOException{

/*
        Properties props = new Properties();
        props.load(this.getClass().getResourceAsStream("project.properties"));
        String baseDir = props.get("project.basedir").toString();
        System.out.println("baseDir: " + baseDir + "\nSystem.getProperty(\"basedir\")" + System.getProperty("basedir"));
*/

        try {
            Actions builder = new Actions(driver);
            System.out.println("Navigeting to " + SUTAddress);
            driver.get(SUTAddress);

            System.out.println("Click Speakers category");
            driver.findElementByXPath("//*[@id=\"speakersImg\"]").click();

            // Select a speaker
            System.out.println("Select a Speaker");
            driver.findElementByXPath("/html/body/div[3]/section/article/div[3]/div/div/div[2]/ul/li[1]").click();

            // Click user icon
            System.out.println("Click user icon");
            driver.findElementByXPath("//*[@id=\"menuUser\"]").click();

            // Type user name
            System.out.println("Type user name");
            driver.findElementByXPath("/html/body/login-modal/div/div/div[3]/sec-form/sec-view[1]/div/input").sendKeys(AOSuserName);

            // Type password
            System.out.println("Type password");
            driver.findElementByXPath("/html/body/login-modal/div/div/div[3]/sec-form/sec-view[2]/div/input").sendKeys(AOSpassword);

            // Click Sign in
            System.out.println("Click Sign in");
            driver.findElementByXPath("//*[@id=\"sign_in_btnundefined\"]").click();

            windowSync(5000);

            // Click Add to cart
            System.out.println("Click Add to cart");
            driver.findElementByXPath("//*[@id=\"productProperties\"]/div[3]/button").click();

            // Click the cart icon
            System.out.println("Click the cart icon");
            driver.findElementByXPath("//*[@id=\"shoppingCartLink\"]").click();

            String totValue = driver.findElementByXPath("//*[@id=\"shoppingCart\"]/table/tfoot/tr[1]/td[2]/span[2]").getText();
            System.out.println("Total cart value = " + totValue);

            // Click Checkout
            System.out.println("Click Checkout");
            driver.findElementByXPath("//*[@id=\"checkOutButton\"]").click();

            // Click Next...
            System.out.println("Click Next...");
            driver.findElementByXPath("//*[@id=\"next_btn\"]").click();

            // Click Pay Now
            System.out.println("Click Pay Now");
            driver.findElementByXPath("//*[@id=\"pay_now_btn_MasterCredit\"]").click();

            windowSync(3000);
            System.out.println("Done!");

        } catch (Exception e) {
            System.out.printf("Exception message: %s\n", e.getMessage());
            e.printStackTrace();
            System.exit(0);
        }
    }

    @AfterClass
    public static void closeBrowser() {
        driver.quit();
    }

    private void windowSync(int milliseconds) throws InterruptedException {
        Thread.sleep(milliseconds);
    }

    private static void SRFRemoteExecution(Environment environment) throws java.io.IOException     {
        System.out.println("This is remote execution");
        environment.initParamsFromJSONConfigFile(System.getProperty("basedir") + "/src/main/resources/config.json");

        clientID = environment.getSrfClientId();            // YOUR SRF CLIENT ID
        clientSecret = environment.getSrfClientSecret();    // YOUR SRF CLIENT SECRET
        SeleniumURL = environment.getSeleniumURL();
        capabilities = environment.initBrowserCapabilities(environment.getBrowser());
        isUsingTunnel = environment.getIsUsingTunnel();

        capabilities.setCapability("tags", environment.getTags());
        capabilities.setCapability("build", environment.getBuildNumber());
        capabilities.setCapability("release", environment.getRelease());
        capabilities.setCapability("version", environment.getVersion());
        capabilities.setCapability("platform", environment.getPlatform());
        capabilities.setCapability("resolution", environment.getResolution());
        capabilities.setCapability("testName", environment.getTestName());
        capabilities.setCapability("browserName", environment.getBrowser());

        if (isUsingTunnel)
            capabilities.setCapability("tunnelName", environment.getTunnelName());
    }

    private static void SRFCloudExecution(Environment environment) throws MalformedURLException {
        System.out.println("This is a cloud execution");

        SeleniumURL = new URL(System.getenv("SELENIUM_ADDRESS"));
        clientID = System.getenv("SRF_CLIENT_ID");
        clientSecret = System.getenv("SRF_CLIENT_SECRET");
        isUsingTunnel = (System.getenv("usingTunnel") != null);
    }
}

