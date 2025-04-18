package drawshapes;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;

@SuppressWarnings("serial")
public class DrawShapes extends JFrame {
    public enum ShapeType {
        SQUARE,
        CIRCLE,
        RECTANGLE
    }

    public enum OperationMode {
        DRAW,
        MOVE,
        RESIZE
    }

    private DrawShapesPanel shapePanel;
    private Scene scene;
    private ShapeType shapeType = ShapeType.SQUARE;
    private Color color = Color.RED;
    private Point startDrag;
    private OperationMode operationMode = OperationMode.DRAW;
    private Point lastClickPoint;

    public DrawShapes(int width, int height) {
        setTitle("Draw Shapes!");
        scene = new Scene();

        // create our canvas, add to this frame's content pane
        shapePanel = new DrawShapesPanel(width, height, scene);
        this.getContentPane().add(shapePanel, BorderLayout.CENTER);
        this.setResizable(false);
        this.pack();
        this.setLocation(100, 100);

        // Add key and mouse listeners to our canvas
        initializeMouseListener();
        initializeKeyListener();

        // initialize the menu options
        initializeMenu();

        // Handle closing the window.
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }
        });
    }

    private void initializeMouseListener() {
        MouseAdapter a = new MouseAdapter() {

            public void mouseClicked(MouseEvent e) {
                System.out.printf("Mouse cliked at (%d, %d)\n", e.getX(), e.getY());
                lastClickPoint = e.getPoint();

                if (e.getButton() == MouseEvent.BUTTON1) {
                    if (operationMode == OperationMode.DRAW) {
                        if (shapeType == ShapeType.SQUARE) {
                            scene.addShape(new Square(color,
                                    e.getX(),
                                    e.getY(),
                                    100));
                        } else if (shapeType == ShapeType.CIRCLE) {
                            scene.addShape(new Circle(color,
                                    e.getPoint(),
                                    100));
                        } else if (shapeType == ShapeType.RECTANGLE) {
                            scene.addShape(new Rectangle(
                                    e.getPoint(),
                                    100,
                                    200,
                                    color));
                        }
                    } else if (operationMode == OperationMode.MOVE) {
                        // Move functionality is handled with key events
                    } else if (operationMode == OperationMode.RESIZE) {
                        // Resize functionality is handled with key events
                    }
                } else if (e.getButton() == MouseEvent.BUTTON2) {
                    // apparently this is middle click
                } else if (e.getButton() == MouseEvent.BUTTON3) {
                    // right right-click
                    Point p = e.getPoint();
                    System.out.printf("Right click is (%d, %d)\n", p.x, p.y);
                    List<IShape> selected = scene.select(p);
                    if (selected.size() > 0) {
                        for (IShape s : selected) {
                            s.setSelected(true);
                        }
                    } else {
                        for (IShape s : scene) {
                            s.setSelected(false);
                        }
                    }
                    System.out.printf("Select %d shapes\n", selected.size());
                }
                repaint();
            }

            /*
             * (non-Javadoc)
             * 
             * @see java.awt.event.MouseListener#mousePressed(java.awt.event.MouseEvent)
             */
            public void mousePressed(MouseEvent e) {
                System.out.printf("mouse pressed at (%d, %d)\n", e.getX(), e.getY());
                scene.startDrag(e.getPoint());
            }

            /*
             * (non-Javadoc)
             * 
             * @see java.awt.event.MouseListener#mouseReleased(java.awt.event.MouseEvent)
             */
            public void mouseReleased(MouseEvent e) {
                System.out.printf("mouse released at (%d, %d)\n", e.getX(), e.getY());
                scene.stopDrag();
                repaint();
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                System.out.printf("mouse drag! (%d, %d)\n", e.getX(), e.getY());
                scene.updateSelectRect(e.getPoint());
                repaint();
            }

            @Override
            public void mouseWheelMoved(MouseWheelEvent e) {
                if (e.getWheelRotation() < 0) {
                    // Mouse wheel up - scale up selected shapes
                    scene.scaleUpSelectedShapes();
                } else {
                    // Mouse wheel down - scale down selected shapes
                    scene.scaleDownSelectedShapes();
                }
                repaint();
            }
        };
        shapePanel.addMouseMotionListener(a);
        shapePanel.addMouseListener(a);
        shapePanel.addMouseWheelListener(a);
    }

    /**
     * Initialize the menu options
     */
    private void initializeMenu() {
        // menu bar
        JMenuBar menuBar = new JMenuBar();

        // file menu
        JMenu fileMenu = new JMenu("File");
        menuBar.add(fileMenu);
        // load
        JMenuItem loadItem = new JMenuItem("Load");
        loadItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, ActionEvent.CTRL_MASK));
        fileMenu.add(loadItem);
        loadItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.out.println(e.getActionCommand());
                JFileChooser jfc = new JFileChooser(".");
                jfc.setDialogTitle("Load Scene");

                int returnValue = jfc.showOpenDialog(null);

                if (returnValue == JFileChooser.APPROVE_OPTION) {
                    File selectedFile = jfc.getSelectedFile();
                    System.out.println("load from " + selectedFile.getAbsolutePath());
                    try {
                        scene.loadFromFile(selectedFile.getAbsolutePath());
                        repaint();
                    } catch (IOException ex) {
                        JOptionPane.showMessageDialog(DrawShapes.this,
                                "Error loading file: " + ex.getMessage(),
                                "Load Error",
                                JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        });
        // save
        JMenuItem saveItem = new JMenuItem("Save");
        saveItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, ActionEvent.CTRL_MASK));
        fileMenu.add(saveItem);
        saveItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.out.println(e.getActionCommand());
                JFileChooser jfc = new JFileChooser(".");
                jfc.setDialogTitle("Save Scene");

                int returnValue = jfc.showSaveDialog(null);

                if (returnValue == JFileChooser.APPROVE_OPTION) {
                    File selectedFile = jfc.getSelectedFile();
                    System.out.println("save to " + selectedFile.getAbsolutePath());
                    try {
                        scene.saveToFile(selectedFile.getAbsolutePath());
                    } catch (IOException ex) {
                        JOptionPane.showMessageDialog(DrawShapes.this,
                                "Error saving file: " + ex.getMessage(),
                                "Save Error",
                                JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        });

        // Undo
        JMenuItem undoItem = new JMenuItem("Undo");
        undoItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z, ActionEvent.CTRL_MASK));
        fileMenu.add(undoItem);
        undoItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (scene.undo()) {
                    repaint();
                } else {
                    JOptionPane.showMessageDialog(DrawShapes.this,
                            "Nothing to undo",
                            "Undo",
                            JOptionPane.INFORMATION_MESSAGE);
                }
            }
        });

        fileMenu.addSeparator();
        // exit
        JMenuItem itemExit = new JMenuItem("Exit");
        fileMenu.add(itemExit);
        itemExit.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String text = e.getActionCommand();
                System.out.println(text);
                System.exit(0);
            }
        });

        // color menu
        JMenu colorMenu = new JMenu("Color");
        menuBar.add(colorMenu);

        // red color
        JMenuItem redColorItem = new JMenuItem("Red");
        colorMenu.add(redColorItem);
        redColorItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String text = e.getActionCommand();
                System.out.println(text);
                // change the color instance variable to red
                color = Color.RED;
            }
        });

        // blue color
        JMenuItem blueColorItem = new JMenuItem("Blue");
        colorMenu.add(blueColorItem);
        blueColorItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String text = e.getActionCommand();
                System.out.println(text);
                // change the color instance variable to blue
                color = Color.BLUE;
            }
        });

        // green color
        JMenuItem greenColorItem = new JMenuItem("Green");
        colorMenu.add(greenColorItem);
        greenColorItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String text = e.getActionCommand();
                System.out.println(text);
                color = Color.GREEN;
            }
        });

        // yellow color
        JMenuItem yellowColorItem = new JMenuItem("Yellow");
        colorMenu.add(yellowColorItem);
        yellowColorItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String text = e.getActionCommand();
                System.out.println(text);
                color = Color.YELLOW;
            }
        });

        // orange color
        JMenuItem orangeColorItem = new JMenuItem("Orange");
        colorMenu.add(orangeColorItem);
        orangeColorItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String text = e.getActionCommand();
                System.out.println(text);
                color = Color.ORANGE;
            }
        });

        // shape menu
        JMenu shapeMenu = new JMenu("Shape");
        menuBar.add(shapeMenu);

        // square
        JMenuItem squareItem = new JMenuItem("Square");
        shapeMenu.add(squareItem);
        squareItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.out.println("Square");
                shapeType = ShapeType.SQUARE;
            }
        });
        // Rectangle
        JMenuItem rectangleItem = new JMenuItem("Rectangle");
        shapeMenu.add(rectangleItem);
        rectangleItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.out.println("Rectangle");
                shapeType = ShapeType.RECTANGLE;
            }
        });

        // circle
        JMenuItem circleItem = new JMenuItem("Circle");
        shapeMenu.add(circleItem);
        circleItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.out.println("Circle");
                shapeType = ShapeType.CIRCLE;
            }
        });

        // operation mode menu
        JMenu operationModeMenu = new JMenu("Operation");
        menuBar.add(operationModeMenu);

        // draw option
        JMenuItem drawItem = new JMenuItem("Draw");
        operationModeMenu.add(drawItem);
        drawItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String text = e.getActionCommand();
                System.out.println(text);
                operationMode = OperationMode.DRAW;
            }
        });

        // resize option
        JMenuItem resizeItem = new JMenuItem("Resize");
        operationModeMenu.add(resizeItem);
        resizeItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String text = e.getActionCommand();
                System.out.println(text);
                operationMode = OperationMode.RESIZE;
            }
        });

        // move option
        JMenuItem moveItem = new JMenuItem("Move");
        operationModeMenu.add(moveItem);
        moveItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String text = e.getActionCommand();
                System.out.println(text);
                operationMode = OperationMode.MOVE;
            }
        });

        // Layer menu (for new feature 1 - changing order of shapes)
        JMenu layerMenu = new JMenu("Layer");
        menuBar.add(layerMenu);

        // Bring to front option
        JMenuItem frontItem = new JMenuItem("Bring to Front");
        layerMenu.add(frontItem);
        frontItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                scene.bringToFront();
                repaint();
            }
        });

        // Send to back option
        JMenuItem backItem = new JMenuItem("Send to Back");
        layerMenu.add(backItem);
        backItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                scene.sendToBack();
                repaint();
            }
        });

        // set the menu bar for this frame
        this.setJMenuBar(menuBar);
    }

    /**
     * Initialize the keyboard listener.
     */
    private void initializeKeyListener() {
        shapePanel.addKeyListener(new KeyListener() {
            public void keyPressed(KeyEvent e) {
                System.out.println("key pressed: " + e.getKeyChar());
                int moveAmount = 10;

                // Move operations
                if (operationMode == OperationMode.MOVE) {
                    if (e.getKeyCode() == KeyEvent.VK_UP) {
                        scene.moveSelectedShapes(0, -moveAmount);
                    } else if (e.getKeyCode() == KeyEvent.VK_DOWN) {
                        scene.moveSelectedShapes(0, moveAmount);
                    } else if (e.getKeyCode() == KeyEvent.VK_LEFT) {
                        scene.moveSelectedShapes(-moveAmount, 0);
                    } else if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
                        scene.moveSelectedShapes(moveAmount, 0);
                    }
                }
                // Resize operations
                else if (operationMode == OperationMode.RESIZE) {
                    if (e.getKeyCode() == KeyEvent.VK_UP) {
                        scene.scaleUpSelectedShapes();
                    } else if (e.getKeyCode() == KeyEvent.VK_DOWN) {
                        scene.scaleDownSelectedShapes();
                    }
                }

                // Undo operation
                if (e.isControlDown() && e.getKeyCode() == KeyEvent.VK_Z) {
                    scene.undo();
                }

                repaint();
            }

            public void keyReleased(KeyEvent e) {
                // Not needed
            }

            public void keyTyped(KeyEvent e) {
                // Not needed
            }
        });
    }

    /**
     * @param args
     */
    public static void main(String[] args) {
        DrawShapes shapes = new DrawShapes(700, 600);
        shapes.setVisible(true);
    }
}
