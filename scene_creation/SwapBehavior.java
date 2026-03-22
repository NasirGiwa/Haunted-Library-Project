import org.jogamp.java3d.*;
import org.jogamp.vecmath.*;
import java.util.*;

class SwapBehavior extends Behavior {
    private List<TransformGroup> books; // List of book TransformGroups
    private List<Vector3f> startPositions; // Starting positions of the books
    private List<Vector3f> targetPositions; // Target positions after shuffling
    private WakeupOnElapsedFrames wakeup = new WakeupOnElapsedFrames(1); // Update every frame
    private long startTime;
    private static final float DURATION = 2000.0f; // 2 seconds for the animation
    private boolean isFinished = false;

    public SwapBehavior(List<TransformGroup> books, List<Vector3f> startPositions, List<Vector3f> targetPositions) {
        this.books = books;
        this.startPositions = startPositions;
        this.targetPositions = targetPositions;
        this.startTime = System.currentTimeMillis();
    }

    public void initialize() {
        wakeupOn(wakeup);
    }

    public void processStimulus(Iterator<WakeupCriterion> criteria) {
        if (isFinished) {
            return; // Stop processing if the animation is finished
        }

        // Calculate the elapsed time
        long currentTime = System.currentTimeMillis();
        float elapsedTime = (currentTime - startTime) / DURATION;

        // If the animation is complete, set the final positions and stop
        if (elapsedTime >= 1.0f) {
            elapsedTime = 1.0f;
            isFinished = true;
        }

        // Interpolate between start and target positions for each book
        for (int i = 0; i < books.size(); i++) {
            TransformGroup bookTG = books.get(i);
            Vector3f startPos = startPositions.get(i);
            Vector3f targetPos = targetPositions.get(i);

            // Linear interpolation
            float x = startPos.x + (targetPos.x - startPos.x) * elapsedTime;
            float y = startPos.y + (targetPos.y - startPos.y) * elapsedTime;
            float z = startPos.z + (targetPos.z - startPos.z) * elapsedTime;

            // Update the book's position
            Transform3D transform = new Transform3D();
            transform.setTranslation(new Vector3f(x, y, z));
            bookTG.setTransform(transform);
        }

        // Continue the animation if not finished
        if (!isFinished) {
            wakeupOn(wakeup);
        }
    }
}