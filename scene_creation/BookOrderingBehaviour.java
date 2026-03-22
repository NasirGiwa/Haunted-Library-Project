//package scene_creation;
//
//import org.jogamp.java3d.*;
//import org.jogamp.java3d.utils.picking.PickCanvas;
//import org.jogamp.java3d.utils.picking.PickResult;
//import org.jogamp.vecmath.*;
//
//import javax.swing.*;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Map;
//
//class BookOrderingBehavior extends Behavior {
//    private PickCanvas pickCanvas;
//    private BranchGroup sceneBG;
//    private TransformGroup lastPickedBookTG;
//    private String currentShelfId;
//    private MainClass mainClass;
//    private Appearance originalAppearance;
//    private Appearance highlightAppearance;
//
//    public BookOrderingBehavior(BranchGroup sceneBG, Canvas3D canvas, BoundingSphere bounds, MainClass mainClass) {
//        this.sceneBG = sceneBG;
//        this.mainClass = mainClass;
//        pickCanvas = new PickCanvas(canvas, sceneBG);
//        pickCanvas.setMode(PickCanvas.GEOMETRY);
//        setSchedulingBounds(bounds);
//
//        highlightAppearance = new Appearance();
//        ColoringAttributes ca = new ColoringAttributes();
//        ca.setColor(new Color3f(1.0f, 0.0f, 0.0f));
//        highlightAppearance.setColoringAttributes(ca);
//        LineAttributes la = new LineAttributes();
//        la.setLineWidth(2.0f);
//        highlightAppearance.setLineAttributes(la);
//        PolygonAttributes pa = new PolygonAttributes();
//        pa.setPolygonMode(PolygonAttributes.POLYGON_LINE);
//        highlightAppearance.setPolygonAttributes(pa);
//    }
//
//    @Override
//    public void initialize() {
//        setEnable(true);
//        wakeupOn(new WakeupOnAWTEvent(java.awt.event.MouseEvent.MOUSE_CLICKED));
//    }
//
//    @Override
//    public void processStimulus(java.util.Iterator<WakeupCriterion> criteria) {
//        while (criteria.hasNext()) {
//            WakeupCriterion wakeup = criteria.next();
//            if (wakeup instanceof WakeupOnAWTEvent) {
//                WakeupOnAWTEvent ev = (WakeupOnAWTEvent) wakeup;
//                java.awt.AWTEvent[] events = ev.getAWTEvent();
//                for (java.awt.AWTEvent event : events) {
//                    if (event instanceof java.awt.event.MouseEvent) {
//                        java.awt.event.MouseEvent mouseEvent = (java.awt.event.MouseEvent) event;
//                        if (mouseEvent.getID() == java.awt.event.MouseEvent.MOUSE_CLICKED) {
//                            updateScene(mouseEvent.getX(), mouseEvent.getY());
//                        }
//                    }
//                }
//            }
//        }
//        wakeupOn(new WakeupOnAWTEvent(java.awt.event.MouseEvent.MOUSE_CLICKED));
//    }
//
//    public void updateScene(int xpos, int ypos) {
//        System.out.println("Mouse clicked at: (" + xpos + ", " + ypos + ")");
//        PickResult pickResult = null;
//        pickCanvas.setShapeLocation(xpos, ypos);
//
//        try {
//            pickResult = pickCanvas.pickClosest();
//            if (pickResult == null) {
//                System.out.println("PickResult is null - nothing picked at (" + xpos + ", " + ypos + ")");
//                return;
//            } else {
//                System.out.println("PickResult found: " + pickResult);
//            }
//        } catch (Exception e) {
//            System.out.println("Exception during picking: " + e.getMessage());
//            e.printStackTrace();
//            return;
//        }
//
//        Node node = pickResult.getNode(PickResult.TRANSFORM_GROUP);
//        if (node == null) {
//            node = pickResult.getNode(PickResult.SHAPE3D);
//            if (node != null) {
//                System.out.println("Picked Shape3D: " + node);
//                while (node != null && !(node instanceof TransformGroup && node.getUserData() instanceof Map)) {
//                    node = node.getParent();
//                }
//                if (node == null) {
//                    System.out.println("Could not find TransformGroup with userData after picking Shape3D");
//                    return;
//                }
//                System.out.println("Found TransformGroup after Shape3D: " + node);
//            } else {
//                System.out.println("No TransformGroup or Shape3D picked in PickResult");
//                return;
//            }
//        } else {
//            System.out.println("Picked TransformGroup: " + node);
//        }
//
//        Object userDataObj = node.getUserData();
//        if (userDataObj == null) {
//            System.out.println("Picked node has no userData");
//            return;
//        } else {
//            System.out.println("Picked node userData: " + userDataObj);
//        }
//
//        if (!(userDataObj instanceof Map)) {
//            System.out.println("Picked node userData is not a Map: " + userDataObj.getClass());
//            return;
//        }
//
//        Map<String, String> userData = (Map<String, String>) userDataObj;
//        if (!"book".equals(userData.get("type"))) {
//            System.out.println("Picked object is not a book: " + userData);
//            return;
//        }
//
//        TransformGroup bookTG = (TransformGroup) node;
//        TransformGroup shelfTG = findParentShelf(bookTG);
//        if (shelfTG == null) {
//            System.out.println("Could not find parent shelf for book");
//            return;
//        }
//
//        currentShelfId = (String) shelfTG.getUserData();
//        System.out.println("Picked book on shelf: " + currentShelfId);
//
//        if (MainClass.bookshelfUsage.getOrDefault(currentShelfId, false)) {
//            System.out.println(currentShelfId + " has already been solved!");
//            JOptionPane.showMessageDialog(null, currentShelfId + " has already been solved!");
//            return;
//        }
//
//        String[] options = {"Solve Puzzle", "Swap Books", "Cancel"};
//        int choice = JOptionPane.showOptionDialog(
//                null,
//                "What would you like to do with the books on " + currentShelfId + "?",
//                "Book Action",
//                JOptionPane.DEFAULT_OPTION,
//                JOptionPane.QUESTION_MESSAGE,
//                null,
//                options,
//                options[1]
//        );
//
//        switch (choice) {
//            case 0: // Solve Puzzle
//                solvePuzzle(currentShelfId);
//                break;
//            case 1: // Swap Books
//                handleBookSwap(bookTG);
//                break;
//            case 2: // Cancel
//                System.out.println("User canceled action on shelf: " + currentShelfId);
//                break;
//            default:
//                System.out.println("Invalid choice on shelf: " + currentShelfId);
//                break;
//        }
//    }
//
//    private void handleBookSwap(TransformGroup bookTG) {
//        if (lastPickedBookTG != null && lastPickedBookTG != bookTG) {
//            setBookAppearance(bookTG, highlightAppearance);
//            Map<String, String> userData1 = (Map<String, String>) lastPickedBookTG.getUserData();
//            Map<String, String> userData2 = (Map<String, String>) bookTG.getUserData();
//            int option = JOptionPane.showConfirmDialog(
//                    null,
//                    "Swap " + userData1.get("texture") + " with " + userData2.get("texture") + "?",
//                    "Confirm Swap",
//                    JOptionPane.YES_NO_OPTION
//            );
//
//            resetBookAppearance(lastPickedBookTG);
//            resetBookAppearance(bookTG);
//
//            if (option == JOptionPane.YES_OPTION) {
//                swapBooks(lastPickedBookTG, bookTG);
//                System.out.println("User confirmed swap");
//
//                if (Library.checkAndShuffleShelf(currentShelfId)) {
//                    System.out.println("Task completed for shelf: " + currentShelfId);
//                    mainClass.incrementPoints();
//                    mainClass.markShelfAsUsed(currentShelfId);
//                    mainClass.checkWinCondition();
//                }
//            } else {
//                System.out.println("User canceled swap");
//            }
//            lastPickedBookTG = null;
//        } else {
//            if (lastPickedBookTG != null) {
//                resetBookAppearance(lastPickedBookTG);
//            }
//            lastPickedBookTG = bookTG;
//            originalAppearance = getBookAppearance(bookTG);
//            setBookAppearance(bookTG, highlightAppearance);
//            System.out.println("First book selected: " + ((Map<String, String>) bookTG.getUserData()).get("texture"));
//        }
//    }
//
//    private void solvePuzzle(String shelfId) {
//        List<TransformGroup> bookRows = Library.shelfBooksMap.get(shelfId);
//        if (bookRows == null) {
//            System.out.println("Shelf not found for solving: " + shelfId);
//            return;
//        }
//
//        // Collect all books and their textures
//        List<TransformGroup> allBooks = new ArrayList<>();
//        List<String> bookTextures = new ArrayList<>();
//        List<Vector3f> startPositions = new ArrayList<>();
//        for (TransformGroup row : bookRows) {
//            for (int i = 0; i < row.numChildren(); i++) {
//                TransformGroup bookTG = (TransformGroup) row.getChild(i);
//                allBooks.add(bookTG);
//                Map<String, String> userData = (Map<String, String>) bookTG.getUserData();
//                bookTextures.add(userData.get("texture"));
//                Transform3D transform = new Transform3D();
//                bookTG.getTransform(transform);
//                Vector3f position = new Vector3f();
//                transform.get(position);
//                startPositions.add(position);
//            }
//        }
//
//        // Launch BookOrderingGame
//        BookOrderingGame game = new BookOrderingGame(bookTextures);
//        game.setVisible(true);
//
//        // Wait for the game to complete (BookOrderingGame should set a flag or return the sorted order)
//        // For simplicity, we'll assume BookOrderingGame has a method to get the sorted order after completion
//        List<String> sortedTextures = game.getSortedTextures(); // This method needs to be implemented in BookOrderingGame
//
//        if (sortedTextures == null || sortedTextures.isEmpty()) {
//            System.out.println("Puzzle solving canceled or failed for shelf: " + shelfId);
//            return;
//        }
//
//        // Map the sorted textures back to the TransformGroups
//        List<TransformGroup> sortedBooks = new ArrayList<>();
//        for (String texture : sortedTextures) {
//            for (TransformGroup bookTG : allBooks) {
//                Map<String, String> userData = (Map<String, String>) bookTG.getUserData();
//                if (texture.equals(userData.get("texture")) && !sortedBooks.contains(bookTG)) {
//                    sortedBooks.add(bookTG);
//                    break;
//                }
//            }
//        }
//
//        // Compute target positions (same as start positions but in sorted order)
//        List<Vector3f> targetPositions = new ArrayList<>();
//        for (int i = 0; i < allBooks.size(); i++) {
//            int sortedIndex = sortedBooks.indexOf(allBooks.get(i));
//            targetPositions.add(startPositions.get(sortedIndex));
//        }
//
//        // Reattach books to their rows in sorted order
//        int bookIndex = 0;
//        for (TransformGroup row : bookRows) {
//            int booksPerRow = row.numChildren();
//            row.removeAllChildren();
//            for (int i = 0; i < booksPerRow && bookIndex < sortedBooks.size(); i++) {
//                row.addChild(sortedBooks.get(bookIndex++));
//            }
//        }
//
//        // Animate the sorting
//        SwapBehavior swapBehavior = new SwapBehavior(sortedBooks, startPositions, targetPositions);
//        swapBehavior.setSchedulingBounds(new BoundingSphere(new Point3d(0.0, 0.0, 0.0), 100.0));
//        TransformGroup tempTG = new TransformGroup();
//        tempTG.addChild(swapBehavior);
//
//        Node parentNode = bookRows.get(0).getParent();
//        if (parentNode instanceof Group) {
//            Group parentGroup = (Group) parentNode;
//            parentGroup.addChild(tempTG);
//        } else {
//            System.out.println("Error: Parent of book row is not a Group: " + parentNode);
//            return;
//        }
//
//        System.out.println("Books sorted on shelf: " + shelfId + " with animation");
//
//        // Since the puzzle is solved, mark the shelf as used and increment points
//        mainClass.incrementPoints();
//        mainClass.markShelfAsUsed(shelfId);
//        mainClass.checkWinCondition();
//    }
//
//    private TransformGroup findParentShelf(TransformGroup bookTG) {
//        Node parent = bookTG.getParent();
//        while (parent != null) {
//            if (parent instanceof TransformGroup) {
//                TransformGroup parentTG = (TransformGroup) parent;
//                Object userData = parentTG.getUserData();
//                if (userData != null && userData.toString().startsWith("shelf_")) {
//                    return parentTG;
//                }
//            }
//            parent = parent.getParent();
//        }
//        return null;
//    }
//
//    private void swapBooks(TransformGroup book1TG, TransformGroup book2TG) {
//        TransformGroup row1 = (TransformGroup) book1TG.getParent();
//        TransformGroup row2 = (TransformGroup) book2TG.getParent();
//
//        int index1 = -1, index2 = -1;
//        for (int i = 0; i < row1.numChildren(); i++) {
//            if (row1.getChild(i) == book1TG) {
//                index1 = i;
//                break;
//            }
//        }
//        for (int i = 0; i < row2.numChildren(); i++) {
//            if (row2.getChild(i) == book2TG) {
//                index2 = i;
//                break;
//            }
//        }
//
//        if (index1 == -1 || index2 == -1) {
//            System.out.println("Error: Could not find book indices for swapping.");
//            return;
//        }
//
//        Map<String, String> userData1 = (Map<String, String>) book1TG.getUserData();
//        Map<String, String> userData2 = (Map<String, String>) book2TG.getUserData();
//        System.out.println("Swapping books: " + userData1.get("texture") + " with " + userData2.get("texture"));
//        System.out.println("Book 1 at index " + index1 + " in row1, Book 2 at index " + index2 + " in row2");
//
//        Transform3D transform1 = new Transform3D();
//        book1TG.getTransform(transform1);
//        Vector3f startPos1 = new Vector3f();
//        transform1.get(startPos1);
//
//        Transform3D transform2 = new Transform3D();
//        book2TG.getTransform(transform2);
//        Vector3f startPos2 = new Vector3f();
//        transform2.get(startPos2);
//
//        List<TransformGroup> booksToSwap = new ArrayList<>();
//        booksToSwap.add(book1TG);
//        booksToSwap.add(book2TG);
//
//        List<Vector3f> startPositions = new ArrayList<>();
//        startPositions.add(startPos1);
//        startPositions.add(startPos2);
//
//        List<Vector3f> targetPositions = new ArrayList<>();
//        targetPositions.add(startPos2);
//        targetPositions.add(startPos1);
//
//        row1.removeChild(index1);
//        row2.removeChild(index2);
//        row1.insertChild(book2TG, index1);
//        row2.insertChild(book1TG, index2);
//
//        SwapBehavior swapBehavior = new SwapBehavior(booksToSwap, startPositions, targetPositions);
//        swapBehavior.setSchedulingBounds(new BoundingSphere(new Point3d(0.0, 0.0, 0.0), 100.0));
//        TransformGroup tempTG = new TransformGroup();
//        tempTG.addChild(swapBehavior);
//
//        Node parentNode = row1.getParent();
//        if (parentNode instanceof Group) {
//            Group parentGroup = (Group) parentNode;
//            parentGroup.addChild(tempTG);
//        } else {
//            System.out.println("Error: Parent of row1 is not a Group: " + parentNode);
//            return;
//        }
//
//        System.out.println("Swap initiated with animation: Book 1 to index " + index2 + " in row2, Book 2 to index " + index1 + " in row1");
//    }
//
//    private Shape3D findBookShape(TransformGroup bookTG) {
//        if (bookTG.numChildren() > 0) {
//            Node child = bookTG.getChild(0);
//            if (child instanceof TransformGroup) {
//                TransformGroup objTG = (TransformGroup) child;
//                if (objTG.numChildren() > 0) {
//                    Node grandChild = objTG.getChild(0);
//                    if (grandChild instanceof TransformGroup) {
//                        TransformGroup objRG = (TransformGroup) grandChild;
//                        if (objRG.numChildren() > 0) {
//                            Node greatGrandChild = objRG.getChild(0);
//                            if (greatGrandChild instanceof Shape3D) {
//                                return (Shape3D) greatGrandChild;
//                            } else {
//                                System.out.println("Great-grandchild is not a Shape3D: " + greatGrandChild);
//                            }
//                        } else {
//                            System.out.println("objRG has no children");
//                        }
//                    } else {
//                        System.out.println("Grandchild is not a TransformGroup: " + grandChild);
//                    }
//                } else {
//                    System.out.println("objTG has no children");
//                }
//            } else {
//                System.out.println("Child is not a TransformGroup: " + child);
//            }
//        } else {
//            System.out.println("Book TransformGroup has no children");
//        }
//        return null;
//    }
//
//    private Appearance getBookAppearance(TransformGroup bookTG) {
//        Shape3D bookShape = findBookShape(bookTG);
//        if (bookShape != null) {
//            return bookShape.getAppearance();
//        }
//        return null;
//    }
//
//    private void setBookAppearance(TransformGroup bookTG, Appearance appearance) {
//        Shape3D bookShape = findBookShape(bookTG);
//        if (bookShape != null) {
//            System.out.println("Setting appearance for book with texture: " + ((Map<String, String>) bookTG.getUserData()).get("texture"));
//            bookShape.setAppearance(appearance);
//        } else {
//            System.out.println("Could not find Shape3D for book");
//        }
//    }
//
//    private void resetBookAppearance(TransformGroup bookTG) {
//        if (originalAppearance != null) {
//            setBookAppearance(bookTG, originalAppearance);
//        }
//    }
//}