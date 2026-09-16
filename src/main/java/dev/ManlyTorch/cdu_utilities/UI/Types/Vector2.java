package dev.ManlyTorch.cdu_utilities.UI.Types;

public class Vector2 {
    public double x;
    public double y;

    public Vector2(double x, double y) { this.x = x; this.y = y; }
    public Vector2(double x) { this(x, 0); }
    public Vector2() { this(0.0f, 0.0f); }

    public Vector2 add(Vector2 b) { return new Vector2(this.x + b.x, this.y + b.y); }
    public Vector2 subtract(Vector2 b) { return new Vector2(this.x - b.x, this.y - b.y); }
    public Vector2 multiply(Vector2 b) { return new Vector2(this.x * b.x, this.y * b.y); }
    public Vector2 divide(Vector2 b) { return new Vector2(this.x / b.x, this.y / b.y); }
    public Vector2 multiply(double b) { return new Vector2(this.x * b, this.y * b); }
    public Vector2 divide(double b) { return new Vector2(this.x / b, this.y / b); }

    public static Vector2 add(Vector2 a, Vector2 b) { return new Vector2(a.x + b.x, a.y + b.y); }
    public static Vector2 subtract(Vector2 a, Vector2 b) { return new Vector2(a.x - b.x, a.y - b.y); }
    public static Vector2 multiply(Vector2 a, Vector2 b) { return new Vector2(a.x * b.x, a.y * b.y); }
    public static Vector2 divide(Vector2 a, Vector2 b) { return new Vector2(a.x / b.x, a.y / b.y); }
    public static Vector2 multiply(Vector2 a, double b) { return new Vector2(a.x * b, a.y * b); }
    public static Vector2 divide(Vector2 a, double b) { return new Vector2(a.x / b, a.y / b); }

    public Vector2 ceil() { return new Vector2((double)Math.ceil(x), (double)Math.ceil(y)); }
    public Vector2 floor() { return new Vector2((double)Math.floor(x), (double)Math.floor(y)); }
    
    public double Magnitude() { return Math.sqrt(x * x + y * y); }
    public Vector2 Unit() { double mag = Magnitude(); return new Vector2((double)(x / mag), (double)(y / mag)); }
}
