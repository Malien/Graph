public interface IGrapher extends Iterable<Vec2> {

    int argc();
    String name();

    void setArgs(float... args);
    void setResolution(int res);
    ArgInfo[] getArgs();
    int getResolution();

    Vec2 solve(float arg);

}
