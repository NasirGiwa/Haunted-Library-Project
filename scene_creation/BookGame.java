package scene_creation;

import org.jogamp.java3d.Transform3D;
import org.jogamp.java3d.TransformGroup;
import org.jogamp.vecmath.Vector3d;
import org.jogamp.vecmath.Vector3f;

public class BookGame{

	 // Method to apply zoom effect on a TransformGroup
		protected void zoomObject(TransformGroup tg, double scale) {
		    Transform3D transform = new Transform3D();
		    tg.getTransform(transform);

		    Vector3d scaleVector = new Vector3d(scale, scale, scale);
		    transform.setScale(scaleVector);

		    tg.setTransform(transform);
		}

	    //For Debugging

	    protected Vector3f getTransformPosition(TransformGroup tg) {
	        Transform3D t = new Transform3D();
	        tg.getTransform(t);
	        Vector3f pos = new Vector3f();
	        t.get(pos);
	        return pos;
	    }

		protected void swapPositions(TransformGroup tg1, TransformGroup tg2) {
		    Transform3D t1 = new Transform3D();
		    Transform3D t2 = new Transform3D();

		    tg1.getTransform(t1);
		    tg2.getTransform(t2);

		    tg1.setTransform(t2);
		    tg2.setTransform(t1);

		    System.out.println("Objects swapped positions!");
		}

}
// NOT USING THIS CODE