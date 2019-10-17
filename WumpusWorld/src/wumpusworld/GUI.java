package wumpusworld;

import javax.swing.*;
import java.awt.event.*;
import java.awt.*;
import java.io.File;
import java.util.Vector;

/**
 * GUI for the Wumpus World. Only supports worlds of 
 * size 4.
 * 
 * @author Johan Hagelbäck
 */
public class GUI implements ActionListener
{
    private JFrame frame;
    private JPanel gamepanel;
    private JLabel score;
    private JLabel status;
    private World w;
    private Agent agent;
    private JPanel[][] blocks;
    private JComboBox mapList;
    private Vector<WorldMap> maps;
    
    private ImageIcon l_breeze;
    private ImageIcon l_stench;
    private ImageIcon l_pit;
    private ImageIcon l_glitter;
    private ImageIcon l_wumpus;
    private ImageIcon l_player_up;
    private ImageIcon l_player_down;
    private ImageIcon l_player_left;
    private ImageIcon l_player_right;
    
    // My icons
    private ImageIcon l_my_breeze;
    private ImageIcon l_my_stench;
    private ImageIcon l_my_glitter;
    private ImageIcon l_my_wumpus;
    private ImageIcon l_my_pit;
    private ImageIcon l_my_player_up;
    private ImageIcon l_my_player_down;
    private ImageIcon l_my_player_left;
    private ImageIcon l_my_player_right;
    private ImageIcon bgImage;
    private ImageIcon bgPit;
    private ImageIcon uk;
    
    /**
     * Creates and start the GUI.
     */
    public GUI()
    {
        if (!checkResources())
        {
            JOptionPane.showMessageDialog(null, "Unable to start GUI. Missing icons.", "Error", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }
        
        MapReader mr = new MapReader();
        maps = mr.readMaps();
        if (maps.size() > 0)
        {
            w = maps.get(0).generateWorld();
        }
        else
        {
            w = MapGenerator.getRandomMap((int)System.currentTimeMillis()).generateWorld();
        }
        
        l_breeze = new ImageIcon("gfx/B.png");
        l_stench = new ImageIcon("gfx/S.png");
        l_pit = new ImageIcon("gfx/P.png");
        l_glitter = new ImageIcon("gfx/G.png");
        l_wumpus = new ImageIcon("gfx/W.png");
        l_player_up = new ImageIcon("gfx/PU.png");
        l_player_down = new ImageIcon("gfx/PD.png");
        l_player_left = new ImageIcon("gfx/PL.png");
        l_player_right = new ImageIcon("gfx/PR.png");
        
        // Set my icons
        l_my_breeze = new ImageIcon("src/wumpusworld/myGFX/Breeze.gif");
        l_my_stench = new ImageIcon("src/wumpusworld/myGFX/Stench.gif");
        l_my_glitter = new ImageIcon("src/wumpusworld/myGFX/Gold.gif");
        l_my_wumpus = new ImageIcon("src/wumpusworld/myGFX/Wumpus.gif");
        l_my_pit = new ImageIcon("src/wumpusworld/myGFX/Pit.png");
        l_my_player_up = new ImageIcon("src/wumpusworld/myGFX/PlayerUp.png");
        l_my_player_down = new ImageIcon("src/wumpusworld/myGFX/PlayerDown.png");
        l_my_player_left = new ImageIcon("src/wumpusworld/myGFX/PlayerLeft.png");
        l_my_player_right = new ImageIcon("src/wumpusworld/myGFX/PlayerRight.png");
        bgImage = new ImageIcon("src/wumpusworld/myGFX/background.png");
        bgPit = new ImageIcon("src/wumpusworld/myGFX/backgroundPit.png");
        uk = new ImageIcon("src/wumpusworld/myGFX/unknown.png");

        createWindow();
    }
    
    /**
     * Checks if all resources (icons) are found.
     * 
     * @return True if all resources are found, false otherwise. 
     */
    private boolean checkResources()
    {
        try
        {
            File f;
            f = new File("gfx/B.png");
            if (!f.exists()) return false;
            f = new File("gfx/S.png");
            if (!f.exists()) return false;
            f = new File("gfx/P.png");
            if (!f.exists()) return false;
            f = new File("gfx/G.png");
            if (!f.exists()) return false;
            f = new File("gfx/W.png");
            if (!f.exists()) return false;
            f = new File("gfx/PU.png");
            if (!f.exists()) return false;
            f = new File("gfx/PD.png");
            if (!f.exists()) return false;
            f = new File("gfx/PL.png");
            if (!f.exists()) return false;
            f = new File("gfx/PR.png");
            if (!f.exists()) return false;
            
            // Check for my icons
            f = new File("src/wumpusworld/myGFX/Breeze.gif");
            if (!f.exists()) return false;
            f = new File("src/wumpusworld/myGFX/Stench.gif");
            if (!f.exists()) return false;
            f = new File("src/wumpusworld/myGFX/Gold.gif");
            if (!f.exists()) return false;
            f = new File("src/wumpusworld/myGFX/Wumpus.gif");
            if (!f.exists()) return false;
            f = new File("src/wumpusworld/myGFX/Pit.png");
            if (!f.exists()) return false;
            f = new File("src/wumpusworld/myGFX/PlayerUp.png");
            if (!f.exists()) return false;
            f = new File("src/wumpusworld/myGFX/PlayerDown.png");
            if (!f.exists()) return false;
            f = new File("src/wumpusworld/myGFX/PlayerLeft.png");
            if (!f.exists()) return false;
            f = new File("src/wumpusworld/myGFX/PlayerRight.png");
            if (!f.exists()) return false;
            f = new File("src/wumpusworld/myGFX/background.png");
            if (!f.exists()) return false;
            f = new File("src/wumpusworld/myGFX/backgroundPit.png");
            if (!f.exists()) return false;
            f = new File("src/wumpusworld/myGFX/unknown.png");
            if (!f.exists()) return false;
            f = new File("src/wumpusworld/myGFX/TurnLeft.png");
            if (!f.exists()) return false;
            f = new File("src/wumpusworld/myGFX/TurnLeftPressed.png");
            if (!f.exists()) return false;
            f = new File("src/wumpusworld/myGFX/MoveForward.png");
            if (!f.exists()) return false;
            f = new File("src/wumpusworld/myGFX/MoveForwardPressed.png");
            if (!f.exists()) return false;
            f = new File("src/wumpusworld/myGFX/TurnRight.png");
            if (!f.exists()) return false;
            f = new File("src/wumpusworld/myGFX/TurnRightPressed.png");
            if (!f.exists()) return false;
            f = new File("src/wumpusworld/myGFX/button.png");
            if (!f.exists()) return false;
            f = new File("src/wumpusworld/myGFX/buttonPressed.png");
            if (!f.exists()) return false;
            f = new File("src/wumpusworld/myGFX/buttonLarge.png");
            if (!f.exists()) return false;
            f = new File("src/wumpusworld/myGFX/buttonLargePressed.png");
            if (!f.exists()) return false;
            f = new File("src/wumpusworld/myGFX/legend.png");
            if (!f.exists()) return false;
        }
        catch (Exception ex)
        {
            return false;
        }
        return true;
    }
    
    /**
     * Creates all window components.
     */
    private void createWindow()
    {
        frame = new JFrame("Wumpus World");
        frame.setSize(820, 640);
        frame.getContentPane().setLayout(new FlowLayout());
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        // Change size to make sure buttons are visible
        frame.setSize(900, 640);
        
        frame.getContentPane().setBackground(Color.black);

        gamepanel = new JPanel();
        gamepanel.setPreferredSize(new Dimension(600,600));
        gamepanel.setBackground(Color.GRAY);
        gamepanel.setLayout(new GridLayout(4,4));
        
        //Add blocks
        blocks = new JPanel[4][4];
        for (int j = 3; j >= 0; j--)
        {
            for (int i = 0; i < 4; i++)
            {
                blocks[i][j] = new JPanel();
                blocks[i][j].setBackground(Color.white);
                blocks[i][j].setPreferredSize(new Dimension(150,150));
                blocks[i][j].setBorder(BorderFactory.createLineBorder(Color.black));
                blocks[i][j].setLayout(new GridLayout(2,2));

                /*Different layout to incorporate my icons,
                BorderLayout without specifying location
                of components will put the m all in center
                and only the last one added (my panel) will
                be shown*/
                blocks[i][j].setLayout(new BorderLayout());
                blocks[i][j].setBorder(BorderFactory.createEmptyBorder());

                gamepanel.add(blocks[i][j]);
            }
        }
        frame.getContentPane().add(gamepanel);
        
        //Add buttons panel
        JPanel buttons = new JPanel();
        buttons.setPreferredSize(new Dimension(200,600));
        buttons.setLayout(new FlowLayout());
        // Change colour because white is boring
        buttons.setBackground(Color.darkGray);
        //Status label
        status = new JLabel("", SwingConstants.CENTER);
        status.setPreferredSize(new Dimension(200,25));
        // Change foreground colour for legibility
        status.setForeground(new Color(250, 234, 140));
        buttons.add(status);
        //Score label
        score = new JLabel("Score: 0", SwingConstants.CENTER);
        score.setPreferredSize(new Dimension(200,25));
        // Change foreground colour for legibility
        score.setForeground(new Color(250, 234, 140));
        buttons.add(score);
        //Buttons
        JButton bl = new JButton(new ImageIcon("gfx/TL.png"));
        bl.setActionCommand("TL");
        bl.addActionListener(this);
        // Set my icon
        bl.setIcon(new ImageIcon("src/wumpusworld/myGFX/TurnLeft.png"));
        bl.setPressedIcon(new ImageIcon("src/wumpusworld/myGFX/TurnLeftPressed.png"));
        bl.setBorder(BorderFactory.createEmptyBorder());
        buttons.add(bl);
        JButton bf = new JButton(new ImageIcon("gfx/MF.png"));
        bf.setActionCommand("MF");
        bf.addActionListener(this);
        // Set my icon
        bf.setIcon(new ImageIcon("src/wumpusworld/myGFX/MoveForward.png"));
        bf.setPressedIcon(new ImageIcon("src/wumpusworld/myGFX/MoveForwardPressed.png"));
        bf.setBorder(BorderFactory.createEmptyBorder());
        buttons.add(bf);
        JButton br = new JButton(new ImageIcon("gfx/TR.png"));
        br.setActionCommand("TR");
        br.addActionListener(this);
        // Set my icon
        br.setIcon(new ImageIcon("src/wumpusworld/myGFX/TurnRight.png"));
        br.setPressedIcon(new ImageIcon("src/wumpusworld/myGFX/TurnRightPressed.png"));
        br.setBorder(BorderFactory.createEmptyBorder());
        buttons.add(br);
        JButton bg = new JButton("Grab");
        bg.setPreferredSize(new Dimension(45,22));
        bg.setActionCommand("GRAB");
        bg.addActionListener(this);

        // Set button image because why not
        bg.setPreferredSize(null);
        bg.setIcon(new ImageIcon("src/wumpusworld/myGFX/button.png"));
        bg.setPressedIcon(new ImageIcon("src/wumpusworld/myGFX/buttonPressed.png"));
        bg.setHorizontalTextPosition(JButton.CENTER);
        bg.setVerticalTextPosition(JButton.CENTER);
        bg.setForeground(new Color(250, 234, 140));
        bg.setBorder(BorderFactory.createEmptyBorder());
        buttons.add(bg);
        JButton bc = new JButton("Climb");
        bc.setPreferredSize(new Dimension(55,22));
        bc.setActionCommand("CLIMB");
        bc.addActionListener(this);
        // Set button image because why not
        bc.setPreferredSize(null);
        bc.setIcon(new ImageIcon("src/wumpusworld/myGFX/button.png"));
        bc.setPressedIcon(new ImageIcon("src/wumpusworld/myGFX/buttonPressed.png"));
        bc.setHorizontalTextPosition(JButton.CENTER);
        bc.setVerticalTextPosition(JButton.CENTER);
        bc.setForeground(new Color(250, 234, 140));
        bc.setBorder(BorderFactory.createEmptyBorder());
        buttons.add(bc);
        JButton bs = new JButton("Shoot");
        bs.setPreferredSize(new Dimension(65,22));
        bs.setActionCommand("SHOOT");
        bs.addActionListener(this);
        // Set button image because why not
        bs.setPreferredSize(null);
        bs.setIcon(new ImageIcon("src/wumpusworld/myGFX/button.png"));
        bs.setPressedIcon(new ImageIcon("src/wumpusworld/myGFX/buttonPressed.png"));
        bs.setHorizontalTextPosition(JButton.CENTER);
        bs.setVerticalTextPosition(JButton.CENTER);
        bs.setForeground(new Color(250, 234, 140));
        bs.setBorder(BorderFactory.createEmptyBorder());
        buttons.add(bs);
        JButton ba = new JButton("Run Solving Agent");
        // Change name of Solving Agent just because
        ba.setText("Run Gunnar");
        ba.setActionCommand("AGENT");
        ba.addActionListener(this);
        // Set button image because why not
        ba.setIcon(new ImageIcon("src/wumpusworld/myGFX/buttonLarge.png"));
        ba.setPressedIcon(new ImageIcon("src/wumpusworld/myGFX/buttonLargePressed.png"));
        ba.setHorizontalTextPosition(JButton.CENTER);
        ba.setVerticalTextPosition(JButton.CENTER);
        ba.setForeground(new Color(250, 234, 140));
        ba.setBorder(BorderFactory.createEmptyBorder());
        buttons.add(ba);
        //Add a delimiter
        JLabel l = new JLabel("");
        l.setPreferredSize(new Dimension(200,25));
        buttons.add(l);
        //Fill dropdown list
        Vector<String> items = new Vector<String>();
        for (int i = 0; i < maps.size(); i++)
        {
            items.add((i+1) + "");
        }
        items.add("Random");
        mapList = new JComboBox(items);
        mapList.setPreferredSize(new Dimension(180,25));
        buttons.add(mapList);
        JButton bn = new JButton("New Game");
        bn.setActionCommand("NEW");
        bn.addActionListener(this);
        // Set button image because why not
        bn.setIcon(new ImageIcon("src/wumpusworld/myGFX/buttonLarge.png"));
        bn.setPressedIcon(new ImageIcon("src/wumpusworld/myGFX/buttonLargePressed.png"));
        bn.setHorizontalTextPosition(JButton.CENTER);
        bn.setVerticalTextPosition(JButton.CENTER);
        bn.setForeground(new Color(250, 234, 140));
        bn.setBorder(BorderFactory.createEmptyBorder());
        buttons.add(bn);
        
        // Setting up my legend
        JLabel legend = new JLabel();
        legend.setIcon(new ImageIcon("src/wumpusworld/MyGFX/legend.png"));
        legend.setPreferredSize(new Dimension(200, 350));
        legend.setLayout(new GridLayout(5, 5));
        JLabel lgContent;
        JLabel lgText;
        lgContent = new JLabel(l_my_breeze);
        legend.add(lgContent);
        lgText = new JLabel("Breeze");
        lgText.setForeground(new Color(250, 234, 140));
        legend.add(lgText);
        lgContent = new JLabel(l_my_stench);
        legend.add(lgContent);
        lgText = new JLabel("Stench");
        lgText.setForeground(new Color(250, 234, 140));
        legend.add(lgText);
        lgContent = new JLabel(l_my_pit);
        legend.add(lgContent);
        lgText = new JLabel("Pit");
        lgText.setForeground(new Color(250, 234, 140));
        legend.add(lgText);
        lgContent = new JLabel(l_my_wumpus);
        legend.add(lgContent);
        lgText = new JLabel("Wumpus");
        lgText.setForeground(new Color(250, 234, 140));
        legend.add(lgText);
        lgContent = new JLabel(l_my_glitter);
        legend.add(lgContent);
        lgText = new JLabel("Gold");
        lgText.setForeground(new Color(250, 234, 140));
        legend.add(lgText);
        
        buttons.add(legend);
        
        frame.getContentPane().add(buttons);
        
        updateGame();
        
        //Show window
        frame.setVisible(true);
    }
    
    /**
     * Button commands.
     * 
     * @param e Button event.
     */
    public void actionPerformed(ActionEvent e)
    {
        if (e.getActionCommand().equals("TL"))
        {
            w.doAction(World.A_TURN_LEFT);
            updateGame();
        }
        if (e.getActionCommand().equals("TR"))
        {
            w.doAction(World.A_TURN_RIGHT);
            updateGame();
        }
        if (e.getActionCommand().equals("MF"))
        {
            w.doAction(World.A_MOVE);
            updateGame();
        }
        if (e.getActionCommand().equals("GRAB"))
        {
            w.doAction(World.A_GRAB);
            updateGame();
        }
        if (e.getActionCommand().equals("CLIMB"))
        {
            w.doAction(World.A_CLIMB);
            updateGame();
        }
        if (e.getActionCommand().equals("SHOOT"))
        {
            w.doAction(World.A_SHOOT);
            updateGame();
        }
        if (e.getActionCommand().equals("NEW"))
        {
            String s = (String)mapList.getSelectedItem();
            if (s.equalsIgnoreCase("Random"))
            {
                w = MapGenerator.getRandomMap((int)System.currentTimeMillis()).generateWorld();
            }
            else
            {
                int i = Integer.parseInt(s);
                i--;
                w = maps.get(i).generateWorld();
            }
            agent = new MyAgent(w);
            updateGame();
        }
        if (e.getActionCommand().equals("AGENT"))
        {
            if (agent == null)
            {
                agent = new MyAgent(w);
            }
            agent.doAction();
            updateGame();
        }
    }
    
    /**
     * Updates the game GUI to a new world state.
     */
    private void updateGame()
    {
        for (int i = 0; i < 4; i++)
        {
            for (int j = 0; j < 4; j++)
            {
                blocks[i][j].removeAll();
                blocks[i][j].setBackground(Color.WHITE);
                // Label with my background
                JLabel bgPane = new JLabel(bgImage);
                bgPane.setLayout(new GridLayout(2,2));
                if (w.hasPit(i+1, j+1))
                {
                    blocks[i][j].add(new JLabel(l_pit));
                    // Set background with pit to my label
                    bgPane.setIcon(bgPit);
                }
                if (w.hasBreeze(i+1, j+1))
                {
                    blocks[i][j].add(new JLabel(l_breeze));
                    // Add my icon to my label
                    bgPane.add(new JLabel(l_my_breeze));
                }
                if (w.hasStench(i+1, j+1))
                {
                    blocks[i][j].add(new JLabel(l_stench));
                    // Add my icon to my label
                    bgPane.add(new JLabel(l_my_stench));
                }
                if (w.hasWumpus(i+1, j+1))
                {
                    blocks[i][j].add(new JLabel(l_wumpus));
                    // Add my icon to my label
                    bgPane.add(new JLabel(l_my_wumpus));
                }
                if (w.hasGlitter(i+1, j+1))
                {
                    blocks[i][j].add(new JLabel(l_glitter));
                    // Add my icon to my label
                    bgPane.add(new JLabel(l_my_glitter));
                }
                if (w.hasPlayer(i+1, j+1))
                {
                    if (w.getDirection() == World.DIR_DOWN) blocks[i][j].add(new JLabel(l_player_down));
                    if (w.getDirection() == World.DIR_UP) blocks[i][j].add(new JLabel(l_player_up));
                    if (w.getDirection() == World.DIR_LEFT) blocks[i][j].add(new JLabel(l_player_left));
                    if (w.getDirection() == World.DIR_RIGHT) blocks[i][j].add(new JLabel(l_player_right));
                    
                    // Add my icon to my label
                    if (w.getDirection() == World.DIR_DOWN) bgPane.add(new JLabel(l_my_player_down));
                    if (w.getDirection() == World.DIR_UP) bgPane.add(new JLabel(l_my_player_up));
                    if (w.getDirection() == World.DIR_LEFT) bgPane.add(new JLabel(l_my_player_left));
                    if (w.getDirection() == World.DIR_RIGHT) bgPane.add(new JLabel(l_my_player_right));
                }
                if (w.isUnknown(i+1, j+1))
                {
                    blocks[i][j].setBackground(Color.GRAY);
                    // Set unknown background to my label
                    bgPane.setIcon(uk);
                }
                // Add my label to the block 
                blocks[i][j].add(bgPane);
                
                blocks[i][j].updateUI();
                blocks[i][j].repaint();
            }
        }
        
        score.setText("Score: " + w.getScore());
        status.setText("");
        if (w.isInPit())
        {
            status.setText("Player must climb up!");
        }
        if (w.gameOver())
        {
            status.setText("GAME OVER");
        }
        
        gamepanel.updateUI();
        gamepanel.repaint();
    }  
}
