package GameClasses;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.BodyType;
import org.jbox2d.dynamics.World;

import javax.swing.*;

import static javax.swing.JFrame.EXIT_ON_CLOSE;


public class Game {

    //Static Fields
    public static int SCREEN_HEIGHT = 600;
    public static int SCREEN_WIDTH = 800;
    public static Dimension FRAME_SIZE = new Dimension(SCREEN_WIDTH, SCREEN_HEIGHT);
    public static float WORLD_WIDTH = 50;//metres
    public static float WORLD_HEIGHT = (float) 0.75 * 50;
    //meters - keeps world dimensions in same aspect ratio as screen dimensions, so that circles get transformed into circles as opposed to ovals
    public static final float GRAVITY = 9.8f;
    public static float radius = .6f;
    public static boolean ALLOW_MOUSE_POINTER_TO_DRAG_BODIES_ON_SCREEN = true;
    public static boolean bombConnected = true;
    public static World world;
    public static final int DELAY = 20;
    public static final int NUM_EULER_UPDATES_PER_SCREEN_REFRESH = 10;
    public static final float DELTA_T = DELAY / 1000.0f;
    public static boolean fire = false;
    public static BasicView view;
    public static JFrame frame;
    public static Vec2 mouseCoordinates;
    public static BasicMouseListener listener = new BasicMouseListener();
    private static BasicKeyListener keyListener = new BasicKeyListener();

    public enum bombType {Explode, Implode, Sticky, Black_Hole}

    public static boolean holdingBomb = false;
    public static float blastRadius = WORLD_WIDTH / 4;

    //Instance Fields
    private bombContactListener stickListener;
    private float rollingFriction = .02f;
    private boolean playerWin = false;
    private boolean playerLose = false;
    private int bombsThrown = 0;
    private boolean bombTension = false;
    private int targetInVortex[] = {0, 0, 0, 0};
    private boolean up;
    private boolean right;
    private int currentLevel;
    private int bombCount;
    public int bombsLeft;
    private float catapultHeight = WORLD_HEIGHT / 6;
    private int blastStrength = 20000;


    public List<Shape2D> shapes;
    public List<Shape2D> shrapnel;
    public List<StraightBarrier> barriers;
    public List<Bomb> bombs;
    public List<CatapultRope> connectors;
    public List<Rectangle> objectives;
    public List<Vortex> vortexes;
    public static List<Stick> sticks;
    public List<ScreenText> messages;
    public List<Rectangle> movingBlocks;

    //Static Methods (Mostly taken from lab work) used to convert between world and screen coordinates. Needed for
    //Drawing anything to the screen
    public static int convertWorldXtoScreenX(float worldX) {
        return (int) (worldX / WORLD_WIDTH * SCREEN_WIDTH);
    }

    public static int convertWorldYtoScreenY(float worldY) {
        // minus sign in here is because screen coordinates are upside down.
        return (int) (SCREEN_HEIGHT - (worldY / WORLD_HEIGHT * SCREEN_HEIGHT));
    }

    //Converted to two methods for width and height so that non-circular shapes resize properly
    public static float convertWorldLengthXToScreenLengthX(float worldLength) {
        return (worldLength / WORLD_WIDTH * SCREEN_WIDTH);
    }

    public static float convertWorldLengthYToScreenLengthY(float worldLength) {
        return (worldLength / WORLD_HEIGHT * SCREEN_HEIGHT);
    }

    public static float convertScreenXtoWorldX(int screenX) {
        return screenX * WORLD_WIDTH / SCREEN_WIDTH;
    }

    public static float convertScreenYtoWorldY(int screenY) {
        return (SCREEN_HEIGHT - screenY) * WORLD_HEIGHT / SCREEN_HEIGHT;
    }


    public Game() {
        stickListener = new bombContactListener();
        //Initialise world, ArrayLists and barriers around world
        world = new World(new Vec2(0, -GRAVITY));
        world.setContinuousPhysics(true);
        world.setContactListener(stickListener);
        shapes = new ArrayList<Shape2D>();
        shrapnel = new ArrayList<Shape2D>();
        barriers = new ArrayList<StraightBarrier>();
        bombs = new ArrayList<Bomb>();
        connectors = new ArrayList<CatapultRope>();
        objectives = new ArrayList<Rectangle>();
        vortexes = new ArrayList<Vortex>();
        sticks = new ArrayList<Stick>();
        messages = new ArrayList<ScreenText>();
        movingBlocks = new ArrayList<Rectangle>();

        currentLevel = 0;

        barriers.add(new StraightBarrier(0, 0, WORLD_WIDTH, 0, Color.WHITE));
        barriers.add(new StraightBarrier(WORLD_WIDTH, 0, WORLD_WIDTH, WORLD_HEIGHT, Color.WHITE));
        barriers.add(new StraightBarrier(WORLD_WIDTH, WORLD_HEIGHT, 0, WORLD_HEIGHT, Color.WHITE));
        barriers.add(new StraightBarrier(0, WORLD_HEIGHT, 0, 0, Color.WHITE));
    }

    public static void main(String[] args) throws Exception {
        final Game game = new Game();
        view = new BasicView(game);
        frame = new JFrame();
        frame.getContentPane().add(view);
        frame.setVisible(true);
        frame.setSize(SCREEN_WIDTH, SCREEN_HEIGHT);
        frame.addKeyListener(keyListener);
        frame.setDefaultCloseOperation(EXIT_ON_CLOSE);
        view.addMouseMotionListener(listener);
        game.startThread(view);
    }

    private void startThread(final BasicView view) throws InterruptedException {
        final Game game = this;
        bombsThrown = 0;

        //Game introduction
        messages.add(new ScreenText(SCREEN_WIDTH / 2f, (SCREEN_HEIGHT/6f)*0.5f, "Welcome to Bomb Slingshot!", 30, Color.white));
        messages.add(new ScreenText(SCREEN_WIDTH / 2f, (SCREEN_HEIGHT/6f)*1f, "The objective is to knock the red targets onto the ground", 25,Color.white));
        messages.add(new ScreenText(SCREEN_WIDTH / 2f, (SCREEN_HEIGHT/6f)*1.5f, "Red bombs explode", 25,Color.RED));
        messages.add(new ScreenText(SCREEN_WIDTH / 2f, (SCREEN_HEIGHT/6f)*2f, "Blue bombs implode", 25,Color.BLUE));
        messages.add(new ScreenText(SCREEN_WIDTH / 2f, (SCREEN_HEIGHT/6f)*2.5f, "Green bombs explode, but also stick to the first object they hit", 25,Color.GREEN));
        messages.add(new ScreenText(SCREEN_WIDTH / 2f, (SCREEN_HEIGHT/6f)*3f, "Purple bombs create a vortex for a duration", 25,Color.MAGENTA));
        messages.add(new ScreenText(SCREEN_WIDTH / 2f, (SCREEN_HEIGHT/6f)*3.5f, "Yellow blocks can be damaged and destroyed by some bombs,", 25, Color.white));
        messages.add(new ScreenText(SCREEN_WIDTH / 2f, (SCREEN_HEIGHT/6f)*4f, "the darker their colour changes, the more damaged they are", 25, Color.white));
        messages.add(new ScreenText(SCREEN_WIDTH / 2f, (SCREEN_HEIGHT/6f)*4.5f, "Use the mouse to drag and catapult your bombs", 25, Color.white));
        messages.add(new ScreenText(SCREEN_WIDTH / 2f, (SCREEN_HEIGHT/6f)*5f, "and the spacebar to detonate them", 25, Color.white));
        messages.add(new ScreenText(SCREEN_WIDTH / 2f, (SCREEN_HEIGHT/6f)*5.5f, "You have limited bombs per Level. Press space to continue", 25, Color.white));

        view.repaint();
        while (!BasicKeyListener.isDetonateKeyPressed()){
            view.repaint();
        }
        messages.clear();

        //Main run thread
        while (true) {
            playLevel(currentLevel);
        }
    }

    private float boxHeight(float boxRadius) {
        return (float) Math.sqrt((boxRadius * boxRadius) * 2);
    }

    //Sets up the game environment, switches based on current level
    private void playLevel(int level) throws InterruptedException {
        clearLevel();
        bombTension = false;
        holdingBomb = false;
        playerLose = false;
        playerWin = false;
        bombConnected = true;
        bombsThrown = 0;
        up = true;
        right = true;

        switch (level) {
            case 0:
                //Catapult launcher
                shapes.add(new Rectangle(WORLD_WIDTH / 4, WORLD_WIDTH / 8, 0, 0, Color.WHITE, 1, 0.02f, WORLD_HEIGHT / 6, 0.2f, false));

                shapes.add(new Rectangle((float) ((WORLD_WIDTH / 8) * 5.75), WORLD_WIDTH / 32, 0, 0, Color.YELLOW, 10, rollingFriction, WORLD_WIDTH / 32, WORLD_WIDTH / 32, true));
                shapes.add(new Rectangle((float) ((WORLD_WIDTH / 8) * 6.25), WORLD_WIDTH / 32, 0, 0, Color.YELLOW, 10, rollingFriction, WORLD_WIDTH / 32, WORLD_WIDTH / 32, true));
                shapes.add(new Rectangle((float) ((WORLD_WIDTH / 8) * 6.75), WORLD_WIDTH / 32, 0, 0, Color.YELLOW, 10, rollingFriction, WORLD_WIDTH / 32, WORLD_WIDTH / 32, true));
                shapes.add(new Rectangle((float) ((WORLD_WIDTH / 8) * 7.25), WORLD_WIDTH / 32, 0, 0, Color.YELLOW, 10, rollingFriction, WORLD_WIDTH / 32, WORLD_WIDTH / 32, true));
                shapes.add(new Rectangle((float) ((WORLD_WIDTH / 8) * 7.75), WORLD_WIDTH / 32, 0, 0, Color.YELLOW, 10, rollingFriction, WORLD_WIDTH / 32, WORLD_WIDTH / 32, true));
                shapes.add(new Rectangle((float) ((WORLD_WIDTH / 8) * 6), (WORLD_WIDTH / 32) * 3, 0, 0, Color.YELLOW, 10, rollingFriction, WORLD_WIDTH / 32, WORLD_WIDTH / 32, true));
                shapes.add(new Rectangle((float) ((WORLD_WIDTH / 8) * 6.5), (WORLD_WIDTH / 32) * 3, 0, 0, Color.YELLOW, 10, rollingFriction, WORLD_WIDTH / 32, WORLD_WIDTH / 32, true));
                shapes.add(new Rectangle((float) ((WORLD_WIDTH / 8) * 7), (WORLD_WIDTH / 32) * 3, 0, 0, Color.YELLOW, 10, rollingFriction, WORLD_WIDTH / 32, WORLD_WIDTH / 32, true));
                shapes.add(new Rectangle((float) ((WORLD_WIDTH / 8) * 7.5), (WORLD_WIDTH / 32) * 3, 0, 0, Color.YELLOW, 10, rollingFriction, WORLD_WIDTH / 32, WORLD_WIDTH / 32, true));
                shapes.add(new Rectangle((float) ((WORLD_WIDTH / 8) * 6.25), (WORLD_WIDTH / 32) * 5, 0, 0, Color.YELLOW, 10, rollingFriction, WORLD_WIDTH / 32, WORLD_WIDTH / 32, true));
                shapes.add(new Rectangle((float) ((WORLD_WIDTH / 8) * 6.75), (WORLD_WIDTH / 32) * 5, 0, 0, Color.YELLOW, 10, rollingFriction, WORLD_WIDTH / 32, WORLD_WIDTH / 32, true));
                shapes.add(new Rectangle((float) ((WORLD_WIDTH / 8) * 7.25), (WORLD_WIDTH / 32) * 5, 0, 0, Color.YELLOW, 10, rollingFriction, WORLD_WIDTH / 32, WORLD_WIDTH / 32, true));
                shapes.add(new Rectangle((float) ((WORLD_WIDTH / 8) * 6.5), (WORLD_WIDTH / 32) * 7, 0, 0, Color.YELLOW, 10, rollingFriction, WORLD_WIDTH / 32, WORLD_WIDTH / 32, true));
                shapes.add(new Rectangle(((WORLD_WIDTH / 8) * 7), (WORLD_WIDTH / 32) * 7, 0, 0, Color.YELLOW, 10, rollingFriction, WORLD_WIDTH / 32, WORLD_WIDTH / 32, true));
                objectives.add(new Rectangle((float) ((WORLD_WIDTH / 8) * 6.75), (WORLD_WIDTH / 32) * 9, 0, 0, Color.RED, 10, rollingFriction, WORLD_WIDTH / 32, WORLD_WIDTH / 32, true));
                objectives.get(0).setObjective(true);
                bombs.add(new Bomb((WORLD_WIDTH / 4) - 1.6f, 1.6f + radius, 0, 0, radius, 1, rollingFriction, ThreadLocalRandom.current().nextInt(0, bombType.values().length)));
                connectors.add(new CatapultRope(shapes.get(0).body, bombs.get(0).body, 1, 200, 0, true, Color.LIGHT_GRAY, 1, catapultHeight));

                bombCount = 6;
                break;

            case 1:
                shapes.add(new Rectangle(WORLD_WIDTH / 4, WORLD_WIDTH / 8, 0, 0, Color.WHITE, 1, 0.02f, WORLD_HEIGHT / 6, 0.2f, false));

                shapes.add(new Rectangle(((WORLD_WIDTH / 8) * 4.75f), (WORLD_HEIGHT / 6) * 0.25f, 0, 0, Color.YELLOW, 10, rollingFriction, WORLD_WIDTH / 32, WORLD_WIDTH / 32, true));
                shapes.add(new Rectangle(((WORLD_WIDTH / 8) * 5.75f), (WORLD_HEIGHT / 6) * 0.25f, 0, 0, Color.YELLOW, 10, rollingFriction, WORLD_WIDTH / 32, WORLD_WIDTH / 32, true));
                shapes.add(new Rectangle(((WORLD_WIDTH / 8) * 5.75f), (WORLD_HEIGHT / 6) * 0.75f, 0, 0, Color.YELLOW, 10, rollingFriction, WORLD_WIDTH / 32, WORLD_WIDTH / 32, true));
                shapes.add(new Rectangle(((WORLD_WIDTH / 8) * 6.75f), (WORLD_HEIGHT / 6) * 0.25f, 0, 0, Color.YELLOW, 10, rollingFriction, WORLD_WIDTH / 32, WORLD_WIDTH / 32, true));
                shapes.add(new Rectangle(((WORLD_WIDTH / 8) * 6.75f), (WORLD_HEIGHT / 6) * 0.75f, 0, 0, Color.YELLOW, 10, rollingFriction, WORLD_WIDTH / 32, WORLD_WIDTH / 32, true));
                shapes.add(new Rectangle(((WORLD_WIDTH / 8) * 6.75f), (WORLD_HEIGHT / 6) * 1.25f, 0, 0, Color.YELLOW, 10, rollingFriction, WORLD_WIDTH / 32, WORLD_WIDTH / 32, true));

                objectives.add(new Rectangle((float) ((WORLD_WIDTH / 8) * 4.75), (WORLD_HEIGHT / 6) * 0.75f, 0, 0, Color.RED, 10, rollingFriction, WORLD_WIDTH / 32, WORLD_WIDTH / 32, true));
                objectives.get(0).setObjective(true);
                objectives.add(new Rectangle((float) ((WORLD_WIDTH / 8) * 5.75), (WORLD_HEIGHT / 6) * 1.25f, 0, 0, Color.RED, 10, rollingFriction, WORLD_WIDTH / 32, WORLD_WIDTH / 32, true));
                objectives.get(1).setObjective(true);
                objectives.add(new Rectangle((float) ((WORLD_WIDTH / 8) * 6.75), (WORLD_HEIGHT / 6) * 1.75f, 0, 0, Color.RED, 10, rollingFriction, WORLD_WIDTH / 32, WORLD_WIDTH / 32, true));
                objectives.get(2).setObjective(true);

                bombs.add(new Bomb((WORLD_WIDTH / 4) - 1.6f, 1.6f + radius, 0, 0, radius, 1, rollingFriction, ThreadLocalRandom.current().nextInt(0, bombType.values().length)));
                connectors.add(new CatapultRope(shapes.get(0).body, bombs.get(0).body, 1, 200, 0, true, Color.LIGHT_GRAY, 1, catapultHeight));

                bombCount = 6;
                break;

            case 2:
                shapes.add(new Rectangle(WORLD_WIDTH / 4, WORLD_WIDTH / 8, 0, 0, Color.WHITE, 1, 0.02f, WORLD_HEIGHT / 6, 0.2f, false));

                movingBlocks.add(new Rectangle((WORLD_WIDTH / 8) * 5, 3.2f, 0, 0, Color.WHITE, 1, 0.02f, 3.2f, 0.2f, false));

                shapes.add(new Rectangle((WORLD_WIDTH / 8) * 7.5f, (WORLD_HEIGHT / 6) * 4, 0, 0, Color.WHITE, 1, 0.02f, 0.2f, WORLD_WIDTH / 16, false));

                objectives.add(new Rectangle(((WORLD_WIDTH / 8) * 7.5f), (WORLD_HEIGHT / 6) * 4.25f, 0, 0, Color.RED, 10, rollingFriction, WORLD_WIDTH / 32, WORLD_WIDTH / 32, true));
                objectives.get(0).setObjective(true);

                bombs.add(new Bomb((WORLD_WIDTH / 4) - 1.6f, 1.6f + radius, 0, 0, radius, 1, rollingFriction, ThreadLocalRandom.current().nextInt(0, bombType.values().length)));
                connectors.add(new CatapultRope(shapes.get(0).body, bombs.get(0).body, 1, 200, 0, true, Color.LIGHT_GRAY, 1, catapultHeight));
                bombCount = 6;
                break;

            case 3:
                shapes.add(new Rectangle(WORLD_WIDTH / 4, WORLD_WIDTH / 8, 0, 0, Color.WHITE, 1, 0.02f, WORLD_HEIGHT / 6, 0.2f, false));

                shapes.add(new Rectangle((WORLD_WIDTH / 8) * 4.5f, (WORLD_HEIGHT / 6), 0, 0, Color.WHITE, 1, 0.02f, 0.2f, WORLD_WIDTH / 16, false));
                shapes.add(new Rectangle((WORLD_WIDTH / 8) * 6.5f, (WORLD_HEIGHT / 6), 0, 0, Color.WHITE, 1, 0.02f, 0.2f, WORLD_WIDTH / 16, false));
                shapes.add(new Rectangle((WORLD_WIDTH / 8) * 5.5f, (WORLD_HEIGHT / 6) * 2, 0, 0, Color.WHITE, 1, 0.02f, 0.2f, WORLD_WIDTH / 16, false));
                shapes.add(new Rectangle((WORLD_WIDTH / 8) * 7.5f, (WORLD_HEIGHT / 6) * 2, 0, 0, Color.WHITE, 1, 0.02f, 0.2f, WORLD_WIDTH / 16, false));
                shapes.add(new Rectangle((WORLD_WIDTH / 8) * 6.5f, (WORLD_HEIGHT / 6) * 3, 0, 0, Color.WHITE, 1, 0.02f, 0.2f, WORLD_WIDTH / 16, false));

                objectives.add(new Rectangle(((WORLD_WIDTH / 8) * 6.5f), ((WORLD_HEIGHT / 6) * 3.25f) + 0.2f, 0, 0, Color.RED, 10, rollingFriction, WORLD_WIDTH / 32, WORLD_WIDTH / 32, true));
                objectives.get(0).setObjective(true);

                bombs.add(new Bomb((WORLD_WIDTH / 4) - 1.6f, 1.6f + radius, 0, 0, radius, 1, rollingFriction, ThreadLocalRandom.current().nextInt(0, bombType.values().length)));
                connectors.add(new CatapultRope(shapes.get(0).body, bombs.get(0).body, 1, 200, 0, true, Color.LIGHT_GRAY, 1, catapultHeight));
                bombCount = 6;
                break;

            case 4:
                shapes.add(new Rectangle(WORLD_WIDTH / 4, WORLD_WIDTH / 8, 0, 0, Color.WHITE, 1, 0.02f, WORLD_HEIGHT / 6, 0.2f, false));

                shapes.add(new Rectangle((WORLD_WIDTH / 8) * 5, (WORLD_HEIGHT / 6) * 1.5f, 0, 0, Color.WHITE, 1, 0.02f, (WORLD_HEIGHT / 6) * 1.5f, 0.2f, false));
                shapes.add(new Rectangle((WORLD_WIDTH / 8) * 5, (WORLD_HEIGHT / 6) * 3.1f, 0, 0, Color.WHITE, 1, 0.02f, 0.2f, WORLD_WIDTH / 32, false));
                shapes.add(new Rectangle((WORLD_WIDTH / 8) * 5, ((WORLD_HEIGHT / 6) * 3.25f) + 0.2f, 0, 0, Color.WHITE, 1, 0.02f, WORLD_WIDTH / 32, WORLD_WIDTH / 32, true));
                shapes.add(new Rectangle((WORLD_WIDTH / 8) * 5, ((WORLD_HEIGHT / 6) * 3.75f) + 0.2f, 0, 0, Color.WHITE, 1, 0.02f, WORLD_WIDTH / 32, WORLD_WIDTH / 32, true));
                shapes.add(new Rectangle((WORLD_WIDTH / 8) * 5, ((WORLD_HEIGHT / 6) * 4.25f) + 0.2f, 0, 0, Color.WHITE, 1, 0.02f, WORLD_WIDTH / 32, WORLD_WIDTH / 32, true));
                shapes.add(new Rectangle((WORLD_WIDTH / 8) * 5, ((WORLD_HEIGHT / 6) * 4.75f) + 0.2f, 0, 0, Color.WHITE, 1, 0.02f, WORLD_WIDTH / 32, WORLD_WIDTH / 32, true));
                shapes.add(new Rectangle((WORLD_WIDTH / 8) * 5, ((WORLD_HEIGHT / 6) * 5.25f) + 0.2f, 0, 0, Color.WHITE, 1, 0.02f, WORLD_WIDTH / 32, WORLD_WIDTH / 32, true));
                shapes.add(new Rectangle((WORLD_WIDTH / 8) * 6.5f, (WORLD_HEIGHT / 6) * 1.5f, 0, 0, Color.WHITE, 1, 0.02f, 0.2f, WORLD_WIDTH / 128, false));


                objectives.add(new Rectangle(((WORLD_WIDTH / 8) * 6.5f), ((WORLD_HEIGHT / 6) * 1.75f) + 0.2f, 0, 0, Color.RED, 10, rollingFriction, WORLD_WIDTH / 32, WORLD_WIDTH / 32, true));
                objectives.get(0).setObjective(true);

                bombs.add(new Bomb((WORLD_WIDTH / 4) - 1.6f, 1.6f + radius, 0, 0, radius, 1, rollingFriction, ThreadLocalRandom.current().nextInt(0, bombType.values().length)));
                connectors.add(new CatapultRope(shapes.get(0).body, bombs.get(0).body, 1, 200, 0, true, Color.LIGHT_GRAY, 1, catapultHeight));
                bombCount = 6;
                break;

            case 5:
                shapes.add(new Rectangle(WORLD_WIDTH / 4, WORLD_WIDTH / 8, 0, 0, Color.WHITE, 1, 0.02f, WORLD_HEIGHT / 6, 0.2f, false));

                shapes.add(new Rectangle(WORLD_WIDTH / 2, WORLD_HEIGHT / 3, 0, 0, Color.WHITE, 1, 0.02f, (WORLD_HEIGHT / 3), 0.2f, false));
                shapes.add(new Rectangle(WORLD_WIDTH / 2, (WORLD_HEIGHT / 6) * 5.5f, 0, 0, Color.WHITE, 1, 0.02f, (WORLD_HEIGHT / 12), 0.2f, false));
                shapes.add(new Rectangle((WORLD_WIDTH / 8) * 5f, (WORLD_HEIGHT / 6) * 1.5f, 0, 0, Color.WHITE, 1, 0.02f, 0.2f, WORLD_WIDTH / 128, false));
                shapes.add(new Rectangle((WORLD_WIDTH / 8) * 6f, (WORLD_HEIGHT / 6) * 1.5f, 0, 0, Color.WHITE, 1, 0.02f, 0.2f, WORLD_WIDTH / 128, false));
                shapes.add(new Rectangle((WORLD_WIDTH / 8) * 7f, (WORLD_HEIGHT / 6) * 1.5f, 0, 0, Color.WHITE, 1, 0.02f, 0.2f, WORLD_WIDTH / 128, false));
                shapes.add(new Rectangle((WORLD_WIDTH / 8) * 4.5f, (WORLD_HEIGHT / 6) * 3.5f, 0, 0, Color.WHITE, 1, 0.02f, 0.2f, WORLD_WIDTH / 128, false));
                shapes.add(new Rectangle((WORLD_WIDTH / 8) * 5.5f, (WORLD_HEIGHT / 6) * 3.5f, 0, 0, Color.WHITE, 1, 0.02f, 0.2f, WORLD_WIDTH / 128, false));
                shapes.add(new Rectangle((WORLD_WIDTH / 8) * 6.5f, (WORLD_HEIGHT / 6) * 3.5f, 0, 0, Color.WHITE, 1, 0.02f, 0.2f, WORLD_WIDTH / 128, false));
                shapes.add(new Rectangle((WORLD_WIDTH / 8) * 7.5f, (WORLD_HEIGHT / 6) * 3.5f, 0, 0, Color.WHITE, 1, 0.02f, 0.2f, WORLD_WIDTH / 128, false));
                shapes.add(new Rectangle((WORLD_WIDTH / 8) * 4.5f, ((WORLD_HEIGHT / 6) * 3.5f) + 0.2f, 0, 0, Color.WHITE, 1, 0.02f, WORLD_WIDTH / 64, WORLD_WIDTH / 64, true));
                shapes.add(new Rectangle((WORLD_WIDTH / 8) * 5.5f, ((WORLD_HEIGHT / 6) * 3.5f) + 0.2f, 0, 0, Color.WHITE, 1, 0.02f, WORLD_WIDTH / 64, WORLD_WIDTH / 64, true));
                shapes.add(new Rectangle((WORLD_WIDTH / 8) * 6.5f, ((WORLD_HEIGHT / 6) * 3.5f) + 0.2f, 0, 0, Color.WHITE, 1, 0.02f, WORLD_WIDTH / 64, WORLD_WIDTH / 64, true));
                shapes.add(new Rectangle((WORLD_WIDTH / 8) * 7.5f, ((WORLD_HEIGHT / 6) * 3.5f) + 0.2f, 0, 0, Color.WHITE, 1, 0.02f, WORLD_WIDTH / 64, WORLD_WIDTH / 64, true));

                movingBlocks.add(new Rectangle(WORLD_WIDTH / 2, (WORLD_HEIGHT / 6) * 4.5f, 0, 0, Color.WHITE, 1, 0.02f, 0.2f, WORLD_WIDTH / 16, false));

                objectives.add(new Rectangle(((WORLD_WIDTH / 8) * 5), ((WORLD_HEIGHT / 6) * 1.5f) + 0.2f, 0, 0, Color.RED, 2, rollingFriction, WORLD_WIDTH / 64, WORLD_WIDTH / 64, true));
                objectives.get(0).setObjective(true);
                objectives.add(new Rectangle(((WORLD_WIDTH / 8) * 6), ((WORLD_HEIGHT / 6) * 1.5f) + 0.2f, 0, 0, Color.RED, 2, rollingFriction, WORLD_WIDTH / 64, WORLD_WIDTH / 64, true));
                objectives.get(1).setObjective(true);
                objectives.add(new Rectangle(((WORLD_WIDTH / 8) * 7), ((WORLD_HEIGHT / 6) * 1.5f) + 0.2f, 0, 0, Color.RED, 2, rollingFriction, WORLD_WIDTH / 64, WORLD_WIDTH / 64, true));
                objectives.get(2).setObjective(true);

                bombs.add(new Bomb((WORLD_WIDTH / 4) - 1.6f, 1.6f + radius, 0, 0, radius, 1, rollingFriction, ThreadLocalRandom.current().nextInt(0, bombType.values().length)));
                connectors.add(new CatapultRope(shapes.get(0).body, bombs.get(0).body, 1, 200, 0, true, Color.LIGHT_GRAY, 1, catapultHeight));
                bombCount = 6;
                break;
            case 6 :
                messages.add(new ScreenText(SCREEN_WIDTH / 2, SCREEN_HEIGHT / 2, "Game Complete!", 30,Color.white));
                view.repaint();
                Thread.sleep(3000);
                System.exit(0);
                break;

        }
        bombsLeft = bombCount;
        for (int i = 0; i < objectives.size(); i++) {
            targetInVortex[i] = 0;
        }
        while (!playerLose && !playerWin) {
            runGame();
        }
        if (playerWin) {
            messages.add(new ScreenText(SCREEN_WIDTH / 2, SCREEN_HEIGHT / 2, "Level Complete!", 30, Color.RED));
            view.repaint();
            Thread.sleep(3000);
        } else {
            messages.add(new ScreenText(SCREEN_WIDTH / 2, SCREEN_HEIGHT / 2, "Level Failed!", 30,Color.RED));
            view.repaint();
            Thread.sleep(3000);
        }
        clearLevel();
    }

    //Runs the majority of the game mechanics
    private void runGame() {
        if(messages.size()>0){
            messages.set(messages.size()-1,new ScreenText(Game.convertWorldXtoScreenX(WORLD_WIDTH/4),Game.convertWorldYtoScreenY((WORLD_HEIGHT/6)*5),"Bombs Remaining: " + Integer.toString(bombsLeft),40,Color.WHITE));
        }
        else{
            messages.add(new ScreenText(Game.convertWorldXtoScreenX(WORLD_WIDTH/4),Game.convertWorldYtoScreenY((WORLD_HEIGHT/6)*5),"Bombs Remaining: " + Integer.toString(bombsLeft),40,Color.WHITE));
        }
        Dimension d = frame.getContentPane().getSize();
        SCREEN_HEIGHT = d.height;
        SCREEN_WIDTH = d.width;
        FRAME_SIZE = new Dimension(SCREEN_WIDTH, SCREEN_HEIGHT);
        if (currentLevel == 2 || currentLevel == 5) {
            movingBlock(movingBlocks.get(0));
        }
        if (bombs.size() > 0) {
            if (BasicMouseListener.getWorldCoordinatesOfMousePointer().x < bombs.get(0).body.getPosition().x + bombs.get(0).getRadius() &&
                    BasicMouseListener.getWorldCoordinatesOfMousePointer().x > bombs.get(0).body.getPosition().x - bombs.get(0).getRadius() &&
                    BasicMouseListener.getWorldCoordinatesOfMousePointer().y < bombs.get(0).body.getPosition().y + bombs.get(0).getRadius() &&
                    BasicMouseListener.getWorldCoordinatesOfMousePointer().y > bombs.get(0).body.getPosition().y - bombs.get(0).getRadius() &&
                    bombConnected && BasicMouseListener.isMouseButtonPressed()) {
                ALLOW_MOUSE_POINTER_TO_DRAG_BODIES_ON_SCREEN = true;
                bombTension = true;
                holdingBomb = true;
                bombs.get(bombs.size() - 1).setPosition(mouseCoordinates);
            }
            if (ALLOW_MOUSE_POINTER_TO_DRAG_BODIES_ON_SCREEN) {
                if (!BasicMouseListener.isMouseButtonPressed()) {
                    ALLOW_MOUSE_POINTER_TO_DRAG_BODIES_ON_SCREEN = false;
                    holdingBomb = false;
                }
            }
            //Releases the bomb from the catapult if it safely past the catapult holder, and is not being held by the player
            if (bombs.get(bombs.size() - 1).body.getPosition().x > (shapes.get(0).body.getPosition().x + bombs.get(bombs.size() - 1).getRadius() + ((Rectangle) shapes.get(0)).getWidth()) && bombConnected && !BasicMouseListener.isMouseButtonPressed()) {
                bombConnected = false;
                bombTension = false;
                connectors.get(0).removerJoints();
                connectors.remove(0);
            }
            //Helps the player drag the bomb, also helps stop the bomb being dragged too far by altering the stored mouse coordinates
            // if they are beyond the catapult stretch limit (see BasicMouseListener code)
            if (holdingBomb) {
                bombs.get(bombs.size() - 1).setPosition(mouseCoordinates);
            }
            if (BasicKeyListener.isDetonateKeyPressed() && bombs.size() > 0 && !bombConnected) {
                this.explosion(bombs.size() - 1);

            }
        }
        for (CatapultRope ec : connectors) {
            if (bombTension) {
                if (!holdingBomb) {
                    ec.applyTensionForceToBothParticles();
                }

            }
        }
        if (connectors.size() > 1) {
            connectors.get(1).removerJoints();
            connectors.remove(1);
        }
        //If there are any vortexes or sticks present, exerts pulls on all nearby bodies
        runVortexes();

        //Check to see if all objectives have fallen
        playerWin = true;
        for (Rectangle o : objectives) {
            //Is objective on the floor
            if (o.body.getPosition().y > o.getHeight() * 1.5) {
                playerWin = false;
            }
        }
        //If all 5 bombs have been thrown, and the player hasn't won
        if (bombsThrown >= bombCount && !playerWin) {
            playerLose = isGameAtRest();
        }
        if(playerWin){
            currentLevel++;
        }
        updateGame();
        try {
            Thread.sleep(DELAY);
        } catch (InterruptedException e) {

        }
    }

    //Used to find out if all non-bomb bodies have come to rest, isAwake() function not always reliable in this way.
    //Also determines if the game objective is stuck in a vortex. If this is the case and the player has no bombs left,
    //it is not possible to win, therefore they must lose.
    //This method is not 100% reliable, it sometimes claims the world is at rest when the objective block is still moving,
    // but it works better than using the isAwake() method, as that almost never seems to return false, resulting in a
    //very long wait if you run out of bombs before it says level failed
    private boolean isGameAtRest() {
        boolean rest = true;
        for (Shape2D s : shapes) {
            if (s.body.getLinearVelocity().y > 0.001 || s.body.getLinearVelocity().x > 0.001) {
                rest = false;
            }
        }
        for (Shape2D o : objectives) {
            if (o.body.getLinearVelocity().y > 0.001 || o.body.getLinearVelocity().x > 0.001) {
                rest = false;
            }
        }
        if(vortexes.size()>0){
            rest = false;
        }
        return rest;
    }

    //Moves the game forward one timestep
    private void updateGame() {
        world.step(DELTA_T, NUM_EULER_UPDATES_PER_SCREEN_REFRESH, NUM_EULER_UPDATES_PER_SCREEN_REFRESH);
        for (Bomb b : bombs) {
            b.notificationOfNewTimestep();
        }
        for (int i = 0; i < shapes.size(); i++) {
            shapes.get(i).notificationOfNewTimestep();
            if (shapes.get(i).getHealth() == 0) {
                world.destroyBody(shapes.get(i).body);
                shapes.remove(shapes.get(i));
            }
        }
        for (Shape2D s : objectives) {
            s.notificationOfNewTimestep();
        }
        view.repaint();
    }

    //Method used to remove all bodies from world, and clear arrayLists
    private void clearLevel() {
        for (Bomb b : bombs) {
            world.destroyBody(b.body);
        }
        bombs.clear();
        for (Shape2D s : shapes) {
            world.destroyBody(s.body);
        }
        shapes.clear();
        for (Shape2D s : shrapnel) {
            world.destroyBody(s.body);
        }
        shrapnel.clear();
        for (Shape2D o : objectives) {
            world.destroyBody(o.body);
        }
        objectives.clear();
        for (CatapultRope c : connectors) {
            c.removerJoints();
        }
        for (Rectangle r : movingBlocks) {
            world.destroyBody(r.body);
        }
        movingBlocks.clear();
        connectors.clear();
        vortexes.clear();
        sticks.clear();
        messages.clear();
    }

    //Method to handle a bomb of any type being detonated
    private void explosion(int bomb) {
        if (this.bombs.size() > (bomb)) {
            switch (bombs.get(bomb).getBombType()) {
                case Explode:
                    bombsThrown++;
                    Vec2 explodeCoords = this.bombs.get(bomb).body.getPosition();
                    world.destroyBody(bombs.get(bomb).body);
                    bombs.clear();
                    shrapnel.add(new Triangle(explodeCoords.x + 1, explodeCoords.y + 1, 0, 0, radius / 2, Color.RED, 1, 0, 3));
                    shrapnel.add(new Triangle(explodeCoords.x - (radius / 2), explodeCoords.y, 0, 0, radius / 2, Color.RED, 1, 0, 3));
                    shrapnel.add(new Triangle(explodeCoords.x, explodeCoords.y + (radius / 2), 0, 0, radius / 2, Color.RED, 1, 0, 3));
                    shrapnel.add(new Triangle(explodeCoords.x, explodeCoords.y - (radius / 2), 0, 0, radius / 2, Color.RED, 1, 0, 3));
                    for (Shape2D bP : shapes) {
                        float blastX = bP.body.getPosition().x - explodeCoords.x;
                        float blastY = bP.body.getPosition().y - explodeCoords.y;
                        Vec2 blast = new Vec2(blastX, blastY);
                        if (blast.length() < blastRadius) {
                            float blastLength = blast.length();
                            Vec2 blastUnitVector = new Vec2(blast.x / blast.normalize(), blast.y / blast.normalize());
                            float blastMultiplier = blastStrength / blast.normalize();
                            Vec2 blastDirection = blastUnitVector.mul(blastMultiplier);
                            bP.body.applyForceToCenter(blastDirection);
                            if (bP.body.getType().equals(BodyType.DYNAMIC)) {
                                bP.setHealth(bP.getHealth() - (int) (((blastRadius - blastLength) / blastRadius) * 10));
                            }
                        }
                    }
                    for (Shape2D bP : shrapnel) {
                        float blastX = bP.body.getPosition().x - explodeCoords.x;
                        float blastY = bP.body.getPosition().y - explodeCoords.y;
                        Vec2 blast = new Vec2(blastX, blastY);
                        if (blast.length() < blastRadius) {
                            Vec2 blastUnitVector = new Vec2(blast.x / blast.normalize(), blast.y / blast.normalize());
                            float blastMultiplier = blastStrength / blast.normalize();
                            Vec2 blastDirection = blastUnitVector.mul(blastMultiplier);
                            bP.body.applyForceToCenter(blastDirection);
                        }
                    }
                    for (Shape2D bP : objectives) {
                        float blastX = bP.body.getPosition().x - explodeCoords.x;
                        float blastY = bP.body.getPosition().y - explodeCoords.y;
                        Vec2 blast = new Vec2(blastX, blastY);
                        if (blast.length() < blastRadius) {
                            Vec2 blastUnitVector = new Vec2(blast.x / blast.normalize(), blast.y / blast.normalize());
                            float blastMultiplier = blastStrength / blast.normalize();
                            Vec2 blastDirection = blastUnitVector.mul(blastMultiplier);
                            bP.body.applyForceToCenter(blastDirection);
                        }
                    }
                    if (connectors.size() > 0) {
                        if (connectors.get(0).getB1() == null) {
                            connectors.get(0).removerJoints();
                            connectors.remove(0);
                        }
                        if (connectors.get(0).getB2() == null) {
                            connectors.get(0).removerJoints();
                            connectors.remove(0);
                        }
                    }
                    if (bombsThrown < bombCount) {
                        bombs.add(new Bomb((WORLD_WIDTH / 4) - 1.6f, 1.6f + radius, 0, 0, radius, 1, rollingFriction, ThreadLocalRandom.current().nextInt(0, bombType.values().length)));
                        connectors.add(new CatapultRope(shapes.get(0).body, bombs.get(0).body, 1, 200, 0, true, Color.LIGHT_GRAY, 1, catapultHeight));
                        bombConnected = true;
                    }
                    bombsLeft = bombCount - bombsThrown;
                    break;
                case Implode:
                    bombsThrown++;
                    Vec2 implodeCoords = this.bombs.get(bomb).body.getPosition();
                    world.destroyBody(bombs.get(bomb).body);
                    bombs.clear();
                    for (Shape2D bP : shapes) {
                        float blastX = implodeCoords.x - bP.body.getPosition().x;
                        float blastY = implodeCoords.y - bP.body.getPosition().y;
                        Vec2 blast = new Vec2(blastX, blastY);
                        if (blast.length() < blastRadius) {
                            Vec2 blastUnitVector = new Vec2(blast.x / blast.normalize(), blast.y / blast.normalize());
                            float blastMultiplier = (blastStrength*2) / blast.normalize();
                            Vec2 blastDirection = blastUnitVector.mul(blastMultiplier);
                            bP.body.applyForceToCenter(blastDirection);
                        }
                    }
                    for (Shape2D bP : shrapnel) {
                        float blastX = implodeCoords.x - bP.body.getPosition().x;
                        float blastY = implodeCoords.y - bP.body.getPosition().y;
                        Vec2 blast = new Vec2(blastX, blastY);
                        if (blast.length() < 6) {
                            Vec2 blastUnitVector = new Vec2(blast.x / blast.normalize(), blast.y / blast.normalize());
                            float blastMultiplier = blastStrength / blast.normalize();
                            Vec2 blastDirection = blastUnitVector.mul(blastMultiplier);
                            bP.body.applyForceToCenter(blastDirection);
                        }
                    }
                    for (Shape2D bP : objectives) {
                        float blastX = implodeCoords.x - bP.body.getPosition().x;
                        float blastY = implodeCoords.y - bP.body.getPosition().y;
                        Vec2 blast = new Vec2(blastX, blastY);
                        if (blast.length() < 6) {
                            Vec2 blastUnitVector = new Vec2(blast.x / blast.normalize(), blast.y / blast.normalize());
                            float blastMultiplier = blastStrength / blast.normalize();
                            Vec2 blastDirection = blastUnitVector.mul(blastMultiplier);
                            bP.body.applyForceToCenter(blastDirection);
                        }
                    }
                    if (connectors.size() > 0) {
                        if (connectors.get(0).getB1() == null) {
                            connectors.get(0).removerJoints();
                            connectors.remove(0);
                        }
                        if (connectors.get(0).getB2() == null) {
                            connectors.get(0).removerJoints();
                            connectors.remove(0);
                        }
                    }
                    if (bombsThrown < bombCount) {
                        bombs.add(new Bomb((WORLD_WIDTH / 4) - 1.6f, 1.6f + radius, 0, 0, radius, 1, rollingFriction, ThreadLocalRandom.current().nextInt(0, bombType.values().length)));
                        connectors.add(new CatapultRope(shapes.get(0).body, bombs.get(0).body, 1, 200, 0, true, Color.LIGHT_GRAY, 1, catapultHeight));
                        bombConnected = true;
                    }
                    bombsLeft = bombCount - bombsThrown;
                    break;
                case Sticky:
                    bombsThrown++;
                    if (this.bombs.get(bomb).stuck && sticks.size() > 0) {
                        sticks.remove(sticks.size() - 1);
                    }
                    Vec2 stickyCoords = this.bombs.get(bomb).body.getPosition();
                    world.destroyBody(bombs.get(bomb).body);
                    bombs.clear();
                    shrapnel.add(new Triangle(stickyCoords.x + 1, stickyCoords.y + 1, 0, 0, radius / 2, Color.RED, 1, 0, 3));
                    shrapnel.add(new Triangle(stickyCoords.x - (radius / 2), stickyCoords.y, 0, 0, radius / 2, Color.RED, 1, 0, 3));
                    shrapnel.add(new Triangle(stickyCoords.x, stickyCoords.y + (radius / 2), 0, 0, radius / 2, Color.RED, 1, 0, 3));
                    shrapnel.add(new Triangle(stickyCoords.x, stickyCoords.y - (radius / 2), 0, 0, radius / 2, Color.RED, 1, 0, 3));
                    for (Shape2D bP : shapes) {
                        float blastX = bP.body.getPosition().x - stickyCoords.x;
                        float blastY = bP.body.getPosition().y - stickyCoords.y;
                        Vec2 blast = new Vec2(blastX, blastY);
                        if (blast.length() < blastRadius) {
                            float blastLength = blast.length();
                            Vec2 blastUnitVector = new Vec2(blast.x / blast.normalize(), blast.y / blast.normalize());
                            float blastMultiplier = 8000 / blast.normalize();
                            Vec2 blastDirection = blastUnitVector.mul(blastMultiplier);
                            bP.body.applyForceToCenter(blastDirection);
                            if (bP.body.getType().equals(BodyType.DYNAMIC)) {
                                bP.setHealth(bP.getHealth() - (int) (((blastRadius - blastLength) / blastRadius) * 10));
                                bP.setHealth(bP.getHealth() - 2);
                            }
                        }
                    }
                    for (Shape2D bP : shrapnel) {
                        float blastX = bP.body.getPosition().x - stickyCoords.x;
                        float blastY = bP.body.getPosition().y - stickyCoords.y;
                        Vec2 blast = new Vec2(blastX, blastY);
                        if (blast.length() < blastRadius) {
                            Vec2 blastUnitVector = new Vec2(blast.x / blast.normalize(), blast.y / blast.normalize());
                            float blastMultiplier = blastStrength / blast.normalize();
                            Vec2 blastDirection = blastUnitVector.mul(blastMultiplier);
                            bP.body.applyForceToCenter(blastDirection);
                        }
                    }
                    for (Shape2D bP : objectives) {
                        float blastX = bP.body.getPosition().x - stickyCoords.x;
                        float blastY = bP.body.getPosition().y - stickyCoords.y;
                        Vec2 blast = new Vec2(blastX, blastY);
                        if (blast.length() < blastRadius) {
                            Vec2 blastUnitVector = new Vec2(blast.x / blast.normalize(), blast.y / blast.normalize());
                            float blastMultiplier = blastStrength / blast.normalize();
                            Vec2 blastDirection = blastUnitVector.mul(blastMultiplier);
                            bP.body.applyForceToCenter(blastDirection);
                        }
                    }
                    if (connectors.size() > 0) {
                        if (connectors.get(0).getB1() == null) {
                            connectors.get(0).removerJoints();
                            connectors.remove(0);
                        }
                        if (connectors.get(0).getB2() == null) {
                            connectors.get(0).removerJoints();
                            connectors.remove(0);
                        }
                    }
                    if (bombsThrown < bombCount) {
                        bombs.add(new Bomb((WORLD_WIDTH / 4) - 1.6f, 1.6f + radius, 0, 0, radius, 1, rollingFriction, ThreadLocalRandom.current().nextInt(0, bombType.values().length)));
                        connectors.add(new CatapultRope(shapes.get(0).body, bombs.get(0).body, 1, 200, 0, true, Color.LIGHT_GRAY, 1, catapultHeight));
                        bombConnected = true;
                    }
                    if (sticks.size() > 0) {
                        if (this.bombs.get(bomb).stuck) {
                            sticks.remove(sticks.size() - 1);
                        }
                    }
                    bombsLeft = bombCount - bombsThrown;
                    break;
                case Black_Hole:
                    bombsThrown++;
                    Vec2 vortexCoords = bombs.get(bomb).body.getPosition();
                    world.destroyBody(bombs.get(bomb).body);
                    bombs.clear();
                    vortexes.add(new Vortex(vortexCoords));
                    if (bombsThrown < bombCount) {
                        bombs.add(new Bomb((WORLD_WIDTH / 4) - 1.6f, 1.6f + radius, 0, 0, radius, 1, rollingFriction, ThreadLocalRandom.current().nextInt(0, bombType.values().length)));
                        connectors.add(new CatapultRope(shapes.get(0).body, bombs.get(0).body, 1, 200, 0, true, Color.LIGHT_GRAY, 1, catapultHeight));
                        bombConnected = true;
                    }
                    bombsLeft = bombCount - bombsThrown;
                    break;
            }
        }
    }

    //Exerts pull from all vortexes present in the game
    private void runVortexes() {
        for (Stick s : sticks)
            s.pull();
        if (vortexes.size() > 0) {
            for (Bomb b : bombs) {
                //Makes sure vortex doesn't pull in bombs that haven't been fired yet
                if(b.body.getPosition().x>shapes.get(0).body.getPosition().x){
                    for (int i = 0; i < vortexes.size(); i++) {
                        if (vortexes.get(i).vortexCounter < 5000) {
                            vortexes.get(i).pull(b.body);
                        } else {
                            vortexes.remove(i);
                        }
                    }
                }

            }
            for (Shape2D b : shapes) {
                for (int i = 0; i < vortexes.size(); i++) {
                    if (vortexes.get(i).vortexCounter < 5000) {
                        vortexes.get(i).pull(b.body);
                    } else {
                        vortexes.remove(i);
                    }
                }
            }
            for (Shape2D b : shrapnel) {
                for (int i = 0; i < vortexes.size(); i++) {
                    if (vortexes.get(i).vortexCounter < 5000) {
                        vortexes.get(i).pull(b.body);
                    } else {
                        vortexes.remove(i);
                    }
                }
            }
            for (Shape2D b : objectives) {
                for (int i = 0; i < vortexes.size(); i++) {
                    if (vortexes.get(i).vortexCounter < 5000) {
                        vortexes.get(i).pull(b.body);
                    } else {
                        vortexes.remove(i);
                    }
                }
            }
        }


    }

    private void movingBlock(Rectangle block) {
        block.body.setGravityScale(0);
        switch (currentLevel) {
            case 0:
                break;
            case 1:
                break;
            case 2:
                if (up && block.body.getPosition().y >= WORLD_HEIGHT - block.getHeight() - 1) {
                    up = false;
                } else if (!up && block.body.getPosition().y <= block.getHeight() + 1) {
                    up = true;
                }
                if (up) {
                    block.body.setTransform(new Vec2(block.body.getPosition().x, block.body.getPosition().y += 0.5f), block.body.getAngle());
                } else {
                    block.body.setTransform(new Vec2(block.body.getPosition().x, block.body.getPosition().y -= 0.5f), block.body.getAngle());
                }
                break;
            case 3:
                break;
            case 4:
                break;
            case 5:
                if (right && block.body.getPosition().x >= WORLD_WIDTH - block.getWidth() - 1) {
                    right = false;
                } else if (!right && block.body.getPosition().x <= block.getWidth() + 1) {
                    right = true;
                }
                if (right) {
                    block.body.setTransform(new Vec2(block.body.getPosition().x += 0.5f, block.body.getPosition().y), block.body.getAngle());
                } else {
                    block.body.setTransform(new Vec2(block.body.getPosition().x -= 0.5f, block.body.getPosition().y), block.body.getAngle());
                }
                break;
        }

    }
}
