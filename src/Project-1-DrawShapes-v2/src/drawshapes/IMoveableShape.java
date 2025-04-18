package drawshapes;

public interface IMoveableShape extends IShape
{
    public void move(int x, int y);
    public void scaleUp();
    public void scaleDown();
}
