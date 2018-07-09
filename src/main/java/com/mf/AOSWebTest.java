package com.mf;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.TimeUnit;
import java.util.HashMap;
import java.io.FileReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import org.json.simple.parser.ParseException;

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
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

public class AOSWebTest {
    private static RemoteWebDriver driver;
    private static DesiredCapabilities capabilities;
    private static String AOSuserName = "Shahar";           // YOUR AOS USER NAME
    private static String AOSpassword = "Password1";           // YOUR AOS PASSWORD (CLEAR TEXT)
    private static String clientID = "";            // YOUR SRF CLIENT ID
    private static String clientSecret = "";        // YOUR SRF CLIENT SECRET
    private static URL SeleniumURL;
    private static String tunnelName = "";
    private static String SUTAddress = "http://www.advantageonlineshopping.com/";
    private static final boolean hasProxy = false;
    private static final boolean isUsingTunnel = false;

    @BeforeClass
    public static void openBrowser() throws MalformedURLException {
        System.out.println("Entering openBrowser(), init global params");
        String testName = "Selenium/Java-AOS-remote-exec";

        String [] tags = new String[2];
        tags[0] = "ContinuousTesting";
        tags[1] = "AOS-web";
        parseJsonConfig(System.getProperty("basedir") + "/src/main/resources/config.json");

        // Cloud Execution
        String remoteDriverAddr = System.getenv("SELENIUM_ADDRESS");
        if (remoteDriverAddr != null) {
            System.out.println("This is a cloud execution");
            SeleniumURL = new URL (remoteDriverAddr);
            clientID = System.getenv("SRF_CLIENT_ID");
            clientSecret = System.getenv("SRF_CLIENT_SECRET");
            testName = "Selenium/Java-AOS-cloud-exec";
        }

        capabilities = DesiredCapabilities.chrome();
        capabilities.setCapability("build", "175");
        capabilities.setCapability("release", "2018.08");
        capabilities.setCapability("tags", tags);

        capabilities.setCapability("version", "64");
        capabilities.setCapability("platform", "Windows 10");
        capabilities.setCapability("resolution", "1366x768");

        // If using tunnel, use Nimbus AOS version and set the tunnel name
        if (isUsingTunnel) {
            System.out.println("Using tunnel");
            SUTAddress = "nimbusserver.aos.com:8000";
            capabilities.setCapability("tunnelName", tunnelName);
        }

        System.out.println("set capabilities:\n" + "testName: " + testName + "\nSRF_CLIENT_ID: " + clientID
                + "\nSRF_CLIENT_SECRET: " + clientSecret);
        capabilities.setCapability("testName", testName);
        capabilities.setCapability("SRF_CLIENT_ID", clientID);
        capabilities.setCapability("SRF_CLIENT_SECRET", clientSecret);

        // this code was not tested //
        if (hasProxy && remoteDriverAddr==null) {
            URL srfGatewayUrl = new URL("https", "ftaas.saas.hpe.com", 443, "/wd/hub/");

            System.out.println("Creating remote web driver with address: " + srfGatewayUrl);

            String proxyHost = ""; //use your org proxy
            int proxyPort = 80;

            HttpClientBuilder builder = HttpClientBuilder.create();
            HttpHost driverProxy = new HttpHost(proxyHost, proxyPort);

            builder.setProxy(driverProxy);

            HttpClient.Factory factory = new MyHttpClientFactory(builder);
            HttpCommandExecutor executor = new HttpCommandExecutor(new HashMap<String, CommandInfo>(), srfGatewayUrl, factory);

            driver = new RemoteWebDriver(executor, capabilities);
        } else {
            System.out.println("Selenium web driver: " + SeleniumURL.toString());
            driver = new RemoteWebDriver(SeleniumURL, capabilities);
        }
        driver.manage().timeouts().implicitlyWait(20, TimeUnit.SECONDS);
    }

    @Test
    public void OnlineShoppingE2E() throws InterruptedException {
        Actions builder = new Actions(driver);
        System.out.println("Navigeting to " + SUTAddress);
        driver.get(SUTAddress);

        windowSync(8000);
        System.out.println("Click the search field");
        driver.findElementByXPath("//*[@id=\"menuSearch\"]").click();
        System.out.println("Type \"Speakers\"");
        driver.findElementByXPath("//*[@id=\"autoComplete\"]").sendKeys("Speakers");     // search speakers
        System.out.println("Select a speaker");
        driver.findElementByCssSelector("a.product:nth-child(3) > img:nth-child(1)").click();           // select a speaker

        System.out.println("Add to cart");
        WebElement addToCart = driver.findElementByCssSelector(".fixedBtn > button:nth-child(1)");      // add to cart
        builder.click(addToCart).build().perform();

        System.out.println("Start checking out flow");
        driver.findElementByXPath("//*[@id=\"checkOutPopUp\"]").click();                                // Check out
        System.out.println("Type user name and password");
        driver.findElementByXPath("/html/body/div[3]/section/article/div/div[1]/div/div[1]/sec-form/sec-view[1]/div/input").sendKeys(AOSuserName);
        driver.findElementByXPath("/html/body/div[3]/section/article/div/div[1]/div/div[1]/sec-form/sec-view[2]/div/input").sendKeys(AOSpassword);
        System.out.println("Start logging out flow");
        WebElement clickLogin = driver.findElementByXPath("//*[@id=\"login_btnundefined\"]");
        builder.click(clickLogin).build().perform();                                                  // Click log in
        windowSync(4000);
        driver.findElementByXPath("//*[@id=\"next_btn\"]").click();                             // Click Next
        driver.findElementByXPath("//*[@id=\"pay_now_btn_MasterCredit\"]").click();             // Click pay now
        System.out.println("Done!");
    }

    @AfterClass
    public static void closeBrowser() {
        driver.quit();
    }

    private void windowSync(int milliseconds) throws InterruptedException {
        Thread.sleep(milliseconds);
    }

    private static void parseJsonConfig(String filePath) {
        System.out.println("Parsing file: " + filePath);
        JSONParser parser = new JSONParser();

        try {
            JSONObject jsonObject = (JSONObject) parser.parse(new FileReader(filePath));
            tunnelName = jsonObject.get("tunnel_name").toString();
            clientID = jsonObject.get("CLIENT_ID").toString();
            clientSecret = jsonObject.get("CLIENT_SECRET").toString();
            AOSuserName = jsonObject.get("AOSUserName").toString();
            AOSpassword = jsonObject.get("AOSUserPassword").toString();
            SeleniumURL = new URL(jsonObject.get("SeleniumURL").toString());

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }

    }
}

