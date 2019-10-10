package wumpusworld;
import java.util.*;
import java.lang.Math;

public class Path
{
    private static int m_Direction;
    private static int m_NrOfRoomsOpen;
    private static LinkedList<MyPRoom> m_OpenList;
    private static int m_GoalX;
    private static int m_GoalY;

    private static MyPRoom m_StartRoom;
    private static MyPRoom m_CurrentRoom;
    private static MyPRoom m_GoalRoom;

    private static World m_World;
    public static LinkedList<MyPRoom> visitedRoomsDeque;

    public Path()
    { }

    /**
     * calculates the correct direction the player has to face to move into an adjacent room.
     * calculate relative coordinates by subtracting next location by the current location.
     * @param relX relative X coordinate. Either 1 or -1 or 0. (next - current)
     * @param relY relative Y coordinate. Either 1 or -1 or 0. (next - current)
     * @return the direction the player should face to move towards the next room.
     */
    public static int calcCorrectDirection(int relX, int relY)
    {
        /*
            insätt 0, 1 (detta ska bli 0).
            abs(2*0 + 1 - 1) = 0

            1,0
            abs(2*1 + 0-1) = 1

            0,-1
            abs(2*0 + -1*2) = 2

            -1,0
            abs(2*-1 + 0-1) = 3
        */
        return Math.abs(2*relX + relY - 1);
    }

    public static int calcDirectionalCost(int corrDirection, int currDirection)
    {
        /*
        say we input 1 and 3.
        normalized = 2
        normalized % 2 = 0.
        returns 0 + abs(0-1)*2 = 2.

        say we input 2 and 1. (vice versa gives same result)
        normalized = 1.
        normalized % 2 = 1.
        returns 1 + abs(1 - 1)*1 = 1.

        say we input 4 and 4.
        normalized = 0.
        normalized % 2 = 0.
        returns 0 + abs(0 -1) * 0 = 0
        */
        int normalized = Math.abs(corrDirection - currDirection);

        return normalized%2 + Math.abs(normalized % 2 - 1)*normalized;
    }

    private static void AddToOpen(int relX, int relY)
    {
        int x = m_CurrentRoom.getX() + relX;
        int y = m_CurrentRoom.getY() + relY;

        if(m_World.isVisited(x, y) || x == m_GoalX && y == m_GoalY)
        {
            MyPRoom room = new MyPRoom(x, y);
            int index;
            if (x != m_GoalX && y != m_GoalY)
            {
                index = visitedRoomsDeque.indexOf(room);
                room = visitedRoomsDeque.get(index);
            }
            int newDir = calcCorrectDirection(relX, relY);
            int g = m_CurrentRoom.getG() + calcDirectionalCost(newDir, m_Direction) + 1;
            
            // System.out.println("Current g: " + m_CurrentRoom.getG());
            // System.out.println("Direction: " + m_Direction);
            // System.out.println("Relative: " + relX + ", " + relY);
            // System.out.println("New direction: " + newDir);
            // System.out.println("DirectionalCost: " + calcDirectionalCost(newDir, m_Direction));
            // System.out.println("Room new g: " + g);

            if (!room.isClosed() && (room.getG() == 0 || room.getG() > g))
            {
                int h = Math.abs(m_GoalX - x) + Math.abs(m_GoalY - y);
                room.setG(g);
                room.setF(g + h);
                room.setEntranceDirection(newDir);
                room.setParentRoom(m_CurrentRoom);
                if (m_OpenList.contains(room))
                {
                    index = m_OpenList.indexOf(room);
                    m_OpenList.add(index, room);
                }
                else
                {
                    m_OpenList.push(room);
                    m_NrOfRoomsOpen++;
                }
            }
        }
    }
    
    private static void CheckAdjacent()
    {
        AddToOpen(-1, 0);
        AddToOpen(1, 0);
        AddToOpen(0, -1);
        AddToOpen(0, 1);
    }
    
    private static boolean ExploreNextRoom()
    {
        if (m_NrOfRoomsOpen != 0)
        {
            int index = --m_NrOfRoomsOpen;
            MyPRoom room = m_OpenList.get(index);
            int tempF = room.getF();
            for (int i = index; i >= 0; i--)
            {
                room = m_OpenList.get(index);
                if ((room.getF() < tempF) && !(room.isClosed()))
                {
                    tempF = room.getF();
                    index = i;
                }
            }

            m_CurrentRoom = room;
            m_CurrentRoom.setClosed(true);
            m_Direction = m_CurrentRoom.getEntranceDirection();
            m_OpenList.remove(index);

            if (m_CurrentRoom.equals(m_GoalRoom))
            {
                return true;
            }

            CheckAdjacent();
        }

        return false;
    }
    
    public static LinkedList<MyPRoom> FindPath(World world, int goalX, int goalY, int startX, int startY)
    {
        for (MyPRoom room : visitedRoomsDeque) {
            room.resetPath();
        }
        m_OpenList = new LinkedList<MyPRoom>();
        m_World = world;

        m_Direction = m_World.getDirection();

        m_NrOfRoomsOpen = 0;

        MyPRoom tempRoom = new MyPRoom(startX, startY);
        int index = visitedRoomsDeque.indexOf(tempRoom);
        m_StartRoom = m_CurrentRoom = visitedRoomsDeque.get(index);
        m_CurrentRoom.setParentRoom(m_StartRoom);
        m_CurrentRoom.setClosed(true);

        m_GoalX = goalX;
        m_GoalY = goalY;
        m_GoalRoom = new MyPRoom(goalX, goalY);

        CheckAdjacent();

        m_CurrentRoom.setClosed(true);

        boolean found = false;

        do
        {
            found = ExploreNextRoom();
        } while (m_NrOfRoomsOpen > 0 && !found);

        LinkedList<MyPRoom> pathFound = new LinkedList<MyPRoom>();
        do
        {
            pathFound.push(m_CurrentRoom);
            m_CurrentRoom = m_CurrentRoom.getParentRoom();
        } while (!m_CurrentRoom.equals(m_StartRoom));

        return pathFound;
    }
}