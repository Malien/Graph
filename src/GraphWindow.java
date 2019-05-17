import org.lwjgl.glfw.*;
import org.lwjgl.opengl.*;
import org.lwjgl.system.*;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.nio.IntBuffer;

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

    private BufferedImage getImage() {
        int[] height = new int[1];
        int[] width = new int[1];
        int[] xpos = new int[1];
        int[] ypos = new int[1];

        glfwGetWindowSize(window, width, height);
        glfwGetWindowPos(window, xpos, ypos);
        try {
            return new Robot().createScreenCapture(new Rectangle(xpos[0], ypos[0], width[0], height[0]));
        } catch (AWTException ex) {
            ex.printStackTrace();
            return null;
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
            double[] mouseX = new double[1];
            double[] mouseY = new double[1];
            int[] winX = new int[1];
            int[] winY = new int[1];
            int[] winW = new int[1];
            int[] winH = new int[1];

            glfwGetCursorPos(window, mouseX, mouseY);
            glfwGetWindowPos(window, winX, winY);
            glfwGetWindowSize(window, winW, winH);

            float oldScale = 5;

            if (shiftModifier) xoff *= 10;
            scale *= 1 + xoff * 0.05;
            if (scale < 0.1) scale = 0.1f;

            float mx = translation.x - (float)mouseX[0];
            float my = translation.y - (float)mouseY[0];

            float dx = mx*((float)xoff * 0.05f);
            float dy = my*((float)xoff * 0.05f);

            System.out.println("old mouse: " + mx + ", "  + my + "; d mouse: " + dx + ", " + dy +
                    "; old scale: " + oldScale + "; new scale: " + scale);

            translation.x += dx;
            translation.y += dy;
        });
        glfwSetWindowSizeCallback(window, (window1, width, height) -> {
            glViewport(0,0,width,height);
            glMatrixMode(GL_PROJECTION);
            glLoadIdentity();

            glOrtho(0.0f,width,height,0,1.0f,-1.0f);

            glMatrixMode(GL_MODELVIEW);
            glLoadIdentity();
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
