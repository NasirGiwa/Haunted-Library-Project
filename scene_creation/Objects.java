package scene_creation;

import java.io.FileNotFoundException;
import java.util.*;
import org.jogamp.java3d.*;
import org.jogamp.java3d.loaders.*;
import org.jogamp.java3d.loaders.objectfile.ObjectFile;
import org.jogamp.java3d.utils.geometry.Box;
import org.jogamp.java3d.utils.geometry.Primitive;
import org.jogamp.java3d.utils.geometry.Sphere;
import org.jogamp.java3d.utils.image.TextureLoader;
import org.jogamp.vecmath.*;
import java.util.Iterator;
import java.util.Random;

public abstract class Objects {
	protected BranchGroup objBG;                           // load external object to 'objBG'
	protected TransformGroup objTG;                        // use 'objTG' to position an object
	protected TransformGroup objRG;                        // use 'objRG' to rotate an object
	protected double scale;                                // use 'scale' to define scaling
	protected Vector3f post;                               // use 'post' to specify location
	protected Shape3D obj_shape;
	protected static String obj_name; // For FanBlades and Guard. Setting appearance for multiple parts of an object
	protected String texture_name; // Filename for texture string

	protected float x, y, z;    // Dimension for square shape

	public abstract TransformGroup position_Object();      // need to be defined in derived classes
	public abstract void add_Child(TransformGroup nextTG);

	// Added: Cache for loaded textures to avoid redundant loading
	private static Map<String, Texture> textureCache = new HashMap<>();

	/* a function to load and return object shape from the file named 'obj_name' */
	protected static Scene loadShape(String obj_name) {  // Changed to static for shared access
		ObjectFile f = new ObjectFile(ObjectFile.RESIZE, (float) (60 * Math.PI / 180.0));
		Scene s = null;
		try {                                              // load object's definition file to 's'
			s = f.load("Objects/"+obj_name + ".obj");
		} catch (FileNotFoundException e) {
			System.err.println(e);
			System.exit(1);
		} catch (ParsingErrorException e) {
			System.err.println(e);
			System.exit(1);
		} catch (IncorrectFormatException e) {
			System.err.println(e);
			System.exit(1);
		}
		return s;                                          // return the object shape in 's'
	}

	/* function to set 'objTG' and attach object after loading the model from external file */
	protected void transform_Object(String obj_name, float x) {
		this.obj_name = obj_name;
		Transform3D scaler = new Transform3D();
		scaler.setScale(scale);                            // set scale for the 4x4 matrix
		scaler.setTranslation(post);                       // set translations for the 4x4 matrix
		objTG = new TransformGroup(scaler);                // set the translation BG with the 4x4 matrix
		objBG = loadShape(obj_name).getSceneGroup();
		Appearance app = obj_Appearance(x);                 // Create the appearance with texture
		// load external object to 'objBG'
		for (int i = 0; i < objBG.numChildren(); i++) {   // Make all the objects pickable
			Node child = objBG.getChild(i);
			if (child instanceof Shape3D) {
				Shape3D shape = (Shape3D) child;
				shape.setAppearance(app);                  // Apply the appearance to all the nodes
				shape.setCapability(Shape3D.ENABLE_PICK_REPORTING);
				shape.setPickable(true);
			}
		}
		obj_shape = (Shape3D) objBG.getChild(0);           // get and cast the object to 'obj_shape'
		obj_shape.setName(obj_name);                       // use the name to identify the object
	}

	protected Appearance app = new Appearance();
	private int shine = 32;                                // specify common values for object's appearance
	protected Color3f[] mtl_clr = {new Color3f(0.000005f, 0.000005f, 0.000005f),
			new Color3f(0.772500f, 0.654900f, 0.000000f),
			new Color3f(0.175000f, 0.175000f, 0.175000f),
			new Color3f(0.000000f, 0.000000f, 0.000000f)};

	protected static Texture texture_App(String file_name) {
		// Check cache first; return cached texture if available
		if (textureCache.containsKey(file_name)) {
			return textureCache.get(file_name);
		}
		TextureLoader loader = new TextureLoader("Textures/"+file_name, null);
		ImageComponent2D image = loader.getImage();        // get the image
		if (image == null)
			System.out.println("Cannot load file: " + file_name);

		Texture2D texture = new Texture2D(Texture2D.BASE_LEVEL,
				Texture2D.RGBA, image.getWidth(), image.getHeight());
		texture.setImage(0, image);                        // define the texture with the image
		textureCache.put(file_name, texture);              // Store in cache
		return texture;
	}

	protected Appearance obj_Appearance(float x) {
		Material mtl = new Material();                     // define material's attributes
		mtl.setShininess(shine);
		// Dim the colors by multiplying by 0.3f for an ominous vibe
		Color3f dimAmbient = new Color3f(mtl_clr[0].x * 0.3f, mtl_clr[0].y * 0.3f, mtl_clr[0].z * 0.3f);
		Color3f dimDiffuse = new Color3f(mtl_clr[1].x * 0.3f, mtl_clr[1].y * 0.3f, mtl_clr[1].z * 0.3f);
		Color3f dimSpecular = new Color3f(mtl_clr[2].x * 0.3f, mtl_clr[2].y * 0.3f, mtl_clr[2].z * 0.3f);
		mtl.setAmbientColor(dimAmbient);                   // use them to define different materials
		mtl.setDiffuseColor(dimDiffuse);
		mtl.setSpecularColor(dimSpecular);
		mtl.setEmissiveColor(mtl_clr[3]);                  // use it to enlighten a button
		mtl.setLightingEnable(true);

		app.setMaterial(mtl);                              // set appearance's material

		// Set appearance's texture for the object
		TexCoordGeneration tcg = new TexCoordGeneration(TexCoordGeneration.OBJECT_LINEAR,
				TexCoordGeneration.TEXTURE_COORDINATE_2);
		app.setTexCoordGeneration(tcg);
		app.setTexture(texture_App(texture_name)); // add texture

		TextureAttributes textureAttrib = new TextureAttributes();
		textureAttrib.setTextureMode(TextureAttributes.REPLACE);
		app.setTextureAttributes(textureAttrib);

		float scl = x;                                  // need to rearrange the four quarters
		// Prevent zero scaling, which causes issues
		Vector3d scale = new Vector3d(scl, scl, scl);
		Transform3D transMap = new Transform3D();
		transMap.setScale(scale);
		textureAttrib.setTextureTransform(transMap);
		return app;
	}

	// In your abstract class Objects, add the following method:
	public BoundingSphere getCollisionBounds() {
		// If your object has dimensions x, y, z (for example, as used in SquareShape),
		// a rough bounding sphere radius can be computed as half the diagonal:
		double radius = Math.sqrt(Math.pow(x / 2.0, 2) + Math.pow(y / 2.0, 2) + Math.pow(z / 2.0, 2));

		// Use the 'post' vector as the center. Ensure post is initialized!
		if (post == null) {
			post = new Vector3f(0f, 0f, 0f);
		}
		Point3d center = new Point3d(post.x, post.y, post.z);

		return new BoundingSphere(center, radius);
	}
}

// Rest of the classes (WallObject, PillarObject, etc.)
class WallObject extends Objects {
	public WallObject(String texture_name) {                 // Filename for the object
		super();
		this.texture_name = texture_name;
		scale = 5d;                                      // actual scale is 0.3 = 1.0 x 0.3
		post = new Vector3f(0.1f, 0.7f, -4.09f);        // Define the location of the wall object
		transform_Object("DoorOpeningWall", 12f);        // set transformation to 'objTG' and load object file
	}

	public TransformGroup position_Object() {
		objTG.addChild(objBG);
		return objTG;                                    // use 'objTG' to attach "FanSwitch" to the previous TG
	}

	public void add_Child(TransformGroup nextTG) {
		objTG.addChild(nextTG);                          // attach the next transformGroup to 'objTG'
	}
}

class PillarObject extends Objects {
	public PillarObject(String texture_name) {               // Filename for the object
		super();
		this.texture_name = texture_name;
		scale = 5d;                                      // actual scale is 0.3 = 1.0 x 0.3
		post = new Vector3f(-0.17f, -0.03f, 1f);           // Define the location of the wall object
		transform_Object("DoorOpeningWall", 0.1f);       // set transformation to 'objTG' and load object file
	}

	public TransformGroup position_Object() {
		objTG.addChild(objBG);
		return objTG;                                    // use 'objTG' to attach "FanSwitch" to the previous TG
	}

	public void add_Child(TransformGroup nextTG) {
		objTG.addChild(nextTG);                          // attach the next transformGroup to 'objTG'
	}
}

class CubicleObject extends Objects {
	public CubicleObject(String texture_name) {              // Filename for the object
		super();
		this.texture_name = texture_name;
		scale = 1d;                                      // actual scale is 0.3 = 1.0 x 0.3
		post = new Vector3f(0f, 0f, 0f);                // Define the location of the wall object
		transform_Object("Cubicle", 15f);                // set transformation to 'objTG' and load object file
	}

	public TransformGroup position_Object() {
		objTG.addChild(objBG);
		return objTG;                                    // use 'objTG' to attach "FanSwitch" to the previous TG
	}

	public void add_Child(TransformGroup nextTG) {
		objTG.addChild(nextTG);                          // attach the next transformGroup to 'objTG'
	}
}

class ShelfObject extends Objects {
	// Added: Shared geometry for all ShelfObject instances
	private static Geometry shelfGeometry;

	public ShelfObject(String texture_name) {                // Filename for the object
		super();
		this.texture_name = texture_name;
		scale = 2d;                                      // actual scale is 0.3 = 1.0 x 0.3
		post = new Vector3f(-0.5f, 0f, -2.5f);          // location to connect "FanSwitch" with "FanStand"
		// Load geometry only once
		if (shelfGeometry == null) {
			Scene s = loadShape("EmptySelf");
			BranchGroup bg = s.getSceneGroup();
			Shape3D shape = (Shape3D) bg.getChild(0);
			shelfGeometry = shape.getGeometry();
		}
		obj_shape = new Shape3D(shelfGeometry);          // Use shared geometry
		obj_shape.setName("EmptySelf");
		obj_shape.setCapability(Shape3D.ENABLE_PICK_REPORTING);
		obj_shape.setPickable(true);
		Appearance app = obj_Appearance(1f);              // set appearance after converting object node to Shape3D
		obj_shape.setAppearance(app);
	}

	public TransformGroup position_Object() {
		Transform3D scaler = new Transform3D();
		scaler.setScale(scale);
		scaler.setTranslation(post);
		objTG = new TransformGroup(scaler);
		objTG.addChild(obj_shape);                       // Changed from objBG to obj_shape
		return objTG;                                    // use 'objTG' to attach "FanSwitch" to the previous TG
	}

	public void add_Child(TransformGroup nextTG) {
		objTG.addChild(nextTG);                          // attach the next transformGroup to 'objTG'
	}
}
class FullShelfObject extends Objects {
	// Added: Shared geometry for all ShelfObject instances
	private static Geometry shelfGeometry;

	public FullShelfObject(String texture_name) {                // Filename for the object
		super();
		this.texture_name = texture_name;
		scale = 2d;                                      // actual scale is 0.3 = 1.0 x 0.3
		post = new Vector3f(0,0,0);          // location to connect "FanSwitch" with "FanStand"
		// Load geometry only once
		if (shelfGeometry == null) {
			Scene s = loadShape("Fullself");
			BranchGroup bg = s.getSceneGroup();
			Shape3D shape = (Shape3D) bg.getChild(0);
			shelfGeometry = shape.getGeometry();
		}
		obj_shape = new Shape3D(shelfGeometry);          // Use shared geometry
		obj_shape.setName("Fullself");
		obj_shape.setCapability(Shape3D.ENABLE_PICK_REPORTING);
		obj_shape.setPickable(true);
		Appearance app = obj_Appearance(1f);              // set appearance after converting object node to Shape3D
		obj_shape.setAppearance(app);
	}

	public TransformGroup position_Object() {
		Transform3D scaler = new Transform3D();
		scaler.setScale(scale);
		scaler.setTranslation(post);
		objTG = new TransformGroup(scaler);
		objTG.addChild(obj_shape);                       // Changed from objBG to obj_shape
		return objTG;                                    // use 'objTG' to attach "FanSwitch" to the previous TG
	}

	public void add_Child(TransformGroup nextTG) {
		objTG.addChild(nextTG);                          // attach the next transformGroup to 'objTG'
	}
}




class LightObject extends Objects {
	private PointLight light;
	private Appearance lightAppearance; // Store the appearance for flickering

	public LightObject(String texture_name) {
		super();
		this.texture_name = texture_name;
		scale = 0.5d; // Increased scale for debugging (was 0.1)

		// Create a PointLight to illuminate the entire library
		Color3f lightColor = new Color3f(0.8f, 0.8f, 0.8f); // White light
		float baseIntensity = 0.3f; // Base intensity
		light = new PointLight();
		light.setColor(lightColor);
		light.setPosition(new Point3f(0.0f, 0.0f, 0.0f));
		light.setInfluencingBounds(new BoundingSphere(new Point3d(0, 0, 0), 100.0)); // Large bounds to cover the library

		// Load the LightingPanel.obj model with debug output
		//System.out.println("Loading LightingPanel.obj...");
		BranchGroup lightModelBG = null;
		try {
			lightModelBG = loadShape("LightingPanel").getSceneGroup();
			//System.out.println("LightingPanel.obj loaded successfully.");
		} catch (Exception e) {
			//System.out.println("Failed to load LightingPanel.obj: " + e.getMessage());
			e.printStackTrace();
			// Fallback: Create a sphere to ensure something is visible
			lightModelBG = new BranchGroup();
			Sphere fallbackSphere = new Sphere(0.2f, Sphere.GENERATE_NORMALS, 30);
			lightModelBG.addChild(fallbackSphere);
		}

		Shape3D lightShape = null;
		try {
			lightShape = (Shape3D) lightModelBG.getChild(0);
			System.out.println("Light shape retrieved: " + lightShape);
		} catch (Exception e) {
			System.out.println("Failed to retrieve Shape3D from LightingPanel: " + e.getMessage());
			e.printStackTrace();
		}

		// Apply an appearance to the model
		lightAppearance = new Appearance();
		Material lightMaterial = new Material();
		lightMaterial.setEmissiveColor(new Color3f(0.3f, 0.3f, 0.3f)); // White to match the light color
		lightMaterial.setAmbientColor(new Color3f(0.1f, 0.1f, 0.1f));
		lightMaterial.setDiffuseColor(new Color3f(0.2f, 0.2f, 0.2f));
		lightMaterial.setSpecularColor(new Color3f(0.0f, 0.0f, 0.0f)); // No specular highlights
		lightMaterial.setLightingEnable(true);
		lightAppearance.setMaterial(lightMaterial);

		TransparencyAttributes ta = new TransparencyAttributes();
		ta.setTransparencyMode(TransparencyAttributes.BLENDED);
		ta.setTransparency(0.3f); // Reduced transparency for better visibility (was 0.5)
		lightAppearance.setTransparencyAttributes(ta);

		if (lightShape != null) {
			lightShape.setAppearance(lightAppearance);
		} else {
			System.out.println("Light shape is null, cannot set appearance.");
		}

		// Create a TransformGroup for the model with scaling and corrective rotation
		TransformGroup modelTG = new TransformGroup();
		Transform3D transform = new Transform3D();
		transform.setScale(scale);
		// Add a corrective rotation if needed (adjust based on your model's orientation)
//		Transform3D rotation = new Transform3D();
//		rotation.rotX(Math.PI / 2); // Example: Rotate 90 degrees around X-axis (adjust as needed)
//		transform.mul(rotation);
		modelTG.setTransform(transform);
		modelTG.addChild(lightModelBG);

		// Add a flickering behavior
		FlickerBehavior flickerBehavior = new FlickerBehavior(light, baseIntensity, lightAppearance);
		flickerBehavior.setSchedulingBounds(new BoundingSphere(new Point3d(0, 0, 0), 100.0));

		// Add the light, model, and behavior to the scene graph
		objTG = new TransformGroup();
		objTG.addChild(light);
		objTG.addChild(modelTG);
		objTG.addChild(flickerBehavior);
	}

	public TransformGroup position_Object() {
		return objTG;
	}

	public void add_Child(TransformGroup nextTG) {
		objTG.addChild(nextTG);
	}
}

class FlickerBehavior extends Behavior {
	private PointLight light;
	private float baseIntensity;
	private Appearance lightAppearance; // To modify the model's emissive color
	private WakeupOnElapsedFrames wakeup = new WakeupOnElapsedFrames(15); // Slower flicker (every 5 frames)
	private Random random = new Random();
	private SoundManager soundManager;
	private long lastSoundTime = 0;
	private static final long SOUND_COOLDOWN = 5000; // 500 ms cooldown between sound plays

	public FlickerBehavior(PointLight light, float baseIntensity, Appearance lightAppearance) {
		this.light = light;
		this.baseIntensity = baseIntensity;
		this.lightAppearance = lightAppearance;
		this.soundManager = new SoundManager(); // Initialize SoundManager
		light.setCapability(PointLight.ALLOW_COLOR_WRITE); // Allow changing the light's color/intensity
		lightAppearance.getMaterial().setCapability(Material.ALLOW_COMPONENT_WRITE); // Allow changing the material's emissive color
		setSchedulingBounds(new BoundingSphere(new Point3d(0, 0, 0), 100.0));
		setEnable(true); // Ensure the behavior is enabled
	}

	public void initialize() {
		wakeupOn(wakeup);
	}

	public void processStimulus(Iterator<WakeupCriterion> criteria) {
		// Randomly adjust the light's intensity to simulate flickering
		float flicker = 0.3f + random.nextFloat() * 0.9f; // Vary between 0.3 and 1.2 for dramatic flicker
		float newIntensity = baseIntensity * flicker;
		Color3f newColor = new Color3f(newIntensity, newIntensity, newIntensity); // White light (removed blue tint)
		light.setColor(newColor);

		// Update the model's emissive color to match the light's intensity
		lightAppearance.getMaterial().setEmissiveColor(newColor);

		// Play a flickering sound if the intensity drops significantly
		if (newIntensity < baseIntensity * 0.5f) { // Play sound when light dims significantly
			long currentTime = System.currentTimeMillis();
			if (currentTime - lastSoundTime >= SOUND_COOLDOWN) {
				soundManager.playSound("flicker.wav", false);
				lastSoundTime = currentTime;
			}
		}

		wakeupOn(wakeup);
	}
}

class DoorObject extends Objects {
	public DoorObject(String texture_name) {                 // Filename for the object
		super();
		this.texture_name = texture_name;
		scale = 0.4d;                                    // actual scale is 0.3 = 1.0 x 0.3
		post = new Vector3f(-0.17f, -0.03f, 0f);        // location to connect "FanSwitch" with "FanStand"
		transform_Object("doorleft", 0f);                 // set transformation to 'objTG' and load object file
		obj_Appearance(0);                               // set appearance after converting object node to Shape3D
	}

	public TransformGroup position_Object() {
		objTG.addChild(objBG);                           // attach "FanSwitch" to 'objTG'
		return objTG;                                    // use 'objTG' to attach "FanSwitch" to the previous TG
	}

	public void add_Child(TransformGroup nextTG) {
		objTG.addChild(nextTG);                          // attach the next transformGroup to 'objTG'
	}
}

class HandleObject extends Objects {
	public HandleObject(String texture_name, String filename) { // Filename for the object
		super();
		this.texture_name = texture_name;
		scale = 0.4d;                                    // actual scale is 0.3 = 1.0 x 0.3
		post = new Vector3f(0f, 0f, 0f);                // location to connect "FanSwitch" with "FanStand"
		transform_Object(filename, 0);                   // set transformation to 'objTG' and load object file
		obj_Appearance(0);                               // set appearance after converting object node to Shape3D
	}

	public TransformGroup position_Object() {
		objTG.addChild(objBG);                           // attach "FanSwitch" to 'objTG'
		return objTG;                                    // use 'objTG' to attach "FanSwitch" to the previous TG
	}

	public void add_Child(TransformGroup nextTG) {
		objTG.addChild(nextTG);                          // attach the next transformGroup to 'objTG'
	}
}



class GroupbooksObject extends Objects {
	// Added: Shared geometry for all GroupbooksObject instances
	private static Geometry bookGeometry;

	public GroupbooksObject(String texture_name, String object_name) { // Filename for the texture and for the object, since there are two group books
		super();
		this.texture_name = texture_name;
		scale = 0.18d;                                   // actual scale is 0.3 = 1.0 x 0.3
		post = new Vector3f(0f, 0f, 0f);                // location to connect "FanSwitch" with "FanStand"
		// Load geometry only once
		if (bookGeometry == null) {
			Scene s = loadShape(object_name);
			BranchGroup bg = s.getSceneGroup();
			Shape3D shape = (Shape3D) bg.getChild(0);
			bookGeometry = shape.getGeometry();
		}
		obj_shape = new Shape3D(bookGeometry);           // Use shared geometry
		obj_shape.setName(object_name);
		obj_shape.setCapability(Shape3D.ENABLE_PICK_REPORTING);
		obj_shape.setPickable(true);
		Appearance app = obj_Appearance(4);               // set appearance after converting object node to Shape3D
		obj_shape.setAppearance(app);
	}

	public TransformGroup position_Object() {
		Transform3D scaler = new Transform3D();
		scaler.setScale(scale);
		scaler.setTranslation(post);
		objTG = new TransformGroup(scaler);
		// Orientate the group of books properly
		// Create a Transform3D for the Y rotation (90° about Y)
		Transform3D yRotation = new Transform3D();
		yRotation.rotY(Math.PI / 2);

		// Create a Transform3D for the Z rotation (90° about X)
		Transform3D zRotation = new Transform3D();
		zRotation.rotZ(Math.PI / 2);

		zRotation.mul(yRotation);

		// Create a new TransformGroup with the combined rotation
		objRG = new TransformGroup(zRotation);

		// Add your loaded object to the rotation transform group
		objRG.addChild(obj_shape);  // Changed from objBG to obj_shape

		// Attach the rotation group to the main transform group (with scaling/translation)
		objTG.addChild(objRG);

		return objTG;
	}

	public void add_Child(TransformGroup nextTG) {
		objTG.addChild(nextTG);                          // attach the next transformGroup to 'objTG'
	}
}

class SinglebookObject extends Objects {
	public SinglebookObject(String texture_name) {           // Filename for the object
		super();
		this.texture_name = texture_name;
		scale = 0.2d;                                    // actual scale is 0.3 = 1.0 x 0.3
		post = new Vector3f(0f, 1.01f, 0f);             // location to connect "FanSwitch" with "FanStand"
		transform_Object("Singlebook1", 0);              // set transformation to 'objTG' and load object file
		obj_Appearance(0);                               // set appearance after converting object node to Shape3D
	}

	public TransformGroup position_Object() {
		// Orientate the group of books properly
		// Create a Transform3D for the Y rotation (90° about Y)
		Transform3D yRotation = new Transform3D();
		yRotation.rotY(Math.PI / 2);

		// Create a Transform3D for the Z rotation (90° about X)
		Transform3D zRotation = new Transform3D();
		zRotation.rotZ(Math.PI / 2);

		Transform3D xRotation = new Transform3D();
		xRotation.rotX(Math.PI / 2);

		zRotation.mul(yRotation);
		zRotation.mul(xRotation);
		// Create a new TransformGroup with the combined rotation
		objRG = new TransformGroup(zRotation);

		// Add your loaded object to the rotation transform group
		objRG.addChild(objBG);

		// Attach the rotation group to the main transform group (with scaling/translation)
		objTG.addChild(objRG);

		return objTG;
	}

	public void add_Child(TransformGroup nextTG) {
		objTG.addChild(nextTG);                          // attach the next transformGroup to 'objTG'
	}
}

class SquareShape extends Objects {
	private float x, y, z;
	private String texture_name;
	private float textureScale;
	private TransformGroup objTG;
	protected Appearance app;

	public SquareShape(String texture_name, float x, float y, float z, float textureScale) {
		this.x = x;
		this.y = y;
		this.z = z;
		this.texture_name = texture_name;
		this.textureScale = textureScale;
		Transform3D translator = new Transform3D();
		translator.setTranslation(new Vector3d(0.0, -0.8, 0));
		objTG = new TransformGroup(translator);
		objTG.addChild(create_Object());
	}

	protected Node create_Object() {
		app = CommonsSK.set_Appearance(CommonsSK.White);
		// Temporarily use a lighter texture for the ceiling if texture_name is "ImageFloor2.jpg"
		if (texture_name.equals("ImageFloor2.jpg")) {
			app.setTexture(Objects.texture_App("beige_image2.jpg"));
			// Debug: Set emissive color to make ceiling visible regardless of lighting
			Material mtl = new Material();
			mtl.setEmissiveColor(new Color3f(0.2f, 0.2f, 0.2f)); // Bright gray, visible without light
			mtl.setLightingEnable(false); // Ignore lighting for debugging
			app.setMaterial(mtl);
		} else {
			app.setTexture(Objects.texture_App(texture_name));
			Material mtl = new Material();
			mtl.setAmbientColor(new Color3f(0.2f, 0.2f, 0.2f));
			mtl.setDiffuseColor(new Color3f(0.9f, 0.9f, 0.9f));
			mtl.setSpecularColor(new Color3f(0.8f, 0.8f, 0.8f));
			mtl.setShininess(45.0f);
			mtl.setLightingEnable(true);
			app.setMaterial(mtl);
		}

		PolygonAttributes pa = new PolygonAttributes();
		pa.setCullFace(PolygonAttributes.CULL_NONE);
		app.setPolygonAttributes(pa);

		TextureAttributes texAttr = new TextureAttributes();
		texAttr.setTextureMode(TextureAttributes.MODULATE);
		Transform3D texTransform = new Transform3D();
		texTransform.setScale(new Vector3d(textureScale, textureScale, textureScale));
		texAttr.setTextureTransform(texTransform);
		app.setTextureAttributes(texAttr);

		return new Box(x, y, z, Primitive.GENERATE_NORMALS | Primitive.GENERATE_TEXTURE_COORDS, app);
	}

	public TransformGroup position_Object() {
		return objTG;
	}

	public void add_Child(TransformGroup nextTG) {
		objTG.addChild(nextTG);
	}
}

class GhostObject extends Objects {
	private Switch visibilitySwitch;
	private TransformGroup positionTG;

	public GhostObject(String texture_name) {
		super();
		this.texture_name = texture_name;
		scale = 0.5d;

		// Initial position (will be updated later, so this is just a placeholder)
		post = new Vector3f(-0.17f + 0.5f, 0.0f, 0f);

		// Define the appearance for the ghost
		Appearance app = new Appearance();
		TransparencyAttributes ta = new TransparencyAttributes();
		ta.setTransparencyMode(TransparencyAttributes.BLENDED);
		ta.setTransparency(0.0f); // Slightly transparent
		app.setTransparencyAttributes(ta);

		Material mtl = new Material();
		Color3f red = new Color3f(1.0f, 1.0f, 1.0f); // Red color for the ghost
		mtl.setEmissiveColor(red);
		mtl.setAmbientColor(red);
		mtl.setDiffuseColor(red);
		mtl.setSpecularColor(new Color3f(1.0f, 1.0f, 1.0f));
		mtl.setShininess(32.0f);
		mtl.setLightingEnable(true);
		app.setMaterial(mtl);

		// Load the ghost.obj model
		objBG = loadShape("ghost").getSceneGroup();
		Shape3D ghostShape = (Shape3D) objBG.getChild(0);
		ghostShape.setAppearance(app); // Apply the appearance to the ghost

		// Create a TransformGroup for the ghost model with a corrective rotation
		TransformGroup modelTG = new TransformGroup();
		Transform3D correctiveRotation = new Transform3D();
		//correctiveRotation.rotX(Math.PI); // Rotate to correct the model's orientation
		modelTG.setTransform(correctiveRotation);
		modelTG.addChild(objBG);

		// Create a Switch node to control visibility
		visibilitySwitch = new Switch();
		visibilitySwitch.setCapability(Switch.ALLOW_SWITCH_WRITE);
		visibilitySwitch.addChild(modelTG);
		visibilitySwitch.setWhichChild(Switch.CHILD_NONE); // Initially hidden

		// Create a TransformGroup for positioning
		positionTG = new TransformGroup();
		positionTG.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
		Transform3D scaler = new Transform3D();
		scaler.setScale(scale);
		scaler.setTranslation(post);
		positionTG.setTransform(scaler);
		positionTG.addChild(visibilitySwitch);
	}

	public TransformGroup position_Object() {
		objTG = positionTG;
		return objTG;
	}

	public void add_Child(TransformGroup nextTG) {
		objTG.addChild(nextTG);
	}

	// Method to show the ghost
	public void showGhost() {
		visibilitySwitch.setWhichChild(Switch.CHILD_ALL);
	}

	// Method to hide the ghost
	public void hideGhost() {
		visibilitySwitch.setWhichChild(Switch.CHILD_NONE);
	}

	// Method to reposition the ghost
	public void setPosition(Vector3f newPosition) {
		Transform3D transform = new Transform3D();
		transform.setScale(scale);
		transform.setTranslation(newPosition);
		positionTG.setTransform(transform);
	}
}

class ScaleBehavior extends Behavior {
	private TransformGroup targetTG;
	private Alpha alpha;
	private WakeupOnElapsedFrames wakeup = new WakeupOnElapsedFrames(0);

	public ScaleBehavior(TransformGroup targetTG) {
		this.targetTG = targetTG;
		alpha = new Alpha(-1, Alpha.INCREASING_ENABLE | Alpha.DECREASING_ENABLE,
				0, 0, 4000, 0, 0, 0, 0, 0); // 4-second cycle
	}

	public void initialize() {
		wakeupOn(wakeup);
	}

	public void processStimulus(java.util.Iterator<WakeupCriterion> criteria) {
		float value = alpha.value();
		float scale = 1f + 1.2f * (float) Math.sin(2 * Math.PI * value); // Scale between 0.4 and 0.6
		Transform3D transform = new Transform3D();
		transform.setScale(scale);
		targetTG.setTransform(transform);
		wakeupOn(wakeup);
	}
}

class FadeBehavior extends Behavior {
	private Appearance appearance;
	private TransparencyAttributes transparency;
	private Alpha alpha;
	private WakeupOnElapsedFrames wakeup = new WakeupOnElapsedFrames(0);

	public FadeBehavior(Appearance appearance) {
		this.appearance = appearance;
		this.transparency = appearance.getTransparencyAttributes();
		alpha = new Alpha(-1, Alpha.INCREASING_ENABLE | Alpha.DECREASING_ENABLE,
				0, 0, 4000, 0, 0, 0, 0, 0); // 4-second cycle
	}

	public void initialize() {
		wakeupOn(wakeup);
	}

	public void processStimulus(java.util.Iterator<WakeupCriterion> criteria) {
		float value = alpha.value();
		float transparencyValue = 0.3f + 0.4f * (float) Math.sin(2 * Math.PI * value); // Transparency between 0.3 and 0.7
		transparency.setTransparency(transparencyValue);
		wakeupOn(wakeup);
	}
}

class SceneBehavior extends Behavior {
	private List<PointLight> lights = new ArrayList<>();
	private List<Float> baseIntensities = new ArrayList<>();
	private List<TransformGroup> animatedObjects = new ArrayList<>();
	private List<Float> animationPhases = new ArrayList<>();
	private WakeupOnElapsedFrames wakeup = new WakeupOnElapsedFrames(20);
	private Random random = new Random();

	public SceneBehavior() {
		setSchedulingBounds(new BoundingSphere(new Point3d(0, 0, 0), 100.0));
		setEnable(true);
	}

	public void addLight(PointLight light, float baseIntensity) {
		lights.add(light);
		baseIntensities.add(baseIntensity);
	}

	public void addAnimatedObject(TransformGroup tg) {
		animatedObjects.add(tg);
		animationPhases.add(random.nextFloat() * 2 * (float) Math.PI);
	}

	public void initialize() {
		wakeupOn(wakeup);
	}

	public void processStimulus(Iterator<WakeupCriterion> criteria) {
		try {
			// Update lights
			for (int i = 0; i < lights.size(); i++) {
				PointLight light = lights.get(i);
				float baseIntensity = baseIntensities.get(i);
				float flicker = 0.3f + random.nextFloat() * 0.9f;
				float newIntensity = baseIntensity * flicker;
				light.setColor(new Color3f(newIntensity, newIntensity, newIntensity * 0.92f));
			}

			// Update animated objects (e.g., ghost floating)
			for (int i = 0; i < animatedObjects.size(); i++) {
				TransformGroup tg = animatedObjects.get(i);
				float phase = animationPhases.get(i);
				float time = (float) (System.currentTimeMillis() % 4000) / 4000.0f;
				float yOffset = (float) Math.sin(2 * Math.PI * time + phase) * 0.25f;
				Transform3D transform = new Transform3D();
				transform.setTranslation(new Vector3f(0f, yOffset, 0f));
				tg.setTransform(transform);
			}
		} catch (Exception e) {
			System.err.println("Error in SceneBehavior: " + e.getMessage());
			e.printStackTrace();
		}
		wakeupOn(wakeup);
	}
}



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
