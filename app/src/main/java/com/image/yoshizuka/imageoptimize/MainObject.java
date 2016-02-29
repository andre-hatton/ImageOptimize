package com.image.yoshizuka.imageoptimize;

/**
 * Created by yoshizuka on 28/02/16.
 */
public class MainObject {

    private String imagePath;

    private long size;

    private ImageOptimize imageOptimize;

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public ImageOptimize getImageOptimize() {
        return imageOptimize;
    }

    public void setImageOptimize(ImageOptimize imageOptimize) {
        this.imageOptimize = imageOptimize;
    }

    public static String humanReadableByteCount(long bytes) {
        int unit = 1024;
        if (bytes < unit) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(unit));
        String pre = "KMGTPE".charAt(exp-1) + "i";
        return String.format("%.1f %so", bytes / Math.pow(unit, exp), pre);
    }
}
