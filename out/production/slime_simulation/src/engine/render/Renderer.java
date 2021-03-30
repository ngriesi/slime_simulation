package engine.render;

import engine.general.Resources;
import engine.general.Transformation;
import engine.general.Window;
import engine.items.GameItem;
import engine.items.Mesh;
import org.joml.Matrix4f;
import org.joml.Vector4f;
import org.lwjgl.opengl.GL11;
import program.Program;

import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.opengl.GL42.*;
import static org.lwjgl.opengl.GL43.GL_SHADER_STORAGE_BUFFER;
import static org.lwjgl.opengl.GL43.glDispatchCompute;

public class Renderer {


    public static int WIDTH = 1920;
    public static int HEIGHT = 1200;

    private static int postProcessingGroupSize = 16;

    private float deltaTime = 1f;

    private float diffuseSpeed = 1f;

    private Vector4f evaporateSpeed = new Vector4f(0.005f, 0.03f, 0.005f, 0.05f);

    private float sensorAngleSpacing = 01f;

    private float turnSpeed = 0.1f;

    private float sensorOffsetDist = 5;

    private int sensorSize = 3;

    /** transformation object used for the matrix calculations */
    private final Transformation transformation;

    /** shader program for the scene (3d objects) */
    private ShaderProgram sceneShaderProgram;

    private ShaderProgram computeShader;

    private ShaderProgram postProcessingShader;

    /**
     * constructor creates transformation object
     */
    public Renderer(){
        transformation = new Transformation();
    }

    /**
     * initialization: sets up the shader programs
     *
     * @param window unused
     * @throws Exception if shaders cant be created or the uniforms cant be found
     */
    @SuppressWarnings("unused")
    public void init(Window window) throws Exception{

        setupSceneShader();

    }

    /**
     * sets up scene shader:
     * creates the shaders, creates the uniforms and initializes the light handler
     *
     * @throws Exception if shaders cant be created or uniforms cant be found
     */
    @SuppressWarnings("unused")
    private void setupSceneShader() throws Exception {
        sceneShaderProgram = new ShaderProgram();
        sceneShaderProgram.createVertexShader(Resources.loadResource("/shader/vertex.shader"));
        sceneShaderProgram.createFragmentShader(Resources.loadResource("/shader/fragment.shader"));
        sceneShaderProgram.link();

        sceneShaderProgram.createUniforms("modelViewMatrix");
        sceneShaderProgram.createUniforms("texture_sampler");


        computeShader = new ShaderProgram();
        computeShader.createComputeShader(Resources.loadResource("/shader/compute.shader"));
        computeShader.link();

        computeShader.createUniforms("sensorAngleSpacing");
        computeShader.createUniforms("deltaTime");
        computeShader.createUniforms("turnSpeed");
        computeShader.createUniforms("sensorOffsetDist");
        computeShader.createUniforms("sensorSize");
        computeShader.createUniforms("height");
        computeShader.createUniforms("width");



        int ssbo = glGenBuffers();
        glBindBuffer(GL_SHADER_STORAGE_BUFFER, ssbo);
        float[] color = createAgents(1000000);
        glBindBufferBase(GL_SHADER_STORAGE_BUFFER, 2, ssbo);
        glBufferData(GL_SHADER_STORAGE_BUFFER, color, GL_DYNAMIC_COPY);
        glBindBuffer(GL_SHADER_STORAGE_BUFFER, 0);

        postProcessingShader = new ShaderProgram();
        postProcessingShader.createComputeShader(Resources.loadResource("/shader/posProcessing.shader"));
        postProcessingShader.link();

        postProcessingShader.createUniforms("width");
        postProcessingShader.createUniforms("height");
        postProcessingShader.createUniforms("deltaTime");
        postProcessingShader.createUniforms("diffuseSpeed");
        postProcessingShader.createUniforms("evaporateSpeed");


    }

    private float[] createAgents(int number) {
        float[] result = new float[number * 4];
        for(int i = 0; i < number; i++) {
            float angle = (float) (Math.random() * Math.PI * 2);
            float dist = (float) (10 * Math.random() + 10);


            result[i * 4] = (float) (1920/2 + dist * Math.cos(angle));
            result[i * 4 + 1] = (float) (1080/2 + dist * Math.sin(angle));
            result[i * 4 + 2] = (float) (angle);
            result[i * 4 + 3] = Math.random()>0.5f?0:1;



            /*
            result[i * 4] = (float) (WIDTH/2);
            result[i * 4 + 1] = (float) (HEIGHT * Math.random());
            result[i * 4 + 2] = (float) (Math.PI);
            result[i * 4 + 3] = Math.random()>0.5f?0:1;

             */
        }
        return result;
    }

    /**
     * Main rendering method updates the viewport if the window got resized,
     * updates the main matrices with the camera and calls the three rendering methods
     *
     * @param window in witch the rendering happens
     */
    @SuppressWarnings("unused")
    public void render(Window window, GameItem gameItem){
        clear();

        GL11.glViewport(0,0,window.getWidth(),window.getHeight());
        if(window.isResized()){
            window.setResized(false);
        }
        renderScene(gameItem);

    }

    /**
     * renders scene: lights and 3d objects
     */
    @SuppressWarnings("unused")
    public void renderScene(GameItem gameItem){

        glClear(GL_COLOR_BUFFER_BIT);
        glBindFramebuffer(GL_FRAMEBUFFER, 0);




        computeShader.bind();

        computeShader.setUniform("sensorAngleSpacing",sensorAngleSpacing);
        computeShader.setUniform("deltaTime",deltaTime);
        computeShader.setUniform("turnSpeed",turnSpeed);
        computeShader.setUniform("sensorOffsetDist",sensorOffsetDist);
        computeShader.setUniform("sensorSize",sensorSize);
        computeShader.setUniform("width",WIDTH);
        computeShader.setUniform("height",HEIGHT);



        glDispatchCompute(32, 16, 16);
        glMemoryBarrier(GL_SHADER_IMAGE_ACCESS_BARRIER_BIT);

        postProcessingShader.bind();

        postProcessingShader.setUniform("width", WIDTH);
        postProcessingShader.setUniform("height", HEIGHT);
        postProcessingShader.setUniform("deltaTime", deltaTime);
        postProcessingShader.setUniform("diffuseSpeed", diffuseSpeed);
        postProcessingShader.setUniform("evaporateSpeed", evaporateSpeed);

        glDispatchCompute(WIDTH/postProcessingGroupSize,HEIGHT/postProcessingGroupSize,1);
        glMemoryBarrier(GL_SHADER_IMAGE_ACCESS_BARRIER_BIT);


        sceneShaderProgram.bind();
        Matrix4f orthographic = transformation.getOrtho2DProjectionMatrix(0,1,1,0,0,1);
        transformation.buildOrtoProjModelMatrix(gameItem, orthographic);
        sceneShaderProgram.setUniform("modelViewMatrix", transformation.buildOrtoProjModelMatrix(gameItem, orthographic));
        sceneShaderProgram.setUniform("texture_sampler",0);
        gameItem.getMesh().render();
        sceneShaderProgram.unbind();

    }



    /**
     * cleans up the shaders
     */
    public void cleanup(){
        if(sceneShaderProgram != null){
            sceneShaderProgram.cleanup();
        }
    }

    /**
     * clears the buffers:
     * colorBuffer
     * depthBuffer
     * stencilBuffer
     */
    private void clear(){
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT | GL_STENCIL_BUFFER_BIT);
    }
}
