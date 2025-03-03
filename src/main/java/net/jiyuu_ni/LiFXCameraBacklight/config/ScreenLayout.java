package net.jiyuu_ni.LiFXCameraBacklight.config;

import java.beans.ConstructorProperties;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ScreenLayout {
	/**
	 * Description of which screen position this layout refers to
	 */
	private final LayoutPosition position;
	
	/**
	 * Name of the camera to associate with this layout
	 */
	private final String cameraName;
	
	/**
	 * MAC address of the LiFX device to associate with this layout
	 */
	private final String multiZoneMAC;
	
	/**
	 * Focus is placed on the `CENTER`, `TOP`, `BOTTOM`, `LEFT`, or `RIGHT`
	 * of the image
	 */
	public enum LayoutPosition {
		CENTER, TOP, BOTTOM, LEFT, RIGHT;
	}
	
	/**
	 * Whether to reverse the lighting's direction for this layout
	 */
	private final boolean reverse;
	
	@ConstructorProperties({"position","camera_name","device_mac_address","reverse"})
	public ScreenLayout(LayoutPosition position, String name, String multiZoneMAC,
			boolean reverse) {
		super();
		this.position = position;
		this.cameraName = name;
		this.multiZoneMAC = multiZoneMAC;
		this.reverse = reverse;
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
	
	@JsonProperty("reverse")
	public boolean getReverse() {
		return reverse;
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
		sb.append("Reverse: ");
		sb.append(reverse);
		sb.append("\n");
		
		return sb.toString();
	}
}
