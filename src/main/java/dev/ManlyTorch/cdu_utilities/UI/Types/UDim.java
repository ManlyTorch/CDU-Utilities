package dev.ManlyTorch.cdu_utilities.UI.Types;

public class UDim {
    public double offset;
    public double scale;

    public UDim(double scale, double offset) {
        this.scale = scale;
        this.offset = offset;
    }
    public UDim(double scale) {
        this(scale,0.0);
    }
    public UDim() {
        this(0.0,0.0);
    }

    public UDim add(UDim other) {
       return new UDim(this.scale + other.scale, this.offset + other.offset);
    }
    public UDim subtract(UDim b) {
        return new UDim(this.scale - b.scale, this.offset - b.offset);
    }
    
    public static UDim add(UDim a, UDim b) {
        return new UDim(a.scale + b.scale, a.offset + b.offset);
    }
    public static UDim subtract(UDim a, UDim b) {
        return new UDim(a.scale - b.scale, a.offset - b.offset);
    }
}