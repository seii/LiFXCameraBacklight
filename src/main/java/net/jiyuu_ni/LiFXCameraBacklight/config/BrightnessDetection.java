package net.jiyuu_ni.LiFXCameraBacklight.config;

import java.beans.ConstructorProperties;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

public class BrightnessDetection {
	// Should webcam attempt to mitigate motion detection with an additional
	//    check for images that are not bright enough to reliably indicate motion
	private final boolean ENABLED;
	
	// If the feature should be enabled, what threshold for brightness (between
	//    0 and 1) should be considered "not bright enough"?
	private final float THRESHOLD;
	
	@ConstructorProperties({"enabled", "threshold"})
	public BrightnessDetection(boolean enabled, float threshold) {
		this.ENABLED = enabled;
		this.THRESHOLD = threshold;
	}

	@JsonProperty("enabled")
	public boolean isEnabled() {
		return ENABLED;
	}

	@JsonProperty("threshold")
	public float getThreshold() {
		return THRESHOLD;
	}
	
	@JsonIgnore
	public static BrightnessDetection createDefaultConfig() {
		return new BrightnessDetection(true, 0.1f);
	}
}
