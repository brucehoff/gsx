# gsx

## GSX Module configuration application

### Download and run the executable jar file

- On a laptop device with wifi, ensure you have the Java Runtime Environment.  E.g., from a shell command line you should be able to:

```
java -version
java version "1.8.0_291"
```


- Visit [this page](https://github.com/brucehoff/gsx/releases/tag/main) and, under "Assets" find a link labeled,"gsxconfig.jar". Click to download.  This is an executable .jar file.  You should be able to double click on it and an application will start up.  Alternatively, from the command line:

```
java -jar /path/to/gsxconfig.jar

```

A small application will appear.


### Update your WiFi network name and password

- Turn off (unplug) the GSX module.  Flip the small switch. Turn it on (plug it in) again.
- On your laptop go to your wifi settings and find the module.  Selecting it will create an _ad hoc_ network between your laptop and the module.
- Now run the gsxconfig.jar app' as explained above.  Click 'Upload'.  This transfers the configuration from the module to your laptop.
- Enter the SSID (i.e., your network name).  
- Select the right security protocol for your wifi, like WPA2-PSK.
- Enter your wifi password.
- The checkboxes next to Trigger indicate which digital input port(s) is/are connected to the sensor(s).  Normally these should be left as they are.
- Click Download.
- The app' will show that the download has completed.
- Turn the module off (unplug it).  
- Switch the small switch from configure mode to run mode. 
- Turn the module on (plug it in) again.