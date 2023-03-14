package tetris;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
public class Tetris {
    public static final int FRAME_WIDTH = 305;
    public static final int FRAME_HEIGHT = 525;

    public static void main(String[] args)
    {
        EventQueue.invokeLater(new Runnable()
        {
            public void run()
            {
                GameWindow gameWindow = new GameWindow();
                gameWindow.setTitle("Tetris");
                gameWindow.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                gameWindow.setLocationByPlatform(true);
                gameWindow.setSize(FRAME_WIDTH, FRAME_HEIGHT);
                gameWindow.setResizable(false);
                gameWindow.setVisible(true);
                gameWindow.startGame();
            }
        });
    }
}

class GameWindow extends JFrame
{
    public static boolean start  = true;
    private static final long serialVersionUID = 1L;
    //block size
    public static final int BLOCK_SIZE = 20;
    //Horizon size
    public static final int HORIZON_NODES = Tetris.FRAME_WIDTH / BLOCK_SIZE;
    //Vertical size
    public static final int VERTICAL_NODES = Tetris.FRAME_HEIGHT / BLOCK_SIZE;
    //stauts of block, 0 means empty, 1 means moving, 2 means placed 
    public static int[][] space = new int[VERTICAL_NODES][HORIZON_NODES];
    //canvas
    private Canvas canvas;
    //timer
    private Timer timer;
    //score for game 
    private int score;
    //usually 7 shape and add one special dot shape, using array, and using (0,0) to be the center pointer. 
    private final int[][][] shapes = {
            {{-1,0},{0,0},{-1,1},{0,1}},// square shape
            {{-1,0},{0,0},{1,0}, {2,0}},  // line shape
            {{-1,0},{0,0},{0,1}, {1,1}},  // Z shape
            {{-1,1},{0,1},{0,0}, {1,0}}, // reverse Z shape
            {{-1,1},{-1,0},{0,0},{1,0}},// L shape
            {{-1,0},{0,0},{1,0}, {1,1}}, // reverse L shape
            {{-1,0},{0,0},{1,0}, {0,1}},// T shape
            {{0,0},{0,0},{0,0},{0,0}} // single dot
    };
    
    //current point position
    private Point centerPos = new Point();
   
    //current shape
    private int[][] currentShape = new int[4][2];

    public GameWindow()
    {
        canvas = new Canvas();
        addKeyListener(new KeyHandler());
        add(canvas);
        pack();
    }
    //start game, give the menu when first start game 
    public void startGame()
    {

        choseShape();
        timer = new Timer(300, new TimerHandler());
        if(start){
            JOptionPane.showMessageDialog(GameWindow.this,
                    String.format("KEY MENU \n\nUp Key: transform\nDown Key: quicker drop\nLeft Key: move left \nRight key: move right\n" +
                            "Space Key: Stop/Continue"));
            start = false;
        }

        timer.start();
    }
    //random choose shape
    private void choseShape()
    {
        int index = (int) Math.round(Math.random() * 7);
        for (int i = 0; i < 4; i++)
        {
            for (int j = 0; j < 2; j++)
            {
                currentShape[i][j] = shapes[index][i][j];
            }
        }
        centerPos.x = HORIZON_NODES / 2;
        centerPos.y = 0;
        updateSpace(1);
    }
    //shape move down
    private boolean moveDown()
    {
        for (int i = 0; i < 4; i++)
        {
            int x = centerPos.x + currentShape[i][0];
            int y = centerPos.y + currentShape[i][1] + 1;
            if (y >= VERTICAL_NODES-1 || space[y][x] == 2)
                return false;
        }
        updateSpace(0);
        centerPos.y++;
        updateSpace(1);
        return true;
    }
    //tansform the shape
    private void transform()
    {
        int[][] temp = new int[4][2];
        for (int i = 0; i < 4; i++)
        {
            for (int j = 0; j < 2; j++)
            {
                temp[i][j] = currentShape[i][j];
            }
        }
        for (int i = 0; i < 4; i++)
        {
            int t = temp[i][1];
            temp[i][1] = temp[i][0];
            temp[i][0] = -1 * t;
        }
        for (int i = 0; i < 4; i++)
        {
            int x = centerPos.x + temp[i][0];
            int y = centerPos.y + temp[i][1];
            if (x < 0 || x >= HORIZON_NODES || y < 0 || y >= VERTICAL_NODES || space[y][x] == 2)
                return;
        }
        updateSpace(0);
        currentShape = temp;
        updateSpace(1);
    }
    //move to left 
    private void moveLeft()
    {
        for (int i = 0; i < 4; i++)
        {
            int x = centerPos.x + currentShape[i][0] - 1;
            int y = centerPos.y + currentShape[i][1];
            if (x < 0 || space[y][x] == 2)
                return;
        }
        updateSpace(0);
        centerPos.x--;
        updateSpace(1);
    }
    //move to right 
    private void moveRight()
    {
        for (int i = 0; i < 4; i++)
        {
            int x = centerPos.x + currentShape[i][0] + 1;
            int y = centerPos.y + currentShape[i][1];
            if (x >= HORIZON_NODES || space[y][x] == 2)
                return;
        }
        updateSpace(0);
        centerPos.x++;
        updateSpace(1);
    }
    //settle down 
    private void fixBox()
    {
        updateSpace(2);
    }
    //remove the line if is all filled 
    private void clearLine()
    {
        int y = centerPos.y + currentShape[0][1];
        int minY = y, maxY = y;
        for (int i = 1; i < 4; i++)
        {
            y = centerPos.y + currentShape[i][1];
            //if (y > maxY) maxY = y;
            // else if (y < minY) minY = y;
            maxY = y>maxY ? y: maxY;
            minY = y < minY ? y:minY;
        }

        for(y = minY; y <= maxY; y++)
        {
            int x;
            for (x = 0; x < HORIZON_NODES; x++)
            {
                if (space[y][x] == 0) break;
            }
            if (x == HORIZON_NODES)
            {
                for (int i = 0; i < HORIZON_NODES; i++)
                {
                    for (int j = y; j > 0; j--)
                    {
                        space[j][i] = space[j-1][i];
                    }
                }
                score ++;
            }
        }
    }
    private void updateSpace(int flag)
    {
        for (int i = 0; i < 4; i++)
        {
            int x = centerPos.x + currentShape[i][0];
            int y = centerPos.y + currentShape[i][1];
            space[y][x] = flag;
        }
    }
    //check if is out of the bound: game over
    private boolean gameOver()
    {
        for (int i = 0; i < 4; i++)
        {
            int y = centerPos.y + currentShape[i][1];
            if (y == 0) return true;
        }
        return false;
    }
    //timer to check every action 
    class TimerHandler implements ActionListener
    {
        public void actionPerformed(ActionEvent e)
        {
            if (!moveDown())
            {
                fixBox(); // check if need to fix 
                clearLine(); // check if need to remove the line 
                timer.stop();  // stop timer 
                if (gameOver())
                    JOptionPane.showMessageDialog(GameWindow.this, String.format("GameOver，Your got points: %d", score));
                else
                    startGame();
            }
            canvas.repaint(); update the change in canvas 
        }
    }
    //key function 
    private class KeyHandler extends KeyAdapter
    {
        public void keyPressed(KeyEvent e)
        {
            super.keyPressed(e);
            switch (e.getKeyCode())
            {
                case KeyEvent.VK_UP:  // transfrom sheap
                    transform();
                    canvas.repaint();
                    break;
                case KeyEvent.VK_DOWN: // quicker drop 
                    timer.setDelay(30);
                    break;
                case KeyEvent.VK_LEFT: // move left 
                    moveLeft();
                    canvas.repaint();
                    break;
                case KeyEvent.VK_RIGHT: // move right 
                    moveRight();
                    canvas.repaint();
                    break;
                case KeyEvent.VK_SPACE:  // stop || continue
                    if(timer.isRunning()){
                        timer.stop();
                        stopMessage();
                    }else{
                        timer.start();
                    }
                    canvas.repaint();
                    break;

            }
        }
    }
    public void stopMessage(){
        JOptionPane.showMessageDialog(GameWindow.this, String.format("Game Stop!\nType Space key to Continues! \nCurrent points: %d", score ));
    }
}

class Canvas extends JComponent
{
    private static final long serialVersionUID = 1L;
    protected void paintComponent(Graphics g)
    {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        for (int i = 0; i < GameWindow.VERTICAL_NODES; i ++)
        {
            for (int j = 0; j < GameWindow.HORIZON_NODES; j ++)
            {
                if (GameWindow.space[i][j] != 0)
                    g2.fillRect(j * 20, i * 20, 19, 19);// use 19 to make each block has thin line between 
            }
        }
    }
}
