package com.example.cubesolver;

import static java.lang.Math.min;
import static java.lang.Math.max;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;

/*
* Used to display a 3x3 Rubik's Cube in an Android application and update its colors as needed.
* It inherits from the View class and overrides the onDraw method to draw the Rubik's Cube on the view.
* In this class, there are 9 color blocks on the front face of the Rubik's Cube and one color block on each of the four side faces.
*/

public class CubeView extends View {

    private final Paint paint = new Paint();
    private Canvas canvas;
    private String[][] frontColors = {{"X", "X", "X"}, {"X", "X", "X"}, {"X", "X", "X",}};  // 3x3 block color
    private String[] sideColors = {"X", "X", "X", "X",};    // top -> left -> down -> right
    final private int padding = 4;  // padding between blocks

    public CubeView(Context context) {
        this(context, null);
    }

    public CubeView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CubeView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    /*
    * This method is called when the view needs to be redrawn.
    * It first sets the background color of the canvas, then calculates the position and size of the Rubik's cube blocks to be drawn,
    * and finally draws the front 9 color blocks and the four side color blocks.
    */
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        this.canvas = canvas;

        int w = getWidth();
        int h = getHeight();
        paint.setColor(Color.rgb(115, 187, 243));
        canvas.drawRect(0, 0, w, h, paint);

        // Calculate box point
        double cubeLen = min(w, h) * 0.8;
        int boxLen = (int) (cubeLen / 3);
        int startX, startY;
        startX = (int) ((w - cubeLen) / 2);
        startY = (int) ((h - cubeLen) / 2);

        paint.setStrokeWidth(3);

        // Paint front color
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                paint.setColor(getColor(frontColors[i][j]));
                int boxStartX = startX + boxLen * i;
                int boxStartY = startY + boxLen * j;
                // add padding
                if (i == 0) boxStartX -= padding;
                else if (i == 2) boxStartX += padding;
                if (j == 0) boxStartY -= padding;
                else if (j == 2) boxStartY += padding;
                // draw
                canvas.drawRect(boxStartX, boxStartY, boxStartX + boxLen, boxStartY + boxLen, paint);
            }
        }

        // Paint side color
        int thickness = (int) (boxLen / 6);
        paint.setColor(getColor(sideColors[0]));
        int boxStartX, boxStartY;
        // Top and bottom
        boxStartX = startX + boxLen;
        boxStartY = startY - 3 * padding - thickness;
        canvas.drawRect(new Rect(boxStartX, boxStartY, boxStartX + boxLen, boxStartY + thickness), paint);  // Top
        paint.setColor(getColor(sideColors[2]));
        boxStartY = startY + 3 * boxLen + 3 * padding;
        canvas.drawRect(new Rect(boxStartX, boxStartY, boxStartX + boxLen, boxStartY + thickness), paint);  // Bottom
        // Left and Right
        paint.setColor(getColor(sideColors[1]));
        boxStartX = startX - 3 * padding - thickness;
        boxStartY = startY + boxLen;
        canvas.drawRect(new Rect(boxStartX, boxStartY, boxStartX + thickness, boxStartY + boxLen), paint);  // Left
        paint.setColor(getColor(sideColors[3]));
        boxStartX = startX + boxLen * 3 + 3 * padding;
        canvas.drawRect(new Rect(boxStartX, boxStartY, boxStartX + thickness, boxStartY + boxLen), paint);  // Right
    }

    // Define color by label
    private int getColor(String color) {
        assert color.length() == 1;
        switch (color) {
            case "Y":
                return Color.rgb(255, 215, 0);
            case "W":
                return Color.rgb(255, 255, 255);
            case "O":
                return Color.rgb(254, 80, 0);
            case "R":
                return Color.rgb(186, 23, 47);
            case "B":
                return Color.rgb(0, 61, 165);
            case "G":
                return Color.rgb(0, 154, 68);
            default:
                return Color.rgb(115, 187, 243);
        }
    }

    private int getColor(char color) {
        return getColor(String.valueOf(color));
    }

    public void setFrontColors(int[][] colors) {
        String[][] colorsString = {{"", "", ""}, {"", "", ""}, {"", "", ""},};
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                colorsString[i][j] = ImageProcess.colorLabel[colors[i][j]];
            }
        }
        setFrontColors(colorsString);  // refresh
    }

    public void setFrontColors(String[][] colors) {
        assert colors.length == 3;
        String center = frontColors[1][1];
        frontColors = colors;
        frontColors[1][1] = center;
        invalidate();
    }

    public void setFrontColors(String colors) {
        assert colors.length() == 9;
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                frontColors[i][j] = String.valueOf(colors.charAt(i * 3 + j));
            }
        }
        invalidate();
    }

    public void setCenterColor(String color) {
        assert color.length() == 1;
        frontColors[1][1] = color;
        invalidate();
    }

    public void setCenterColor(char color) {
        frontColors[1][1] = String.valueOf(color);
        invalidate();
    }

    public void setSideColors(String[] colors) {
        assert colors.length == 4;
        sideColors = colors;
        invalidate();
    }

    public void setSideColors(String colors) {
        assert colors.length() == 4;
        for (int i = 0; i < colors.length(); i++) {
            sideColors[i] = String.valueOf(colors.charAt(i));
        }
        invalidate();
    }
}

