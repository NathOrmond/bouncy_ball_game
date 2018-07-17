package runtime;

import java.applet.Applet;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.io.FileNotFoundException;
import java.net.URL;
import java.util.Random;
import gameobjects.AgilityDown;
import gameobjects.AgilityUp;
import gameobjects.Ball;
import gameobjects.GravDown;
import gameobjects.GravUp;
import gameobjects.Item;
import gameobjects.Platform;
import gameobjects.ScoreUp;
import leaderboard.ReadLeaderFile;
import resources.Resources;

/**
 * @author Nathan Ormond
 */

public class StartingPoint extends Applet implements Runnable, KeyListener, MouseMotionListener, MouseListener {

	/****************************************************************************************************
	 * RUNTIME VARIABLES
	 ****************************************************************************************************/
	private static final long serialVersionUID = 1918883748881378430L;
	private Image i;
	private Graphics doubleG;
	private int maxNumPlatforms = 7;
	private int maxNumItems = 5;
	int score;
	Ball b;
	Platform platforms[] = new Platform[maxNumPlatforms];
	Item items[] = new Item[maxNumItems];
	private double backgroundImageX = 0;
	private double backgroundImageDX = 0.3;
	URL url;
	Image backgroundImage;
	int levelCheck = 0;
	boolean gameOver = false;
	boolean mouseIn = false;
	private int mouseBoxX, mouseBoxY, mouseBoxWidth, mouseBoxHeight;
	boolean highScoresEntered = false;
	String nameText = "";

	/****************************************************************************************************
	 * GAME RUNTIME
	 ****************************************************************************************************/
	/**
	 * Runs on application initialisation
	 */
	@Override
	public void init() {
		setSize(800, 600);
		addListeners();
		new Resources(this);
		playBackGroundMusic();
		setBackGroundImage();
	}

	/**
	 * Initialises variables and methods
	 */
	@Override
	public void start() {
		score = 0;
		b = new Ball();
		initialPlatformBehaviours();
		for (int i = 0; i < items.length; i++) {
			Random r = new Random();
			itemSwitch(r, i, (2000 * i));
		}
		Thread thread = new Thread(this);
		thread.start();
	}

	/**
	 * main game loop runs on a loop after start is finished
	 */
	@Override
	public void run() {
		while (true) {
			gameOver = b.getGameOver();
			if (levelCheck > 1000) {
				Resources.level++;
				levelCheck = 0;
			}
			levelCheck++;
			Random r = new Random();
			b.update(this);
			if (!gameOver) {
				score++;
			}
			if (backgroundImageX > -getWidth()) {
				backgroundImageX -= backgroundImageDX;
			} else {
				backgroundImageX = 0;
			}
			for (int i = 0; i < platforms.length; i++) {
				platforms[i].update(this, b);
			}
			for (int i = 0; i < items.length; i++) {
				items[i].update(this, b);

				if (items[i].isCreateNew()) {
					items[i] = null;
					itemSwitch(r, i, (10 * r.nextInt(500)));
				}
			}
			repaint();
			try {
				Thread.sleep(17);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void stop() {
		// not used
	}

	@Override
	public void destroy() {
		// not used
	}

	/****************************************************************************************************
	 * GRAPHICS
	 ****************************************************************************************************/

	/**
	 * double buffering updates graphics with latest positions
	 */
	@Override
	public void update(Graphics g) {
		if (i == null) {	
			i = createImage(this.getSize().width, this.getSize().height);	
			doubleG = i.getGraphics();
		}
		doubleG.setColor(getBackground());	
		doubleG.fillRect(0, 0, this.getSize().width, this.getSize().height);
		doubleG.setColor(getForeground());
		paint(doubleG);
		g.drawImage(i, 0, 0, this);
	}

	/**
	 * paints the graphics onto the screen
	 */
	@Override
	public void paint(Graphics g) {
		g.drawImage(backgroundImage, (int) backgroundImageX, 0, this);
		g.drawImage(backgroundImage, (int) backgroundImageX + getWidth(), 0, this);
		b.paint(g);
		for (int i = 0; i < platforms.length; i++) {
			platforms[i].paint(g);
		}
		for (int i = 0; i < items.length; i++) {
			items[i].paint(g);
		}
		paintScore(g);
		
		if (gameOver) {
			paintMenu(g);
		}
	}

	/**
	 * Draws the graphics for the score
	 * 
	 * @param g
	 */
	private void paintScore(Graphics g) {
		String scoreStr = Integer.toString(score);
		Font font = new Font("Serif", Font.BOLD, 32);
		g.setFont(font);
		g.setColor(Color.BLACK);
		g.drawString(scoreStr, getWidth() - 150 + 2, 50 + 2);
		g.setColor(Color.WHITE);
		g.drawString(scoreStr, getWidth() - 150, 50);
	}

	/**
	 * Paints the menu when game is over
	 * 
	 * @param g
	 */
	private void paintMenu(Graphics g) {
		if (!highScoresEntered) {
			if (checkIfScoreIsInTopTen()) {
				if (checkIfScoreIsHighScore()) {
					paintNewHighScore(g);
				}
				paintEnterName(g);
				paintTypedName(g);
			} else { 
				highScoresEntered = true;
			}
		} else { 
			paintScoreTooLowForLeaderBoard(g);
			paintTopThreeScores(g);
			gameOverGraphic(g);
			continueGraphics(g);
		}
	}
	
	private void paintEnterName(Graphics g) {
		String str = "ENTER NAME : ";
		g.drawString(str, getWidth() / 2 - (str.length() * 10), getHeight() / 2 + 40);
	}
	
	private void paintTypedName(Graphics g) { 
		String str = nameText;
		g.drawString(str, getWidth() / 2 - (str.length() * 10), getHeight() / 2 + 40);
	}

	private void paintNewHighScore(Graphics g) { 
		String str = "NEW HIGH SCORE!";
		g.drawString(str, getWidth() / 2 - (str.length() * 10), getHeight() / 2 + 40);
		
	}
	
	private void paintScoreTooLowForLeaderBoard(Graphics g) { 
		String str = "SCORE TOO LOW FOR LEADERBOARD";
		g.drawString(str, getWidth() / 2 - (str.length() * 10), getHeight() / 2 + 40);
		
	}
	
	private void paintTopThreeScores(Graphics g) { 
		String str = "1), 2), 3)";
		g.drawString(str, getWidth() / 2 - (str.length() * 10), getHeight() / 2 + 40);
	}

	private void gameOverGraphic(Graphics g) {
		g.setColor(Color.WHITE);
		String str = "GAME OVER ";
		g.drawString(str, getWidth() / 2 - (str.length() * 10), getHeight() / 2);
	}

	private void continueGraphics(Graphics g) {
		if (mouseIn) {
			g.setColor(Color.RED);
		} else {
			g.setColor(Color.WHITE);
		}
		String str = " CONTINUE ";
		// Defines area for mouse to be in (area which contains text)
		setMouseBoxVariables(getWidth() / 2 - (str.length() * 10), getHeight() / 2 + 5, 2 * (str.length() * 10), 40);
		g.drawString(str, getWidth() / 2 - (str.length() * 10), getHeight() / 2 + 40);
	}

	/****************************************************************************************************
	 * CLASS METHODS
	 ****************************************************************************************************/

	/**
	 * Makes this class a listener of things which will trigger events
	 */
	private void addListeners() {
		addKeyListener(this);
		addMouseMotionListener(this);
		addMouseListener(this);
	}

	/**
	 * Plays the selected music
	 */
	private void playBackGroundMusic() {
		Resources.music.loop();
		// ToDo create a list of music to loop through as game plays
	}

	/**
	 * Sets background image to definition within resources class
	 */
	private void setBackGroundImage() {
		backgroundImage = Resources.backGroundImage;
	}
	
	private boolean mouseWithinBox(int x, int y) { 
		if(((x > mouseBoxX) && (x < (mouseBoxX + mouseBoxWidth)))&& ((y > mouseBoxY) && (y < (mouseBoxY + mouseBoxHeight)))) { 
			return true;
		}
		return false;
	}

	/**************************************************
	 * Game Object methods
	 **************************************************/

	/**
	 * ensures ball doesnt fall through a hole causing game to end with no chance
	 * for player to stop it upon ball restart
	 */
	private void initialPlatformBehaviours() {
		for (int i = 0; i < platforms.length; i++) {
			Random r = new Random();
			platforms[i] = new Platform(i * 160, 300);
		}
	}

	/**
	 * Switch for different types of items which are implemented within game
	 * 
	 * @param r
	 * @param i
	 * @param addition
	 */
	private void itemSwitch(Random r, int i, int addition) {
		switch (r.nextInt(maxNumItems)) {
		case 0:
			items[i] = new GravUp(getWidth() + addition);
			break;
		case 1:
			items[i] = new GravDown(getWidth() + addition);
			break;
		case 2:
			items[i] = new AgilityUp(getWidth() + addition);
			break;
		case 3:
			items[i] = new AgilityDown(getWidth() + addition);
			break;
		case 4:
			items[i] = new ScoreUp(getWidth() + addition, this);
			break;
		}
		items[i].setCreateNew(false);
	}

	/**************************************************
	 * Game reset methods
	 **************************************************/

	/**
	 * Restarts the game when conditions are met
	 */
	private void gameResetEvent() {
		b = new Ball();
		score = 0;
		Resources.level = 1;
		mouseIn = false;
		gameOver = false;
		highScoresEntered = false;
		initialPlatformBehaviours();
	}

	private boolean checkIfScoreIsInTopTen() {
		return false;
	}

	private boolean checkIfScoreIsHighScore() {
		ReadLeaderFile read = new ReadLeaderFile();
		try {
			read.readFileTokens();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		return false;
	}

	/**************************************************
	 * Event Actions (Overridden)
	 **************************************************/

	@Override
	public void keyPressed(KeyEvent e) {
		switch (e.getKeyCode()) {
			case KeyEvent.VK_LEFT:
				b.moveLeft();
					break;
			case KeyEvent.VK_RIGHT:
				b.moveRight();
					break;
		}
		
		if(!highScoresEntered && gameOver) { 
			switch(e.getKeyCode()) { 
				default: 
					nameText += e.getKeyChar();
					break;
			
				case KeyEvent.VK_ENTER: 
					highScoresEntered = true;
					break;
			}
		}
	}

	@Override
	public void mouseMoved(MouseEvent e) {
		mouseIn = mouseWithinBox(e.getX(), e.getY());
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		if (gameOver && mouseIn && highScoresEntered) {
			gameResetEvent();
		}
	}

	@Override
	public void keyReleased(KeyEvent e) {
		//deceleration on ball movement keys
	}

	@Override
	public void keyTyped(KeyEvent arg0) {
		// not used
	}

	@Override
	public void mouseDragged(MouseEvent arg0) {
		// not used
	}

	@Override
	public void mouseEntered(MouseEvent e) {
		// not used

	}

	@Override
	public void mouseExited(MouseEvent e) {
		// not used

	}

	@Override
	public void mousePressed(MouseEvent e) {
		// not used

	}

	@Override
	public void mouseReleased(MouseEvent e) {
		// not used

	}

	/**************************************************
	 * GETTERS AND SETTERS
	 **************************************************/

	public int getScore() {
		return score;
	}

	public void setScore(int score) {
		this.score = score;
	}

	private void setMouseBoxVariables(int x, int y, int z, int Eph) {
		this.mouseBoxX = x;
		this.mouseBoxY = y;
		this.mouseBoxWidth = z;
		this.mouseBoxHeight = Eph;
	}

}
