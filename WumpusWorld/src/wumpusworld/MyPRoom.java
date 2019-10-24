package wumpusworld;

public class MyPRoom
{
    /**
     * x-coordinate of the room
     */
    private int m_x;
    /**
     * y-coordinate of the room
     */
    private int m_y;

    /**
     * Used  for pathfinding.
     * Distance traveled from start room to current room
     */
    private int m_g;
    /**
     * Used  for pathfinding.
     * Heuristic distance from current room to goal room
     */
    private int m_h;
    /**
     * Used  for pathfinding.
     * g value + h value
     */
    private int m_f;
    /**
     * Used for pathfinding.
     * If {@code true} room has already been explored
     */
    private boolean m_closed;
    /**
     * Used for pathfinding.
     * The room from which the AI moved to enter the curent room.
     * Needed to track the path back from the goal room to the start room.
     */
    private MyPRoom m_ParentRoom;
    /**
     * Used for pathfinding.
     * The direction the AI was facing when entering the room.
     * Needed for the AI to know how to turn to move on from the room
     */
    private int m_EntranceDirection;

    /**
     * Is true if the room has a stench
     */
    private boolean m_stench;
    /**
     * Is true if the room has a breeze
     */
    private boolean m_breeze;
    /**
     * Is true if the room has glitter
     */
    private boolean m_glitter;
    /**
     * Is true if the room has a wumpus
     */
    private boolean m_wumpus;
    /**
     * Is true if the room has a pit
     */
    private boolean m_pit;
    /**
     * Is true if the room is unknown
     */
    private boolean m_unknown;

    /**
     * Create a new room at coordinates {@code (x, y)}
     * @param x The x-coordinate of the new room
     * @param y The y-coordinate of the new room
     */
    public MyPRoom(int x, int y)
    {
        m_x = x;
        m_y = y;
        
        m_g = 0;
        m_h = 0;
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

    /**
     * @return x-coordinate of the room
     */
    public int getX()
    {
        return m_x;
    }
    
    /**
     * @return y-coordinate of the room
     */
    public int getY()
    {
        return m_y;
    }

    /**
     * @return {@code true} if room has stench
     */
    public boolean hasStench()
    {
        return m_stench;
    }
    
    /**
     * @return {@code true} if room has breeze
     */
    public boolean hasBreeze()
    {
        return m_breeze;
    }
    
    /**
     * @return {@code true} if room has glitter
     */
    public boolean hasGlitter()
    {
        return m_glitter;
    }
    
    /**
     * @return {@code true} if room has wumpus
     */
    public boolean hasWumpus()
    {
        return m_wumpus;
    }
    
    /**
     * @return {@code true} if room has pit
     */
    public boolean hasPit()
    {
        return m_pit;
    }
    
    /**
     * @return {@code true} if room is unknown
     */
    public boolean isUnknown()
    {
        return m_unknown;
    }

    /**
     * Sets {@code m_pit} value of room to {@code hasPit}
     * @param hasPit Value to set {@code hasPit} to
     */
    public void setPit(boolean hasPit)
    {
        m_pit = hasPit;
    }

    /**
     * Get g value of room
     * @return g value of rooom
     */
    public int getG()
    {
        return m_g;
    }
    
    /**
     * Get h value of room
     * @return h value of rooom
     */
    public int getH()
    {
        return m_h;
    }
    
    /**
     * Get f value of room
     * @return f value of rooom
     */
    public int getF()
    {
        return m_f;
    }
    
    /**
     * Returns {@code true} if room is closed
     * @return {@code true} if room is closed
     */
    public boolean isClosed()
    {
        return m_closed;
    }
    
    /**
     * Get parent of room
     * @return {@code MyPRoom} object that is parent of room
     */
    public MyPRoom getParentRoom()
    {
        return m_ParentRoom;
    }
    
    /**
     * Get entrance direction of room
     * @return {@code int} representing entrance direction of rooom
     */
    public int getEntranceDirection()
    {
        return m_EntranceDirection;
    }
    
    /**
     * Set g value of room
     * @param value The value to give to g
     */
    public void setG(int value)
    {
        m_g = value;
    }
    
    /**
     * Set h value of room
     * @param value The value to give to h
     */
    public void setH(int value)
    {
        m_h = value;
    }
    
    /**
     * Set f value of room
     * @param value The value to give to f
     */
    public void setF(int value)
    {
        m_f = value;
    }
    
    /**
     * Set if room is closed
     * @param value The value to set {@code m_closed} to
     */
    public void setClosed(boolean value)
    {
        m_closed = value;
    }
    
    /**
     * Set parent of room
     * @param room {@code MyPRoom} object to set asthe parent of the room
     */
    public void setParentRoom(MyPRoom room)
    {
        m_ParentRoom = room;
    }
    
    /**
     * Set entrance direction of room
     * @param direction {@code int} representing the direction to set as the entrance direction of the room
     */
    public void setEntranceDirection(int direction)
    {
        m_EntranceDirection = direction;
    }
    
    /**
     * Function to reset all variables needed for pathfinding
     */
    public void resetPath()
    {
        m_g = 0;
        m_f = 0;
        m_closed = false;
        m_ParentRoom = null;
        m_EntranceDirection = 0;
    }
}