package com.mf;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.JSONArray;
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
    private String testName = "My Test";
    private String resolution = "800x600";
    private String tunnelName = "My Tunnel";
    private URL SeleniumURL;
    private String srfClientID;
    private String srfClientSecret;
    private String buildNumber;
    private String release;
    private boolean isUsingTunnel;
    private String[] tags;

    public Environment() { }

    public String AllEnvironmentToString(){
        return String.format("Test name: %s\nSRF_CLIENT_ID: %s\n" +
                        "SRF_CLIENT_SECRET: %s\n" + "SeleniumURL: %s\nBuild number: %s\nRelease: %s\n" +
                        "Browser: %s\nVersion: %s\nPlatform: %s\nResolutions: %s\nTunnel name: %s\n" +
                        "Using tunnel? %s\nTags: %s\n",
                getTestName(), getSrfClientId(), getSrfClientSecret(), getSeleniumURL(), getBuildNumber(),
                getRelease(), getBrowser(), getVersion(), getPlatform(), getResolution(), getTunnelName(),
                String.valueOf(getIsUsingTunnel()), java.util.Arrays.toString(getTags()));
    }

    public String getBrowser() { return browser; }
    public void setBrowser(String browser) {
        this.browser = browser;
    }
    public String getVersion (){ return this.version; }
    public void setVersion(String version){ this.version = version; }
    public String getPlatform() { return this.platform; }
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
    public void setIsUsingTunnel(boolean isUsingTunnel) { this.isUsingTunnel = isUsingTunnel;}
    public boolean getIsUsingTunnel() { return isUsingTunnel; }
    public void setTags(JSONArray tagsArray) {
        String[] tags = new String[tagsArray.size()];
        for (int i = 0; i < tagsArray.size(); i++) {
            tags[i] = (String)tagsArray.get(i);
        }
        this.tags = tags;
    }

    public String[] getTags() { return tags; }

    // init the local variables from a json file
    public void initParamsFromJSONConfigFile(String filePath) {
        System.out.println("Parsing file: " + filePath);
        JSONParser parser = new JSONParser();

        try {
            JSONObject jsonObject = (JSONObject) parser.parse(new FileReader(filePath));

            setSeleniumURL(new URL(jsonObject.get("SeleniumURL").toString()));
            setTunnelName(jsonObject.get("tunnel_name").toString());
            setSrfClientId(jsonObject.get("SRF_CLIENT_ID").toString());
            setSrfClientSecret(jsonObject.get("SRF_CLIENT_SECRET").toString());
            setRelease(jsonObject.get("release").toString());
            setBrowser(jsonObject.get("browser").toString());
            setVersion(jsonObject.get("version").toString());
            setBuildNumber(jsonObject.get("buildNumber").toString());
            setPlatform(jsonObject.get("platform").toString());
            setResolution(jsonObject.get("resolution").toString());
            setIsUsingTunnel(jsonObject.get("usingTunnel").toString().equals("true"));
            setTags((JSONArray)jsonObject.get("tags"));
            setTestName(jsonObject.get("testName").toString());

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
        System.out.println("Inside initBrowserCapabilities() - browser is " + browser);
        if (browser == null) browser = this.getBrowser();

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