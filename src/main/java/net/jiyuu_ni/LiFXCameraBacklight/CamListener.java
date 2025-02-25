package net.jiyuu_ni.LiFXCameraBacklight;

import java.awt.image.BufferedImage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.WebcamMotionEvent;
import com.github.sarxos.webcam.WebcamMotionListener;
import com.stuntguy3000.lifxlansdk.object.product.MultiZone;
import com.stuntguy3000.lifxlansdk.object.protocol.Color;

import net.jiyuu_ni.LiFXCameraBacklight.config.ScreenLayout;
import net.jiyuu_ni.LiFXCameraBacklight.util.ColorUtil;

public class CamListener implements WebcamMotionListener {
	
	private static final Logger logger = LoggerFactory.getLogger(CamListener.class);

	public CamListener() {
		super();
	}
	
	@Override
	public void motionDetected(WebcamMotionEvent wme) {
		logger.trace("Entering motionDetected");
		
		// Determine which webcam the motion came from
		Webcam webcam = wme.getWebcam();
		
		logger.debug("Motion detected by webcam {}", webcam.getName());
		
		// Convert webcam to BufferedImage
    	BufferedImage image = webcam.getImage();
    	
    	if(image != null) {
    		if(App.multiZoneMap != null && !App.multiZoneMap.isEmpty()) {
    			// Check each layout for any that match the webcam which detected motion
        		for(ScreenLayout layout : App.currentConfig.getLayoutList()) {
        			if(webcam.getName().equals(layout.getCameraName())) {
        				// Get a reference to the specific multi zone-compatible device
        				MultiZone tempZone = App.multiZoneMap.get(
        						layout.getMultiZoneMAC());
        				
        				if(tempZone != null) {
        					logger.debug("Calculating new colors for {}",
            						layout.getMultiZoneMAC());
            				
            				// Calculate colors to set
            				Color[] tempColors = ColorUtil.getColorFromImage(image,
            						layout.getPosition(),
            						tempZone.getZonesCount());
            				
            				logger.debug("Setting multi-zone colors for {}",
            						layout.getMultiZoneMAC());
            				tempZone.setExtendedColorZones(0, 0, false, tempColors);
        				}else {
        					logger.error("Unable to set colors for device MAC {} " +
        							"using camera {} as that MAC did not match any " +
        							"detected LiFX device.",
        							layout.getMultiZoneMAC(), layout.getCameraName());
        				}
        			}
        		}
    		}else {
    			logger.error("No LiFX devices found or no layouts defined in " +
    					"configuration file. Please attach a multi-zone compatible " +
    					"LiFX device and/or define a layout using at least one device" +
    					"in the configuration file.");
    		}
    	}else {
    		logger.warn("No image was returned from the webcam, so no color " +
    				"information will be sent to LiFX devices.");
    	}
    	
    	logger.trace("Exiting motionDetected");
	}

}
