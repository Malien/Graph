public class ArgInfo {

    float value;
    String name;
    float max;
    float min;

    public ArgInfo(float value, String name, float min, float max) {
        this.value = value;
        this.name = name;
        this.max = max;
        this.min = min;
    }

    public float range(){
        return max - min;
    }

    public float normalized(){
        return value - min;
    }
}
