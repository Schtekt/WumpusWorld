package wumpusworld;
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
    List<MyPRoom> availableRooms;
    List<MyPRoom> allRooms;
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
        allRooms = new ArrayList<MyPRoom>();
        safeRooms = new ArrayList<MyPRoom>();

        for(int i = 0; i < w.getSize(); i++)
        {
            for(int j = 0; j < w.getSize(); j++)
            {
                allRooms.add(new MyPRoom(i+1, j+1));
            }
        }

        // commented out code used to verify that all rooms have been added
        for(int i = 0; i < w.getSize()*w.getSize(); i++)
        {
            MyPRoom tmp = allRooms.get(i);
            System.out.println(tmp.getX() + ", " + tmp.getY() + " " + tmp.getPerception());
        }
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
        
        addAvailableRooms(cX, cY);
        addSafeRooms();

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
    
    void addAvailableRooms(int playerX, int playerY)
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
    } 
    
    void addSafeRooms()
    {
        for(int i = 0; i < availableRooms.size(); i++)
        {
            MyPRoom tmp = availableRooms.get(i);
            int x = tmp.getX();
            int y = tmp.getY();
            if(pitNo(x,y) && wumpNo(x,y))
            {
                // Found a room that absoluteley does not have a wumpus or pit within. 
                // Add to rooms which we can move to.
                safeRooms.add(tmp);
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
    /**
     * Genertes a random instruction for the Agent.
     */
    public int decideRandomMove()
    {
      return (int)(Math.random() * 4);
    }
    
    
}

