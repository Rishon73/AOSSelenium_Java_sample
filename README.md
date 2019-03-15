# AOSSelenium_Java_sample

Simple Java Selenium script to work against AOS.
<br>Based on the Java sample (non-proxy) in <A href="https://admhelp.microfocus.com/srf/en/1.20/Content/remote-sel.htm#hp-minitoc-item-1" target="_blank">SRF documentation</a>. 

* The script will execute on SRF. 
* Note the ___src/main/resources/config.json___ config file, you will need to make some modifications:
  * Required - modify __SRF_CLIENT_ID__ and ___SRF_CLIENT_SECRET__ to real values
  * Required - add __AOSUserName__ and update the __AOSUserPassword__ accordingly
  * Optional - modify __usingTunnel__ to false/true. If false, will not use the tunnel and the test will run against https://advantageonlineshopping.com/#/. If true, the __tunnel_name__ will be used and the test will run against [nimbusserver.aos.com:8000](http://nimbusserver.aos.com:8000)
  * The rest are optional
