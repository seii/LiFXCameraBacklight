package net.jiyuu_ni.LiFXCameraBacklight.config;

import java.beans.ConstructorProperties;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ScreenLayout {
	private final LayoutPosition position;
	private final String cameraName;
	private final String multiZoneMAC;
	
	public enum LayoutPosition {
		CENTER, TOP, BOTTOM, LEFT, RIGHT;
	}
	
	@ConstructorProperties({"position","camera_name","device_mac_address"})
	public ScreenLayout(LayoutPosition position, String name, String multiZoneMAC) {
		super();
		this.position = position;
		this.cameraName = name;
		this.multiZoneMAC = multiZoneMAC;
	}

	@JsonProperty("position")
	public LayoutPosition getPosition() {
		return position;
	}
	
	@JsonProperty("camera_name")
	public String getCameraName() {
		return cameraName;
	}

	@JsonProperty("device_mac_address")
	public String getMultiZoneMAC() {
		return multiZoneMAC;
	}

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		
		sb.append("Position: ");
		sb.append(position);
		sb.append("\n  * ");
		sb.append("Camera Name: ");
		sb.append(cameraName);
		sb.append("\n  * ");
		sb.append("Multi Zone MAC: ");
		sb.append(multiZoneMAC);
		sb.append("\n");
		
		return sb.toString();
	}
}
