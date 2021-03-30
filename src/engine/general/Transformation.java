package engine.general;

import engine.items.GameItem;
import org.joml.Matrix4f;
import org.joml.Vector3f;

/**
 * class containing the methods for the matrix calculations for the 2d and 3d rendering
 */
public class Transformation {

    /** determines the way the scene is visible depending of the field of view and the window size */
    private final Matrix4f projectionMatrix;

    /** the determines the way one single GameModel is viewed */
    private final Matrix4f modelViewMatrix;

    /** applies the values of the cameras in the 3d scene to the objects*/
    private final Matrix4f viewMatrix;

    /** Matrix for orthographic projection ( hud ) */
    private final Matrix4f orthoMatrix;

    /** field to temporarily store the position and rotation values of a gameItem */
    private final Matrix4f modelMatrix;

    /** Matrix for an orthographic model */
    private final Matrix4f orthographic2DMatrix;

    /** Matrix for orthographic projection */
    private final Matrix4f orthoProjectionMatrix;

    /** model matrix for orthographic projecting */
    private final Matrix4f orthoModelMatrix;

    /** light view matrix */
    private final Matrix4f lightViewMatrix;

    /** Light model matrix */
    private final Matrix4f modelLightMatrix;

    /** light model view Matrix */
    private final Matrix4f modelLightViewMatrix;

    /**
     * create a new object for every matrix
     */

    public Transformation(){
        projectionMatrix = new Matrix4f();
        modelMatrix = new Matrix4f();
        viewMatrix = new Matrix4f();
        modelViewMatrix = new Matrix4f();
        orthoMatrix = new Matrix4f();
        orthographic2DMatrix = new Matrix4f();
        orthoModelMatrix = new Matrix4f();
        orthoProjectionMatrix = new Matrix4f();
        lightViewMatrix = new Matrix4f();
        modelLightMatrix = new Matrix4f();
        modelLightViewMatrix = new Matrix4f();
    }

    /**
     * @return the projection Matrix of the Scene
     */

    public Matrix4f getProjectionMatrix() {
        return projectionMatrix;
    }

    /**
     * @return the view Matrix (camera matrix)
     */

    public Matrix4f getViewMatrix() {
        return viewMatrix;
    }

    /**
     * Creates the matrix that has to be used for the specific game item by using the transformation applied to this game item
     *
     * @param gameItem that gets rendered with this transformation
     * @param viewMatrix of the current scene/camera
     * @return model view Matrix
     */

    public Matrix4f buildModelViewMatrix(GameItem gameItem, Matrix4f viewMatrix){
        Vector3f rotation = gameItem.getRotation();
        modelMatrix.identity().translate(gameItem.getPosition()).
                rotateX((float)Math.toRadians(-rotation.x)).
                rotateY((float)Math.toRadians(-rotation.y)).
                rotateZ((float)Math.toRadians(-rotation.z)).
                scale(gameItem.getScale()).scale(gameItem.getScale3());
        modelViewMatrix.set(viewMatrix);
        return modelViewMatrix.mul(modelMatrix);
    }

    /**
     * Sets values of the ProjectionMatrix depending on the Parameters:
     *
     * @param fov field of View
     * @param width Window width
     * @param height Window height
     * @param zNear distance to near plane
     * @param zFar distance to far plane
     * @return ProjectionMatrix
     */
    @SuppressWarnings("UnusedReturnValue")
    public Matrix4f updateProjectionMatrix(float fov, float width, float height, float zNear, float zFar) {
        projectionMatrix.identity();
        return projectionMatrix.setPerspective(fov,width/height,zNear,zFar);
    }

    /**
     * sets values  for an orthographic projection matrix
     *
     * @param left left of the scene
     * @param right right of the scene
     * @param bottom bottom of the scene
     * @param top top og the scene
     * @param near distance of the near plane
     * @param far distance of the far plane
     * @return orthographic projection matrix with newly set values
     */
    public Matrix4f updateOrthoProjectionMatrix(float left, float right, float bottom, float top, float near, float far) {
        orthoProjectionMatrix.identity();
        orthoProjectionMatrix.setOrtho(left, right, bottom, top, near, far);
        return orthoProjectionMatrix;
    }

    public Matrix4f getLightViewMatrix() {
        return lightViewMatrix;
    }

    public Matrix4f getOrthoProjectionMatrix() {
        return orthoProjectionMatrix;
    }

    public void setLightViewMatrix(Matrix4f lightViewMatrix) {
        this.lightViewMatrix.set(lightViewMatrix);
    }

    public Matrix4f updateLightViewMatrix(Vector3f position, Vector3f rotation) {
        return updateGenericViewMatrix(position, rotation, lightViewMatrix);
    }

    private Matrix4f updateGenericViewMatrix(Vector3f position, Vector3f rotation, Matrix4f matrix) {
        matrix.identity();
        // First do the rotation so camera rotates over its position
        matrix.rotate((float)Math.toRadians(rotation.x), new Vector3f(1, 0, 0))
                .rotate((float)Math.toRadians(rotation.y), new Vector3f(0, 1, 0));
        // Then do the translation
        matrix.translate(-position.x, -position.y, -position.z);
        return matrix;
    }

    /**
     * creates a matrix for the orthographic projection of hud elements by using the windows bounds
     * making the left top the origin by setting left and top to 0 and the right and bottom can be applied to any number
     * setting right to one means an object with the x coordinate 1 has its origin at the right edge of the window
     *
     * @param left 0
     * @param right width of the window
     * @param bottom height of the window
     * @param top 0
     * @return orthographic Matrix
     */

    public final Matrix4f getOrtho2DProjectionMatrix(float left, float right, float bottom, float top, float zNear, float zFar) {
        orthoMatrix.identity();
        orthoMatrix.setOrtho(left, right, bottom, top,zNear,zFar);
        return orthoMatrix;
    }

    /**
     * Creates the matrix that has to be used for the specific game item by using the transformation applied to this game item
     * this Method is for the game items drawn orthographically
     *
     * @param gameItem game item that gets rendered with this transformation
     * @param orthoMatrix of the current window
     * @return orthoProjectionModelMatrix
     */

    public Matrix4f buildOrtoProjModelMatrix(GameItem gameItem, Matrix4f orthoMatrix) {
        Vector3f rotation = gameItem.getRotation();
        Matrix4f modelMatrix = new Matrix4f();
        modelMatrix.identity().translate(gameItem.getPosition()).
                rotateX((float)Math.toRadians(-rotation.x)).
                rotateY((float)Math.toRadians(-rotation.y)).
                rotateZ((float)Math.toRadians(-rotation.z)).
                scale(gameItem.getScale3().x,gameItem.getScale3().y,gameItem.getScale3().z);
        orthographic2DMatrix.set(orthoMatrix);
        orthographic2DMatrix.mul(modelMatrix);
        return orthographic2DMatrix;
    }


}
