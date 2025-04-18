package drawshapes;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;

/**
 * A scene of shapes. Uses the Model-View-Controller (MVC) design pattern,
 * though note that model knows something about the view, as the draw()
 * method both in Scene and in Shape uses the Graphics object. That's kind of
 * sloppy,
 * but it also helps keep things simple.
 * 
 * This class allows us to talk about a "scene" of shapes,
 * rather than individual shapes, and to apply operations
 * to collections of shapes.
 * 
 * @author jspacco
 *
 */
public class Scene implements Iterable<IShape> {
    private List<IShape> shapeList = new LinkedList<IShape>();
    private SelectionRectangle selectRect;
    private boolean isDrag;
    private Point startDrag;
    private Stack<List<IShape>> undoStack = new Stack<>();

    public void updateSelectRect(Point drag) {
        for (IShape s : this) {
            s.setSelected(false);
        }
        if (drag.x > startDrag.x) {
            if (drag.y > startDrag.y) {
                // top-left to bottom-right
                selectRect = new SelectionRectangle(startDrag.x, drag.x, startDrag.y, drag.y);
            } else {
                // bottom-left to top-right
                selectRect = new SelectionRectangle(startDrag.x, drag.x, drag.y, startDrag.y);
            }
        } else {
            if (drag.y > startDrag.y) {
                // top-right to bottom-left
                selectRect = new SelectionRectangle(drag.x, startDrag.x, startDrag.y, drag.y);
            } else {
                // bottom-left to top-right
                selectRect = new SelectionRectangle(drag.x, startDrag.x, drag.y, startDrag.y);
            }
        }
        List<IShape> selectedShapes = this.select(selectRect);
        for (IShape s : selectedShapes) {
            s.setSelected(true);
        }
    }

    public void stopDrag() {
        this.isDrag = false;
    }

    public void startDrag(Point p) {
        this.isDrag = true;
        this.startDrag = p;
    }

    /**
     * Draw all the shapes in the scene using the given Graphics object.
     * 
     * @param g
     */
    public void draw(Graphics g) {
        for (IShape s : shapeList) {
            if (s != null) {
                s.draw(g);
            }
        }
        if (isDrag) {
            selectRect.draw(g);
        }
    }

    /**
     * Get an iterator that can iterate through all the shapes
     * in the scene.
     */
    public Iterator<IShape> iterator() {
        return shapeList.iterator();
    }

    /**
     * Return a list of shapes that contain the given point.
     * 
     * @param point The point
     * @return A list of shapes that contain the given point.
     */
    public List<IShape> select(Point point) {
        List<IShape> selected = new LinkedList<IShape>();
        for (IShape s : shapeList) {
            if (s.contains(point)) {
                selected.add(s);
            }
        }
        return selected;
    }

    /**
     * Return a list of shapes in the scene that intersect the given shape.
     * 
     * @param s The shape
     * @return A list of shapes intersecting the given shape.
     */
    public List<IShape> select(IShape shape) {
        List<IShape> selected = new LinkedList<IShape>();
        for (IShape s : shapeList) {
            if (s.intersects(shape)) {
                selected.add(s);
            }
        }
        return selected;
    }

    /**
     * Add a shape to the scene. It will be rendered next time
     * the draw() method is invoked.
     * 
     * @param s
     */
    public void addShape(IShape s) {
        saveStateForUndo();
        shapeList.add(s);
    }

    /**
     * Remove a list of shapes from the given scene.
     * 
     * @param shapesToRemove
     */
    public void removeShapes(Collection<IShape> shapesToRemove) {
        saveStateForUndo();
        shapeList.removeAll(shapesToRemove);
    }

    /**
     * Save the current state before making changes (for undo)
     */
    private void saveStateForUndo() {
        List<IShape> currentState = new LinkedList<>();
        for (IShape shape : shapeList) {
            currentState.add(shape);
        }
        undoStack.push(currentState);
    }

    /**
     * Undo the last operation
     * 
     * @return true if undo was successful, false if nothing to undo
     */
    public boolean undo() {
        if (undoStack.isEmpty()) {
            return false;
        }

        shapeList = undoStack.pop();
        return true;
    }

    /**
     * Move all selected shapes
     * 
     * @param x amount to move in x direction
     * @param y amount to move in y direction
     */
    public void moveSelectedShapes(int x, int y) {
        boolean hasSelectedShapes = false;
        for (IShape shape : shapeList) {
            if (shape.isSelected() && shape instanceof IMoveableShape) {
                hasSelectedShapes = true;
                break;
            }
        }

        if (hasSelectedShapes) {
            saveStateForUndo();
            for (IShape shape : shapeList) {
                if (shape.isSelected() && shape instanceof IMoveableShape) {
                    ((IMoveableShape) shape).move(x, y);
                }
            }
        }
    }

    /**
     * Scale up all selected shapes
     */
    public void scaleUpSelectedShapes() {
        boolean hasSelectedShapes = false;
        for (IShape shape : shapeList) {
            if (shape.isSelected() && shape instanceof IMoveableShape) {
                hasSelectedShapes = true;
                break;
            }
        }

        if (hasSelectedShapes) {
            saveStateForUndo();
            for (IShape shape : shapeList) {
                if (shape.isSelected() && shape instanceof IMoveableShape) {
                    ((IMoveableShape) shape).scaleUp();
                }
            }
        }
    }

    /**
     * Scale down all selected shapes
     */
    public void scaleDownSelectedShapes() {
        boolean hasSelectedShapes = false;
        for (IShape shape : shapeList) {
            if (shape.isSelected() && shape instanceof IMoveableShape) {
                hasSelectedShapes = true;
                break;
            }
        }

        if (hasSelectedShapes) {
            saveStateForUndo();
            for (IShape shape : shapeList) {
                if (shape.isSelected() && shape instanceof IMoveableShape) {
                    ((IMoveableShape) shape).scaleDown();
                }
            }
        }
    }

    /**
     * Bring selected shapes to the front of the display order
     */
    public void bringToFront() {
        saveStateForUndo();
        List<IShape> selectedShapes = new LinkedList<>();
        List<IShape> unselectedShapes = new LinkedList<>();

        // Separate selected and unselected shapes
        for (IShape shape : shapeList) {
            if (shape.isSelected()) {
                selectedShapes.add(shape);
            } else {
                unselectedShapes.add(shape);
            }
        }

        // Clear the list and add unselected first, then selected on top
        shapeList.clear();
        shapeList.addAll(unselectedShapes);
        shapeList.addAll(selectedShapes);
    }

    /**
     * Send selected shapes to the back of the display order
     */
    public void sendToBack() {
        saveStateForUndo();
        List<IShape> selectedShapes = new LinkedList<>();
        List<IShape> unselectedShapes = new LinkedList<>();

        // Separate selected and unselected shapes
        for (IShape shape : shapeList) {
            if (shape.isSelected()) {
                selectedShapes.add(shape);
            } else {
                unselectedShapes.add(shape);
            }
        }

        // Clear the list and add selected first, then unselected on top
        shapeList.clear();
        shapeList.addAll(selectedShapes);
        shapeList.addAll(unselectedShapes);
    }

    /**
     * Save the scene to a text file
     * 
     * @param filePath the path to save the file
     * @throws IOException if there's an error writing to the file
     */
    public void saveToFile(String filePath) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            for (IShape shape : shapeList) {
                writer.write(shape.toString());
                writer.newLine();
            }
        }
    }

    /**
     * Load a scene from a text file
     * 
     * @param filePath the path to load the file from
     * @throws IOException if there's an error reading the file
     */
    public void loadFromFile(String filePath) throws IOException {
        saveStateForUndo();
        shapeList.clear();

        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.trim().split("\\s+");
                if (parts.length > 0) {
                    if (parts[0].equals("SQUARE")) {
                        // Format: SQUARE x y size COLOR selected
                        int x = Integer.parseInt(parts[1]);
                        int y = Integer.parseInt(parts[2]);
                        int size = Integer.parseInt(parts[3]);
                        Color color = parseColor(parts[4]);
                        boolean selected = Boolean.parseBoolean(parts[5]);

                        Square square = new Square(color, x, y, size);
                        square.setSelected(selected);
                        shapeList.add(square);
                    } else if (parts[0].equals("CIRCLE")) {
                        // Format: CIRCLE x y diameter COLOR selected
                        int x = Integer.parseInt(parts[1]);
                        int y = Integer.parseInt(parts[2]);
                        int diameter = Integer.parseInt(parts[3]);
                        Color color = parseColor(parts[4]);
                        boolean selected = Boolean.parseBoolean(parts[5]);

                        Circle circle = new Circle(color, new Point(x, y), diameter);
                        circle.setSelected(selected);
                        shapeList.add(circle);
                    } else if (parts[0].equals("RECTANGLE")) {
                        // Format: RECTANGLE x y width height COLOR selected
                        int x = Integer.parseInt(parts[1]);
                        int y = Integer.parseInt(parts[2]);
                        int width = Integer.parseInt(parts[3]);
                        int height = Integer.parseInt(parts[4]);
                        Color color = parseColor(parts[5]);
                        boolean selected = Boolean.parseBoolean(parts[6]);

                        Rectangle rect = new Rectangle(new Point(x + width / 2, y + height / 2), width, height, color);
                        rect.setSelected(selected);
                        shapeList.add(rect);
                    }
                }
            }
        }
    }

    private Color parseColor(String colorStr) {
        if (colorStr.equals("RED")) {
            return Color.RED;
        } else if (colorStr.equals("BLUE")) {
            return Color.BLUE;
        } else if (colorStr.equals("GREEN")) {
            return Color.GREEN;
        } else if (colorStr.equals("YELLOW")) {
            return Color.YELLOW;
        } else if (colorStr.equals("ORANGE")) {
            return Color.ORANGE;
        } else {
            return Color.BLACK;
        }
    }

    @Override
    public String toString() {
        String shapeText = "";
        for (IShape s : shapeList) {
            shapeText += s.toString() + "\n";
        }
        return shapeText;
    }
}
