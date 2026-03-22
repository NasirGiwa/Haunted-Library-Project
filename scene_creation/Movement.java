package scene_creation;

import org.jogamp.java3d.*;
import org.jogamp.vecmath.*;

import java.awt.event.KeyEvent;
import java.util.Iterator;

public class Movement {
    private static final float MOVE_STEP = 0.15f;
    private static final float JUMP_HEIGHT = 1.0f;
    private static final float CROUCH_HEIGHT = 1.0f;
    private static final float DOOR_INTERACT_DISTANCE = 5.0f;
    private static final float EYE_HEIGHT = 0.28f; // Add an eye height offset to raise the camera
    private static final float GRAVITY = -0.05f; // Gravity acceleration per frame
    private static final float TERMINAL_VELOCITY = -0.5f; // Maximum falling speed
    private float yaw = 0.0f;
    private float pitch = 0.0f;
    private float rotationSensitivity = 0.005f;
    private final TransformGroup viewTG;
    private boolean isCrouching = false;
    private boolean isJumping = false;
    private float verticalVelocity = 0.0f; // For jumping and gravity
    private float defaultHeight;
    private Transform3D lastViewTransform = new Transform3D();

    public Movement(TransformGroup viewTG) {
        this.viewTG = viewTG;
        this.defaultHeight = Library.position.y; // Should be 2.0f
        lastViewTransform.setTranslation(Library.position);
        System.out.println("Initial defaultHeight: " + defaultHeight);
    }

    // Method to create and return the BranchGroup containing the GravityBehavior
    public BranchGroup createGravityBehavior() {
        BranchGroup behaviorBG = new BranchGroup();
        GravityBehavior gravityBehavior = new GravityBehavior(this);
        gravityBehavior.setSchedulingBounds(new BoundingSphere(new Point3d(0, 0, 0), 100.0));
        behaviorBG.addChild(gravityBehavior);
        return behaviorBG;
    }

    public Vector3f getPosition() {
        return Library.position;
    }

    protected static void updatePosition() {
        Transform3D transform = new Transform3D();
        transform.setTranslation(Library.position);
        Library.characterTG.setTransform(transform);
    }

    public void keyPressed(KeyEvent e) {
        float moveX = 0, moveZ = 0, moveY = 0;
        boolean updatePosition = false;

        switch (e.getKeyCode()) {
            case KeyEvent.VK_W:
            case KeyEvent.VK_UP:
                moveX = -(float) (Math.sin(yaw) * Math.cos(pitch)) * MOVE_STEP;
                moveZ = -(float) (Math.cos(yaw) * Math.cos(pitch)) * MOVE_STEP;
                updatePosition = true;
                break;
            case KeyEvent.VK_S:
            case KeyEvent.VK_DOWN:
                moveX = (float) (Math.sin(yaw) * Math.cos(pitch)) * MOVE_STEP;
                moveZ = (float) (Math.cos(yaw) * Math.cos(pitch)) * MOVE_STEP;
                updatePosition = true;
                break;
            case KeyEvent.VK_A:
            case KeyEvent.VK_LEFT:
                moveX = -(float) Math.cos(yaw) * MOVE_STEP;
                moveZ = (float) Math.sin(yaw) * MOVE_STEP;
                updatePosition = true;
                break;
            case KeyEvent.VK_D:
            case KeyEvent.VK_RIGHT:
                moveX = (float) Math.cos(yaw) * MOVE_STEP;
                moveZ = -(float) Math.sin(yaw) * MOVE_STEP;
                updatePosition = true;
                break;
            case KeyEvent.VK_SPACE:
                if (!isJumping && !isCrouching && Math.abs(Library.position.y - defaultHeight) < 0.1f) {
                    isJumping = true;
                    verticalVelocity = 0.3f; // Initial upward velocity for jump
                    System.out.println("Jump initiated with velocity: " + verticalVelocity);
                } else {
                    System.out.println("Cannot jump: isJumping=" + isJumping + ", isCrouching=" + isCrouching + ", y=" + Library.position.y + ", defaultHeight=" + defaultHeight);
                }
                break;
            case KeyEvent.VK_CONTROL:
                if (!isCrouching && !isJumping) {
                    moveY = -CROUCH_HEIGHT;
                    isCrouching = true;
                    updatePosition = true;
                    System.out.println("Crouching to height: " + (Library.position.y + moveY));
                }
                break;
            case KeyEvent.VK_T:
                toggleNearbyDoors();
                break;
            case KeyEvent.VK_H: // Debug key to reset height
                Library.position.y = defaultHeight;
                verticalVelocity = 0.0f;
                updatePosition();
                System.out.println("Height reset to: " + Library.position);
                updateLook();
                break;
            default:
                return;
        }

        if (updatePosition) {
            Vector3f proposedPosition = new Vector3f(Library.position);
            proposedPosition.x += moveX;
            proposedPosition.z += moveZ;
            proposedPosition.y += moveY;

            if (tryMove(proposedPosition)) {
                Library.lastSafePosition.set(Library.position);
                Library.position.set(proposedPosition);
                updatePosition();
               // System.out.println("Moved to: " + Library.position);
            } else {
               // System.out.println("Blocked by collision at: " + proposedPosition);
            }
            updateLook();
        }
    }

    public void keyReleased(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_CONTROL && isCrouching) {
            Vector3f proposedPosition = new Vector3f(Library.position);
            proposedPosition.y = defaultHeight;

            System.out.println("Attempting to stand up from " + Library.position.y + " to " + defaultHeight);

            if (tryMove(proposedPosition)) {
                Library.lastSafePosition.set(Library.position);
                Library.position.set(proposedPosition);
                updatePosition();
                System.out.println("Successfully stood up to: " + Library.position);
            } else {
                System.out.println("Collision detected while trying to stand up at " + proposedPosition);
                // Revert to last safe position if possible
                proposedPosition.set(Library.lastSafePosition);
                if (tryMove(proposedPosition)) {
                    Library.position.set(proposedPosition);
                    updatePosition();
                    System.out.println("Reverted to last safe position: " + Library.position);
                } else {
                    // Force reset to default height as a last resort
                    Library.position.y = defaultHeight;
                    updatePosition();
                    System.out.println("Forced height reset to: " + Library.position);
                }
            }

            isCrouching = false;
            updateLook();
        }
    }

    public void processMouseMovement(int deltaX, int deltaY, boolean shiftDown) {
        if (!shiftDown) {
            yaw += deltaX * rotationSensitivity;
            pitch += deltaY * rotationSensitivity;
            float pitchLimit = (float) Math.toRadians(89);
            pitch = Math.max(-pitchLimit, Math.min(pitchLimit, pitch));
            updateLook();
            System.out.println("Mouse moved: deltaX=" + deltaX + ", deltaY=" + deltaY + ", yaw=" + yaw + ", pitch=" + pitch);
        }
    }

    private void updateLook() {
        Transform3D rotation = new Transform3D();
        rotation.rotY(yaw);
        Transform3D pitchRot = new Transform3D();
        pitchRot.rotX(pitch);
        rotation.mul(pitchRot);
        Transform3D translation = new Transform3D();
        translation.setTranslation(new Vector3f(Library.position.x, Library.position.y + EYE_HEIGHT, Library.position.z));
        Transform3D viewTransform = new Transform3D();
        viewTransform.mul(translation, rotation);
        viewTG.setTransform(viewTransform);
        viewTG.getTransform(lastViewTransform);
    }

    private boolean tryMove(Vector3f proposedPosition) {
        Transform3D tempTransform = new Transform3D();
        tempTransform.setTranslation(proposedPosition);
        Library.characterTG.setTransform(tempTransform);

        CollisionDetectCharacter.colliding = false;

        if (CollisionDetectCharacter.colliding) {
            System.out.println("Collision detected at " + proposedPosition + ", reverting to " + Library.position);
            tempTransform.setTranslation(Library.position);
            Library.characterTG.setTransform(tempTransform);
            return false;
        }
        return true;
    }

    public void restoreViewState() {
        viewTG.setTransform(lastViewTransform);
        Library.characterTG.setTransform(lastViewTransform);
        Library.position.set(getPositionFromTransform(lastViewTransform));
        System.out.println("View restored to transform position: " + Library.position + ", yaw: " + yaw + ", pitch: " + pitch);
    }

    private Vector3f getPositionFromTransform(Transform3D transform) {
        Vector3f pos = new Vector3f();
        transform.get(pos);
        return pos;
    }

    public void saveViewState() {
        viewTG.getTransform(lastViewTransform);
        System.out.println("Saved view transform: " + getPositionFromTransform(lastViewTransform));
    }

    protected void toggleNearbyDoors() {
        if (Library.doors == null || Library.doors.isEmpty()) {
            System.out.println("No doors defined in Library!");
            return;
        }

        System.out.println("Checking doors... Total doors: " + Library.doors.size());
        Vector3f playerPos = getPosition();
        System.out.println("Player position: " + playerPos);

        // Determine the state of the doors (open or closed) based on the first door within range
        boolean shouldOpen = true; // Default to opening
        for (TransformGroup doorTG : Library.doors) {
            Vector3f doorPos = new Vector3f();
            Transform3D doorTransform = new Transform3D();
            doorTG.getTransform(doorTransform);
            doorTransform.get(doorPos);

            float dx = playerPos.x - doorPos.x;
            float dy = playerPos.y - doorPos.y;
            float dz = playerPos.z - doorPos.z;
            float distance = (float) Math.sqrt(dx * dx + dy * dy + dz * dz);

            if (distance <= DOOR_INTERACT_DISTANCE) {
                Object userData = doorTG.getUserData();
                System.out.println("Door userData: " + userData);
                boolean isOpen = userData instanceof String && ((String) userData).startsWith("door_open");
                shouldOpen = !isOpen; // If any door is open, we’ll close all; if closed, we’ll open all
                break;
            }
        }

        // Now toggle all doors within range to the same state
        for (TransformGroup doorTG : Library.doors) {
            Vector3f doorPos = new Vector3f();
            Transform3D doorTransform = new Transform3D();
            doorTG.getTransform(doorTransform);
            doorTransform.get(doorPos);

            float dx = playerPos.x - doorPos.x;
            float dy = playerPos.y - doorPos.y;
            float dz = playerPos.z - doorPos.z;
            float distance = (float) Math.sqrt(dx * dx + dy * dy + dz * dz);
            System.out.println("Distance to door at " + doorPos + ": " + distance + " (max allowed: " + DOOR_INTERACT_DISTANCE + ")");

            if (distance <= DOOR_INTERACT_DISTANCE) {
                float doorWidth = 1.0f; // Adjust based on your DoorObject dimensions
                float hingeOffsetX;

                // Determine hinge offset based on door position
                if (doorPos.x < 0) { // Left door (x = -2.5)
                    hingeOffsetX = -doorWidth / 2.0f; // Hinge on the left edge
                } else { // Right door (x = 2.5)
                    hingeOffsetX = doorWidth / -1.2f; // Hinge on the right edge
                }

                Transform3D currentTransform = new Transform3D();
                doorTG.getTransform(currentTransform);

                Transform3D translateToOrigin = new Transform3D();
                translateToOrigin.setTranslation(new Vector3f(hingeOffsetX, 0, 0));

                Transform3D rotation = new Transform3D();
                if (shouldOpen) {
                    // Left door opens inward (toward negative z), right door opens outward (toward positive z)
                    if (doorPos.x < 0) {
                        rotation.rotY(Math.toRadians(45)); // Left door: rotate clockwise to open inward
                    } else {
                        rotation.rotY(Math.toRadians(-45)); // Right door: rotate counterclockwise to open outward
                    }
                    currentTransform.mul(translateToOrigin);
                    currentTransform.mul(rotation);
                    Transform3D translateBack = new Transform3D();
                    translateBack.setTranslation(new Vector3f(-hingeOffsetX, 0, 0));
                    currentTransform.mul(translateBack);
                    doorTG.setTransform(currentTransform);
                    doorTG.setUserData("door_open");
                    System.out.println("Opened door at " + doorPos + " (distance: " + distance + ")");
                } else {
                    // Both doors should close
                    if (doorPos.x < 0) {
                        rotation.rotY(Math.toRadians(-45)); // Left door: rotate counterclockwise to close
                    } else {
                        rotation.rotY(Math.toRadians(45)); // Right door: rotate clockwise to close
                    }
                    currentTransform.mul(translateToOrigin);
                    currentTransform.mul(rotation);
                    Transform3D translateBack = new Transform3D();
                    translateBack.setTranslation(new Vector3f(-hingeOffsetX, 0, 0));
                    currentTransform.mul(translateBack);
                    doorTG.setTransform(currentTransform);
                    doorTG.setUserData("door");
                    System.out.println("Closed door at " + doorPos + " (distance: " + distance + ")");
                }
            }
        }
    }

    // Inner class to handle gravity and falling
    private class GravityBehavior extends Behavior {
        private WakeupOnElapsedFrames wakeup = new WakeupOnElapsedFrames(1);
        private Movement movement;

        public GravityBehavior(Movement movement) {
            this.movement = movement;
        }

        public void initialize() {
            wakeupOn(wakeup);
        }

        public void processStimulus(Iterator<WakeupCriterion> criteria) {
            if (!movement.isCrouching) {
             //   System.out.println("GravityBehavior: y=" + Library.position.y + ", defaultHeight=" + movement.defaultHeight + ", velocity=" + movement.verticalVelocity + ", isJumping=" + movement.isJumping);
                // Apply gravity if the player is above the ground or falling
                if (Library.position.y > movement.defaultHeight || movement.verticalVelocity < 0) {
                    movement.verticalVelocity += GRAVITY;
                    if (movement.verticalVelocity < TERMINAL_VELOCITY) {
                        movement.verticalVelocity = TERMINAL_VELOCITY;
                    }

                    Vector3f proposedPosition = new Vector3f(Library.position);
                    proposedPosition.y += movement.verticalVelocity;

                    if (proposedPosition.y <= movement.defaultHeight) {
                        proposedPosition.y = movement.defaultHeight;
                        movement.verticalVelocity = 0.0f;
                        movement.isJumping = false;
                        System.out.println("Landed at: " + proposedPosition);
                    }

                    if (movement.tryMove(proposedPosition)) {
                        Library.lastSafePosition.set(Library.position);
                        Library.position.set(proposedPosition);
                        updatePosition();
                        System.out.println("Gravity applied, new position: " + Library.position + ", velocity: " + movement.verticalVelocity);
                    } else {
                        // If we hit something while falling, stop falling
                        if (movement.verticalVelocity < 0) {
                            movement.verticalVelocity = 0.0f;
                            movement.isJumping = false;
                            System.out.println("Collision while falling, stopping at: " + Library.position);
                        } else if (movement.verticalVelocity > 0) {
                            // Hit something while jumping (e.g., ceiling)
                            movement.verticalVelocity = 0.0f;
                            System.out.println("Hit ceiling at: " + Library.position + ", starting to fall");
                        }
                    }
                } else {
                    movement.verticalVelocity = 0.0f;
                    movement.isJumping = false;
                }
            }
            movement.updateLook();
            wakeupOn(wakeup);
        }
    }
}