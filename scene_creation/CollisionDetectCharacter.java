package scene_creation;

import java.util.Iterator;

import org.jogamp.java3d.Behavior;
import org.jogamp.java3d.Shape3D;
import org.jogamp.java3d.WakeupCriterion;
import org.jogamp.java3d.WakeupOnCollisionEntry;
import org.jogamp.java3d.WakeupOnCollisionExit;
import org.jogamp.java3d.WakeupOr;

public class CollisionDetectCharacter extends Behavior{
    private Shape3D shape;
    private WakeupOnCollisionEntry wEnter;
    private WakeupOnCollisionExit wExit;
    // Removed Library and Movement instance variables to use Library's static fields directly.
    // protected static boolean colliding remains unchanged.
    protected static boolean colliding = false;                  //Flag to stop movement if collision has occurred

    public CollisionDetectCharacter(Shape3D s) {
        shape = s;
    }

    @Override
    public void initialize() {
        wEnter = new WakeupOnCollisionEntry(shape, WakeupOnCollisionEntry.USE_GEOMETRY);
        wExit  = new WakeupOnCollisionExit(shape, WakeupOnCollisionExit.USE_GEOMETRY);
        wakeupOn(new WakeupOr(new WakeupCriterion[] { wEnter, wExit }));
    }

    @Override
    public void processStimulus(Iterator<WakeupCriterion> criteria) {
        boolean collisionEntry = false;
        boolean collisionExit  = false;

        while (criteria.hasNext()) {
            WakeupCriterion wc = criteria.next();
            if (wc instanceof WakeupOnCollisionEntry)
                collisionEntry = true;
            if (wc instanceof WakeupOnCollisionExit)
                collisionExit = true;
        }

        if (collisionEntry) {
            colliding = true;  // lock movement
            // Revert to last safe position.
            Library.position.set(Library.lastSafePosition); // ADDED: Use Library static fields
            Movement.updatePosition(); // ADDED: Update position via Movement
            System.out.println("Collision detected: reverting to safe position: " + Library.lastSafePosition);
        }
        if (collisionExit) {
            colliding = false; // allow movement again
            // Update lastSafePosition to the current position.
            Library.lastSafePosition.set(Library.position); // ADDED: Use Library static fields
            System.out.println("Collision resolved: updating safe position to: " + Library.position);
        }

        wakeupOn(new WakeupOr(new WakeupCriterion[] { wEnter, wExit }));
    }
}
