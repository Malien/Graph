public class Vec2 {

    float x;
    float y;

    public Vec2(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public Vec2(){}

    @Override
    public String toString() {
        return "Vec2{" +
                x +
                ", " + y +
                '}';
    }
}
