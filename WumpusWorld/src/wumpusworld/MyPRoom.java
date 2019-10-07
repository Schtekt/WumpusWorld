package wumpusworld;

public class MyPRoom
{
    private int m_x;
    private int m_y;

    private boolean m_stench;
    private boolean m_breeze;
    private boolean m_glitter;
    private boolean m_wumpus;
    private boolean m_pit;
    private boolean m_gold;
    private boolean m_unknown;

    public MyPRoom(int x, int y)
    {
        m_x = x;
        m_y = y;

        m_stench = false;
        m_breeze = false;
        m_glitter = false;
        m_wumpus = false;
        m_pit = false;
        m_unknown = true;
    }

    public int getX()
    {
        return m_x;
    }

    public int getY()
    {
        return m_y;
    }

    public boolean hasStench()
    {
        return m_stench;
    }

    public boolean hasBreeze()
    {
        return m_breeze;
    }

    public boolean hasGlitter()
    {
        return m_glitter;
    }

    public boolean hasWumpus()
    {
        return m_wumpus;
    }
    public boolean hasPit()
    {
        return m_pit;
    }

    public boolean isUnknown()
    {
        return m_unknown;
    }

    public void setPerception(boolean hasStench, boolean hasBreeze, boolean hasGlitter, boolean hasWumpus, boolean hasPit)
    {
        m_unknown = false;
        m_stench = hasStench;
        m_breeze = hasBreeze;
        m_glitter = hasGlitter;
        m_pit = hasPit;
        m_wumpus = hasWumpus;
    }
}