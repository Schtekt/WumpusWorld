package wumpusworld;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

/**
 * Contains starting code for creating your own Wumpus World agent.
 * Currently the agent only make a random decision each turn.
 * 
 * @author Johan Hagelbäck
 */
public class MyAgent implements Agent
{
    private World w;
    int rnd;
    int count = 0;
    List<MyPRoom> availableRooms;
    LinkedList<MyPRoom> availableRoomsDeque;
    LinkedList<MyPRoom> safeRoomsDeque;
    // Uneccessary?
    //List<MyPRoom> allRooms;
    List<MyPRoom> safeRooms;
    /**
     * Creates a new instance of your solver agent.
     * 
     * @param world Current world state 
     */
    public MyAgent(World world)
    {
        w = world;

        availableRooms = new ArrayList<MyPRoom>();
        safeRooms = new ArrayList<MyPRoom>();
        //visitedRooms = new ArrayList<MyPRoom>();
        availableRoomsDeque = new LinkedList<MyPRoom>();
        safeRoomsDeque = new LinkedList<MyPRoom>();
        //MyPRoom tmp = new MyPRoom(1,1);
        //tmp.setPerception(w.hasStench(1, 1), w.hasBreeze(1,1), w.hasGlitter(1,1), w.hasWumpus(1, 1), w.hasPit(1,1));
    }
   
            
    /**
     * Asks your solver agent to execute an action.
     */

    public void doAction()
    {
        //Location of the player
        int cX = w.getPlayerX();
        int cY = w.getPlayerY();
        
        //Basic action:
        //Grab Gold if we can.
        if (w.hasGlitter(cX, cY))
        {
            w.doAction(World.A_GRAB);
            return;
        }
        
        //Basic action:
        //We are in a pit. Climb up.
        if (w.isInPit())
        {
            w.doAction(World.A_CLIMB);
            return;
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
        
        updateAvailableRooms(cX, cY);
        updateSafeRooms();

        try
        {
            String content = "";
            content += "When player was in position (" + cX + ", " + cY + ") and turned to " + w.getDirection() + " on turn " + ++count + "\nThe ai found following rooms as available ones:\n";
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
        //decide next move
        rnd = decideRandomMove();
        if (rnd==0)
        {
            w.doAction(World.A_TURN_LEFT);
            w.doAction(World.A_MOVE);
        }
        
        if (rnd==1)
        {
            w.doAction(World.A_MOVE);
        }
                
        if (rnd==2)
        {
            w.doAction(World.A_TURN_LEFT);
            w.doAction(World.A_TURN_LEFT);
            w.doAction(World.A_MOVE);
        }
                        
        if (rnd==3)
        {
            w.doAction(World.A_TURN_RIGHT);
            w.doAction(World.A_MOVE);
        }
                
    }    
    //based on uneccesary system.
    /*void addAvailableRooms(int playerX, int playerY)
    {
        MyPRoom tmpe = allRooms.get((playerX - 1)*4 + playerY - 1);
        System.out.println(tmpe.getX() + ", " + tmpe.getY());

        if(w.isValidPosition(playerX + 1, playerY))
        {
            MyPRoom tmp = allRooms.get((((playerX)*4) + playerY - 1));

            if(!availableRooms.contains(tmp) && tmp.getPerception() == World.UNKNOWN)
            {
                availableRooms.add(tmp);
                System.out.println("Added room!" + (tmp.getX()) + ", " + tmp.getY());
            }
        }

        if(w.isValidPosition(playerX - 1, playerY))
        {
            MyPRoom tmp = allRooms.get(((playerX - 2)*4 + playerY - 1));

            if(!availableRooms.contains(tmp) && tmp.getPerception() == World.UNKNOWN)
            {
                availableRooms.add(tmp);
                System.out.println("Added room!" + (tmp.getX()) + ", " + tmp.getY());
            }
        }

        if(w.isValidPosition(playerX, playerY + 1))
        {
            MyPRoom tmp = allRooms.get(((playerX - 1)*4 + (playerY)));

            if(!availableRooms.contains(tmp) && tmp.getPerception() == World.UNKNOWN)
            {
                availableRooms.add(tmp);
                System.out.println("Added room!" + tmp.getX() + ", " + (tmp.getY()));
            }
        }

        if(w.isValidPosition(playerX, playerY - 1))
        {
            MyPRoom tmp = allRooms.get(((playerX - 1)*4 + (playerY - 2)));

            if(!availableRooms.contains(tmp) && tmp.getPerception() == World.UNKNOWN)
            {
                availableRooms.add(tmp);
                System.out.println("Added room!" + tmp.getX() + ", " + (tmp.getY()));
            }
        }
    }*/
    
    void addAvailableRooms(int playerX, int playerY)
    {
        int x = playerX + (w.getDirection() -2)%2;
        int y = playerY + (w.getDirection() - 1) % 2;
        if(w.isValidPosition(x, y) && !w.isVisited(x, y))
        {
            MyPRoom tmp = new MyPRoom(x, y);
            if(!availableRoomsDeque.contains(tmp))
                availableRoomsDeque.push(tmp);
        }

        x = playerX + (w.getDirection() - 1) % 2;
        y = playerY + (2 - w.getDirection()) % 2;
        if(w.isValidPosition(x, y) && !w.isVisited(x, y))
        {
            MyPRoom tmp = new MyPRoom(x, y);
            if(!availableRoomsDeque.contains(tmp))
                availableRoomsDeque.push(tmp);
        }

        x = playerX + (1 - w.getDirection()) % 2;        
        y = playerY + (w.getDirection() - 2) % 2;
        if(w.isValidPosition(x, y) && !w.isVisited(x, y))
        {
            MyPRoom tmp = new MyPRoom(x, y);
            if(!availableRoomsDeque.contains(tmp))
                availableRoomsDeque.push(tmp);
        }

        x = playerX + (2 - w.getDirection()) % 2;
        y = playerY + (1 - w.getDirection()) % 2;
        if(w.isValidPosition(x, y) && !w.isVisited(x, y))
        {
            MyPRoom tmp = new MyPRoom(x, y);
            if(!availableRoomsDeque.contains(tmp))
                availableRoomsDeque.push(tmp);
        }

        if(w.isValidPosition(playerX + 1, playerY) && !w.isVisited(playerX + 1, playerY))
        {
            MyPRoom tmp = new MyPRoom(playerX + 1, playerY);
            
            if(!availableRooms.contains(tmp))
            {
                availableRooms.add(tmp);
            }
        }

        if(w.isValidPosition(playerX - 1, playerY) && !w.isVisited(playerX - 1, playerY))
        {
            MyPRoom tmp = new MyPRoom(playerX - 1, playerY);
            
            if(!availableRooms.contains(tmp))
            {
                availableRooms.add(tmp);
            }
        }

        if(w.isValidPosition(playerX, playerY + 1) && !w.isVisited(playerX, playerY + 1))
        {
            MyPRoom tmp = new MyPRoom(playerX, playerY + 1);
            
            if(!availableRooms.contains(tmp))
            {
                availableRooms.add(tmp);
            }
        }

        if(w.isValidPosition(playerX, playerY - 1) && !w.isVisited(playerX, playerY - 1))
        {
            MyPRoom tmp = new MyPRoom(playerX, playerY - 1);
            
            if(!availableRooms.contains(tmp))
            {
                availableRooms.add(tmp);
            }
        }
    }

    void updateAvailableRooms(int playerX, int playerY)
    {
        addAvailableRooms(playerX, playerY);

        for(int i = 0; i < availableRoomsDeque.size(); i++)
        {
            MyPRoom tmp = availableRoomsDeque.get(i);
            if(w.isVisited(tmp.getX(), tmp.getY()))
            {
                availableRoomsDeque.remove(i);
            }
        }

        for(int i = 0; i < availableRooms.size(); i++)
        {
            MyPRoom tmp = availableRooms.get(i);
            if(w.isVisited(tmp.getX(), tmp.getY()))
            {
                availableRooms.remove(i);
            }
        }
    }
    
    void updateSafeRooms()
    {
        for(int i = 0; i < availableRooms.size(); i++)
        {
            MyPRoom tmp = availableRooms.get(i);
            int x = tmp.getX();
            int y = tmp.getY();

            if(pitNo(x,y) && wumpNo(x,y) && !safeRooms.contains(tmp))
            {
                // Found a room that absoluteley does not have a wumpus or pit within. 
                // Add to rooms which we can move to.
                safeRooms.add(tmp);
            }
        }

        for(int i = 0; i < safeRooms.size(); i++)
        {
            MyPRoom tmp = safeRooms.get(i);
            if(w.isVisited(tmp.getX(), tmp.getY()))
            {
                safeRooms.remove(i);
            }
        }

        for(int i = 0; i < availableRoomsDeque.size(); i++)
        {
            MyPRoom tmp = availableRoomsDeque.get(i);
            int x = tmp.getX();
            int y = tmp.getY();

            if(pitNo(x,y) && wumpNo(x,y) && !safeRoomsDeque.contains(tmp))
            {
                safeRoomsDeque.add(tmp);
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
        return     w.isValidPosition(x + 1, y) && w.isValidPosition(x + 1, y) && !w.hasStench(x + 1, y) && !w.isUnknown(x + 1, y)
                || w.isValidPosition(x - 1, y) && w.isValidPosition(x - 1, y) && !w.hasStench(x - 1, y) && !w.isUnknown(x - 1, y)
                || w.isValidPosition(x, y + 1) && w.isValidPosition(x, y + 1) && !w.hasStench(x, y + 1) && !w.isUnknown(x, y + 1)
                || w.isValidPosition(x, y - 1) && w.isValidPosition(x, y - 1) && !w.hasStench(x, y - 1) && !w.isUnknown(x, y - 1);
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
    
    /**
     * calculates the correct direction the player has to face to move into an adjacent room.
     * calculate relative coordinates by subtracting next location by the current location.
     * @param relX relative X coordinate. Either 1 or -1 or 0. (next - current)
     * @param relY relative Y coordinate. Either 1 or -1 or 0. (next - current)
     * @return the direction the player should face to move towards the next room.
     */
    int calcCorrectDirection(int relX, int relY)
    {
        /*
            insätt 1, 0 (detta ska bli 0).
            abs(1 - 1 + 2*0) = 0

            0,1
            abs(0-1 + 2*1) = 1

            -1,0
            abs(-1-1 + 2*0) = 2

            0,-1
            abs(0-1 + 2*-1) = 3
        */
        return Math.abs(relX - 1 + 2*relY);
    }

    int calcDirectionalCost(int corrDirection, int currDirection)
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
    /**
     * Genertes a random instruction for the Agent.
     */
    public int decideRandomMove()
    {
      return (int)(Math.random() * 4);
    }
    
    
}

