package com.image.yoshizuka.imageoptimize;

/**
 * Created by yoshizuka on 28/02/16.
 */
public class MainObject {

    private String imagePath;

    private String size;

    private ImageOptimize imageOptimize;

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public ImageOptimize getImageOptimize() {
        return imageOptimize;
    }

    public void setImageOptimize(ImageOptimize imageOptimize) {
        this.imageOptimize = imageOptimize;
    }
}
