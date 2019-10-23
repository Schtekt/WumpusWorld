package wumpusworld;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

import wumpusworld.MyProbability.Coordinate;

/**
 * Contains starting code for creating your own Wumpus World agent.
 * Currently the agent only makes a random decision each turn.
 * 
 * @author Johan Hagelbäck
 */
public class MyAgent implements Agent
{
    private World w;
    int count = 0;
    /**
     * A double ended queue containing all non-visited rooms available for movement
     */
    LinkedList<MyPRoom> availableRoomsDeque;
    /**
     * A double ended queue containing all rooms known to be free from pits and wumpuses
     */
    LinkedList<MyPRoom> safeRoomsDeque;
    /**
     * The path of rooms for the AI to follow from the current room to the target room
     */
    LinkedList<MyPRoom> path;
    /**
     * Best available room found to be on the other side of a pit. The room is saved in case crossing the
     * pit is later found to be the best possible action to take
     */
    MyPRoom otherSideOfPit;
    /**
     * A boolean stating whether or not the AI is currently on its way to kill the wumpus
     */
    boolean killWump;

    MyProbability probCalc;
    /**
     * Creates a new instance of your solver agent.
     * 
     * @param world Current world state 
     */
    public MyAgent(World world)
    {
        w = world;
        // Function to initiate static class Path
        Path.Init(w);

        // Initiate member variables
        availableRoomsDeque = new LinkedList<MyPRoom>();
        safeRoomsDeque = new LinkedList<MyPRoom>();
        path = new LinkedList<MyPRoom>();
        otherSideOfPit = null;
        killWump = false;

        probCalc = new MyProbability(w.getSize());
    }
   
    /**
     * Tries to move the player to the specified room.
     * Returns {@code false} if the room is not adjacent
     * to the current room.
     * @param room The room to which the player should move
     * @param goal The goal of the path
     * @return {@code false} if the target room is not adjacent to the current room
     */
    public boolean MoveToRoom(MyPRoom room, MyPRoom goal)
    {
        //We are in a pit. Climb up.
        if (w.isInPit())
        {
            w.doAction(World.A_CLIMB);
        }
        // Find relative coordinates of target room
        int relX = room.getX() - w.getPlayerX();
        int relY = room.getY() - w.getPlayerY();
        // Check so the target room is actually adjacent to the current room
        if (Math.abs(relX + relY) == 1)
        {
            // Numbered map of the directions
            //     0
            // 3        1
            //     2

            // Calculate which direction to turn to and find which sequence of moves
            // will turn the AI to that direction
            int dir = Path.calcCorrectDirection(relX, relY);
            int turn = w.getDirection() - dir;
            switch(turn)
            {
                case 1:
                case -3:
                    w.doAction(World.A_TURN_LEFT);
                    break;
                case -1:
                case 3:
                    w.doAction(World.A_TURN_RIGHT);
                    break;
                case 2:
                case -2:
                    w.doAction(World.A_TURN_LEFT);
                    w.doAction(World.A_TURN_LEFT);
                    break;
                default:
                    break;
            }
            // If the AI is headed into a room where a wumpus might be, shoot
            if (killWump && room.equals(goal))
            {
                w.doAction(World.A_SHOOT);
            }
            // After turning and possibly shooting, finally move forward
            w.doAction(World.A_MOVE);
            return true;
        }
        return false;
    }

    /**
     * Prepares the probability class and runs {@code MyProbability.calculate()}.
     * @return {@code true}
     */
    public boolean doCalc()
    {
        String[][] perception = new String[w.getSize()][w.getSize()];
        for(int i = 1; i <= w.getSize(); i++)
        {
            for(int j = 1; j <= w.getSize(); j++)
            {
                perception[i-1][j-1] = percieveRoom(i, j);
            }
        }
        probCalc.setData(perception);
        
        probCalc.calculate(availableRoomsDeque);

        return true;
    }
            
    /**
     * Asks your solver agent to execute an action.
     */
    public void doAction()
    {
        //Location of the player
        int cX = w.getPlayerX();
        int cY = w.getPlayerY();
        
        MyPRoom visitedRoom = new MyPRoom(cX, cY);
        if(!Path.visitedRoomsDeque.contains(visitedRoom))
        {
            Path.visitedRoomsDeque.push(visitedRoom);
        }
        
        //Basic action:
        //Grab Gold if we can.
        if (w.hasGlitter(cX, cY))
        {
            w.doAction(World.A_GRAB);
            return;
        }
        
        updateAvailableRooms(cX, cY);
        updateSafeRooms();

        // Select the first room in the safe rooms linked list.
        // It should be the cheapest one to move to from the 
        // player's current position
        MyPRoom goalRoom;
        boolean calcDone = false;
        if(safeRoomsDeque.isEmpty() && !availableRoomsDeque.isEmpty())
        {
            calcDone = doCalc();
        }
        otherSideOfPit = null;
        boolean maybeWump = false;
        do {
            killWump = false;
            if(!safeRoomsDeque.isEmpty())
            {
                goalRoom = safeRoomsDeque.pop();
                availableRoomsDeque.remove(goalRoom);
            }
            else if(!availableRoomsDeque.isEmpty())
            {
                if (!calcDone)
                {
                    calcDone = doCalc();
                }
                Coordinate room = probCalc.getSafestCoordinates(availableRoomsDeque, w.hasArrow());
                MyPRoom tmp = new MyPRoom(room.m_X, room.m_Y);
                
                if(room.m_probabilityWump > 0)
                {
                    killWump = true;
                }
                
                goalRoom = tmp;
                
                if (room.m_probabilityWump > 0 && room.m_probabilityWump < 100 && otherSideOfPit != null)
                {
                    maybeWump = true;
                    goalRoom = otherSideOfPit;
                }
                else if(availableRoomsDeque.contains(tmp))
                {
                    availableRoomsDeque.remove(tmp);
                }
            }
            else
            {
                goalRoom = new MyPRoom(cX, cY);
            }

            // Find path to next room
            path = Path.FindPath(goalRoom.getX(), goalRoom.getY(), cX, cY);
            if(Path.m_Pit)
            {
                otherSideOfPit = goalRoom;
            }

        }while (Path.m_Pit && !maybeWump);

        // Loop through the path and move to each room in turn until the goal room is reached
        while (!path.isEmpty())
        {
            MoveToRoom(path.pop(), goalRoom);
        }
        
        //Test the environment
        if (w.hasBreeze(cX, cY))
        {
            System.out.println("I am in a Breeze");
        }
        if (w.hasStench(cX, cY))
        {
            System.out.println("I am in a Stench");
        }
        if (w.hasPit(cX, cY))
        {
            System.out.println("I am in a Pit");
            int index = Path.visitedRoomsDeque.indexOf(visitedRoom);
            visitedRoom = Path.visitedRoomsDeque.get(index);
            if(!visitedRoom.hasPit())
            {
                visitedRoom.setPit(true);
            }
        }
        if (w.getDirection() == World.DIR_RIGHT)
        {
            System.out.println("I am facing Right");
        }
        if (w.getDirection() == World.DIR_LEFT)
        {
            System.out.println("I am facing Left");
        }
        if (w.getDirection() == World.DIR_UP)
        {
            System.out.println("I am facing Up");
        }
        if (w.getDirection() == World.DIR_DOWN)
        {
            System.out.println("I am facing Down");
        }

        try
        {
            String content = "";
            content += "When player was in position (" + cX + ", " + cY + ") and turned to " + w.getDirection() + " on turn " + ++count + "\nThe ai found the following rooms as available ones:\n";
            for(int i = 0; i < availableRoomsDeque.size(); i++)
            {
                MyPRoom tmp = availableRoomsDeque.get(i);
                content += "(" + tmp.getX() + ", " + tmp.getY() + ")\n";
            }

            if(safeRoomsDeque.size() > 0)
            {
                content += "It also found theese rooms to be safe.\n";

                for(int i = 0; i < safeRoomsDeque.size(); i++)
                {
                    MyPRoom tmp = safeRoomsDeque.get(i);
                    content += "(" + tmp.getX() + ", " + tmp.getY() + ")\n";
                }
            }
            FileWriter wr = new FileWriter("../info.txt");
            wr.write(content);
            wr.close();
        }
        catch(IOException e)
        {

        }              
    }

    void addAvailableRooms(int playerX, int playerY)
    {
        int x = playerX + (w.getDirection() -2)%2;
        int y = playerY + (w.getDirection() - 1) % 2;

        if(w.isValidPosition(x, y) && !w.isVisited(x, y))
        {
            MyPRoom tmp = new MyPRoom(x, y);
            if(availableRoomsDeque.contains(tmp))
            {
                availableRoomsDeque.remove(tmp);
            }
            availableRoomsDeque.push(tmp);
        }

        x = playerX + (w.getDirection() - 1) % 2;
        y = playerY + (2 - w.getDirection()) % 2;
        if(w.isValidPosition(x, y) && !w.isVisited(x, y))
        {
            MyPRoom tmp = new MyPRoom(x, y);
            if(availableRoomsDeque.contains(tmp))
            {
                availableRoomsDeque.remove(tmp);
            }
            availableRoomsDeque.push(tmp);
        }

        x = playerX + (1 - w.getDirection()) % 2;        
        y = playerY + (w.getDirection() - 2) % 2;
        if(w.isValidPosition(x, y) && !w.isVisited(x, y))
        {
            MyPRoom tmp = new MyPRoom(x, y);
            if(availableRoomsDeque.contains(tmp))
            {
                availableRoomsDeque.remove(tmp);
            }
            availableRoomsDeque.push(tmp);
        }

        x = playerX + (2 - w.getDirection()) % 2;
        y = playerY + (1 - w.getDirection()) % 2;
        if(w.isValidPosition(x, y) && !w.isVisited(x, y))
        {
            MyPRoom tmp = new MyPRoom(x, y);
            if(availableRoomsDeque.contains(tmp))
            {
                availableRoomsDeque.remove(tmp);
            }
            availableRoomsDeque.push(tmp);
        }
    }

    void updateAvailableRooms(int playerX, int playerY)
    {
        addAvailableRooms(playerX, playerY);

        for(int i = 0; i < availableRoomsDeque.size(); i++)
        {
            MyPRoom tmp = availableRoomsDeque.get(i);
            tmp.setH(Math.abs(tmp.getX() - playerX) + Math.abs(tmp.getY() - playerY));
            if(w.isVisited(tmp.getX(), tmp.getY()))
            {
                availableRoomsDeque.remove(i);
            }
        }

        availableRoomsDeque.sort(new Comparator<MyPRoom>() {
            @Override
            public int compare(MyPRoom r1, MyPRoom r2)
            {
                return  r1.getH() - r2.getH();
            }
        });
    }
    
    void updateSafeRooms()
    {
        for(int i = availableRoomsDeque.size() - 1; i >= 0; i--)
        {
            MyPRoom tmp = availableRoomsDeque.get(i);
            int x = tmp.getX();
            int y = tmp.getY();

            if(pitNo(x,y) && wumpNo(x,y))
            {
                if(safeRoomsDeque.contains(tmp))
                {
                    safeRoomsDeque.remove(tmp);
                }
                safeRoomsDeque.push(tmp);
            }
        }

        for(int i = 0; i < safeRoomsDeque.size(); i++)
        {
            MyPRoom tmp = safeRoomsDeque.get(i);
            if(w.isVisited(tmp.getX(), tmp.getY()))
            {
                safeRoomsDeque.remove(i);
            }
        }
    }
    
    boolean pitNo(int x, int y)
    {
        return     w.isValidPosition(x + 1, y) && !w.hasBreeze(x + 1, y) && !w.isUnknown(x + 1, y)
                || w.isValidPosition(x - 1, y) && !w.hasBreeze(x - 1, y) && !w.isUnknown(x - 1, y)
                || w.isValidPosition(x, y + 1) && !w.hasBreeze(x, y + 1) && !w.isUnknown(x, y + 1)
                || w.isValidPosition(x, y - 1) && !w.hasBreeze(x, y - 1) && !w.isUnknown(x, y - 1);
    }

    boolean wumpNo(int x, int y)
    {
        return     w.isValidPosition(x + 1, y) && !w.hasStench(x + 1, y) && !w.isUnknown(x + 1, y)
                || w.isValidPosition(x - 1, y) && !w.hasStench(x - 1, y) && !w.isUnknown(x - 1, y)
                || w.isValidPosition(x, y + 1) && !w.hasStench(x, y + 1) && !w.isUnknown(x, y + 1)
                || w.isValidPosition(x, y - 1) && !w.hasStench(x, y - 1) && !w.isUnknown(x, y - 1);
    }
    
    String percieveRoom(int x, int y)
    {
        String tmp = "";
        
        if(w.isUnknown(x, y))
        {
            tmp += World.UNKNOWN;
        }
        else
        {
            if(w.hasStench(x, y))
            {
                tmp += World.STENCH;
            }

            if(w.hasBreeze(x, y))
            {
                tmp += World.BREEZE;
            }

            if(w.hasPit(x, y))
            {
                tmp += World.PIT;
            }

            if(w.hasGlitter(x, y))
            {
                tmp += World.GLITTER;
            }

            if(w.hasWumpus(x, y))
            {
                tmp += World.WUMPUS;
            }
        }

        return tmp;
    }
    
    int calcMoveCost(int playX, int playY, int tarX, int tarY, int currDirection)
    {
        if(w.isValidPosition(playX + 1, playY))
        {
            calcMoveCost(playX + 1, playY, tarX, tarY, World.DIR_RIGHT);
        }

        if(w.isValidPosition(playX - 1, playY))
        {
            calcMoveCost(playX - 1, playY, tarX, tarY, World.DIR_LEFT);
        }

        if(w.isValidPosition(playX, playY + 1))
        {
            calcMoveCost(playX, playY + 1, tarX, tarY, World.DIR_UP);
        }

        if(w.isValidPosition(playX, playY - 1))
        {
            calcMoveCost(playX, playY - 1, tarX, tarY, World.DIR_DOWN);
        }
        return 0;
    }
}

