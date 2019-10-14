package wumpusworld;

import java.util.*;

public class MyProbability
{
    private String[][] m_knownData;

    private int m_pitProb[][];
    private int m_wumpProb[][];

    public MyProbability(int sizeOfGrid)
    {
        m_knownData = new String[sizeOfGrid][sizeOfGrid];
        m_pitProb = new int[sizeOfGrid][sizeOfGrid];
        m_wumpProb = new int[sizeOfGrid][sizeOfGrid];

        for(int i = 0; i < sizeOfGrid; i++)
        {
            for(int j = 0; j < sizeOfGrid; j++)
            {
                m_knownData[i][j] = World.UNKNOWN;
                m_pitProb[i][j] = -1;
                m_wumpProb[i][j] = -1;
            }
        }

    }

    public void setData(String[][] data)
    {
        for(int i = 0; i < data.length; i++)
        {
            for(int j = 0; j < data[i].length; j++)
            {
                m_knownData[i][j] = data[i][j];
            }
        }
    }

    public class Coordinate
    {
        public int m_X;
        public int m_Y;
        public int m_probabilityPit;
        public int m_probabilityWump;
        public Coordinate(int x, int y)
        {
            m_X = x;
            m_Y = y;
        }
        public boolean equals(Object o)
        {
            if(m_X == ((Coordinate)o).m_X && m_Y == ((Coordinate)o).m_Y)
            {
                return true;
            }
            return false;
        }
    }

    /**
     * calculates the probabilities of the existence of wumpus and/or 
     * pits of all locations in the world set with the method setData(String[][] data).
     * The probabilities are dependent of the perceptions.
     * @param availableRooms rooms that should get values. Any room that isn't adjacent to a stench or breeze AND is not present in the param will get a -1 as value.
     */
    public void calculate(List<MyPRoom> availableRooms)
    {
        // Find all positions where a pit COULD be found.
        ArrayList<Coordinate> possiblePits = addPossiblePits();

        ArrayList<Coordinate> possibleWumpus = addPossibleWumpus();

        // Generate models for all possible combinations of pits...
        if(possiblePits.size() > 0)
        {
            //System.out.println("Found possible pits!");
            ArrayList<String[][]> models = possibleWorlds(possiblePits);
            //System.out.println("Generated models");
            // Throw out any model that disobeys the rules (a model will be removed if there is a breeze without a pit).
            ArrayList<String[][]> legitModels = new ArrayList<String[][]>();
            for(int i = 0; i < models.size(); i++)
            {
                if(legitWorldPit(models.get(i)))
                {
                    legitModels.add(models.get(i));
                }
            }

            //NOW we can calculate...
            for(int i = 0; i < legitModels.size(); i++)
            {
                String[][]tmp = legitModels.get(i);

                for(int j = 0; j < possiblePits.size(); j++)
                {
                    Coordinate coord = possiblePits.get(j);
                    coord.m_probabilityPit += tmp[coord.m_X][coord.m_Y].contains(World.PIT) ? 1:0;

                    possiblePits.set(j, coord);
                }
            }

            for(int i = 0; i < possiblePits.size(); i++)
            {
                Coordinate tmp = possiblePits.get(i);
                tmp.m_probabilityPit =  tmp.m_probabilityPit* 100 / legitModels.size();
                possiblePits.set(i, tmp);
                m_pitProb[tmp.m_X][tmp.m_Y] = tmp.m_probabilityPit;
                System.out.println("There is a chance of " + tmp.m_probabilityPit + "% that there is a pit in location (" + (tmp.m_X + 1) + ", " + (tmp.m_Y + 1) + ")\n");
            }
        }

        if(possibleWumpus.size() > 0)
        {
            ArrayList<Coordinate> legitWump = new ArrayList<Coordinate>();
            System.out.println("possibleWumps: " + possibleWumpus.size());
            for(int i = 0; i < possibleWumpus.size(); i++)
            {
                String[][] tmp = new String[m_knownData.length][m_knownData.length];
                for (int j = 0; j < m_knownData.length; j++)
                {
                    for (int k = 0; k < m_knownData.length; k++)
                    {
                        tmp[j][k] = m_knownData[j][k];
                    }
                }
                Coordinate tmpCoord = possibleWumpus.get(i);
                tmp[tmpCoord.m_X][tmpCoord.m_Y] = World.WUMPUS;

                if(legitWorldWump(tmp))
                {
                    legitWump.add(tmpCoord);
                }
                else
                {
                    // System.out.println("Threw wump! " + i);
                }
            }
            possibleWumpus = legitWump;

            for(int i = 0; i < possibleWumpus.size(); i++)
            {
                Coordinate tmp = possibleWumpus.get(i);

                tmp.m_probabilityWump = 100 / possibleWumpus.size();
                possibleWumpus.set(i,tmp);
                m_wumpProb[tmp.m_X][tmp.m_Y] = tmp.m_probabilityWump;

                System.out.println("There is a chance of " + tmp.m_probabilityWump + "% that there is a wumpus in location (" + (tmp.m_X + 1) + ", " + (tmp.m_Y + 1) + ")\n");
            }
        }
        
        for(int i = 0; i < availableRooms.size(); i++)
        {
            MyPRoom tmpRoom = availableRooms.get(i);
            if(m_pitProb[tmpRoom.getX() - 1][tmpRoom.getY() - 1] == -1)
            {
                m_pitProb[tmpRoom.getX() - 1][tmpRoom.getY() - 1] = 0;
            }

            if(m_wumpProb[tmpRoom.getX() - 1][tmpRoom.getY() - 1] == -1)
            {
                m_wumpProb[tmpRoom.getX() - 1][tmpRoom.getY() - 1] = 0;
            }
        }
    }

    /**
     * A function that gives out all possible positions for pits.
     * No logic involved, any unkown area in the known data could contain a pit.
     * @return an ArrayList<Coordinate> of all possible pit locations.
     */
    private ArrayList<Coordinate> addPossiblePits()
    {
        ArrayList<Coordinate> possiblePits = new ArrayList<Coordinate>();

        for(int i = 0; i < m_knownData.length; i++)
        {
            for(int j = 0; j < m_knownData[i].length; j++)
            {
                if(m_knownData[i][j].contains(World.BREEZE))
                {
                    if(i+1 < m_knownData.length && m_knownData[i+1][j].contains(World.UNKNOWN))
                    {
                        Coordinate tmp = new Coordinate(i+1, j);
                        if(!possiblePits.contains(tmp))
                        {
                            possiblePits.add(tmp);
                        }
                    }

                    if(i - 1 >= 0 && m_knownData[i-1][j].contains(World.UNKNOWN))
                    {
                        Coordinate tmp = new Coordinate(i-1, j);

                        if(!possiblePits.contains(tmp))
                        {
                            possiblePits.add(tmp);
                        }
                    }

                    if( j + 1 < m_knownData[i].length && m_knownData[i][j + 1].contains(World.UNKNOWN))
                    {
                        Coordinate tmp = new Coordinate(i, j + 1);
                        if(!possiblePits.contains(tmp))
                        {
                            possiblePits.add(tmp);
                        }
                    }

                    if(j - 1 >= 0 && m_knownData[i][j - 1].contains(World.UNKNOWN))
                    {
                        Coordinate tmp = new Coordinate(i, j - 1);
                        if(!possiblePits.contains(tmp))
                        {
                            possiblePits.add(tmp);
                        }
                    }
                }
            }
        }
        return possiblePits;
    }

    /**
     * A function that gives out all possible positions for the wumpus.
     * No logic involved, any unkown area in the known data could contain a wumpus.
     * @return an ArrayList<Coordinate> of all possible locations for a Wumpus.
     */
    private ArrayList<Coordinate> addPossibleWumpus()
    {
        ArrayList<Coordinate> possibleWumpus = new ArrayList<Coordinate>();
        for(int i = 0; i < m_knownData.length; i++)
        {
            for(int j = 0; j < m_knownData[i].length; j++)
            {
                if(m_knownData[i][j].contains(World.STENCH))
                {
                    if(i+1 < m_knownData.length && m_knownData[i+1][j].contains(World.UNKNOWN))
                    {
                        Coordinate tmp = new Coordinate(i+1, j);
                        if(!possibleWumpus.contains(tmp))
                        {
                            possibleWumpus.add(tmp);
                        }
                    }

                    if(i - 1 >= 0 && m_knownData[i-1][j].contains(World.UNKNOWN))
                    {
                        Coordinate tmp = new Coordinate(i-1, j);

                        if(!possibleWumpus.contains(tmp))
                        {
                            possibleWumpus.add(tmp);
                        }
                    }

                    if( j + 1 < m_knownData[i].length && m_knownData[i][j + 1].contains(World.UNKNOWN))
                    {
                        Coordinate tmp = new Coordinate(i, j + 1);
                        if(!possibleWumpus.contains(tmp))
                        {
                            possibleWumpus.add(tmp);
                        }
                    }

                    if(j - 1 >= 0 && m_knownData[i][j - 1].contains(World.UNKNOWN))
                    {
                        Coordinate tmp = new Coordinate(i, j - 1);
                        if(!possibleWumpus.contains(tmp))
                        {
                            possibleWumpus.add(tmp);
                        }
                    }
                }
            }
        }
        return possibleWumpus;
    }
    
    /**
     * Checks if the given world is satisfiable. 
     * If the world follows the rule that all breezes must be adjacent to atleast one pit, returns true.
     * @param world model of the world that is to be tested.
     * @return true if the world is satisfiable with regard to breezes.
     */
    private boolean legitWorldPit(String[][] world)
    {
        for(int i = 0; i < world.length; i++)
        {
            for(int j = 0; j < world[i].length; j++)
            {
                if(world[i][j].contains(World.BREEZE))
                {
                    boolean res = false;

                    if(i + 1 < world.length && world[i + 1][j].contains(World.PIT))
                    {
                        res = true;
                    }

                    if( i - 1 >= 0 && world[i - 1][j].contains(World.PIT))
                    {
                        res = true;
                    }

                    if( j + 1 < world[i].length && world[i][j + 1].contains(World.PIT))
                    {
                        res = true;
                    }

                    if( j - 1 >= 0  && world[i][j - 1].contains(World.PIT))
                    {
                        res = true;
                    }

                    if(!res)
                    {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    /**
     * Checks if the given world is satisfiable. 
     * If the world follows the rule that all stenches must be adjacent to one wumpus.
     * @param world model of the world that is to be tested.
     * @return true if the world is satisfiable with regard to stenches AND that there only may be one wumpus.
     */
    private boolean legitWorldWump(String[][] world)
    {
        for(int i = 0; i < world.length; i++)
        {
            for(int j = 0; j < world[i].length; j++)
            {
                if(world[i][j].contains(World.STENCH))
                {
                    boolean res = false;
                    int wumpX = -1;
                    int wumpY = -1;

                    if(i + 1 < world.length && world[i + 1][j].contains(World.WUMPUS))
                    {
                        wumpX = i+1;
                        wumpY = j;
                        res = true;
                    }

                    if(i - 1 >= 0 && world[i - 1][j].contains(World.WUMPUS))
                    {
                        // There may only be one wumpus!
                        if(res)
                        {
                            return false;
                        }
                        wumpX = i-1;
                        wumpY = j;
                        res = true;
                    }
                
                    if( j + 1 < world[i].length && world[i][j + 1].contains(World.WUMPUS))
                    {
                        // There may only be one wumpus!
                        if(res)
                        {
                            return false;
                        }
                        wumpX = i;
                        wumpY = j + 1;
                        res = true;
                    }

                    if( j - 1 >= 0  && world[i][j - 1].contains(World.WUMPUS))
                    {
                        // There may only be one wumpus!
                        if(res)
                        {
                            return false;
                        }
                        wumpX = i;
                        wumpY = j - 1;
                        res = true;
                    }
                    
                    if(!res)
                    {
                        return false;
                    }
                    else
                    {
                        boolean unknownUp, unknownRight, unknownDown, unknownLeft;
                        unknownUp = unknownRight = unknownDown = unknownLeft = true;
                        if (wumpY + 1 < world.length)
                        {
                            System.out.println("Checking up");
                            unknownUp = world[wumpX][wumpY + 1].contains(World.UNKNOWN) || world[wumpX][wumpY + 1].contains(World.STENCH);
                            if(!unknownUp)
                            {
                                System.out.println("FAIL!");
                            }
                        }
                        if (wumpX + 1 < world.length)
                        {
                            System.out.println("Checking right");
                            unknownRight = world[wumpX + 1][wumpY].contains(World.UNKNOWN) || world[wumpX + 1][wumpY].contains(World.STENCH);
                            if(!unknownRight)
                            {
                                System.out.println("FAIL!");
                            }
                        }
                        if (wumpY - 1 >= 0)
                        {
                            System.out.println("Checking down");
                            unknownDown = world[wumpX][wumpY - 1].contains(World.UNKNOWN) || world[wumpX][wumpY - 1].contains(World.STENCH);
                            if(!unknownDown)
                            {
                                System.out.println("FAIL!");
                            }
                        }
                        if (wumpX - 1 >= 0)
                        {
                            System.out.println("Checking left");
                            unknownLeft = world[wumpX - 1][wumpY].contains(World.UNKNOWN) || world[wumpX - 1][wumpY].contains(World.STENCH);
                            if(!unknownLeft)
                            {
                                System.out.println("FAIL!");
                            }
                        }

                        if (unknownUp && unknownRight && unknownDown && unknownLeft)
                        {
                            System.out.println("There is wump in (" + wumpX + ", " + wumpY + ")");
                        }
                        else
                        {
                            System.out.println("There is no wump in (" + wumpX + ", " + wumpY + ")");
                        }
                        return unknownUp && unknownRight && unknownDown && unknownLeft;

                    }
                }
            }
        }
        return true;
    }

    /**
     * Used to generate all possible worlds (without using logic!) to get all combinations of pits.
     * this is done by going through the list of the pits and setting them up to either exist or not, making a tree of models.
     * @param possiblePits list of positions where a pit might appear.
     * @return an ArrayList of all possible models given possible positions of pits
     */
    private ArrayList<String[][]> possibleWorlds(ArrayList<Coordinate> possiblePits)
    {
        ArrayList<String[][]> models;
        models = new ArrayList<String[][]>();
        models.add(m_knownData);

        /*for(int i = 0; i < m_knownData.length; i++)
        {
            for(int j = 0; j < m_knownData[i].length; j++)
            {
                if(m_knownData[j][m_knownData.length - 1 - i] == "")
                    System.out.print(" ");
                System.out.print(m_knownData[j][m_knownData.length - 1 - i] + "|");
            }
            System.out.print("\n");
        }*/

        //System.out.println("Generated first model...");
        recurPossibleWorlds(models, possiblePits, 0, 0, true);
        /*for(int i = 0; i < models.size(); i++)
        {
            String [][] tmp = models.get(i);
            System.out.println("Model nr " + i);
            for(int j = 0; j < tmp.length; j++)
            {
                for(int k = 0; k < tmp[j].length; k++)
                {

                    // rotera med följande matris!
                    // 0, -1
                    // 1,  0
                    if(tmp[k][tmp.length - 1 - j] == "")
                        System.out.print(" ");
                    System.out.print(tmp[k][tmp.length - 1 - j] + "|");
                }
                System.out.print("\n");
            }
            System.out.println("__________________");
        }*/
        return models;
    }

    /**
     * helping recursive method to possibleWorlds. 
     * @param models
     * @param possiblePits
     * @param depth
     * @param model
     * @param setPit
     */
    private void recurPossibleWorlds(ArrayList<String[][]> models, ArrayList<Coordinate> possiblePits, int depth, int model, boolean setPit)
    {
        Coordinate tmp = possiblePits.get(depth);

        String [][] nextModel = new String[m_knownData.length][m_knownData.length];
        
        String [][] tmpModPitYes = models.get(model);
        
        for(int i = 0; i < nextModel.length; i++)
        {
            for(int j = 0; j < nextModel[i].length; j++)
            {
                nextModel[i][j] = tmpModPitYes[i][j];
            }
        }
        
        tmpModPitYes[tmp.m_X][tmp.m_Y] = World.PIT;
        nextModel[tmp.m_X][tmp.m_Y] = "";
        models.set(model, tmpModPitYes);
        models.add(nextModel);
        // the pit either exists or it doesnt.
        if(depth < possiblePits.size() - 1)
        {
            recurPossibleWorlds(models, possiblePits, depth + 1, model, true);
            recurPossibleWorlds(models, possiblePits, depth + 1, model + 1, false);
        }
    }

    public void getPitProbabilities(int[][] arr)
    {
        for(int i = 1; i < arr.length; i++)
        {
            for(int j = 1; j < arr[i].length; j++)
            {
                arr[i][j] = m_pitProb[i-1][j-1];
            }
        }
    }

    public void getWumpProbabilities(int[][] arr)
    {
        for(int i = 1; i < arr.length; i++)
        {
            for(int j = 1; j < arr[i].length; j++)
            {
                arr[i][j] = m_wumpProb[i-1][j-1];
            }
        }   
    }

    public Coordinate getSafestCoordinates(List<MyPRoom> rooms)
    {
        int bestWump = 100;
        int bestPit = 100;
        Coordinate toReturn = null;

        for(int i = 0; i < rooms.size(); i++)
        {
            MyPRoom tmp = rooms.get(i);
            // System.out.println(tmp.getX() + ", " + tmp.getY() + "has a wump of " + m_wumpProb[tmp.getX() - 1][tmp.getY() - 1]);
            // System.out.println(tmp.getX() + ", " + tmp.getY() + "has a pit of " + m_pitProb[tmp.getX() - 1][tmp.getY() - 1]);

            if((m_wumpProb[tmp.getX() - 1][tmp.getY() - 1] < bestWump || toReturn == null))
            {
                if (toReturn == null)
                {
                    toReturn = new Coordinate(tmp.getX(), tmp.getY());
                    bestWump = m_wumpProb[tmp.getX() - 1][tmp.getY() - 1];
                    bestPit = m_pitProb[tmp.getX() - 1][tmp.getY() - 1];
                    toReturn.m_probabilityWump = bestWump;
                    toReturn.m_probabilityPit = bestPit;
                }
                else if (!(toReturn.m_probabilityWump == 100 && toReturn.m_probabilityPit == 0))
                {
                    toReturn = new Coordinate(tmp.getX(), tmp.getY());
                    bestWump = m_wumpProb[tmp.getX() - 1][tmp.getY() - 1];
                    bestPit = m_pitProb[tmp.getX() - 1][tmp.getY() - 1];
                    toReturn.m_probabilityWump = bestWump;
                    toReturn.m_probabilityPit = bestPit;
                }
            }
            else if(m_pitProb[tmp.getX() - 1][tmp.getY() - 1] < bestPit && m_wumpProb[tmp.getX() - 1][tmp.getY() - 1] <= bestWump && !(toReturn.m_probabilityWump == 100 && toReturn.m_probabilityPit == 0))
            {
                // System.out.println("switching from (" + toReturn.m_X + ", " + toReturn.m_Y + ")" + " to (" + tmp.getX() + ", " + tmp.getY() + ")");
                toReturn = new Coordinate(tmp.getX(), tmp.getY());
                bestWump = m_wumpProb[tmp.getX() - 1][tmp.getY() - 1];
                bestPit = m_pitProb[tmp.getX() - 1][tmp.getY() - 1];
                toReturn.m_probabilityWump = bestWump;
                toReturn.m_probabilityPit = bestPit;
            }
        }

        // System.out.println("Suggesting " + toReturn.m_X + ", " + toReturn.m_Y);
        return toReturn;
    }
}