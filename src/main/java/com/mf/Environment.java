package com.mf;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.openqa.selenium.remote.DesiredCapabilities;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;

public class Environment {
    private String browser = "firefox";
    private String version = "latest";
    private String platform = "Windows 10";
    private String testName = "Selenium Burst JAVA";
    private String resolution = "800x600";
    private URL SeleniumURL;
    private String tunnelName;
    private String srfClientID;
    private String srfClientSecret;
    private String buildNumber;
    private String release;

    public Environment(String configFilePath) {
        initParamsFromJSONConfigFile(configFilePath);
    }

    public String AllEnvironmentToString(){
        return String.format("Test name: %s\nSRF_CLIENT_ID: %s\n" +
                        "SRF_CLIENT_SECRET: %s\n" + "SeleniumURL: %s\nBuild number: %s\nRelease: %s\n" +
                        "Browser: %s\nVersion: %s\nPlatform: %s\nResolutions: %s",
                getTestName(), getSrfClientId(), getSrfClientSecret(), getSeleniumURL(),
                getBuildNumber(), getRelease(), getBrowser(), getVersion(), getPlatform(), getResolution());
    }

    public String getBrowser() {
        return browser;
    }

    public void setBrowser(String browser) {
        this.browser = browser;
    }

    public String getVersion (){
        return this.version;
    }

    public void setVersion(String version){
        this.version = version;
    }

    public String getPlatform() {
        return this.platform;
    }

    public void setPlatform(String platform) { this.platform = platform; }

    public String getTestName(){
        return this.testName;
    }

    public void setTestName(String testName) {
        this.testName = testName;
    }

    public String getSrfClientId() {
        return srfClientID;
    }

    public void setSrfClientId(String srfClientID) {
        this.srfClientID = srfClientID;
    }

    public String getSrfClientSecret() {
        return srfClientSecret;
    }

    public void setSrfClientSecret(String srfClientSecret) {
        this.srfClientSecret = srfClientSecret;
    }

    public String getResolution() {
        return resolution;
    }

    public void setResolution(String resolution) {
        this.resolution = resolution;
    }

    public URL getSeleniumURL() { return SeleniumURL; }

    public void setSeleniumURL(URL SeleniumURL) { this.SeleniumURL = SeleniumURL; }

    public void setTunnelName(String tunnelName) { this.tunnelName = tunnelName; }

    public String getTunnelName() { return tunnelName; }

    public void setBuildNumber(String buildNumber) { this.buildNumber = buildNumber; }

    public String getBuildNumber() { return buildNumber; }

    public void setRelease(String release) { this.release = release; }

    public String getRelease() { return release; }


    // init the local variables from a json file
    private void initParamsFromJSONConfigFile(String filePath) {
        System.out.println("Parsing file: " + filePath);
        JSONParser parser = new JSONParser();

        try {
            JSONObject jsonObject = (JSONObject) parser.parse(new FileReader(filePath));
            setSeleniumURL(new URL(jsonObject.get("SeleniumURL").toString()));
            setTunnelName(jsonObject.get("tunnel_name").toString());
            setSrfClientId(jsonObject.get("CLIENT_ID").toString());
            setSrfClientSecret(jsonObject.get("CLIENT_SECRET").toString());
            setRelease(jsonObject.get("release").toString());
            setBrowser(jsonObject.get("browser").toString());
            setVersion(jsonObject.get("version").toString());
            setBuildNumber(jsonObject.get("buildNumber").toString());
            setPlatform(jsonObject.get("platform").toString());
            setResolution(jsonObject.get("resolution").toString());

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    // init the DesiredCapabilities object for the correct browser
    public DesiredCapabilities initBrowserCapabilities(String browser) {
        DesiredCapabilities capabilities;
        if(browser.toLowerCase().equals("chrome"))
            capabilities = DesiredCapabilities.chrome();
        else if (browser.toLowerCase().equals("firefox"))
            capabilities = DesiredCapabilities.firefox();
        else if (browser.toLowerCase().equals("ie"))
            capabilities = DesiredCapabilities.internetExplorer();
        else
            capabilities = initBrowserCapabilities(this.getBrowser());
        return capabilities;
    }
}
