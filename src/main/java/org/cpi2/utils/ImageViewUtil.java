package org.cpi2.utils;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.io.File;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Utility class for handling ImageView related operations.
 */
public class ImageViewUtil {
    private static final Logger LOGGER = Logger.getLogger(ImageViewUtil.class.getName());
    
    /**
     * Load an image from a file into an ImageView.
     * 
     * @param imageView The ImageView to load the image into
     * @param file The file containing the image
     * @return true if successfully loaded, false otherwise
     */
    public static boolean loadImageFromFile(ImageView imageView, File file) {
        if (file == null || !file.exists() || imageView == null) {
            return false;
        }
        
        try {
            Image image = new Image(file.toURI().toString());
            if (image.isError()) {
                LOGGER.log(Level.WARNING, "Error loading image from file: " + file.getPath());
                return false;
            }
            
            imageView.setImage(image);
            return true;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Exception loading image from file: " + file.getPath(), e);
            return false;
        }
    }
    
    /**
     * Load an image from a resource path into an ImageView.
     * 
     * @param imageView The ImageView to load the image into
     * @param resourcePath The resource path of the image (e.g., "/images/placeholder.png")
     * @return true if successfully loaded, false otherwise
     */
    public static boolean loadImageFromResource(ImageView imageView, String resourcePath) {
        if (resourcePath == null || imageView == null) {
            return false;
        }
        
        try {
            InputStream is = ImageViewUtil.class.getResourceAsStream(resourcePath);
            if (is == null) {
                LOGGER.log(Level.WARNING, "Resource not found: " + resourcePath);
                return false;
            }
            
            Image image = new Image(is);
            if (image.isError()) {
                LOGGER.log(Level.WARNING, "Error loading image from resource: " + resourcePath);
                return false;
            }
            
            imageView.setImage(image);
            return true;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Exception loading image from resource: " + resourcePath, e);
            return false;
        }
    }
    
    /**
     * Set a colored background for an ImageView when no image is available.
     * 
     * @param imageView The ImageView to apply the background to
     * @param colorHex Hex code for the background color (e.g., "#e0e0e0")
     */
    public static void setColoredBackground(ImageView imageView, String colorHex) {
        if (imageView == null) {
            return;
        }
        
        imageView.setImage(null);
        imageView.setStyle("-fx-background-color: " + colorHex + "; " + 
                           "background-color: " + colorHex + ";");
    }
    
    /**
     * Apply default styling to an ImageView.
     * 
     * @param imageView The ImageView to style
     * @param width Width to set
     * @param height Height to set
     * @param preserveRatio Whether to preserve the ratio
     */
    public static void applyDefaultStyling(ImageView imageView, double width, double height, boolean preserveRatio) {
        if (imageView == null) {
            return;
        }
        
        imageView.setFitWidth(width);
        imageView.setFitHeight(height);
        imageView.setPreserveRatio(preserveRatio);
    }
}
