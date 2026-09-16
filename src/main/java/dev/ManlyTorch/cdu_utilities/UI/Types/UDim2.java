package dev.ManlyTorch.cdu_utilities.UI.Types;

public class UDim2 {
    public UDim x;
    public UDim y;

    public UDim2(double xScale, double xOffset, double yScale, double yOffset) { this.x = new UDim(xScale, xOffset); this.y = new UDim(yScale, yOffset); }
    public UDim2(double xScale, double xOffset, double yScale) { this(xScale, xOffset, yScale, 0.0); }
    public UDim2(double xScale, double xOffset) { this(xScale, xOffset, 0.0, 0.0); }
    public UDim2(double xScale) { this(xScale, 0.0, 0.0, 0.0); }
    public UDim2(UDim x, UDim y) { this.x = x; this.y = y; }
    public UDim2(UDim x) { this(x, new UDim()); }
    public UDim2() { this(0.0f, 0.0f, 0.0f, 0.0f); }

    public static UDim2 fromScale(double xScale, double yScale) { return new UDim2(xScale, 0.0f, yScale, 0.0f); }
    public static UDim2 fromScale(double xScale) { return fromScale(xScale, 0.0f); }

    public static UDim2 fromOffset(double xOffset, double yOffset) { return new UDim2(0.0f, xOffset, 0.0f, yOffset); }
    public static UDim2 fromOffset(double xOffset) { return fromOffset(xOffset, 0.0f); }
    
    public UDim2 add(UDim2 b) { return new UDim2(this.x.add(b.x), this.y.add(b.y)); }
    public UDim2 subtract(UDim2 b) { return new UDim2(this.x.subtract(b.x), this.y.subtract(b.y)); }
    
    public static UDim2 add(UDim2 a, UDim2 b) { return new UDim2(a.x.add(b.x), a.y.add(b.y)); }
    public static UDim2 subtract(UDim2 a, UDim2 b) { return new UDim2(a.x.subtract(b.x), a.y.subtract(b.y)); }
}