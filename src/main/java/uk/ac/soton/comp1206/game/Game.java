package uk.ac.soton.comp1206.game;

import javafx.application.Platform;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.component.GameBlock;
import uk.ac.soton.comp1206.component.GameBlockCoordinate;
import uk.ac.soton.comp1206.event.GameLoopListener;
import uk.ac.soton.comp1206.event.LineFadeListener;
import uk.ac.soton.comp1206.event.NextPieceListener;
import uk.ac.soton.comp1206.event.gameStopListener;
import uk.ac.soton.comp1206.media.Multimedia;

import java.util.HashSet;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.BiFunction;

/**
 * The Game class handles the main logic, state and properties of the TetrECS game. Methods to manipulate the game state
 * and to handle actions made by the player should take place inside this class.
 */
public class Game {

  /**
   * logger for game class
   */
  private static final Logger logger = LogManager.getLogger(Game.class);

  /**
   * Number of rows
   */
  protected final int rows;

  /**
   * Number of columns
   */
  protected final int cols;

  /**
   * The grid model linked to the game
   */
  protected final Grid grid;
  /**
   * The current piece in game, and is the piece which is shown on the right of the game
   */
  protected GamePiece currentPiece;
  /**
   * The following piece in the game
   */
  protected GamePiece followingPiece;
  /**
   * current score
   */
  protected IntegerProperty score = new SimpleIntegerProperty(0);
  /**
   * current level
   */
  protected IntegerProperty level = new SimpleIntegerProperty(0);
  /**
   * current lives
   */
  protected IntegerProperty lives = new SimpleIntegerProperty(3);
  /**
   * current multiplayer
   */
  protected IntegerProperty multiplier = new SimpleIntegerProperty(1);
  /**
   * use to play sound
   */
  protected Multimedia multimedia = new Multimedia();
  /**
   * control when the game should be stopped
   */
  protected ScheduledExecutorService service;

  /**
   * represents a delayed or periodic task that has been scheduled to run in the future
   */
  protected ScheduledFuture<?> scheduledFuture;

  /**
   * the listener which used to handle event when next piece happens
   */
  protected NextPieceListener nextPieceListener;

  /**
   * listener which is called when a line is cleared
   */
  protected LineFadeListener lineFadeListener;
  /**
   * listener which is called when game is on loop
   */
  protected GameLoopListener gameLoopListener;

  /**
   * listener which is called when a game is ended
   */
  protected gameStopListener gameStopListener;

  /**
   * hash set used to save coordinates
   */
  private HashSet<GameBlockCoordinate> hashSet;


  /**
   * Create a new game with the specified rows and columns. Creates a corresponding grid model.
   *
   * @param cols number of columns
   * @param rows number of rows
   */
  public Game(int cols, int rows) {
    this.cols = cols;
    this.rows = rows;

    //Create a new grid model to represent the game state
    this.grid = new Grid(cols, rows);
  }

  /**
   * these are 4 getter to get the integer property of the live score level and multiplayer
   *
   * @return lives score level multiplayer
   */
  public IntegerProperty livesProperty() {
    return lives;
  }

  /**
   * the property of score which is used to track the scores in the game
   *
   * @return the score property
   */
  public IntegerProperty scoreProperty() {
    return score;
  }

  /**
   * the property of score which is used to track the level in the game
   *
   * @return the level property
   */
  public IntegerProperty levelProperty() {
    return level;
  }

  /**
   * the property of multiplier
   *
   * @return multiplier property
   */

  public IntegerProperty multiplierProperty() {
    return multiplier;
  }

  /**
   * Start the game
   */
  public void start() {
    logger.info("open a new game");
    initialise();
    loop();
  }

  /**
   * Initialise a new game and set up anything that needs to be done at the start
   */
  public void initialise() {
    logger.info("Initialising game");
    //初始化游戏要先装载piece
    followingPiece = spawnPiece();
    currentPiece = followingPiece;
    followingPiece = spawnPiece();
    //tell listener to do the method it contains
    nextPieceListener.nextPiece(currentPiece, followingPiece);
    //call the nextPieceListener
    service = Executors.newSingleThreadScheduledExecutor();
  }

  /**
   * Handle what should happen when a particular block is clicked
   *
   * @param gameBlock the block that was clicked
   */
  public boolean blockClicked(GameBlock gameBlock) {
    //Get the position of this block
    if (grid.canPlayPiece(currentPiece, gameBlock.getX(), gameBlock.getY())) {
      //if we could play,then play
      grid.playPiece(currentPiece, gameBlock.getX(), gameBlock.getY());
      //load next piece
      return piecePlace();
    } else {
      return false;
    }
  }

  /**
   * we could use this method when place a new piece
   */
  public boolean piecePlace() {
    nextPiece();
    afterPiece();
    resetLoop();
    return true;
  }

  /**
   * Get the grid model inside this game representing the game state of the board
   *
   * @return game grid model
   */
  public Grid getGrid() {
    return grid;
  }

  /**
   * Get the number of columns in this game
   *
   * @return number of columns
   */
  public int getCols() {
    return cols;
  }

  /**
   * Get the number of rows in this game
   *
   * @return number of rows
   */
  public int getRows() {
    return rows;
  }

  /**
   * Create a new piece with random number
   *
   * @return the new created piece
   */
  public GamePiece spawnPiece() {
    Random random = new Random();
    int randomNumber = random.nextInt(15);
    return GamePiece.createPiece(randomNumber);
  }

  /**
   * load next piece and set following piece
   */
  public void nextPiece() {
    //use following piece to replace current piece and update following piece
    currentPiece = followingPiece;
    followingPiece = spawnPiece();
    //tell listener to do the method it contains
    nextPieceListener.nextPiece(currentPiece, followingPiece);
    //call the nextPieceListener
  }


  /**
   * Use after place a piece
   * use a hashset to save the coordinates of the block, if found that one line is full
   * of number which is bigger than 0, we set the flag clear to true ,which means we need to
   * clear the element in this line
   * <p>
   * use GameBlockCoordinate to contain the information of the coordinates of the blocks
   */
  public void afterPiece() {
    hashSet = new HashSet<>();

    int horizontalLines = countCompletedLines(rows, cols, grid::get);
    int verticalLines = countCompletedLines(cols, rows, (i, j) -> grid.get(j, i));

    int lines = horizontalLines + verticalLines;

    if (lines > 0) {
      for (GameBlockCoordinate gameBlockCoordinate : hashSet) {
        grid.set(gameBlockCoordinate.getX(), gameBlockCoordinate.getY(), 0);
      }

      logger.info("clearing!");
      score(lines, hashSet.size());

      //add after applying the multiplier
      this.multiplier.set(multiplier.get() + 1);

      if (lineFadeListener != null) {
        lineFadeListener.lineToClear(hashSet);
        logger.info("line clearing!");
      }
    } else {
      multiplier.set(1);
    }
  }

  /**
   * use to assist the afterPiece
   *
   * @param outer    the out number
   * @param inner    the inner number
   * @param getValue the get value
   * @return the lines we cleared
   */
  private int countCompletedLines(int outer, int inner, BiFunction<Integer, Integer, Integer> getValue) {
    int lines = 0;

    for (int i = 0; i < outer; i++) {
      boolean completed = true;
      for (int j = 0; j < inner; j++) {
        if (getValue.apply(i, j) == 0) {
          completed = false;
          break;
        }
      }
      if (completed) {
        lines++;
        for (int j = 0; j < inner; j++) {
          hashSet.add(new GameBlockCoordinate(i, j));
        }
      }
    }

    return lines;
  }


  /**
   * use this method to count current score
   *
   * @param lines  the lines we cleared
   * @param blocks the blocks we cleared
   */
  public void score(int lines, int blocks) {

    score.set(score.get() + lines * blocks * 10 * this.multiplier.get());
    this.level.set(this.score.get() / 1000);
    multimedia.playAudio("level.wav");
  }

  /**
   * end the timer
   */
  public void endGame() {
    logger.info("we now end the game");
    service.shutdownNow();
  }

  /**
   * rotate the current piece
   */
  public void rotateCurrentPiece() {
    currentPiece.rotate();
  }

  /**
   * swap the current piece with following piece
   */
  public void swapCurrentPiece() {
    GamePiece tempPiece = followingPiece;
    followingPiece = currentPiece;
    currentPiece = tempPiece;
    multimedia.playAudio("rotate.wav");
  }

  /**
   * set the listener for next piece
   *
   * @param nextPieceListener nextPiece listener
   */
  public void setNextPieceListener(NextPieceListener nextPieceListener) {
    this.nextPieceListener = nextPieceListener;
  }

  /**
   * set the listener of line fade
   *
   * @param listener lineFade listener
   */

  public void setLineFadeListener(LineFadeListener listener) {
    this.lineFadeListener = listener;
  }

  /**
   * set game loop listener
   *
   * @param gameLoopListener gameLoop listener
   */
  public void setOnGameLoopListener(GameLoopListener gameLoopListener) {
    this.gameLoopListener = gameLoopListener;
  }

  /**
   * set game end listener
   *
   * @param gameStopListener gameEnd listener
   */
  public void setGameEndListener(gameStopListener gameStopListener) {
    this.gameStopListener = gameStopListener;
  }

  /**
   * get the current piece
   *
   * @return current piece
   */
  public GamePiece getCurrentPiece() {
    return currentPiece;
  }

  /**
   * get next piece
   *
   * @return next piece
   */
  public GamePiece getFollowingPiece() {
    return followingPiece;
  }

  /**
   * get the time delay according to current level
   *
   * @return delay time
   */
  public int getTimerDelay() {
    int delay = 12000 - (500 * level.get());
    return Math.max(delay, 2500);
  }

  /**
   * after fired, let lives-1
   * gameLoop would be called by timer after every time it finish a loop
   */
  public void gameLoop() {
    //discard the current piece
    nextPiece();

    if (lives.get() == 0) {
      //if we have no more lives, we would stop the game
      Platform.runLater(() -> gameStopListener.endGame(this));

    } else {
      updateTime();
    }
    //if not end we need to reset the timeline
    gameLoopListener.gameLoop(getTimerDelay());
    //every time finishing checking, we restart the loop
    loop();
  }

  /**
   * use scheduledFuture to represent a loop
   */
  public void loop() {
    //call the gameLoop to check current status after a specified time
    scheduledFuture = service.schedule(() -> {
      //discard the current piece
      nextPiece();

      if (lives.get() == 0) {
        //if we have no more lives, we would stop the game
        Platform.runLater(() -> gameStopListener.endGame(Game.this));

      } else {
        updateTime();
      }
      //if not end we need to reset the timeline
      gameLoopListener.gameLoop(getTimerDelay());
      //every time finishing checking, we restart the loop
      loop();
    }, getTimerDelay(), TimeUnit.MILLISECONDS);
    gameLoopListener.gameLoop(getTimerDelay());
  }

  /**
   * used to update the time in the game
   */
  public void updateTime() {
    int temp = lives.get();
    lives.set(temp - 1);
    multimedia.playAudio("lifelose.wav");
    multiplier.set(1);
  }

  /**
   * after we put a piece on the board,we reset the loop
   * let the restart
   */
  public void resetLoop() {
    scheduledFuture.cancel(false);
    loop();
  }
}
