package net.jiyuu_ni.LiFXCameraBacklight.config;

import java.beans.ConstructorProperties;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import net.jiyuu_ni.LiFXCameraBacklight.config.ScreenLayout.LayoutPosition;

public class ConfigFile {
	/**
	 *User-configured path to a log file
	 */
	private final Path LOG_FILE_PATH;
	
	/**
	 * User-configured network broadcast IP address
	 */
	private final String NETWORK_BROADCAST_ADDRESS;
	
	/**
	 * List of names representing webcams the user wishes to use
	 */
	private final List<String> WEBCAM_LIST;
	
	/**
	 * List of bindings between a piece of the screen and a light
	 */
	private final List<ScreenLayout> LAYOUT_LIST;
	
	/**
	 * Brightness detection settings
	 */
	private final BrightnessDetection BRIGHTNESS;
	
	/**
	 * Whether to show a preview GUI of the webcam(s)
	 */
	private final boolean SHOW_GUI;
	
	@ConstructorProperties({"log_path", "broadcast_ip_address", "webcam_list",
		"layout_list", "brightness_detection", "show_gui"})
	public ConfigFile(Path logPath, String ipAddress, List<String> webcamList,
			List<ScreenLayout> layoutList, BrightnessDetection brightness,
			boolean showGui) {
		super();
		this.LOG_FILE_PATH = logPath;
		this.NETWORK_BROADCAST_ADDRESS = ipAddress;
		this.WEBCAM_LIST = webcamList;
		this.LAYOUT_LIST = layoutList;
		this.BRIGHTNESS = brightness;
		this.SHOW_GUI = showGui;
	}
	
	@JsonProperty("log_path")
	public Path getLogPath() {
		return LOG_FILE_PATH;
	}
	
	@JsonProperty("broadcast_ip_address")
	public String getBroadcastIp() {
		return NETWORK_BROADCAST_ADDRESS;
	}

	@JsonProperty("webcam_list")
	public List<String> getWebcamList() {
		return WEBCAM_LIST;
	}

	@JsonProperty("layout_list")
	public List<ScreenLayout> getLayoutList() {
		return LAYOUT_LIST;
	}
	
	@JsonProperty("brightness_detection")
	public BrightnessDetection getBrightness() {
		return BRIGHTNESS;
	}

	@JsonProperty("show_gui")
	public boolean isGUI() {
		return SHOW_GUI;
	}
	
	@JsonIgnore
	public static ConfigFile createDefaultConfig() {
		// Use current working directory
		Path tempPath = Path.of("").toAbsolutePath();
		
		// Use "192.168.0.255" as default broadcast address
		String ipAddress = "192.168.0.255";
		
		String tempDeviceName = "Example Webcam 0";
		
		// Use dummy name for webcam name
		List<String> tempWebcamList = new ArrayList<String>(1);
		tempWebcamList.add(tempDeviceName);
		
		List<ScreenLayout> tempLayoutList = new ArrayList<ScreenLayout>(5);
		
		// Create each possible layout in the default config for demonstration
		for (LayoutPosition tempPosition : ScreenLayout.LayoutPosition.values()) {
			ScreenLayout tempLayout = new ScreenLayout(tempPosition, tempDeviceName,
					"00-NO-TR-EA-L0-00", false);
			tempLayoutList.add(tempLayout);
		}
		
		BrightnessDetection tempBright = new BrightnessDetection(false, 0.1f);
		
		return new ConfigFile(tempPath, ipAddress, tempWebcamList,
				tempLayoutList, tempBright, false);
	}

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("Path: ");
		sb.append(LOG_FILE_PATH.toString());
		sb.append("\nBroadcast Address: ");
		sb.append(NETWORK_BROADCAST_ADDRESS);
		sb.append("\nWebcam List:\n");
		
		for(String webcam : WEBCAM_LIST) {
			sb.append("  * ");
			sb.append(webcam);
		}
		
		sb.append("\nLayout List:\n");
		
		for(ScreenLayout layout : LAYOUT_LIST) {
			sb.append("  * ");
			sb.append(layout.toString());
		}
		
		sb.append("\nBrightness Detection:");
		sb.append("  * Enabled: ");
		sb.append(BRIGHTNESS.isEnabled());
		sb.append("\n  * Threshold: ");
		sb.append(BRIGHTNESS.getThreshold());
		
		sb.append("\nShow GUI: ");
		sb.append(SHOW_GUI);
		
		
		return sb.toString();
	}
}
