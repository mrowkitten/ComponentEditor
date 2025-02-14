package net.dasdarklord.componenteditor.util;

import java.awt.*;

public class HSBColor {
    private float hue;
    private float saturation;
    private float brightness;

    public HSBColor(float h, float s, float b) {
        this.hue = h;
        this.saturation = s;
        this.brightness = b;
    }

    public HSBColor(float[] hsb) {
        this.hue = hsb[0];
        this.saturation = hsb[1];
        this.brightness = hsb[2];
    }

    public static HSBColor of(Color c) {
        float[] hsbVals = Color.RGBtoHSB(c.getRed(), c.getGreen(), c.getBlue(), null);
        return new HSBColor(hsbVals[0], hsbVals[1], hsbVals[2]);
    }

    public Color toColor() {
        return Color.getHSBColor(hue, saturation, brightness);
    }

    public String toString() {
        return "HSB(" + hue + ", " + saturation + ", " + brightness + ")";
    }

    public boolean isGrayscale() {
        return saturation == 0 || brightness == 0;
    }

    public float getBrightness() {
        return brightness;
    }

    public float getHue() {
        return hue;
    }

    public float getSaturation() {
        return saturation;
    }

    public void setBrightness(float b) {
        this.brightness = b;
    }

    public void setHue(float h) {
        this.hue = h;
    }

    public void setSaturation(float s) {
        this.saturation = s;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (!(obj instanceof HSBColor hsb)) return false;
        return hsb.getHue() == hue && hsb.getSaturation() == saturation && hsb.getBrightness() == brightness;
    }
}
