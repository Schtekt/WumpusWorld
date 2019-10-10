package wumpusworld;
import java.util.*;
import java.lang.Math;

public class Path
{
    // Current direction player is facing
    private static int m_Direction;

    // Open list
    private static LinkedList<MyPRoom> m_OpenList;

    // Size of open list
    private static int m_NrOfRoomsOpen;

    // Coordinates for the goal room
    private static int m_GoalX;
    private static int m_GoalY;

    // Objects holding the start, current and goal rooms
    private static MyPRoom m_StartRoom;
    private static MyPRoom m_CurrentRoom;
    private static MyPRoom m_GoalRoom;

    // The final path of rooms from the start to the goal room
    private static LinkedList<MyPRoom> m_PathFound;

    // Current world state
    private static World m_World;
    // Queue of visited rooms, i.e. rooms to check
    public static LinkedList<MyPRoom> visitedRoomsDeque;


    public Path()
    { }

    public static void Init(World world)
    {
        m_World = world;
        m_OpenList = new LinkedList<MyPRoom>();
        m_PathFound = new LinkedList<MyPRoom>();
    }

    /**
     * Calculates the correct direction the player has to face to move into an adjacent room.
     * Calculate relative coordinates by subtracting next location by the current location.
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
            if (!room.equals(m_GoalRoom))
            {
                index = visitedRoomsDeque.indexOf(room);
                room = visitedRoomsDeque.get(index);
            }
            else
            {
                room = m_GoalRoom;
            }
            int newDir = calcCorrectDirection(relX, relY);
            int g = m_CurrentRoom.getG() + calcDirectionalCost(newDir, m_Direction) + 1 + (room.hasPit() ? 1000:1);
            
            // System.out.println("\nCurrent room: " + m_CurrentRoom.getX() + ", " + m_CurrentRoom.getY());
            // System.out.println("Exploring room " + x + ", " + y);
            
            if (!room.isClosed() && (room.getG() == 0 || g < room.getG()))
            {
                // System.out.println("Current g: " + m_CurrentRoom.getG());
                // System.out.println("Direction: " + m_Direction);
                // System.out.println("Relative: " + relX + ", " + relY);
                // System.out.println("New direction: " + newDir);
                // System.out.println("DirectionalCost: " + calcDirectionalCost(newDir, m_Direction));
                // System.out.println("Room old g: " + room.getG());
                // System.out.println("Room new g: " + g);

                int h = Math.abs(m_GoalX - x) + Math.abs(m_GoalY - y);
                // System.out.println("Room h: " + h);
                // System.out.println("Room f: " + (g + h));
                room.setG(g);
                room.setF(g + h);
                room.setEntranceDirection(newDir);
                room.setParentRoom(m_CurrentRoom);
                if (!m_OpenList.contains(room))
                {
                    m_OpenList.push(room);
                    m_NrOfRoomsOpen++;
                    // System.out.println("Added to open list");
                }
                // else
                // {
                //     System.out.println("Updated element");
                // }
            }
            // else if (room.isClosed())
            // {
            //     System.out.println("Room is closed");
            // }
            // else
            // {
            //     System.out.println("Room already added");
            // }
            // System.out.println("Parent room: " + room.getParentRoom().getX() + ", " + room.getParentRoom().getY());
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
            // System.out.println("\n");
            for (int i = index; i >= 0; i--)
            {
                room = m_OpenList.get(i);
                // System.out.println("Room " + room.getX() + ", " + room.getY() + " has f: " + room.getF());
                if ((room.getF() < tempF) && !(room.isClosed()))
                {
                    tempF = room.getF();
                    index = i;
                }
            }

            m_CurrentRoom = m_OpenList.get(index);
            m_CurrentRoom.setClosed(true);
            m_Direction = m_CurrentRoom.getEntranceDirection();
            m_OpenList.remove(index);
            // System.out.println("Moving to room " + m_CurrentRoom.getX() + ", " + m_CurrentRoom.getY());

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
        // Reset all relevant data
        for (MyPRoom room : visitedRoomsDeque) {
            room.resetPath();
        }
        m_OpenList.clear();
        m_PathFound.clear();
        m_NrOfRoomsOpen = 0;
        
        m_Direction = m_World.getDirection();

        // Create temp room to search for actual room in queue
        MyPRoom tempRoom = new MyPRoom(startX, startY);
        // Find index of start room
        int index = visitedRoomsDeque.indexOf(tempRoom);
        // Find start room in queue, set to current room
        m_StartRoom = m_CurrentRoom = visitedRoomsDeque.get(index);
        // Parent room needed to trace path back from goal room
        m_CurrentRoom.setParentRoom(m_StartRoom);
        // Set closed to avoid searching room again
        m_CurrentRoom.setClosed(true);

        // Set goal room
        m_GoalX = goalX;
        m_GoalY = goalY;
        m_GoalRoom = new MyPRoom(goalX, goalY);

        CheckAdjacent();

        boolean found = false;

        do
        {
            found = ExploreNextRoom();
        } while (m_NrOfRoomsOpen > 0 && !found);

        do
        {
            m_PathFound.push(m_CurrentRoom);
            m_CurrentRoom = m_CurrentRoom.getParentRoom();
        } while (!m_CurrentRoom.equals(m_StartRoom));

        return m_PathFound;
    }
}