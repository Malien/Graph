import java.util.Iterator;

public class FlowerGrapher implements IGrapher {

    public static final float RANGE_MIN = 0;
    public static final float RANGE_MAX = 360;

    private int resolution = 10000;
    private ArgInfo[] args = {
            new ArgInfo(10, "R₀", -100, 100),
            new ArgInfo(40, "A", -100, 100),
            new ArgInfo(45, "S", 0, 360)};

    @Override
    public int argc() {
        return args.length;
    }

    @Override
    public String name() {
        return  "<html>X = \uD835\uDF0C cos(\uD835\uDF11)<br>"+
                "Y = \uD835\uDF0C sin(\uD835\uDF11)<br>"+
                "\uD835\uDF0C = R₀ + A cos(\uD835\uDF11 2\uD835\uDF0B/S)</html>";
    }

    @Override
    public void setArgs(float... args) {
        if (args.length > argc()){
            throw new RuntimeException("Tried to set more arguments than function can handle");
        }
        for (int i=0; i<args.length; i++){
            this.args[i].value = args[i];
        }
    }

    @Override
    public void setResolution(int res) {
        this.resolution = res;
    }

    @Override
    public ArgInfo[] getArgs() {
        return args.clone();
    }

    @Override
    public int getResolution() {
        return resolution;
    }

    @Override
    public Vec2 solve(float arg) {
        Vec2 out = new Vec2();
        float p = args[0].value + (float) (args[1].value*Math.cos(arg*2*Math.PI/Math.toRadians(args[2].value)));
        out.x = (float) (p*Math.cos(arg));
        out.y = (float) (p*Math.sin(arg));
        return out;
    }

    @Override
    public Iterator<Vec2> iterator() {
        return new Iterator<Vec2>() {
            float increment = (RANGE_MAX - RANGE_MIN) / resolution;
            float pos = 0;
            int _resolution = resolution;

            @Override
            public boolean hasNext() {
                return pos <= _resolution;
            }

            @Override
            public Vec2 next() {
                return solve(RANGE_MIN + increment*pos++);
            }
        };
    }

}
