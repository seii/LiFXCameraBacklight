package net.jiyuu_ni.LiFXCameraBacklight.util;

import java.awt.image.BufferedImage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.stuntguy3000.lifxlansdk.object.protocol.Color;

import net.jiyuu_ni.LiFXCameraBacklight.config.ScreenLayout.LayoutPosition;

public class ColorUtil {
	private static final Logger logger = LoggerFactory.getLogger(ColorUtil.class);
	
	/**
	 * Get average color, in RGB values, from a specified portion of an image.
	 * NOTE: All range numbers are inclusive
	 * @param bi Image to be analyzed
	 * @param xMin Minimum X coordinate (row start) of area to be analyzed
	 * @param xMax Maximum X coordinate (row end) of area to be analyzed
	 * @param yMin Minimum Y coordinate (column start) of area to be analyzed
	 * @param yMax Maximum Y coordinate (column end) of area to be analyzed
	 * @return One `java.awt.Color`, in RGB values
	 */
	private static java.awt.Color getAverageColor(BufferedImage bi, int xMin,
			int xMax, int yMin, int yMax) {
		logger.trace("Entering getAverageColor");
		
		long sumr = 0, sumg = 0, sumb = 0;
		
		// With the coordinates established, capture all relevant pixels into
		//    a matrix whose every element contains all three of R, G, and B values
		for(int x = xMin; x < xMax; x++) {
			for(int y = yMin; y < yMax; y++) {
				java.awt.Color pixel = new java.awt.Color(bi.getRGB(x, y));
                sumr += pixel.getRed();
                sumg += pixel.getGreen();
                sumb += pixel.getBlue();
			}
		}
		
		// Total number of elements in the resulting matrix is:
		//    int[xMax - xMin][yMax - yMin]
		int dim = (xMax - xMin) * (yMax - yMin);
		
		logger.trace("Exiting getAverageColor");
		
		// Mean average of a matrix is (sum of elements) / (number of elements)
		//    Since every element has all three of R, G, and B it's possible to
		//    divide each color's sum by the total dimension of the matrix
		return new java.awt.Color(Math.round(sumr / dim), Math.round(sumg / dim),
				Math.round(sumb / dim));
	}
	
	/**
	 * Get average color, in RGB values, from a specified portion of an image
	 * focusing on a named `LayoutPosition` of that image
	 * NOTE: All range numbers are inclusive
	 * Based on: https://stackoverflow.com/a/71065493
	 * @param bi Image to be analyzed
	 * @param position Specific `ScreenLayout` to analyze
	 * @param Minimum X coordinate (row start) of area to be analyzed
	 * @param xMax Maximum X coordinate (row end) of area to be analyzed
	 * @param yMin Minimum Y coordinate (column start) of area to be analyzed
	 * @param yMax Maximum Y coordinate (column end) of area to be analyzed
	 * @return One `java.awt.Color`, in RGB values
	 */
	public static java.awt.Color getAverageColor(BufferedImage bi,
			LayoutPosition position, int xMin, int yMin, int sampleSizeX,
			int sampleSizeY) {
		logger.trace("Entering getAverageColor");
		
		int xMax = 0;
		int yMax = 0;
		
		// First, determine how different layouts are going to affect which
		//    coordinates on the image need to be processed
		switch(position) {
			case LEFT: {
				xMin = 0;
				xMax = sampleSizeX;
				yMax = yMin + sampleSizeY;
				break;
			}
			case RIGHT: {
				xMin = bi.getWidth() - sampleSizeX;
				xMax = bi.getWidth();
				yMax = yMin + sampleSizeY;
				break;
			}
			case TOP: {
				xMax = xMin + sampleSizeX;
				yMin = 0;
				yMax = sampleSizeY;
				break;
			}
			case BOTTOM: {
				xMax = xMin + sampleSizeX;
				yMin = bi.getHeight() - sampleSizeY;
				yMax = bi.getHeight();
				break;
			}
			case CENTER:
			default: {
				xMin = bi.getWidth() / 2 - (sampleSizeX / 2);
				yMin = bi.getHeight() / 2 - (sampleSizeY / 2);
				xMax = bi.getWidth() / 2 + (sampleSizeX / 2);
				yMax = bi.getHeight() / 2 + (sampleSizeY / 2);
				break;
			}
		}
		
		logger.trace("Exiting getAverageColor");
		
		return getAverageColor(bi, xMin, xMax, yMin, yMax);
	}
	
	/**
	 * Determine array of colors to send to LiFX multizone-compatible lights
	 * @param image Image to be analyzed
	 * @param layout Specific `ScreenLayout` to analyze
	 * @param numZones Number of individual light zones to calculate
	 * @param reverse `true` if color should be output in reverse order
	 * @return Array of colors to send to LiFX lights
	 */
	public static Color[] getColorFromImage(BufferedImage image,
			LayoutPosition layout, int numZones, boolean reverse) {
		logger.trace("Entering getColorFromImage");
		
		Color[] colorArray = new Color[numZones];
		
		// Calculate number of pixels it's safe to use per zone without overflowing
		// TODO: Allow this to be user-configured as well
		int numPixelsX = Math.floorDiv(image.getWidth(), numZones);
		int numPixelsY = Math.floorDiv(image.getHeight(), numZones);
		
		// Allow reversing the color output to account for different physical
		//    lighting layouts (among other possibilities)
		if(reverse) {
			for(int i = numZones; i > 0; i--) {
				// Pass entire original image for calculations and specify where in the
				//    image to look instead of allocating new sub-images each time this
				//    calculation is performed
				java.awt.Color tempColor = getAverageColor(image, layout,
						numPixelsX * i, numPixelsY * i, numPixelsX, numPixelsY);
				colorArray[i] = Color.fromRGB(tempColor.getRed(), tempColor.getGreen(),
						tempColor.getBlue());
				
				logger.trace("Color array for zone {} is {}", i, colorArray[i]);
			}
		}else {
			for(int i = 0; i < numZones; i++) {
				// Pass entire original image for calculations and specify where in the
				//    image to look instead of allocating new sub-images each time this
				//    calculation is performed
				java.awt.Color tempColor = getAverageColor(image, layout,
						numPixelsX * i, numPixelsY * i, numPixelsX, numPixelsY);
				colorArray[i] = Color.fromRGB(tempColor.getRed(), tempColor.getGreen(),
						tempColor.getBlue());
				
				logger.trace("Color array for zone {} is {}", i, colorArray[i]);
			}
		}
		
		logger.trace("Exiting getColorFromImage");
		
		return colorArray;
	}
	
	/**
	 * Determine if an image is "too dark" based on a provided brightness
	 * threshold (between 0.0 and 1.0).
	 * @param image Image to be analyzed
	 * @param threshold Threshold (between 0.0 and 1.0) at which brightness is
	 * considered too low for reliable motion detection
	 */
	public static boolean isImageTooDark(BufferedImage image, float threshold) {
		/*
		 * This method will likely be processing full webcam frames, which can easily
		 * be 1280 x 720 pixels or more. In order to detect "darkness" without spending
		 * too much CPU time on processing every possible pixel, the provided image will be
		 * split into 12 horizontal "strips". The 3rd, 6th, and 9th strips will then
		 * be averaged in their entirety, minus 10% on the left and right ends to 
		 * account for camera images which don't exactly match the TV screen they're
		 * pointed at. If all three strips average at or below the
		 * specified threshold, that should be sufficient to indicate that the
		 * majority of the image is at or below the threshold as well.
		 * 
		 * All of this means that instead of processing 1280 * 720 = 921,600 pixels
		 * we're only processing (1280 / 12 * 0.8) * (720 / 12) =  5,120 pixels.
		 */
		
		logger.trace("Entering isImageTooDark");
		
		boolean result = false;
		
		float margin = 0.1f;
		int xMin = Math.round(image.getWidth() * margin);
		int xMax = Math.round(image.getWidth() * (1 - margin));
		float brightness = 0f;
		
		for(int i = 0; i < 12; i++) {
			// Only calculate on passes 3, 6, and 9
			if(i % 3 == 0) {
				int yMin = Math.round((image.getHeight() / 12) * i);
				int yMax = Math.round(image.getHeight() / 12 * (i + 1));
				
				java.awt.Color tempColor = getAverageColor(image, xMin, xMax, yMin, yMax);
				
				float[] tempHSB = java.awt.Color.RGBtoHSB(tempColor.getRed(),
						tempColor.getGreen(), tempColor.getBlue(), null);
				brightness += tempHSB[2];
			}
		}
		
		// Average of the 3 brightness bands
		float avgBrightness = brightness / 3;
		logger.debug("Average image brightness: {}", avgBrightness);
		
		if(avgBrightness <= threshold) {
			result = true;
		}
		
		logger.debug("Darkness calculation result: {}", result);
		logger.trace("Exiting isImageTooDark");
		
		return result;
	}
}
