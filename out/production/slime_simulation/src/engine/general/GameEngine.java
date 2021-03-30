package engine.general;

/**
 * main engine class: contains the main loop and calls all
 * the necessary methods every frame for updating, rendering and
 * input handling.
 */
public class GameEngine implements Runnable{

    @SuppressWarnings({"unused", "FieldCanBeLocal"})
    private static long lastTime;

    @SuppressWarnings("unused")
    public static void pTime(String msg) {
        //System.out.println( System.currentTimeMillis() - lastTime + " " + msg);
        lastTime = System.currentTimeMillis();
    }

    /**frames per second (rendering and input) */
    @SuppressWarnings("WeakerAccess")
    public static final int TARGET_FPS = 400;

    /** updates per second */
    @SuppressWarnings("WeakerAccess")
    public static final int TARGET_UPS = 30;

    /** window used*/
    private final Window window;

    /**timer of loop times*/
    private final Timer timer;

    /** interface implemented by the game class */
    private final IGameLogic gameLogic;

    /** values needed for time calculations */
    private float accumulator;
    @SuppressWarnings("FieldCanBeLocal")
    private float interval = 1f/TARGET_UPS;
    @SuppressWarnings("FieldCanBeLocal")
    private float elapsedTime;

    /**
     * constructor uses parameters to create the window
     * it also gets the gameLogic passed
     *
     * @param windowTitle for window
     * @param width for window
     * @param height for window
     * @param vSync for window
     * @param gameLogic the game
     */
    public GameEngine(String windowTitle, int width, int height, boolean vSync, IGameLogic gameLogic) {
        window = new Window(windowTitle,width,height,vSync,this);
        this.gameLogic = gameLogic;
        timer = new Timer();
    }

    /**
     * run method of the main game loop thread
     *
     * initialises everything and runs the loop
     *
     * cleans up at the end
     */
    @Override
    public void run() {
        try{
            init();
            gameLoop();
        }catch (Exception e){
            e.printStackTrace();
        } finally {
            cleanup();
        }
    }

    /**
     * initialises all the used objects
     *
     * @throws Exception if game logic initialization fails
     */
    private void init() throws Exception {
        window.init();
        timer.init();
        gameLogic.init(window);

    }

    /**
     * main game loop
     */
    private void gameLoop(){

        accumulator = 0f;


        boolean running = true;
        //noinspection ConstantConditions
        while (running && !window.windowShouldClose()){


            frameAction();

            if(!window.isvSync()){
                sync();
            }
        }
    }

    /**
     * action performed every frame
     */
    void frameAction() {

        elapsedTime = timer.getElapsedTime();
        accumulator += elapsedTime;

        pTime("input");

        input();

        pTime("update");

        while (accumulator >= interval){
            update(interval);
            accumulator -= interval;
        }

        pTime("render");

        render();
    }

    /**
     * used to calculate the waiting time if vSync isn't used
     */
    private void sync(){

        float loopSlot = 1f/TARGET_FPS;
        double endTime = timer.getLastLoopTime() + loopSlot;
        while (timer.getTime() < endTime){
            try {
                Thread.sleep(1);
            } catch (InterruptedException ignored) {}
        }
    }

    /**
     * calls the input of the objects
     */
    protected void input(){
        gameLogic.input(window);
    }

    /**
     * updates the program
     *
     * @param interval used to pass the time it took since the last update cycle
     */
    private void update(float interval){

        gameLogic.update(interval);
    }

    /**
     * renders the program
     */
    protected void render(){


        gameLogic.render(window);
        window.swapBuffers();
        window.events();


    }

    /**
     * calls the cleanup method of the program
     */
    private void cleanup(){
        gameLogic.cleanup();
    }
}
