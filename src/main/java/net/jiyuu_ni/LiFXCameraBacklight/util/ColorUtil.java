package net.jiyuu_ni.LiFXCameraBacklight.util;

import java.awt.image.BufferedImage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.stuntguy3000.lifxlansdk.object.protocol.Color;

import net.jiyuu_ni.LiFXCameraBacklight.config.ScreenLayout.LayoutPosition;

public class ColorUtil {
	private static final Logger logger = LoggerFactory.getLogger(ColorUtil.class);
	
	/*
	 * Get average colors from specified zone of a BufferedImage
	 * Based on: https://stackoverflow.com/a/71065493
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
	
	public static Color[] getColorFromImage(BufferedImage image,
			LayoutPosition layout, int numZones) {
		logger.trace("Entering getColorFromImage");
		
		Color[] colorArray = new Color[numZones];
		
		// Calculate number of pixels it's safe to use per zone without overflowing
		// TODO: Allow this to be user-configured as well
		int numPixelsX = Math.floorDiv(image.getWidth(), numZones);
		int numPixelsY = Math.floorDiv(image.getHeight(), numZones);
		
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
		
		logger.trace("Exiting getColorFromImage");
		
		return colorArray;
	}
}
