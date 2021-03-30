package engine.general;

import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.opengl.GL;

import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryUtil.NULL;

@SuppressWarnings("unused")
public class Window {


    public void closeWindow() {
        glfwSetWindowShouldClose(windowHandle,true);
    }




    /** window title */
    private final String title;

    /** width and height of window in pixels */
    private int width,height;

    /** id code to identify window in glfw context */
    private long windowHandle;

    /** variable used to perform action in game loop thread after resized callback was called */
    private boolean resized;

    /** determines if window should use vSync */
    private boolean vSync;

    /** saves the key that was the last pressed an is currently still pressed, default value 0 */
    private List<Integer> lastPressed;

    /** if false, window only gets rendered if necessary */
    private boolean renderAlways;

    /** info about the primary monitor */
    private GLFWVidMode monitorData;

    /** the engine for this window */
    private GameEngine engine;

    /** determines if the window can be closed */
    private boolean closeable;


    /**
     * Constructor sets all parameters except windowHandle and update mode
     *
     * @param title window title
     * @param width window width in pixels
     * @param height window height in pixels
     * @param vSync should use vSync
     */

    public Window(String title, int width, int height, boolean vSync, GameEngine engine) {
        this.title = title;
        this.width = width;
        this.height = height;
        this.vSync = vSync;
        this.engine = engine;
        this.resized = false;
        this.renderAlways = false;
        this.closeable = true;
        this.lastPressed = new ArrayList<>();
    }

    /**
     * Method initializes window by creating an openGl window and setting the
     * parameters depending on the preset set in the constructor
     *
     */

    void init() {



        //ErrorCallback
        GLFWErrorCallback.createPrint(System.err).set();

        //Initializing GLFW
        if(!glfwInit()){
            throw new IllegalStateException("Unable to initialize GLFW");
        }
         glfwDefaultWindowHints(); //optional
            glfwWindowHint(GLFW_VISIBLE, GL_FALSE); //window will stay hidden
            glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3); //major glfw version used (libraries)
            glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 2); //minor glfw version used (libraries)
            glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);
            glfwWindowHint(GLFW_OPENGL_FORWARD_COMPAT, GL_TRUE);


            // Create window
            windowHandle = glfwCreateWindow(width, height, title, glfwGetPrimaryMonitor(), NULL);
            if (windowHandle == NULL) {
                throw new RuntimeException("Failed to create window");
            }


            //setup key callback
            glfwSetKeyCallback(windowHandle, (window, key, scancode, action, mods) -> {
                if (key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE) {
                    glfwSetWindowShouldClose(window, true);
                }
                if (action == GLFW_PRESS) {
                    lastPressed.add(key);
                }

                if (action == GLFW_RELEASE && lastPressed.contains(key)) {
                    lastPressed.remove((Integer) key);
                }
            });

            //get Resolution of the primary monitor
            monitorData = glfwGetVideoMode(glfwGetPrimaryMonitor());

            //center window
            //noinspection ConstantConditions
            glfwSetWindowPos(windowHandle, (monitorData.width() - width) / 2, (monitorData.height() - height) / 2);

            //make opengl context current
            glfwMakeContextCurrent(windowHandle);

            glfwSetWindowSizeCallback(windowHandle, (window, width, height) -> {
                this.width = width;
                this.height = height;
                this.setResized(true);
            });


            //glfwSetWindowMonitor(windowHandle,glfwGetPrimaryMonitor(),0,0,monitorData.width(),monitorData.height(),monitorData.refreshRate());

            if (isvSync()) {
                glfwSwapInterval(1);
            }


            //Make window visible
            glfwShowWindow(windowHandle);

            glfwFocusWindow(windowHandle);


            GL.createCapabilities();

            //resize Callback
            glfwSetFramebufferSizeCallback(windowHandle, (window, width, height) -> engine.frameAction());

            //support for transparent textures
            glEnable(GL_BLEND);
            glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

            //glPolygonMode(GL_FRONT_AND_BACK,GL_LINE);

            //glEnable(GL_CULL_FACE);
           // glCullFace(GL_BACK);

            glEnable(GL_STENCIL_TEST);

            glEnable(GL_DEPTH_TEST);

    }

    /**
     * marks the end of a frame:
     * the buffer that the scene was rendered on the last frame gets displayed
     * and the one that was displayed for the last frame is the one on which
     * the rendering of the next frame happens
     */
    void swapBuffers() {

        glfwSwapBuffers(windowHandle);

    }

    public void forceEvents() {
        glfwPostEmptyEvent();
    }

    /**
     * method handles the way the events are used
     */
    void events(){

        glfwPollEvents();
    }

    /**returns true if the window should be close (user tried to close it)
     *
     * @return if window should close
     */
    @SuppressWarnings("WeakerAccess")
    public boolean windowShouldClose() {
        return glfwWindowShouldClose(windowHandle);
    }

    /**
     * Sets the resized field
     * The field is set to true by the callback and to false by the
     * main game thread after updating the framebuffer and window content
     *
     * @param resized value set to resized
     */

    public void setResized(boolean resized) {
        this.resized = resized;
    }

    /**
     * returns the title of the window
     *
     * @return title of the window
     */

    @SuppressWarnings("unused")
    public String getTitle() {
        return title;
    }

    /**
     * returns the window width in pixels
     *
     * @return window width in pixels
     */

    public int getWidth() {
        return width;
    }

    /**
     * returns the window height in pixels
     *
     * @return window height in pixels
     */


    public int getHeight() {
        return height;
    }

    /**
     * returns the windowHandle, the unique identifier of the window
     *
     * @return window handle
     */

    public long getWindowHandle() {
        return windowHandle;
    }

    /**
     * check if the window was resized
     *
     * @return content of field resized
     */

    public boolean isResized() {
        return resized;
    }

    /**
     * check if window uses vSync
     *
     * @return content of field vSync
     */

    boolean isvSync() {
        return vSync;
    }

    /**
     * @param keyCode code of the key that is checked
     * @return returns true if the key is pressed
     */
    public boolean isKeyPressed(int keyCode){ return glfwGetKey(windowHandle,keyCode) == GLFW_PRESS;}

    /**
     * returns the visibility of the window
     * @return window visibility
     */
    public boolean isShown() {
        return 1==glfwGetWindowAttrib(windowHandle, GLFW_VISIBLE);
    }

    /**
     * shows window
     */
    public void show() {
        glfwShowWindow(windowHandle);
    }

    /**
     * hides window
     */
    public void hide() {
        glfwHideWindow(windowHandle);
    }


    /**
     * @return last Pressed key if its is still pressed
     */
    public List<Integer> getLastPressed() {
        return lastPressed;
    }

    /**
     * @return whether window should be rendered every frame
     */
    public boolean isRenderAlways() {
        return renderAlways;
    }

    /**
     * @return the primary monitors data
     */
    @SuppressWarnings("unused")
    public GLFWVidMode getMonitorData() {
        return monitorData;
    }

    /**
     * makes program not finish on window close
     */
    public void disableClosing() {
        closeable = false;
    }

    public void setRenderAlways(boolean renderAlways) {
        this.renderAlways = renderAlways;
    }
}
