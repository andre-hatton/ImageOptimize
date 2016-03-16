package com.image.yoshizuka.imageoptimize;

import java.io.ByteArrayOutputStream;
import java.io.File;

/**
 * Created by yoshizuka on 28/02/16.
 */
public class ImageOptimize {

    /**
     * L'image compressé
     */
    private ByteArrayOutputStream stream;

    /**
     * Le fichier de sortie
     */
    private File out;

    /**
     * Checmin de l'image
     */
    private String path;

    private int width, height, realWidth, realHeight, newWidth, newHeight;

    private int ratio = 80;

    public ByteArrayOutputStream getStream() {
        return stream;
    }

    public void setStream(ByteArrayOutputStream stream) {
        this.stream = stream;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public File getOut() {
        return out;
    }

    public void setOut(File out) {
        this.out = out;
    }

    public int size() {
        return stream == null ? 0 : stream.size();
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public boolean hasRatio() {
        return height == (width * realHeight) / realWidth;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public int getRatio() {
        return ratio;
    }

    public void setRatio(int ratio) {
        this.ratio = ratio;
    }

    public int getRealWidth() {
        return realWidth;
    }

    public void setRealWidth(int realWidth) {
        this.realWidth = realWidth;
    }

    public int getRealHeight() {
        return realHeight;
    }

    public void setRealHeight(int realHeight) {
        this.realHeight = realHeight;
    }

    public int getNewWidth() {
        return newWidth;
    }

    public void setNewWidth(int newWidth) {
        this.newWidth = newWidth;
    }

    public int getNewHeight() {
        return newHeight;
    }

    public void setNewHeight(int newHeight) {
        this.newHeight = newHeight;
    }

    public String getImageWidth() {
        return newWidth > 0 ? String.valueOf(newWidth) : String.valueOf(realWidth);
    }
    public String getImageHeight() {
        return newHeight > 0 ? String.valueOf(newHeight) : String.valueOf(realHeight);
    }
}
