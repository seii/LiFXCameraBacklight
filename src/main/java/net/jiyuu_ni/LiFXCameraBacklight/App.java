package net.jiyuu_ni.LiFXCameraBacklight;

import java.awt.FlowLayout;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import javax.swing.JFrame;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.exc.StreamReadException;
import com.fasterxml.jackson.core.exc.StreamWriteException;
import com.fasterxml.jackson.databind.DatabindException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.WebcamMotionDetector;
import com.github.sarxos.webcam.WebcamPanel;
import com.github.sarxos.webcam.WebcamResolution;
import com.stuntguy3000.lifxlansdk.handler.PacketHandler;
import com.stuntguy3000.lifxlansdk.helper.MultiZoneHelper;
import com.stuntguy3000.lifxlansdk.object.product.Device;
import com.stuntguy3000.lifxlansdk.object.product.MultiZone;

import ch.qos.logback.classic.LoggerContext;
import net.jiyuu_ni.LiFXCameraBacklight.config.ConfigFile;
import net.jiyuu_ni.LiFXCameraBacklight.config.ScreenLayout;
import net.jiyuu_ni.LiFXCameraBacklight.config.ScreenLayout.LayoutPosition;

/**
 * Hello world!
 *
 */
public class App
{
	public static ConfigFile currentConfig;
	public static HashMap<String, MultiZone> multiZoneMap;
	
	private static List<Webcam> webcamList;
	private static List<WebcamMotionDetector> detectorList;
	
	private static final String CONFIG_PATH = "";
	private static final String CONFIG_NAME = "LiFXCameraBacklight.conf";
	private static final String LOG_PATH = "";
	private static final String LOG_NAME = "LiFXCameraBacklight.log";
	private static final int MOTION_SENSOR_INTERVAL = 250;
	private static final String NETWORK_BROADCAST_ADDRESS = "192.168.0.255";
	private static final String DEFAULT_MAC_ADDRESS = "00-NO-TR-EA-L0-00";
	private static final ObjectMapper jsonMapper = new ObjectMapper();
	private static final Logger logger = LoggerFactory.getLogger(App.class);
	
	// TODO: Watch / handle camera connection / disconnection
	// TODO: Separate logic so less is being done in "main"
	
	public App() {
		logger.trace("Entering constructor for App");
		
		// Ignore null values when writing json.
		jsonMapper.setDefaultPropertyInclusion(
				JsonInclude.Value.construct(Include.ALWAYS, Include.NON_NULL));
	    jsonMapper.setSerializationInclusion(Include.NON_NULL);
		
		logger.info("Searching for available webcams...");
		
		// Gathering all webcams is relatively heavy, start it immediately
		webcamList = Webcam.getWebcams();
		
		logger.info("Discovered the following available webcams:");
		
		for(Webcam webcam : webcamList) {
			logger.info("- {}", webcam.getName());
		}
		
		String tempIp = NETWORK_BROADCAST_ADDRESS;
		
		Path tempPath = Path.of(CONFIG_PATH, CONFIG_NAME);
		
		// Check for existing configuration file
		if(Files.exists(tempPath, LinkOption.NOFOLLOW_LINKS)) {
			logger.info("Configuration file found at {}", tempPath.toString());
		    currentConfig = loadConfig();
		    
		    initBroadcast(tempIp);
		    
		    // Check for available (and compatible) LiFX devices
			populateMultiZoneMap();
			
			if(multiZoneMap == null || multiZoneMap.isEmpty()) {
				logger.error("No multi-zone compatible LiFX devices found!");
			}
		    
		    // Get list of user's selected webcam names
		    List<String> tempWebcamList = currentConfig.getWebcamList();
		    
		    if(tempWebcamList != null && tempWebcamList.size() > 0) {
		    	webcamList = new ArrayList<Webcam>(tempWebcamList.size());
			    
		    	// Populate actual webcam objects from the provided names
			    for(String name : tempWebcamList) {
			    	logger.info("Attempting to access {}", name);
			    	Webcam tempWebcam = Webcam.getWebcamByName(name);
			    	
			    	if(tempWebcam != null) {
			    		logger.info("Successfully accessed {}", tempWebcam.getName());
			    		webcamList.add(tempWebcam);
			    	}
			    }
		    }
		    
		    // Check for user-defined layouts linking a camera to a device
		    if(currentConfig.getLayoutList() != null &&
		    		!currentConfig.getLayoutList().isEmpty()) {
		    	HashMap<String, MultiZone> tempMultiZoneMap =
		    			new HashMap<String, MultiZone>(1);
		    	
		    	for(ScreenLayout layout : currentConfig.getLayoutList()) {
		    		tempMultiZoneMap.put(layout.getMultiZoneMAC(),
		    				multiZoneMap.get(layout.getMultiZoneMAC()));
		    	}
		    	
		    	multiZoneMap = tempMultiZoneMap;
		    }else {
		    	multiZoneMap = new HashMap<String, MultiZone>(0);
		    	
		    	logger.error("No layouts were defined in the configuration. " +
		    			"Camera(s) will still initialize, but no signals will be sent " +
		    			"to any LiFX device.");
		    }
		    
		    tempWebcamList = null;
		}
		// Create default configuration file if none exists
		else {
			currentConfig = ConfigFile.createDefaultConfig();
			logger.debug(currentConfig.toString());
			
			initBroadcast(tempIp);
			
			// Check for available (and compatible) LiFX devices
			populateMultiZoneMap();
			
			if(multiZoneMap == null || multiZoneMap.isEmpty()) {
				logger.error("No multi-zone compatible LiFX devices found!");
			}
			
			logger.info("No configuration file found, example will be created at {}",
					Path.of(currentConfig.getLogPath().toString(), CONFIG_NAME)
					.toString());
			
			if(webcamList != null && !webcamList.isEmpty()) {
				webcamList = new ArrayList<Webcam>(1);
				webcamList.add(Webcam.getDefault());
				
				MultiZone tempMultiZone = null;
				
				// See if any legitimate LiFX device were detected
				//    and return the first valid one if so
				if(multiZoneMap != null && !multiZoneMap.isEmpty()) {
					for(String mac : multiZoneMap.keySet()) {
						if(!mac.equals(DEFAULT_MAC_ADDRESS)) {
							tempMultiZone = multiZoneMap.get(mac);
						}
					}
				}
				
				ScreenLayout tempScreenLayout = null;
				
				// If there is a valid LiFX device, default to registering it
				//    as a CENTER layout to the webcam so a demonstration is
				//    always possible, even without a configuration file
				if(tempMultiZone != null) {
					logger.info("Setting default configuration to use the first " +
							"compatible LiFX device detected ({}) in CENTER " +
							"layout using the default webcam {}",
							tempMultiZone.getMacAddress(), webcamList.get(0).getName());
					
					tempScreenLayout = new ScreenLayout(
							LayoutPosition.CENTER, webcamList.get(0).getName(),
							tempMultiZone.getMacAddress());
				}
				// If there aren't any LiFX devices, populate CENTER with a known
				//    fake MAC address
				else {
					tempScreenLayout = new ScreenLayout(
							LayoutPosition.CENTER, webcamList.get(0).getName(),
							DEFAULT_MAC_ADDRESS);
				}
				
				logger.info("To configure this program, copy any of the webcam " +
						"names and LiFX information (already detected above) to " +
						"fill out a valid configuration file. Once configured, " +
						"restart this program.",
						webcamList.get(0).getName());
				
				// Make sure internal config is rewritten to be consistent
				//    with the "using only default webcam" and "first detected"
				//    LiFX device or fake values" approach
				currentConfig = new ConfigFile(Path.of(""), NETWORK_BROADCAST_ADDRESS,
						Collections.singletonList(webcamList.get(0).getName()),
						Collections.singletonList(tempScreenLayout), false);
				
				// Save the default config, which if at all possible now
				//    contains some real cameras and devices detected on
				//    the user's system and network
				saveConfig();
			}else {
				logger.error("No available webcams discovered on this system!");
			}
		}
		
		// Set up motion detectors on any available webcams
		if(webcamList != null && !webcamList.isEmpty()) {
			// Initialize detector list before trying to set any up
			detectorList = new ArrayList<WebcamMotionDetector>(0);
			
			for(Webcam webcam : webcamList) {
				logger.info("Setting webcam view size");
		    	webcam.setViewSize(WebcamResolution.VGA.getSize());
				
				logger.info("Setting up motion detector for webcam {} with " +
						"interval of {} milliseconds between motion triggers",
						webcam.getName(), MOTION_SENSOR_INTERVAL);
				WebcamMotionDetector detector = new WebcamMotionDetector(webcam);
				// Interval in milliseconds between triggers
				// TODO: Allow this to be user-configured
				detector.setInterval(MOTION_SENSOR_INTERVAL);
				detector.addMotionListener(new CamListener());
				detectorList.add(detector);
			}
		}
		
		logger.trace("Exiting constructor for App");
	}

	public static void main( String[] args ) throws InterruptedException
    {
    	logger.trace("Entering main");
    	
    	App currentInstance = new App();
  	
    	for(WebcamMotionDetector detector : detectorList) {
    		logger.info("Starting motion detector for {}",
    				detector.getWebcam().getName());
			detector.start();
    	}
    	
    	if(currentConfig != null && currentConfig.isGUI()) {
    		showUI();
    	}else {
    		for(Webcam webcam : webcamList) {
        		logger.info("Activating webcam {}", webcam.getName());
        		webcam.open();
        	}
    		
    		while(webcamList != null && !webcamList.isEmpty()) {
        		Thread.sleep(50);
        	}
    	}
    	
    	logger.trace("Exiting main");
    	
    	// assume SLF4J is bound to logback-classic in the current environment
    	LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
    	loggerContext.stop();
    }
	
	private void initBroadcast(String tempIp) {
		// Set network broadcast address used to send UDP discovery
		//    packets when trying to discover LiFX devices
		try {
			// No actual MAC address validity checking, but at least
			//    sanity checking that the entry isn't null and is at least
			//    8 characters long (Format: x.x.x.x)
			if(currentConfig != null && currentConfig.getBroadcastIp() != null &&
					currentConfig.getBroadcastIp().length() > 7) {
				tempIp = currentConfig.getBroadcastIp();
			}
			
			logger.info("Setting network broadcast address to {}",
					tempIp);
		    PacketHandler.setBroadcastAddress(
		    		InetAddress.getByName(tempIp));
		} catch (UnknownHostException e) {
		    e.printStackTrace();
		}
	}
	
	private void populateMultiZoneMap() {
		multiZoneMap = new HashMap<String, MultiZone>(0);
		
		logger.info("Detecting LiFX devices, this may take a minute...");
		logger.info("The following devices are multizone-compatible:");
		
		for(MultiZone multi : MultiZoneHelper.findMultiZones()) {
			logger.info("Name: {}, MAC: {}", multi.getLabel(),
					multi.getMacAddress());
			multiZoneMap.put(multi.getMacAddress(), multi);
		}
	}
    
    private static void showUI() {
    	
    	logger.trace("Entering showUI");
		
		List<WebcamPanel> panels = new ArrayList<WebcamPanel>();
		JFrame window = new JFrame("Webcam");
		
		window.setLayout(new FlowLayout());

		for (Webcam webcam : webcamList) {
			WebcamPanel panel = new WebcamPanel(webcam, false);

			panels.add(panel);
			window.add(panel);
		}

		window.setTitle("Few Cameras At Once");
		window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		window.pack();
		window.setVisible(true);

		for(WebcamPanel panel : panels) {
			panel.start();
		}
		
		logger.trace("Exiting showUI");
    }
	
	private ConfigFile loadConfig() {
		logger.trace("Entering loadConfig");
		
		ConfigFile result = null;
		
		try {
			result = jsonMapper.readValue(
					Path.of(CONFIG_PATH, CONFIG_NAME).toFile(), ConfigFile.class);
		} catch (StreamReadException e) {
			e.printStackTrace();
		} catch (DatabindException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		logger.trace("Exiting loadConfig");
		
		return result;
	}
	
	private void saveConfig() {
		logger.trace("Entering saveConfig");
		logger.debug("{}", currentConfig.toString());
		
		try {
			jsonMapper.writerWithDefaultPrettyPrinter().writeValue(
					Path.of(CONFIG_PATH, CONFIG_NAME).toFile(), currentConfig);
		} catch (StreamWriteException e) {
			e.printStackTrace();
		} catch (DatabindException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		logger.trace("Exiting saveConfig");
	}
}
