package wumpusworld;

public class MyPRoom
{
    private int m_x;
    private int m_y;
    private String m_p;

    public MyPRoom(int x, int y)
    {
        m_x = x;
        m_y = y;

        m_p = World.UNKOWN;
    }

    public int getX()
    {
        return m_x;
    }

    public int getY()
    {
        return m_y;
    }

    public String getPerception()
    {
        return m_p;
    }

    public void setPerception(String perception)
    {
        m_p = perception;
    }
}