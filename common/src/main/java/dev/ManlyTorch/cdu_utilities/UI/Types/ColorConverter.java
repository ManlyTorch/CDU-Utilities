package dev.ManlyTorch.cdu_utilities.UI.Types;

import java.util.Map;
public class ColorConverter {
    private static final Map<String, Integer> NAMED_COLORS = Map.ofEntries(
        Map.entry("black", 0x000000),
        Map.entry("dark_blue", 0x0000aa),
        Map.entry("dark_green", 0x00aa00),
        Map.entry("dark_aqua", 0x00aaaa),
        Map.entry("dark_red", 0xaa0000),
        Map.entry("dark_purple", 0xaa00aa),
        Map.entry("gold", 0xffaa00),
        Map.entry("gray", 0xaaaaaa),
        Map.entry("dark_gray", 0x555555),
        Map.entry("blue", 0x5555ff),
        Map.entry("green", 0x55ff55),
        Map.entry("aqua", 0x55ffff),
        Map.entry("red", 0xff5555),
        Map.entry("light_purple", 0xff55ff),
        Map.entry("yellow", 0xffff55),
        Map.entry("white", 0xffffff)
    );

    public static int fromString(String input) {
        String key = input.toLowerCase().replace("#", "");
        Integer named = NAMED_COLORS.get(key);
        if (named != null) return 0x000000 | named;
        return 0x000000 | Integer.parseInt(key, 16);
    }
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
