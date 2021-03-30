package engine.items;

import org.lwjgl.system.MemoryStack;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL12.GL_CLAMP_TO_EDGE;
import static org.lwjgl.opengl.GL30.glGenerateMipmap;
import static org.lwjgl.stb.STBImage.*;

public class Texture {

    /** Modes of texture filtering */
    public enum FilterMode {
        NEAREST,LINEAR
    }

    /** texture id */
    private final int id;

    /** texture image width in pixels*/
    private final int width;

    /** texture image height in pixels */
    private final int height;

    /** determines the texture filter used */
    private FilterMode filterMode = FilterMode.NEAREST;



    /**
     * creates a texture from a byte buffer and stores the id that points to it in the id field
     *
     * @param imageBuffer byte buffer with image data
     * @throws Exception if stbi_load_from_memory cant load the data into a buffer
     */
    @SuppressWarnings("unused")
    public Texture(ByteBuffer imageBuffer) throws Exception {
        ByteBuffer buf;
        //Load texture File
        try(MemoryStack stack = MemoryStack.stackPush()){
            IntBuffer w = stack.mallocInt(1);
            IntBuffer h = stack.mallocInt(1);
            IntBuffer channels = stack.mallocInt(1);
            buf = stbi_load_from_memory(imageBuffer,w,h,channels,4);
            if(buf == null){
                throw new Exception("Image file not loaded: "+stbi_failure_reason());
            }

            //Get width and height of image
            width = w.get();
            height = h.get();
        }

        this.id =  createTexture(buf);

        stbi_image_free(buf);
    }

    /**
     * creates texture form image file and stores the texture id in the id field
     *
     * @param filename name of the image file
     * @param filterMode filtering mode for the texture
     * @throws Exception if stbi_load cant load the image into a buffer
     */
    public Texture(String filename,FilterMode filterMode) throws Exception {
        this.filterMode = filterMode;
        ByteBuffer buf;
        //Load texture File
        try(MemoryStack stack = MemoryStack.stackPush()){
            IntBuffer w = stack.mallocInt(1);
            IntBuffer h = stack.mallocInt(1);
            IntBuffer channels = stack.mallocInt(1);
            buf = stbi_load(filename,w,h,channels,4);
            if(buf == null){
                throw new Exception("Image file ["+filename +"] not loaded: "+stbi_failure_reason());
            }

            //Get width and height of image
            width = w.get();
            height = h.get();
        }

        this.id =  createTexture(buf);
    }

    public Texture(int id, int width, int height) {
        this.id = id;
        this.width = width;
        this.height = height;
    }

    /**
     * creates texture form byte buffer and returns the texture id
     * it also frees the buffer again
     *
     * @param buf image data
     * @return texture id
     */
    private int createTexture(ByteBuffer buf){
        //Create an openGl texture
        int textureId = glGenTextures();
        //Bind the texture
        glBindTexture(GL_TEXTURE_2D,textureId);

        //Tell openGl how to unpack the RGBA bytes, Each component is 1 byte size
        glPixelStorei(GL_UNPACK_ALIGNMENT,1);

        switch (filterMode) {
            case NEAREST:
                glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
                glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
                break;
            case LINEAR:
                glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
                glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
                break;
        }



        //upload the texture data
        glTexImage2D(GL_TEXTURE_2D,0,GL_RGBA,width,height,0,GL_RGBA,GL_UNSIGNED_BYTE,buf);
        //Generate MipMap
        glGenerateMipmap(GL_TEXTURE_2D);

        stbi_image_free(buf);

        return textureId;
    }

    /**
     * @return texture width in pixels
     */
    public int getWidth() {
        return width;
    }

    /**
     * @return texture height in pixels
     */
    public int getHeight() {
        return height;
    }

    /**
     * binds texture
     */
    @SuppressWarnings("unused")
    public void bind() {
        glBindTexture(GL_TEXTURE_2D,id);
    }

    /**
     * @return texture id
     */
    public int getId(){
        return id;
    }

    /**
     * deletes the texture with this id
     */
    public void cleanup(){
        glDeleteTextures(id);
    }


}
