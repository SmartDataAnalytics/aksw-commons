package org.aksw.commons.io.hadoop.binseach.v2;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.geom.Point2D;
import java.util.Random;

import javax.swing.JFrame;
import javax.swing.JPanel;

/* THIS FILE IS UNFINISHED. The main issue is to decide on what strategy to use to map offsets to cells. Also, the result should be exportable as HTML or D3.js */

class LinearTo2DMapper {
    private long dataLength;

    private final int width;
    private final int height;
    // private final long cellCount; // cellCount := width = height
    // private final long dataSize; // Data size is scaled to cellCount
    private final float pixelHeight;

    private final float ratio;
    private final float cellBaseSize;

    // private Point2D maxCellSize;

    public LinearTo2DMapper(long length, int width, int height) {
        super();
        this.dataLength = length;
        this.width = width;
        this.height = height;

        this.ratio = width / (float)height;
        this.cellBaseSize = (float)Math.sqrt(length / ratio);
        // this.cellBaseSize = Math.max(1, Math.min(10, cellBaseSize));

        // this.cellCount = width * height;

        float neededRows = (dataLength / (float)width) + 1f;
        this.pixelHeight = Math.max(1, neededRows / height);
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public float getPixelHeight() {
        return pixelHeight;
    }

    public float getPixelWidth() {
        return 1;
    }

    public Point2D map(long pos) {
        int rowId = (int)(pos / (float)width);
        int colId = (int)(pos - rowId * width);

        int finalRow = (int)(rowId * pixelHeight);
        return new Point(colId, finalRow);
    }
}

public class SourceAccessVisualizer extends JPanel {
    private static final long serialVersionUID = 1L;

    private LinearTo2DMapper mapper;

    public SourceAccessVisualizer(LinearTo2DMapper mapper) {
        this.mapper = mapper;
        // this.byteAccessFrequency = byteAccessFrequency;
        setPreferredSize(new Dimension(mapper.getWidth(), mapper.getHeight()));
    }

    public void addRect(Graphics g, long offset, long end, Color color) {
        Point2D a = mapper.map(offset);
        Point2D o = mapper.map(end);


        int startRow = (int)a.getY();
        int endRow = (int)o.getY();

        g.setColor(Color.RED);

        int h = Math.min(1, (int)mapper.getPixelHeight());

        if (startRow == endRow) {
            int w = Math.max(1, (int)(o.getX() - a.getX()));
            g.fillRect((int)a.getX(), startRow, w, h);
        } else {
            int w = Math.min(1, mapper.getWidth() - (int)o.getX());
            g.fillRect((int)a.getX(), startRow, w, h);
            // for (int i = startRow + 1; i < endRow; ++i) {
            int deltaRow = endRow - startRow;
            if (deltaRow -1 > 0) {
                g.fillRect(0, startRow + 1, mapper.getWidth(), (int)((deltaRow - 1) * mapper.getPixelHeight()));
            }
            g.fillRect(0, endRow, (int)o.getX(), h);
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
//System.out.println("render");
        this.setBackground(Color.BLACK);
        // g.fillRect(0, 0, 200, 200);
        // g.setColor(Color.BLACK);
        // g.fillRect(0, 0, getWidth(), getHeight());

        addRect(g, 35000, 70000, Color.RED);

        // int rows = byteAccessFrequency.length; // Number of byte offsets
        // int cols = byteAccessFrequency[0].length; // Number of frequency bins

//        int pixelWidth = Math.min(1, (int)(width / (float)rows)); // Calculate width of each "pixel"
//        int pixelHeight = Math.min(1, (int)(height / (float)cols)); // Calculate height of each "pixel"
//
//        for (int x = 0; x < rows; x++) {
//            for (int y = 0; y < cols; y++) {
//                // Get the color for the current frequency value
//                Color color = getColorForFrequency(byteAccessFrequency[x][y]);
//
                // Set the color and draw the pixel (as a rectangle)
//                g.setColor(new Color(255, 0,0));
//                g.fillRect(0, 0, 200, 200);
//            }
//        }
    }

    private Color getColorForFrequency(double frequency) {
        // Example: Scale frequency from 0 to 10 and map to a blue-to-red gradient
        int colorValue = (int) (255 * frequency / 10); // Scale frequency to a color range
        return new Color(colorValue, 0, 255 - colorValue); // Gradient from blue (low) to red (high)
    }

    public static void main(String[] args) {
        // Create a JFrame to display the canvas
        JFrame frame = new JFrame("Byte Frequency Heat Map");
        // SourceAccessVisualizer canvas = new SourceAccessVisualizer(new LinearTo2DMapper(100000l, 800, 600));
        SourceAccessVisualizer canvas = new SourceAccessVisualizer(new LinearTo2DMapper(100000l, 800, 600));
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(canvas);
        frame.pack();
        frame.setVisible(true);
    }
}
