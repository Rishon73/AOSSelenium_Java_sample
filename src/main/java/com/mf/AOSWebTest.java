package com.mf;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.TimeUnit;
import java.util.HashMap;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openqa.selenium.WebElement;
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
    private static final boolean isUsingTunnel = false;
    private static final String AOSuserName = "Shahar";           // YOUR AOS USER NAME
    private static final String AOSpassword = "Password1";        // YOUR AOS PASSWORD (CLEAR TEXT)
    private boolean errorInScript = false;

    @BeforeClass
    public static void openBrowser() throws MalformedURLException {
        System.out.println("Entering openBrowser(), init global params");
        Environment environment = new Environment(System.getProperty("basedir") + "/src/main/resources/config.json");

        String clientID = environment.getSrfClientId();            // YOUR SRF CLIENT ID
        String clientSecret = environment.getSrfClientSecret();    // YOUR SRF CLIENT SECRET
        URL SeleniumURL = environment.getSeleniumURL();
        String testName = environment.getTestName();
        String buildNumber = environment.getBuildNumber();
        String release = environment.getRelease();
        String browser = environment.getBrowser();
        String version = environment.getVersion();
        String platform = environment.getPlatform();
        String resolution = environment.getResolution();
        String[] tags = new String[]{"ContinuousTesting", "AOS-web"};

        System.out.println("\n=== Print all environment vars right after init variables ===");
        System.out.println(environment.AllEnvironmentToString());

        // Cloud Execution
        if (System.getenv("SELENIUM_ADDRESS") != null) {
            System.out.println("This is a cloud execution");

            SeleniumURL = new URL(System.getenv("SELENIUM_ADDRESS"));
            clientID = System.getenv("SRF_CLIENT_ID");
            clientSecret = System.getenv("SRF_CLIENT_SECRET");
            buildNumber = (System.getenv("build") != null) ? System.getenv("build") : buildNumber;
            release = (System.getenv("release") != null) ? System.getenv("release") : release;
        }

        DesiredCapabilities capabilities = environment.initBrowserCapabilities(browser);
        capabilities.setCapability("build", buildNumber);
        capabilities.setCapability("release", release);
        capabilities.setCapability("version", version);
        capabilities.setCapability("platform", platform);
        capabilities.setCapability("resolution", resolution);
        capabilities.setCapability("tags", tags);

        // If using tunnel, use Nimbus AOS version and set the tunnel name
        if (isUsingTunnel) {
            System.out.println("Using tunnel");
            SUTAddress = "nimbusserver.aos.com:8000";
            capabilities.setCapability("tunnelName", environment.getTunnelName());
        }

        capabilities.setCapability("testName", testName);
        capabilities.setCapability("SRF_CLIENT_ID", clientID);
        capabilities.setCapability("SRF_CLIENT_SECRET", clientSecret);

        System.out.println("\n=== Print all environment vars right before creating a WD object ===");
        System.out.println(environment.AllEnvironmentToString());

        /*
        When accessing the web driver over proxy
        This code was not tested
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
    }

    @Test
    public void OnlineShoppingE2E() throws InterruptedException {
        try {
            Actions builder = new Actions(driver);
            System.out.println("Navigeting to " + SUTAddress);
            driver.get(SUTAddress);

            System.out.println("Click Speakers category");
            driver.findElementByXPath("//*[@id=\"speakersImg\"]").click();

            // Select a speaker
            System.out.println("Select a Speaker");
            driver.findElementByXPath("/html/body/div[3]/section/article/div[3]/div/div/div[2]/ul/li[1]").click();

            // Add to cart
            System.out.println("Add to cart");
            WebElement addToCart = driver.findElementByCssSelector(".fixedBtn > button:nth-child(1)");
            builder.click(addToCart).build().perform();

            // Check-out
            System.out.println("Start checking out flow");
            driver.findElementByXPath("//*[@id=\"checkOutPopUp\"]").click();

            // Sign in
            System.out.println("Type user name and password");
            driver.findElementByXPath("/html/body/div[3]/section/article/div/div[1]/div/div[1]/sec-form/sec-view[1]/div/input").sendKeys(AOSuserName);
            driver.findElementByXPath("/html/body/div[3]/section/article/div/div[1]/div/div[1]/sec-form/sec-view[2]/div/input").sendKeys(AOSpassword);
            System.out.println("Start logging out flow");
            WebElement clickLogin = driver.findElementByXPath("//*[@id=\"login_btnundefined\"]");
            builder.click(clickLogin).build().perform();

            // Click Next and pay now
            windowSync(4000);
            driver.findElementByXPath("//*[@id=\"next_btn\"]").click();
            driver.findElementByXPath("//*[@id=\"pay_now_btn_MasterCredit\"]").click();

            System.out.println("Done!");

        } catch (Exception e) {
            System.out.printf("Exception message: %s\n", e.getMessage());
            e.printStackTrace();
        }
    }

    @AfterClass
    public static void closeBrowser() {
        driver.quit();
    }

    private void windowSync(int milliseconds) throws InterruptedException {
        Thread.sleep(milliseconds);
    }
}

