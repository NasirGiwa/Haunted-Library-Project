package scene_creation;

import org.jogamp.java3d.*;
import org.jogamp.java3d.utils.geometry.Sphere;
import org.jogamp.vecmath.*;
import java.util.Iterator;
import java.util.Map;

// GasSphereObject represents a sphere with a gas effect that disappears when all tasks are solved.
public class GasSphereObject extends Objects {
    private TransformGroup movementTG;              // TransformGroup for the gas sphere's movement.
    private BranchGroup detachableBG;               // BranchGroup to allow detachment from the scene.
    private Map<String, Boolean> bookshelfUsage;    // Reference to the bookshelf usage map from MainClass.

    // Behavior to animate the gas sphere moving up and down.
    private class MovementBehavior extends Behavior {
        private TransformGroup targetTG;
        private WakeupOnElapsedFrames wakeup = new WakeupOnElapsedFrames(0);

        public MovementBehavior(TransformGroup targetTG) {
            this.targetTG = targetTG;
            setSchedulingBounds(new BoundingSphere(new Point3d(0, 0, 0), 1000));
        }

        @Override
        public void initialize() {
            wakeupOn(wakeup);                       // Start the behavior on initialization.
        }

        @Override
        public void processStimulus(Iterator<WakeupCriterion> criteria) {
            double time = System.currentTimeMillis() * 0.001;
            float zOffset = (float) Math.sin(time) * 0.5f; // Oscillate ±0.5 units vertically.
            Transform3D transform = new Transform3D();
            transform.setTranslation(new Vector3f(post.x, post.y , post.z+ zOffset));
            targetTG.setTransform(transform);       // Apply the transformation.
            wakeupOn(wakeup);                       // Continue the behavior.
        }
    }

    // Behavior to rotate the gas effect around the sphere.
    private class GasRotationBehavior extends Behavior {
        private TransformGroup targetTG;
        private WakeupOnElapsedFrames wakeup = new WakeupOnElapsedFrames(0);

        public GasRotationBehavior(TransformGroup targetTG) {
            this.targetTG = targetTG;
            setSchedulingBounds(new BoundingSphere(new Point3d(0, 0, 0), 1000));
        }

        @Override
        public void initialize() {
            wakeupOn(wakeup);                       // Start the behavior on initialization.
        }

        @Override
        public void processStimulus(Iterator<WakeupCriterion> criteria) {
            double angle = (System.currentTimeMillis() % 10000) / 10000.0 * 2 * Math.PI; // Rotate every 10 seconds.
            Transform3D rotation = new Transform3D();
            rotation.rotY(angle);                   // Rotate around Y-axis for swirling effect.
            targetTG.setTransform(rotation);        // Apply the rotation.
            wakeupOn(wakeup);                       // Continue the behavior.
        }
    }

    // Behavior to check if all tasks are solved and remove the gas sphere.
    private class DisappearBehavior extends Behavior {
        private BranchGroup detachableBG;
        private WakeupOnElapsedFrames wakeup = new WakeupOnElapsedFrames(0);

        public DisappearBehavior(BranchGroup detachableBG) {
            this.detachableBG = detachableBG;
            setSchedulingBounds(new BoundingSphere(new Point3d(0, 0, 0), 1000));
        }

        @Override
        public void initialize() {
            wakeupOn(wakeup);                       // Start the behavior on initialization.
        }

        @Override
        public void processStimulus(Iterator<WakeupCriterion> criteria) {
            boolean allTasksSolved = true;
            for (int i = 1; i <= 8; i++) {         // Check all 8 shelves (shelf_1 to shelf_8).
                String shelfId = "shelf_" + i;
                if (!bookshelfUsage.getOrDefault(shelfId, false)) {
                    allTasksSolved = false;         // If any shelf isn’t solved, keep the sphere.
                    break;
                }
            }
            if (allTasksSolved) {                   // All tasks solved, remove the sphere.
                System.out.println("All tasks solved! Gas sphere disappearing...");
                detachableBG.detach();              // Detach from the scene graph.
                return;                             // Stop the behavior.
            }
            wakeupOn(wakeup);                       // Continue checking.
        }
    }

    // Constructor for the GasSphereObject.
    public GasSphereObject(float x, float y, float z, String sphereTexture, String gasTexture, Map<String, Boolean> bookshelfUsage) {
        super();
        this.scale = 1.0d;                          // Default scale of the object.
        this.post = new Vector3f(x, y, z);          // Initial position of the gas sphere.
        this.texture_name = sphereTexture;          // Texture for the inner sphere.
        this.bookshelfUsage = bookshelfUsage;       // Store reference to bookshelf usage map.

        // Create the inner sphere.
        Appearance sphereApp = obj_Appearance(1.0f); // Appearance with full opacity.
        Sphere sphere = new Sphere(0.2f, Sphere.GENERATE_NORMALS | Sphere.GENERATE_TEXTURE_COORDS, 32, sphereApp);

        // Create the gas effect (outer sphere).
        Appearance gasApp = new Appearance();
        this.texture_name = gasTexture;             // Temporarily set texture for gas.
        gasApp.setTexture(texture_App(gasTexture)); // Apply gas texture.
        TransparencyAttributes ta = new TransparencyAttributes();
        ta.setTransparencyMode(TransparencyAttributes.BLENDED);
        ta.setTransparency(0.7f);                   // Make gas 70% transparent.
        gasApp.setTransparencyAttributes(ta);
        PolygonAttributes pa = new PolygonAttributes();
        pa.setCullFace(PolygonAttributes.CULL_NONE); // Visible from both sides.
        gasApp.setPolygonAttributes(pa);
        TextureAttributes texAttr = new TextureAttributes();
        texAttr.setTextureMode(TextureAttributes.MODULATE);
        gasApp.setTextureAttributes(texAttr);
        Sphere gasSphere = new Sphere(0.3f, Sphere.GENERATE_NORMALS | Sphere.GENERATE_TEXTURE_COORDS, 32, gasApp);

        // Set up gas rotation TransformGroup.
        TransformGroup gasRotationTG = new TransformGroup();
        gasRotationTG.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
        gasRotationTG.addChild(gasSphere);

        // Combine sphere and gas into a detachable BranchGroup.
        detachableBG = new BranchGroup();
        detachableBG.setCapability(BranchGroup.ALLOW_DETACH); // Allow removal from scene.
        detachableBG.addChild(sphere);
        detachableBG.addChild(gasRotationTG);

        // Set up movement TransformGroup.
        Transform3D initialTransform = new Transform3D();
        initialTransform.setScale(scale);
        initialTransform.setTranslation(post);
        movementTG = new TransformGroup(initialTransform);
        movementTG.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
        movementTG.setUserData("gasSphereObject");

        // Add behaviors to animate and manage the gas sphere.
        MovementBehavior movementBehavior = new MovementBehavior(movementTG);
        GasRotationBehavior gasRotationBehavior = new GasRotationBehavior(gasRotationTG);
        DisappearBehavior disappearBehavior = new DisappearBehavior(detachableBG);
        movementTG.addChild(detachableBG);          // Add detachable group to movement TG.
        movementTG.addChild(movementBehavior);
        movementTG.addChild(gasRotationBehavior);
        movementTG.addChild(disappearBehavior);

        objTG = movementTG;                         // Assign movementTG as the main TransformGroup.
    }

    @Override
    public TransformGroup position_Object() {
        return objTG;                               // Return the main TransformGroup for positioning.
    }

    @Override
    public void add_Child(TransformGroup nextTG) {
        objTG.addChild(nextTG);                     // Add a child TransformGroup to the main TG.
    }
}