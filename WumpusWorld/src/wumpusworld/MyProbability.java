package wumpusworld;

import java.util.*;

public class MyProbability
{
    // m_knownData is used as the knowledgebase of the AI.
    private String[][] m_knownData;

    // m_pitProb and m_wumpProb are used to store the current percentages of how sure the AI is of the existence of a pit respectively wumpus.
    private int m_pitProb[][];
    private int m_wumpProb[][];

    //constructor, initiate everything
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

    // Sets the data of the knowledgebase, when calculating probabilities the AI needs to percieve the world, where are the breezes and stenches?
    public void setData(String[][] data)
    {
        for(int i = 0; i < data.length; i++)
        {
            for(int j = 0; j < data[i].length; j++)
            {
                m_knownData[i][j] = data[i][j];
                m_pitProb[i][j] = -1;
                m_wumpProb[i][j] = -1;
            }
        }
    }

    // helpful class to determine specific coordinates, this helps us when we consider possible positions of pits and/or the wumpus.
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

        // if the coordinates are on the same position, they are considered the same!
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
        // Find all positions where a pit COULD be found. Based on what data has been percieved with the setData function.
        ArrayList<Coordinate> possiblePits = addPossiblePits();

        // Find all positions where the wumpus COULD be found. Based on what data has been percieved with the setData function.
        ArrayList<Coordinate> possibleWumpus = addPossibleWumpus();

        // time to check for pits
        if(possiblePits.size() > 0)
        {
            // Generate models for all possible combinations of pits... and save them to a list.
            ArrayList<String[][]> models = possibleWorlds(possiblePits);

            // Add models that obeys the rules of the world to a new list.
            ArrayList<String[][]> legitModels = new ArrayList<String[][]>();
            for(int i = 0; i < models.size(); i++)
            {
                if(legitWorldPit(models.get(i)))
                {
                    legitModels.add(models.get(i));
                }
            }

            // With all maps that obey the game rules saved to a list, calculations can actually begin.
            // Count number of times a pit appears in coordinates across all maps. let's call this count an occurrence score for future reference.
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

            // now that all pits have gained an occurence score we simply divide their score by the number of maps
            // We also multiply by 100 as to not use decimals in our ints.
            for(int i = 0; i < possiblePits.size(); i++)
            {
                Coordinate tmp = possiblePits.get(i);
                tmp.m_probabilityPit =  tmp.m_probabilityPit* 100 / legitModels.size();
                possiblePits.set(i, tmp);
                m_pitProb[tmp.m_X][tmp.m_Y] = tmp.m_probabilityPit;
            }
        }

        // Time to check for wumpus! (works alot like the pit check...)
        if(possibleWumpus.size() > 0)
        {
            // Create a list where we will store all positions that the wumpus could reside in according to the game rules.
            ArrayList<Coordinate> legitWump = new ArrayList<Coordinate>();

            // for each possible wumpus position, create a new world, check that this world is following the rules of the game
            // and if it does, add it to the list of valid wumpus locations.
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
            }

            // Unneccessary really, but it saves some calculations if this method was to run more than once.
            possibleWumpus = legitWump;

            // for each of the now validated positions of the wumpus, set a chance of the wumpus existing there to 100 divided by the
            // number of positions.
            // This is different from the pits since there can only be one wumpus.
            for(int i = 0; i < possibleWumpus.size(); i++)
            {
                Coordinate tmp = possibleWumpus.get(i);

                tmp.m_probabilityWump = 100 / possibleWumpus.size();
                possibleWumpus.set(i,tmp);
                m_wumpProb[tmp.m_X][tmp.m_Y] = tmp.m_probabilityWump;
            }
        }
        
        // Now check all of the rooms that the AI could move to
        // if it hasnt been evaluated there couldn't
        // possibly be any danger there, so set their chances of containing wumpus and/or pits to 0.
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
        // this method runs on the basis that the m_knownData variable has been filled with perceptionsfrom the wumpusworld map.
        ArrayList<Coordinate> possiblePits = new ArrayList<Coordinate>();

        // Add all possible locations for pits, that is all around a breeze in unchecked rooms.
        // So check the world for breezes.
        for(int i = 0; i < m_knownData.length; i++)
        {
            for(int j = 0; j < m_knownData[i].length; j++)
            {
                if(m_knownData[i][j].contains(World.BREEZE))
                {
                    // add the room to the of right the breeze as a potential pit if it is unknown.
                    if(i+1 < m_knownData.length && m_knownData[i+1][j].contains(World.UNKNOWN))
                    {
                        Coordinate tmp = new Coordinate(i+1, j);
                        if(!possiblePits.contains(tmp))
                        {
                            possiblePits.add(tmp);
                        }
                    }
                    // add the room to the left of the breeze as a potential pit if it is unknown.
                    if(i - 1 >= 0 && m_knownData[i-1][j].contains(World.UNKNOWN))
                    {
                        Coordinate tmp = new Coordinate(i-1, j);

                        if(!possiblePits.contains(tmp))
                        {
                            possiblePits.add(tmp);
                        }
                    }

                    // add the room above the breeze as a potential pit if it is unknown.
                    if( j + 1 < m_knownData[i].length && m_knownData[i][j + 1].contains(World.UNKNOWN))
                    {
                        Coordinate tmp = new Coordinate(i, j + 1);
                        if(!possiblePits.contains(tmp))
                        {
                            possiblePits.add(tmp);
                        }
                    }

                    // add the room below the breeze as a potential pit if it is unknown.
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
        //return the list containing possible pits.
        return possiblePits;
    }

    /**
     * A function that gives out all possible positions for the wumpus.
     * No logic involved, any unkown area in the known data could contain a wumpus.
     * @return an ArrayList<Coordinate> of all possible locations for a Wumpus.
     */
    private ArrayList<Coordinate> addPossibleWumpus()
    {
        // this method runs on the basis that m_knownData has been filled with perceptions of the wumpusWorld.
        ArrayList<Coordinate> possibleWumpus = new ArrayList<Coordinate>();

        // a wumpus could be anywhere around a stench, so add it to the list if the room adjacent to the stench is unknown.
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
        // Loop through the two dimensional world...
        for(int i = 0; i < world.length; i++)
        {
            for(int j = 0; j < world[i].length; j++)
            {
                // if the world contains a breeze there needs to be atleast one pit next to it
                // so, if there aint no pit there that world is rejected!
                if(world[i][j].contains(World.BREEZE))
                {
                    boolean res = false;

                    // check right
                    if(i + 1 < world.length && world[i + 1][j].contains(World.PIT))
                    {
                        res = true;
                    }

                    // check left
                    if( i - 1 >= 0 && world[i - 1][j].contains(World.PIT))
                    {
                        res = true;
                    }
                    // check up
                    if( j + 1 < world[i].length && world[i][j + 1].contains(World.PIT))
                    {
                        res = true;
                    }

                    // check down
                    if( j - 1 >= 0  && world[i][j - 1].contains(World.PIT))
                    {
                        res = true;
                    }

                    if(!res)
                    {
                        return false;
                    }
                }
                // if there is a pit anywhere in the world, there must be breezes around that pit on all discovered rooms
                // if there is a breeze missing in a room where the AI has visited that is adjacent to a possible pit, this world is rejected!
                if(world[i][j].contains(World.PIT))
                {
                    if(i + 1 < world.length && !(world[i + 1][j].contains(World.BREEZE) || world[i + 1][j].contains(World.UNKNOWN)))
                    {
                        return false;
                    }
                    
                    if( i - 1 >= 0 && !(world[i - 1][j].contains(World.BREEZE) || world[i - 1][j].contains(World.UNKNOWN)))
                    {
                        return false;
                    }
                    
                    if( j + 1 < world[i].length && !(world[i][j + 1].contains(World.BREEZE) || world[i][j + 1].contains(World.UNKNOWN)))
                    {
                        return false;
                    }
                    
                    if( j - 1 >= 0  && !(world[i][j - 1].contains(World.BREEZE) || world[i][j - 1].contains(World.UNKNOWN)))
                    {
                        return false;
                    }
                }
            }
        }
        // all tests passed, the world is valid.
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
        // loop through the two dimensional world...
        for(int i = 0; i < world.length; i++)
        {
            for(int j = 0; j < world[i].length; j++)
            {
                // if the world has a stench, there must be a wumpus!
                // look for a wumpus around the stench, if one isn't found or if more than one is found reject the world!
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
                    // if there was one and only one wumpus around the stench, check that there is a stench in all visited
                    // rooms around the wumpus.
                    else
                    {
                        boolean unknownUp, unknownRight, unknownDown, unknownLeft;
                        unknownUp = unknownRight = unknownDown = unknownLeft = true;
                        if (wumpY + 1 < world.length)
                        {
                            unknownUp = world[wumpX][wumpY + 1].contains(World.UNKNOWN) || world[wumpX][wumpY + 1].contains(World.STENCH);
                        }
                        if (wumpX + 1 < world.length)
                        {
                            unknownRight = world[wumpX + 1][wumpY].contains(World.UNKNOWN) || world[wumpX + 1][wumpY].contains(World.STENCH);
                        }
                        if (wumpY - 1 >= 0)
                        {
                            unknownDown = world[wumpX][wumpY - 1].contains(World.UNKNOWN) || world[wumpX][wumpY - 1].contains(World.STENCH);
                        }
                        if (wumpX - 1 >= 0)
                        {
                            unknownLeft = world[wumpX - 1][wumpY].contains(World.UNKNOWN) || world[wumpX - 1][wumpY].contains(World.STENCH);
                        }

                        if(!unknownUp || !unknownRight || !unknownDown || !unknownLeft)
                        {
                            return false;
                        }
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
        // create a list variable that will contain all possible worlds
        ArrayList<String[][]> models = new ArrayList<String[][]>();

        // initiate a new world containing all data from m_knowndata, deepcopy as not to compromise the member variable.
        String[][] toSend = new String[m_knownData.length][m_knownData.length];

        for(int i = 0; i < m_knownData.length; i++)
        {
            for(int j = 0; j < m_knownData[i].length; j++)
            {
                toSend[i][j] = m_knownData[i][j];
            }
        }

        // add this map to the models, it will be used in the following recursive function.
        models.add(toSend);

        // run a recursive funtion whihc generates all possible maps. saves theese maps to the list.
        recurPossibleWorlds(models, possiblePits, 0, 0);

        return models;
    }

    /**
     * helping recursive method to possibleWorlds. 
     * @param models
     * @param possiblePits
     * @param depth
     * @param model
     */
    private void recurPossibleWorlds(ArrayList<String[][]> models, ArrayList<Coordinate> possiblePits, int depth, int model)
    {
        // the pit can either exist or not, so like a binary tree this method chooses to give the last added map in the list a pit
        // and then generate a new map where this pit does not exist.

        // the current pit that we are looking at.
        Coordinate tmp = possiblePits.get(depth);

        // Create a new map and deepcopy the information from the last model in the tree to this variable.
        String [][] nextModel = new String[m_knownData.length][m_knownData.length];
        
        // tmpModPitYes will always add pits to it's map.
        String [][] tmpModPitYes = models.get(model);
        
        for(int i = 0; i < nextModel.length; i++)
        {
            for(int j = 0; j < nextModel[i].length; j++)
            {
                nextModel[i][j] = tmpModPitYes[i][j];
            }
        }
        // add a pit to the first map, and no pit to the other model
        tmpModPitYes[tmp.m_X][tmp.m_Y] += World.PIT;
        // make the spot unknown so that calculations of validity won't throw valid maps.
        nextModel[tmp.m_X][tmp.m_Y] = World.UNKNOWN;

        models.set(model, tmpModPitYes);
        models.add(nextModel);

        // if the depth hasnt been reached, keep digging!
        if(depth < possiblePits.size() - 1)
        {
            recurPossibleWorlds(models, possiblePits, depth + 1, model);
            recurPossibleWorlds(models, possiblePits, depth + 1, model + 1);
        }
    }

    /**
     * Gets the first and safest room in the list. Calculate must be used before this method.
     * @param rooms list of rooms that are to be evaluated
     * @param hasArrow boolean showing if the AI has an arrow or not, if it has an arrow, it will be able to enter rooms where the wumpus might reside.
     * @return coordinates of the safest room
     */
    public Coordinate getSafestCoordinates(List<MyPRoom> rooms, boolean hasArrow)
    {
        // Initiate variables that are to be used.
        int bestWump = 100;
        int bestPit = 100;
        int startPoint = 0;
        Coordinate toReturn = null;
        MyPRoom tmp = rooms.get(startPoint++);

        // Add temporary values to the variable toReturn. Values such as the probability of a pit and/or wumpus...
        toReturn = new Coordinate(tmp.getX(), tmp.getY());
        bestWump = m_wumpProb[tmp.getX() - 1][tmp.getY() - 1];
        bestPit = m_pitProb[tmp.getX() - 1][tmp.getY() - 1];
        toReturn.m_probabilityWump = bestWump;
        toReturn.m_probabilityPit = bestPit;

        // first if check, if the AI has a choice between atleast two rooms and the first room was a wumpus AND the AI already fired their arrow, then avoid
        // the wumpus at all cost...
        if(toReturn.m_probabilityWump == 100 && !hasArrow && rooms.size() > 1)
        {
            tmp = rooms.get(startPoint++);
            toReturn = new Coordinate(tmp.getX(), tmp.getY());
            bestWump = m_wumpProb[tmp.getX() - 1][tmp.getY() - 1];
            bestPit = m_pitProb[tmp.getX() - 1][tmp.getY() - 1];
            toReturn.m_probabilityWump = bestWump;
            toReturn.m_probabilityPit = bestPit;
        }

        // Loop and compare all rooms.
        for(int i = startPoint; i < rooms.size(); i++)
        {
            tmp = rooms.get(i);

            // If the wumpus' position is known, and there is no pit there, then choose the wumpus room as the target.
            // (Assuming that the player has an arrow that is...)
            // These if-else-if statements are a bit of a botch job. They accomplish what they are supposed to,
            // but should really be cleaned up
            if (m_wumpProb[tmp.getX() - 1][tmp.getY() - 1] == 100 && m_pitProb[tmp.getX() - 1][tmp.getY() - 1] == 0 && toReturn.m_probabilityPit != 0)
            {
                if (!hasArrow && rooms.size() > 1)
                {
                    continue;
                }
                toReturn = new Coordinate(tmp.getX(), tmp.getY());
                bestWump = m_wumpProb[tmp.getX() - 1][tmp.getY() - 1];
                bestPit = m_pitProb[tmp.getX() - 1][tmp.getY() - 1];
                toReturn.m_probabilityWump = bestWump;
                toReturn.m_probabilityPit = bestPit;
            }
            else if(((m_wumpProb[tmp.getX() - 1][tmp.getY() - 1] < bestWump && (m_pitProb[tmp.getX() - 1][tmp.getY() - 1] != 100 && bestPit < 100))
                && (toReturn.m_probabilityWump != 100 || (m_wumpProb[tmp.getX() - 1][tmp.getY() - 1] == 0 && m_pitProb[tmp.getX() - 1][tmp.getY() - 1] == 0)))
                || (((m_pitProb[tmp.getX() - 1][tmp.getY() - 1] < bestPit || (bestPit == 100 && toReturn.m_probabilityWump == 100)) && m_wumpProb[tmp.getX() - 1][tmp.getY() - 1] <= bestWump)
                && (toReturn.m_probabilityWump != 100 || (m_wumpProb[tmp.getX() - 1][tmp.getY() - 1] == 0 && m_pitProb[tmp.getX() - 1][tmp.getY() - 1] == 0) || (bestPit == 100 && toReturn.m_probabilityWump == 100))))
            {
                toReturn = new Coordinate(tmp.getX(), tmp.getY());
                bestWump = m_wumpProb[tmp.getX() - 1][tmp.getY() - 1];
                bestPit = m_pitProb[tmp.getX() - 1][tmp.getY() - 1];
                toReturn.m_probabilityWump = bestWump;
                toReturn.m_probabilityPit = bestPit;
                bestWump = (bestWump == 0 && bestPit == 100) ? 100 : bestWump;
            }
        }
        return toReturn;
    }
}