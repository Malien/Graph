import org.lwjgl.BufferUtils;
import org.lwjgl.glfw.*;
import org.lwjgl.opengl.*;
import org.lwjgl.system.*;

import java.awt.image.BufferedImage;
import java.nio.IntBuffer;

import static java.awt.image.BufferedImage.TYPE_INT_RGB;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.glfw.Callbacks.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryUtil.NULL;

public class GraphWindow {

    private long window;
    private IGrapher grapher;
    private Double prevX = null;
    private Double prevY = null;
    private Vec2 translation = new Vec2(300, 300);
    private float scale = 5;
    private boolean shiftModifier = false;

    private BufferedImage buf;
    private boolean imgRequest;
    private Callback<BufferedImage> imageCallback;

    private TinkeringFrame frame;

    public GraphWindow(IGrapher grapher, TinkeringFrame frame){
        this.grapher = grapher;
        this.frame = frame;
        frame.registerViewer(this);
    }

    public static void main(String[] args){
        IGrapher grapher = new FlowerGrapher();
        TinkeringFrame frame = new TinkeringFrame(grapher);
        frame.setVisible(true);
        new GraphWindow(grapher, frame).run();
    }

    public void terminate(){
        glfwFreeCallbacks(window);
        glfwDestroyWindow(window);

        glfwTerminate();
        glfwSetErrorCallback(null).free();
        frame.dispose();
        System.exit(0);
    }

    public void run(){
        System.out.println("Graph Window initialization");

        init();
        loop();

        terminate();
    }

    public void requestImage(Callback<BufferedImage> callback) {
        buf = null;
        imgRequest = true;
        imageCallback = callback;
    }

    private BufferedImage getImage(){
        try (MemoryStack stack = MemoryStack.stackPush()) {
            IntBuffer pWidth = stack.mallocInt(1);
            IntBuffer pHeight = stack.mallocInt(1);

            glfwGetWindowSize(window, pWidth, pHeight);

            int width = pWidth.get(0);
            int height = pHeight.get(0);

            int[] buffer = new int[width*height];
            glReadPixels(0,0, width, height, GL_INT, GL_RGB, buffer);

            BufferedImage img = new BufferedImage(width, height, TYPE_INT_RGB);
            img.setRGB(0, 0, width, height, buffer, 0, width);
            return img;
        }
    }

    private void init(){
        GLFWErrorCallback.createPrint(System.err).set();

        if (!glfwInit()){
            throw new RuntimeException("Can't initialize GLFW");
        }

        glfwDefaultWindowHints();
        glfwWindowHint(GLFW_VISIBLE, GLFW_TRUE);
        glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE);

        window = glfwCreateWindow(600, 600, "Hello OpenGL", NULL, NULL);
        if (window == NULL){
            throw new RuntimeException("Can't create window");
        }

        glfwSetKeyCallback(window, (window, key, scancode, action, mods) -> {
            if (key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE){
                glfwSetWindowShouldClose(window, true);
            }
            if ((key == GLFW_KEY_LEFT_SHIFT || key == GLFW_KEY_RIGHT_SHIFT) && action == GLFW_PRESS){
                shiftModifier = true;
            }
            if ((key == GLFW_KEY_LEFT_SHIFT || key == GLFW_KEY_RIGHT_SHIFT) && action == GLFW_RELEASE){
                shiftModifier = false;
            }
            if (key == GLFW_KEY_R && action == GLFW_RELEASE){
                try (MemoryStack stack = MemoryStack.stackPush()) {
                    IntBuffer pWidth = stack.mallocInt(1);
                    IntBuffer pHeight = stack.mallocInt(1);

                    glfwGetWindowSize(window, pWidth, pHeight);
                    translation.x = pWidth.get(0) / 2f;
                    translation.y = pHeight.get(0) / 2f;
                }
                scale = 5;
            }
        });
        glfwSetScrollCallback(window, (window, yoff, xoff) -> {
            if (shiftModifier) xoff *= 10;
            scale *= 1 + xoff * 0.05;
            if (scale < 0.1) scale = 0.1f;
        });

        try (MemoryStack stack = MemoryStack.stackPush()) {
            IntBuffer pWidth = stack.mallocInt(1);
            IntBuffer pHeight = stack.mallocInt(1);

            glfwGetWindowSize(window, pWidth, pHeight);

            GLFWVidMode vidmode = glfwGetVideoMode(glfwGetPrimaryMonitor());

            glfwSetWindowPos(
                    window,
                    (vidmode.width() - pWidth.get(0))/ 2,
                    (vidmode.height() - pHeight.get(0))/2
            );

            glfwMakeContextCurrent(window);
            glfwSwapInterval(1);

            glfwShowWindow(window);
        }
    }

    private void loop(){
        GL.createCapabilities();

        glMatrixMode(GL_PROJECTION);
        glLoadIdentity();
        glOrtho(0, 600, 600, 0, 1, -1);
        glMatrixMode(GL_MODELVIEW);

        glPolygonMode( GL_FRONT_AND_BACK, GL_LINE );
        glColor3i(255, 255, 255);
        glClearColor(1.0f, 1.0f, 1.0f, 0.0f);
        while (!glfwWindowShouldClose(window)) {
            if (glfwGetMouseButton(window, GLFW_MOUSE_BUTTON_1) == GLFW_PRESS) {
                double[] xpos = new double[1];
                double[] ypos = new double[1];

                glfwGetCursorPos(window, xpos, ypos);

                if (prevX != null && prevY != null) {
                    translation.x += xpos[0] - prevX;
                    translation.y += ypos[0] - prevY;
                }
                prevX = xpos[0];
                prevY = ypos[0];
            } else {
                if (prevX != null){
                    prevX = null;
                }
                if (prevY != null){
                    prevY = null;
                }
            }

            if (imgRequest) {
                imageCallback.call(getImage());
                imgRequest = false;
            }

            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

            glBegin(GL_LINE_STRIP);
                for (Vec2 point : grapher) {
                    glVertex2f(point.x*scale + translation.x, point.y*scale + translation.y);
                }
            glEnd();

            glfwSwapBuffers(window);

            glfwPollEvents();
        }
    }

}
