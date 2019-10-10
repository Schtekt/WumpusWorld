package wumpusworld;

import java.util.ArrayList;

public class MyProbability
{
    private String[][] m_knownData;

    public MyProbability(int sizeOfGrid)
    {
        m_knownData = new String[sizeOfGrid][sizeOfGrid];

        for(int i = 0; i < sizeOfGrid; i++)
        {
            for(int j = 0; j < sizeOfGrid; j++)
            m_knownData[i][j] = World.UNKNOWN;
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

        // Generate models for all possible combinations of pits...
        if(possiblePits.size() > 0)
        {
            System.out.println("Found possiblepits!");
            ArrayList<String[][]> models = possibleWorlds(possiblePits);
            System.out.println("Generated models");
            // Throw out any model that disobeys the rules (a model will be removed if there is a breeze without a pit).
            ArrayList<String[][]> legitModels = new ArrayList<String[][]>();
            for(int i = 0; i < models.size(); i++)
            {
                if(legitWorld(models.get(i)))
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

            System.out.println("Gave pits their nrOfs...");

            for(int i = 0; i < possiblePits.size(); i++)
            {
                Coordinate tmp = possiblePits.get(i);
                tmp.m_probability =  tmp.m_probability* 100 / legitModels.size();
                possiblePits.set(i, tmp);

                System.out.println("There is a chance of " + tmp.m_probability + "% that there is a pit in location (" + tmp.m_X + ", " + tmp.m_Y + ")\n");
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
    private boolean legitWorld(String[][] world)
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
                
                /*
                if(world[i][j].contains(World.STENCH))
                {
                    boolean res = false;

                    if(i + 1 < world.length && world[i + 1][j].contains(World.WUMPUS))
                    {
                        res = true;
                    }

                    if( i - 1 > 0 && world[i - 1][j].contains(World.WUMPUS))
                    {
                        if(res)
                        {
                            return false;
                        }
                        res = true;
                    }

                    if( j + 1 < world[i].length && world[i][j + 1].contains(World.WUMPUS))
                    {
                        if(res)
                        {
                            return false;
                        }
                        res = true;
                    }

                    if( j - 1 > 0  && world[i][j - 1].contains(World.WUMPUS))
                    {
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
                */
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
}