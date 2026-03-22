package scene_creation;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.jogamp.java3d.*;
import org.jogamp.java3d.utils.geometry.Sphere;
import org.jogamp.vecmath.*;

// Library class constructs a 3D scene of a haunted library with walls, shelves, doors, and interactive objects.
public class Library {
    private static final int OBJ_NUM = 18;          // Total number of objects in the scene.
    private static Objects[] object3D = new Objects[OBJ_NUM]; // Array of scene objects.
    protected static TransformGroup characterTG;    // TransformGroup for the player character.
    private static TransformGroup Shifted;          // Shifted rows for books on shelves.
    private static TransformGroup Shifted2;
    protected static Vector3f position = new Vector3f(); // Current character position.
    protected static Vector3f lastSafePosition = new Vector3f(); // Last safe character position.
    protected static List<TransformGroup> doors = new ArrayList<>(); // List of doors in the scene.

    // Positions a wall at a specified location using a translation vector.
    private static TransformGroup define_wall(TransformGroup wall, Vector3f vector) {
        TransformGroup WallTG = new TransformGroup();
        Transform3D WallTrans = new Transform3D();
        WallTrans.setTranslation(vector);
        WallTG.setTransform(WallTrans);
        WallTG.addChild(wall);
        return WallTG;
    }

    // Creates a row of books along the Z-axis with specified spacing.
    private static TransformGroup duplicateBooksZAxis(int numBooks, float spacing) {
        TransformGroup booksGroup = new TransformGroup();
        float totalSpacing = (numBooks - 1) * spacing;
        float startZ = -totalSpacing / 2;

        List<String> textures = new ArrayList<>();
        textures.add("RedImage.png");
        textures.add("YellowImage.png");
        textures.add("BlueImage.png");
        Collections.shuffle(textures);

        for (int i = 0; i < numBooks; i++) {
            String texture = textures.get(i % textures.size());
            GroupbooksObject books = new GroupbooksObject(texture, "Groupbooks1");
            TransformGroup bookTG = new TransformGroup();
            bookTG.setCapability(TransformGroup.ALLOW_TRANSFORM_READ);
            bookTG.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
            bookTG.setCapability(TransformGroup.ENABLE_PICK_REPORTING);
            bookTG.setUserData("book");

            Transform3D translation = new Transform3D();
            Vector3f offset = new Vector3f(0f, 0f, startZ + i * spacing);
            translation.setTranslation(offset);
            bookTG.setTransform(translation);
            bookTG.addChild(books.position_Object());
            booksGroup.addChild(bookTG);
        }
        return booksGroup;
    }

    // Creates a single shelf with multiple rows of books.
    private static TransformGroup createShelf(String textureFile, Vector3f translation, int shelfNumber) {
        ShelfObject shelf = new ShelfObject(textureFile);
        TransformGroup ShelfTG = shelf.position_Object();

        TransformGroup booksRowTG1 = duplicateBooksZAxis(3, 0.35f);
        TransformGroup booksRowTG2 = duplicateBooksZAxis(3, 0.35f);
        TransformGroup booksRowTG3 = duplicateBooksZAxis(3, 0.35f);
        TransformGroup booksRowTG4 = duplicateBooksZAxis(3, 0.35f);
        TransformGroup booksRowTG5 = duplicateBooksZAxis(3, 0.35f);

        Transform3D shift = new Transform3D();
        shift.setTranslation(new Vector3f(0, 0.39f, 0f));

        Shifted = new TransformGroup();
        Shifted.setCapability(TransformGroup.ENABLE_PICK_REPORTING);
        Shifted2 = new TransformGroup();
        Shifted2.setCapability(TransformGroup.ENABLE_PICK_REPORTING);
        TransformGroup Shifted3 = new TransformGroup();
        TransformGroup Shifted4 = new TransformGroup();
        TransformGroup Shifted5 = new TransformGroup();

        Shifted.setTransform(shift);
        Shifted.addChild(booksRowTG2);
        shift.setTranslation(new Vector3f(0, 0.79f, 0f));
        Shifted2.setTransform(shift);
        Shifted2.addChild(booksRowTG3);
        shift.setTranslation(new Vector3f(0, -0.39f, 0f));
        Shifted3.setTransform(shift);
        Shifted3.addChild(booksRowTG4);
        shift.setTranslation(new Vector3f(0, -0.79f, 0f));
        Shifted4.setTransform(shift);
        Shifted4.addChild(booksRowTG5);

        Transform3D Offset = new Transform3D();
        Offset.setTranslation(new Vector3f(0.4f, 1.1f, 0.3f));
        TransformGroup Lights = new LightObject("LightTexture.jpg").position_Object();
        Lights.setTransform(Offset);

        Transform3D offset2 = new Transform3D();
        offset2.setTranslation(new Vector3f(5f, 1.1f, 0.3f));
        TransformGroup light2TG = new LightObject("LightTexture.jpg").position_Object();
        light2TG.setTransform(offset2);

        ShelfTG.addChild(light2TG);
        ShelfTG.addChild(booksRowTG1);
        ShelfTG.addChild(Shifted);
        ShelfTG.addChild(Shifted2);
        ShelfTG.addChild(Shifted3);
        ShelfTG.addChild(Shifted4);
        ShelfTG.addChild(Lights);

        TransformGroup positionedShelfTG = new TransformGroup();
        Transform3D transform = new Transform3D();
        transform.rotY(Math.PI / 2);
        transform.setTranslation(translation);
        positionedShelfTG.setTransform(transform);
        positionedShelfTG.addChild(ShelfTG);
        positionedShelfTG.setUserData("shelf_" + shelfNumber);

        return positionedShelfTG;
    }

    // Creates multiple shelves with specified spacing.
    private static TransformGroup createShelves(int numShelves, float spacing, String textureFile, int startNumber) {
        TransformGroup shelvesTG = new TransformGroup();
        float totalLength = (numShelves - 1) * spacing;
        float startZ = -totalLength / 2;

        for (int i = 0; i < numShelves; i++) {
            float zPos = startZ + i * spacing;
            Vector3f shelfPos = new Vector3f(0f, 2f, zPos);
            TransformGroup shelfTG = createShelf(textureFile, shelfPos, startNumber + i);
            shelvesTG.addChild(shelfTG);
        }
        return shelvesTG;
    }

    // Constructs the library scene with all objects.
    protected static TransformGroup create_Library(Map<String, Boolean> bookshelfUsage) {
        TransformGroup libraryTG = new TransformGroup();
        System.out.println("Creating Library scene...");

        characterTG = new TransformGroup();
        characterTG.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);

        Appearance sphereAppearance = new Appearance();
        Material sphereMaterial = new Material();
        sphereMaterial.setAmbientColor(new Color3f(0.3f, 0.3f, 0.3f));
        sphereMaterial.setDiffuseColor(new Color3f(0.7f, 0.7f, 0.7f));
        sphereMaterial.setSpecularColor(new Color3f(1.0f, 1.0f, 1.0f));
        sphereMaterial.setShininess(32.0f);
        sphereMaterial.setLightingEnable(true);
        sphereAppearance.setMaterial(sphereMaterial);

        Sphere character = new Sphere(0.2f, sphereAppearance);
        characterTG.addChild(character);

        Transform3D Offset1 = new Transform3D();
        position.set(0.0f, 2f, 0.0f);
        Offset1.setTranslation(new Vector3f(0f, 2f, 0.0f));
        characterTG.setTransform(Offset1);
        lastSafePosition.set(position);

        Shape3D characterShape = (Shape3D) character.getChild(0);
        CollisionDetectCharacter cds = new CollisionDetectCharacter(characterShape);
        cds.setSchedulingBounds(new BoundingSphere(new Point3d(0, 0, 0), 100));
        characterTG.addChild(cds);

        object3D[0] = new SquareShape("CarpetTexture.png", 6f, 0.01f, 8f, 10f);
        object3D[1] = new SquareShape("beige_image2.jpg", 6f, 4f, 0.05f, 10f);
        object3D[2] = new SquareShape("Capture2.JPG", 9f, 4f, 0.9f, 1.6f);
        object3D[3] = new WallObject("beige_image2.jpg");
        object3D[4] = new SquareShape("beige_image2.jpg", 0.05f, 4f, 14f, 10f);
        object3D[5] = new SquareShape("ImageFloor2.jpg", 6f, 0.01f, 15f, 5f);
        object3D[6] = new DoorObject("DoorTexture.jfif");
        object3D[7] = new DoorObject("DoorTexture.jfif");
        object3D[8] = new SquareShape("browntiledtexture.png", 6f, 0.01f, 7f, 5f);
        object3D[9] = new HandleObject("ImageMetal2.jpg", "DoorHandleLeft");
        object3D[10] = new HandleObject("ImageMetal2.jpg", "DoorHandleRight");
        object3D[11] = new CubicleObject("chairtexture.png");
        object3D[12] = new SquareShape("beige_image2.jpg", 0.05f, 4f, 9f, 10f);
        object3D[13] = new GhostObject("GhostTexture.png");
        object3D[14] = new GasSphereObject(-1.5f, 1.0f, -0.9f, "SphereTexture.jpg", "GasTexture.png", bookshelfUsage);

        TransformGroup gasSphereObjectTG = object3D[14].position_Object();
        libraryTG.addChild(gasSphereObjectTG);

        Transform3D scaleTransform = new Transform3D();
        scaleTransform.setScale(object3D[7].scale);
        Transform3D rotation = new Transform3D();
        rotation.rotY(Math.PI);
        Transform3D translation = new Transform3D();
        translation.setTranslation(new Vector3f(0.185f, -0.03f, 0f));
        Transform3D combined = new Transform3D();
        combined.mul(translation, rotation);
        combined.mul(scaleTransform);
        object3D[7].objTG.setTransform(combined);

        TransformGroup door1TG = object3D[6].position_Object();
        door1TG.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
        door1TG.setUserData("door");
        object3D[3].add_Child(door1TG);
        doors.add(door1TG);

        TransformGroup door2TG = object3D[7].position_Object();
        door2TG.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
        door2TG.setUserData("door");
        object3D[3].add_Child(door2TG);
        doors.add(door2TG);

        Transform3D yAxis = new Transform3D();
        yAxis.rotY(Math.PI / 2);
        yAxis.setTranslation(new Vector3f(0f, 0.15f, -1f));
        TransformGroup frontWallTG = define_wall(object3D[1].position_Object(), new Vector3f(0f, 4f, 13f));
        TransformGroup backWallTG = define_wall(object3D[2].position_Object(), new Vector3f(0f, 4f, -8f));
        TransformGroup leftWallTG = define_wall(object3D[3].position_Object(), new Vector3f(0f, 2f, 0f));
        leftWallTG.setTransform(yAxis);
        TransformGroup rightWallTG = define_wall(object3D[4].position_Object(), new Vector3f(6f, 4f, 0));
        TransformGroup ceilingTG = define_wall(object3D[5].position_Object(), new Vector3f(0f, 4.2f, 0));
        TransformGroup WoodFloorTG = define_wall(object3D[8].position_Object(), new Vector3f(0f, 0.001f, 10.2f));

        TransformGroup shelvesTG1 = createShelves(4, 2f, "BookshelfTexture.jpg", 1);
        TransformGroup shelvesTG2 = createShelves(4, 2f, "BookshelfTexture.jpg", 5);
        Transform3D Offset = new Transform3D();
        Offset.setTranslation(new Vector3f(4.5f, 0.0f, 7f));
        shelvesTG2.setTransform(Offset);
        Transform3D Offset2 = new Transform3D();
        Offset2.setTranslation(new Vector3f(0.5f, 0.0f, 7f));
        shelvesTG1.setTransform(Offset2);

        Transform3D Offset3 = new Transform3D();
        Offset3.setTranslation(new Vector3f(0.65f, 0.0f, 0.1f));
        TransformGroup handleRightTG = new TransformGroup();
        handleRightTG.setTransform(Offset3);
        handleRightTG.addChild(object3D[9].position_Object());

        Transform3D Offset4 = new Transform3D();
        Offset4.setTranslation(new Vector3f(-0.008f, -0.02f, -0.05f));
        Transform3D rotationLeft = new Transform3D();
        rotationLeft.rotY(Math.toRadians(180));
        Transform3D combinedTransform = new Transform3D();
        combinedTransform.mul(Offset4, rotationLeft);
        TransformGroup handleLeftTG = new TransformGroup();
        handleLeftTG.setTransform(combinedTransform);
        handleLeftTG.addChild(object3D[10].position_Object());

        Transform3D Offset5 = new Transform3D();
        Offset5.setTranslation(new Vector3f(-4f, 0.0f, 10f));
        TransformGroup ExtraWallTG = object3D[12].position_Object();
        ExtraWallTG.setTransform(Offset5);

        Transform3D Offset6 = new Transform3D();
        Offset6.setTranslation(new Vector3f(0f, 1f, 0f));
        TransformGroup CubicleTG = new TransformGroup();
        CubicleTG.setTransform(Offset6);
        CubicleTG.addChild(object3D[11].position_Object());

        TransformGroup ghostTG = object3D[13].position_Object();
        Transform3D ghostTransform = new Transform3D();
        ghostTransform.setTranslation(new Vector3f(1f, -2f, 0f));
        ghostTG.setTransform(ghostTransform);

        object3D[0].add_Child(shelvesTG1);
        object3D[0].add_Child(shelvesTG2);
        object3D[0].add_Child(CubicleTG);

        object3D[6].add_Child(handleRightTG);
        object3D[7].add_Child(handleLeftTG);

        libraryTG.addChild(object3D[0].position_Object());
        libraryTG.addChild(frontWallTG);
        libraryTG.addChild(backWallTG);
        libraryTG.addChild(leftWallTG);
        libraryTG.addChild(rightWallTG);
        libraryTG.addChild(ceilingTG);
        libraryTG.addChild(WoodFloorTG);
        libraryTG.addChild(ExtraWallTG);
        libraryTG.addChild(characterTG);
        libraryTG.addChild(ghostTG);

        System.out.println("Library scene graph constructed successfully");
        return libraryTG;
    }

    public static Objects[] getObjects() {
        return object3D;
    }
}