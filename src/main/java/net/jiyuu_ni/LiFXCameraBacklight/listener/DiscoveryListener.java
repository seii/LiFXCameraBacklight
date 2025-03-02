package net.jiyuu_ni.LiFXCameraBacklight.listener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.sarxos.webcam.WebcamDiscoveryEvent;
import com.github.sarxos.webcam.WebcamDiscoveryListener;

import net.jiyuu_ni.LiFXCameraBacklight.App;

public class DiscoveryListener implements WebcamDiscoveryListener {
	
	private static final Logger logger = LoggerFactory.getLogger(DiscoveryListener.class);

	public DiscoveryListener() {
		
	}

	@Override
	public void webcamFound(WebcamDiscoveryEvent event) {
		logger.trace("Entering webcamFound");
		logger.debug("Webcam connected: {}", event.getWebcam().getName());
		
		App.addActiveWebcam(event.getWebcam());
		
		logger.trace("Exiting webcamFound");
	}

	@Override
	public void webcamGone(WebcamDiscoveryEvent event) {
		logger.trace("Entering webcamGone");
		logger.debug("Webcam disconnected: {}", event.getWebcam().getName());
		
		App.removeActiveWebcam(event.getWebcam());
		
		logger.trace("Exiting webcamGone");
	}
}
