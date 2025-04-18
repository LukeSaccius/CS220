package drawshapes;

import java.awt.Color;

public class SelectionRectangle extends Rectangle
{
    public SelectionRectangle(int left, int right, int top, int bottom){
        super(left, right, top, bottom);
        setColor(new Color(1,1,1,0.5f));
    }
}
