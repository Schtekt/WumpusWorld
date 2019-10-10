package wumpusworld;

public class MyPRoom
{
    private int m_x;
    private int m_y;

    private int m_g;
    private int m_f;
    private boolean m_closed;
    private MyPRoom m_ParentRoom;
    private int m_EntranceDirection;

    private boolean m_stench;
    private boolean m_breeze;
    private boolean m_glitter;
    private boolean m_wumpus;
    private boolean m_pit;
    private boolean m_unknown;

    public MyPRoom(int x, int y)
    {
        m_x = x;
        m_y = y;
        
        m_g = 0;
        m_f = 0;
        m_closed = false;
        m_ParentRoom = null;
        m_EntranceDirection = 0;

        m_stench = false;
        m_breeze = false;
        m_glitter = false;
        m_wumpus = false;
        m_pit = false;
        m_unknown = true;
    }

    public boolean equals(Object other)
    {
        if(m_x == ((MyPRoom)other).m_x && m_y == ((MyPRoom)other).m_y)
        {
            return true;
        }
        return false;
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

    public int getG()
    {
        return m_g;
    }

    public int getF()
    {
        return m_f;
    }

    public boolean isClosed()
    {
        return m_closed;
    }

    public MyPRoom getParentRoom()
    {
        return m_ParentRoom;
    }

    public int getEntranceDirection()
    {
        return m_EntranceDirection;
    }

    public void setG(int value)
    {
        m_g = value;
    }

    public void setF(int value)
    {
        m_f = value;
    }

    public void setClosed(boolean value)
    {
        m_closed = value;
    }

    public void setParentRoom(MyPRoom room)
    {
        m_ParentRoom = room;
    }

    public void setEntranceDirection(int direction)
    {
        m_EntranceDirection = direction;
    }

    public void resetPath()
    {
        m_g = 0;
        m_f = 0;
        m_closed = false;
        m_ParentRoom = null;
        m_EntranceDirection = 0;
    }
}