package wumpusworld;

import java.util.ArrayList;

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
        public int m_probability;
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

    public void calculate()
    {
        // Find all positions where a pit COULD be found.
        ArrayList<Coordinate> possiblePits = addPossiblePits();

        ArrayList<Coordinate> possibleWumpus = addPossibleWumpus();

        // Generate models for all possible combinations of pits...
        if(possiblePits.size() > 0)
        {
            System.out.println("Found possible pits!");
            ArrayList<String[][]> models = possibleWorlds(possiblePits);
            System.out.println("Generated models");
            // Throw out any model that disobeys the rules (a model will be removed if there is a breeze without a pit).
            ArrayList<String[][]> legitModels = new ArrayList<String[][]>();
            for(int i = 0; i < models.size(); i++)
            {
                if(legitWorldPit(models.get(i)))
                {
                    legitModels.add(models.get(i));
                }
                else
                {
                    System.out.println("Threw model nr " + i + "!");
                }
            }

            System.out.println("Threw out false models");

            //NOW we can calculate...
            for(int i = 0; i < legitModels.size(); i++)
            {
                String[][]tmp = legitModels.get(i);

                for(int j = 0; j < possiblePits.size(); j++)
                {
                    Coordinate coord = possiblePits.get(j);
                    coord.m_probability += tmp[coord.m_X][coord.m_Y].contains(World.PIT) ? 1:0;

                    possiblePits.set(j, coord);
                }
            }

            System.out.println("Gave pits their occurrence score...");

            for(int i = 0; i < possiblePits.size(); i++)
            {
                Coordinate tmp = possiblePits.get(i);
                tmp.m_probability =  tmp.m_probability* 100 / legitModels.size();
                possiblePits.set(i, tmp);
                m_pitProb[tmp.m_X][tmp.m_Y] = tmp.m_probability;
                //System.out.println("There is a chance of " + tmp.m_probability + "% that there is a pit in location (" + (tmp.m_X + 1) + ", " + (tmp.m_Y + 1) + ")\n");
            }
        }

        if(possibleWumpus.size() > 0)
        {
            ArrayList<Coordinate> legitWump = new ArrayList<Coordinate>();
            for(int i = 0; i < possibleWumpus.size(); i++)
            {
                String[][] tmp = m_knownData;
                Coordinate tmpCoord = possibleWumpus.get(i);
                tmp[tmpCoord.m_X][tmpCoord.m_Y] += World.WUMPUS;

                if(legitWorldWump(tmp))
                {
                    legitWump.add(tmpCoord);
                }
            }
            possibleWumpus = legitWump;

            for(int i = 0; i < possibleWumpus.size(); i++)
            {
                Coordinate tmp = possibleWumpus.get(i);

                tmp.m_probability = 100 / possibleWumpus.size();
                possibleWumpus.set(i,tmp);
                m_wumpProb[tmp.m_X][tmp.m_Y] = tmp.m_probability;

                //System.out.println("There is a chance of " + tmp.m_probability + "% that there is a wumpus in location (" + (tmp.m_X + 1) + ", " + (tmp.m_Y + 1) + ")\n");
            }
        }
    }

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

    private boolean legitWorldWump(String[][] world)
    {
        for(int i = 0; i < world.length; i++)
        {
            for(int j = 0; j < world[i].length; j++)
            {
                if(world[i][j].contains(World.STENCH))
                {
                    boolean res = false;
                    if(i + 1 < world.length && world[i + 1][j].contains(World.WUMPUS))
                    {             
                        res = true;
                    }

                    if( i - 1 > 0 && world[i - 1][j].contains(World.WUMPUS))
                    {
                        // There may only be one wumpus!
                        if(res)
                        {
                            return false;
                        }
                        res = true;
                    }
                
                    if( j + 1 < world[i].length && world[i][j + 1].contains(World.WUMPUS))
                    {
                        // There may only be one wumpus!
                        if(res)
                        {
                            return false;
                        }
                        res = true;
                    }

                    if( j - 1 > 0  && world[i][j - 1].contains(World.WUMPUS))
                    {
                        // There may only be one wumpus!
                        if(res)
                        {
                            return false;
                        }
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

    private ArrayList<String[][]> possibleWorlds(ArrayList<Coordinate> possiblePits)
    {
        ArrayList<String[][]> models;
        models = new ArrayList<String[][]>();
        models.add(m_knownData);

        for(int i = 0; i < m_knownData.length; i++)
        {
            for(int j = 0; j < m_knownData[i].length; j++)
            {
                if(m_knownData[j][m_knownData.length - 1 - i] == "")
                    System.out.print(" ");
                System.out.print(m_knownData[j][m_knownData.length - 1 - i] + "|");
            }
            System.out.print("\n");
        }

        System.out.println("Generated first model...");
        recurPossibleWorlds(models, possiblePits, 0, 0, true);
        for(int i = 0; i < models.size(); i++)
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
        }
        return models;
    }

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
}