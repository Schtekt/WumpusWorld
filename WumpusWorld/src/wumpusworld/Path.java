package wumpusworld;
import java.util.*;
import java.lang.Math;

public class Path
{
    /**
     * Current direction player is facing.
     */
    private static int m_Direction;

    /**
     * Open list.
     */
    private static LinkedList<MyPRoom> m_OpenList;

    /**
     * X coordinate of the goal room.
     */

    private static int m_GoalX;
    /**
     * Y coordinate of the goal room.
     */
    private static int m_GoalY;

    /**
     * The start room.
     */
    private static MyPRoom m_StartRoom;
    /**
     * The current room being explored.
     */
    private static MyPRoom m_CurrentRoom;
    /**
     * The goal room.
     */
    private static MyPRoom m_GoalRoom;

    /**
     * The final path of rooms from the start to the goal room.
    */
     private static LinkedList<MyPRoom> m_PathFound;

    /**
     * Current world state.
     */
    private static World m_World;
    
    /**
     * Queue of visited rooms, i.e. rooms to check.
     */
    public static LinkedList<MyPRoom> visitedRoomsDeque;

    /**
     * Boolean indicating whether the path goes accros
     * a pit.
     */
    public static boolean m_Pit;

    public Path()
    { }

    /**
     * Initiates the Path class to enable usage
     * of the FindPath function.
     * @param world Current world state
     */
    public static void Init(World world)
    {
        m_World = world;
        m_OpenList = new LinkedList<MyPRoom>();
        m_PathFound = new LinkedList<MyPRoom>();
    }

    /**
     * Calculates the correct direction the player has to face to move into an adjacent room.
     * Calculate relative coordinates by subtracting next location by the current location.
     * @param relX Relative X coordinate. Either 1 or -1 or 0. (next - current)
     * @param relY Relative Y coordinate. Either 1 or -1 or 0. (next - current)
     * @return the direction the player should face to move towards the next room.
     */
    public static int calcCorrectDirection(int relX, int relY)
    {
        /*
            Input 0, 1 (this should become 0).
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

    /**
     * Calculates the cost of turning to a specified direction
     * from the direction teh player is currently facing.
     * @param corrDirection The direction to turn to
     * @param currDirection The direction the player is currently facing
     * @return cost of turning
     */
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

    /**
     * Checks if a room would be valid to move to from
     * the current room. If it is, the room is added to
     * the open list, its g and f values are set, and
     * the current room is set as parent room.
     * @param relX x coordinate of the room relative to the current room
     * @param relY y coordinate of the room relative to the current room
     */
    private static void AddToOpen(int relX, int relY)
    {
        // Calculate absolute coordinates of room
        int x = m_CurrentRoom.getX() + relX;
        int y = m_CurrentRoom.getY() + relY;

        // If room is visited (or is the goal room) it is viable for moving
        // to and should be checked
        if(m_World.isVisited(x, y) || x == m_GoalX && y == m_GoalY)
        {
            // Create a temporary room object to use for searching
            // in the LinkedList
            MyPRoom room = new MyPRoom(x, y);
            int index;
            // If the room is the goal room it is not in the
            // list of visited rooms and as such we should not
            // try to find it there
            if (!room.equals(m_GoalRoom))
            {
                index = visitedRoomsDeque.indexOf(room);
                room = visitedRoomsDeque.get(index);
            }
            // If the room is the goal room we need to set it as such to
            // make sure the values we set for the room are saved
            else
            {
                room = m_GoalRoom;
            }

            // If the room is closed then the algorithm has already visited
            // the room and it should not be added to the open list again
            if (!room.isClosed())
            {
                // We need to calculate the total cost of turning and walking
                // to the room from the start room. Moving ahead one room,
                // turning 90 degrees, or climbing out of a hole has a
                // cost of one (1). Falling into a hole has a cost of 1000
                int newDir = calcCorrectDirection(relX, relY);
                int g = m_CurrentRoom.getG() + calcDirectionalCost(newDir, m_Direction) + 1 + (room.hasPit() ? 1001:0);
                
                // The room should only be updated if it has not been explored before (g == 0)
                // or if the new path to the room is better than the one previously found (g < room.getG())
                if ((room.getG() == 0 || g < room.getG()))
                {
                    // Simple heuristic function, Manhattan distance from room to goal room
                    int h = Math.abs(m_GoalX - x) + Math.abs(m_GoalY - y);
                    room.setG(g);
                    room.setF(g + h);
                    // Set the entrance direction of the room to know how the player needs
                    // to turn when moving out of the room
                    room.setEntranceDirection(newDir);
                    // Set parent room to be able to trace the path back from the goal room
                    // to the start room
                    room.setParentRoom(m_CurrentRoom);
                    // If the open list does not contain the room we need to add it. Otherwise
                    // it has been updated
                    if (!m_OpenList.contains(room))
                    {
                        m_OpenList.push(room);
                    }
                }
            }
        }
    }
    
    /**
     * Runs function AddToOpen for all rooms adjacent
     * to the current room.
     */
    private static void CheckAdjacent()
    {
        AddToOpen(-1, 0);
        AddToOpen(1, 0);
        AddToOpen(0, -1);
        AddToOpen(0, 1);
    }
    
    /**
     * Function to move onto the room in the open list with the smallest
     * f value. Returns {@code true} if it moves into the goal room.
     * @return {@code true} if the room with the smallest f value in the open list
     * is the goal room
     */
    private static boolean ExploreNextRoom()
    {
        // We sort the open list by f value...
        m_OpenList.sort(new Comparator<MyPRoom>() {
            @Override
            public int compare(MyPRoom r1, MyPRoom r2)
            {
                return r1.getF() - r2.getF();
            }
        });

        // ...and pop the top room (lowest f value);
        m_CurrentRoom = m_OpenList.pop();
        // We close the room to avoid moving to it several times...
        m_CurrentRoom.setClosed(true);
        // ...and set the player's direction to the entrance direction
        // of the room
        m_Direction = m_CurrentRoom.getEntranceDirection();

        // If we have moved to the goal room we return true
        if (m_CurrentRoom.equals(m_GoalRoom))
        {
            return true;
        }

        // The nwe check the rooms adjacent to the room we just moved to
        CheckAdjacent();

        return false;
    }
    
    /**
     * Finds the cheapest path from the the specified start room to
     * the specified goal room. Returns the path as a {@code LinkedList}
     * of {@code MyPRoom}.
     * @param goalX the x coordinate of the goal room
     * @param goalY the y coordinate of the goal room
     * @param startX the x coordinate of the start room
     * @param startY the y coordinate of the start room
     * @return the path from the start room to the goal room
     */
    public static LinkedList<MyPRoom> FindPath(int goalX, int goalY, int startX, int startY)
    {
        // Reset all relevant data
        for (MyPRoom room : visitedRoomsDeque) {
            room.resetPath();
        }
        m_OpenList.clear();
        m_PathFound.clear();
        m_Pit = false;
        
        // Set the player's current direction
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

        // Check all rooms adjacent to the start room
        CheckAdjacent();

        boolean found = false;

        // We keep exploring the rooms until we have found
        // a path to the goal room
        do
        {
            found = ExploreNextRoom();
        } while (!found);

        // We track the path pack from the goal room, through its
        // parent, to the start room
        do
        {
            m_PathFound.push(m_CurrentRoom);
            m_Pit = m_CurrentRoom.hasPit();
            m_CurrentRoom = m_CurrentRoom.getParentRoom();
        } while (!m_CurrentRoom.equals(m_StartRoom));

        // Return the path found
        return m_PathFound;
    }
}