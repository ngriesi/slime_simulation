package engine.items;

/**
 * Game Item Object used by many hud components
 * contains a quad mesh
 */
public class Quad extends GameItem{
    /**
     * default constructor creates the quad
     */
    public Quad() {
        createQuad();
    }

    /**
     * creates a quad with the origin at the center and the height and width of one
     * resizing is done by scaling
     */
    private void createQuad() {

        float[] positions = {
            -1f,-1f,0,
            1f,-1f,0,
            -1f,1f,0,
            1f,1f,0
        };

        float[] texCords = {
                0,0,
                1,0,
                0,1,
                1,1
        };


        int[] indices = {
                1,2,3,
                1,0,2
        };

        this.setPosition(0.5f,0.5f,0);
        this.setScale3(0.5f,0.5f,0.5f);
        Mesh mesh = new Mesh(positions,texCords,indices);
        this.setMesh(mesh);
    }
}
