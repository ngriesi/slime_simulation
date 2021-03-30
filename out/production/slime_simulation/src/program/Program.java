package program;

import engine.general.IGameLogic;
import engine.general.Window;
import engine.items.Quad;
import engine.items.Texture;
import engine.render.Renderer;

import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.opengl.GL42.glBindImageTexture;
import static org.lwjgl.opengl.GL43.*;
import static org.lwjgl.opengles.GLES31.GL_SHADER_STORAGE_BUFFER;
import static org.lwjgl.system.MemoryUtil.NULL;

public class Program implements IGameLogic {

    private Renderer renderer;

    private Quad quad;

    public static int tex_output;

    public static int tex_output_temp;

    public static int framebuffer;

    @Override
    public void init(Window window) throws Exception {
        renderer = new Renderer();
        quad = new Quad();


        // dimensions of the image
        int tex_w = Renderer.WIDTH, tex_h = Renderer.HEIGHT;

        tex_output = glGenTextures();
        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D, tex_output);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);

        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA32F, tex_w, tex_h, 0, GL_RGBA, GL_FLOAT,
                NULL);
        glBindImageTexture(0, tex_output, 0, false, 0, GL_READ_WRITE, GL_RGBA32F);

        quad.getMesh().getMaterial().setTexture(new Texture(tex_output,tex_w,tex_h));


        tex_output_temp = glGenTextures();
        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D, tex_output_temp);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);

        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA32F, tex_w, tex_h, 0, GL_RGBA, GL_FLOAT,
                NULL);



        framebuffer = glGenBuffers();
        glBindBuffer(GL_FRAMEBUFFER, framebuffer);
        glFramebufferTexture(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, tex_output_temp, 0);



        renderer.init(window);
    }

    @Override
    public void input(Window window) {

    }

    @Override
    public void update(float interval) {

    }

    @Override
    public void render(Window window) {

        int ssbo = glGenBuffers();
        glBindBuffer(GL_SHADER_STORAGE_BUFFER, ssbo);
        float[] color = {0};
        glBindBufferBase(GL_SHADER_STORAGE_BUFFER, 3, ssbo);
        glBufferData(GL_SHADER_STORAGE_BUFFER, color, GL_DYNAMIC_COPY);
        glBindBuffer(GL_SHADER_STORAGE_BUFFER, 0);

        renderer.render(window,quad);
    }

    @Override
    public void cleanup() {

    }
}
