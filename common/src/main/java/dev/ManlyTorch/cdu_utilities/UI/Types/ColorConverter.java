package dev.ManlyTorch.cdu_utilities.UI.Types;

public class ColorConverter {
    public static int fromRGB(int t, int r, int g, int b) {
        return t << 24 | r << 16 | g << 8 | b;
    }
    public static int fromHSV(int transparency, int hue, int saturation, int value) {
        int h = (int)(hue * 6);
        float f = hue * 6 - h;
        float p = value * (1 - saturation);
        float q = value * (1 - f * saturation);
        float t = value * (1 - (1 - f) * saturation);
        switch (h) {
            case 0: return ColorConverter.fromRGB(transparency, (int)value*256, (int)t*256, (int)p*256);
            case 1: return ColorConverter.fromRGB(transparency, (int)q*256, (int)value*256, (int)p*256);
            case 2: return ColorConverter.fromRGB(transparency, (int)p*256, (int)value*256, (int)t*256);
            case 3: return ColorConverter.fromRGB(transparency, (int)p*256, (int)q*256, (int)value*256);
            case 4: return ColorConverter.fromRGB(transparency, (int)t*256, (int)p*256, (int)value*256);
            case 5: return ColorConverter.fromRGB(transparency, (int)value*256, (int)p*256, (int)q*256);
            default: throw new RuntimeException("Couldn't convert HSV: " + hue + ", " + saturation + ", " + value);
        }
    }
}
