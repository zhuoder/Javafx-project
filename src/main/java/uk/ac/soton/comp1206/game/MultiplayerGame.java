package uk.ac.soton.comp1206.game;

import javafx.application.Platform;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.network.Communicator;
import uk.ac.soton.comp1206.ui.GameWindow;

import java.util.LinkedList;
import java.util.concurrent.Executors;

/**
 * this class is used for game logic of multi-game
 */
public class MultiplayerGame extends Game {

  /**
   * the logger of this class
   */
  private static final Logger logger = LogManager.getLogger(MultiplayerGame.class);

  /**
   * gameWindow used to set communicator
   */
  private GameWindow gameWindow;

  /**
   * communicator used to send and receive messages
   */
  protected Communicator communicator;

  /**
   * queue of linked list used to saving game piece
   */
  protected LinkedList<GamePiece> queue;


  /**
   * Create a new game with the specified rows and columns. Creates a corresponding grid model.
   *
   * @param cols number of columns
   * @param rows number of rows
   */
  public MultiplayerGame(int cols, int rows, GameWindow gameWindow) {
    super(cols, rows);
    this.gameWindow = gameWindow;
  }

  /**
   * override initialise method
   */
  @Override
  public void initialise() {
    queue = new LinkedList<>();
    service = Executors.newSingleThreadScheduledExecutor();
    communicator = gameWindow.getCommunicator();
    communicator.send("PIECE");
    communicator.send("PIECE");
    communicator.send("PIECE");
    communicator.send("PIECE");
    communicator.send("PIECE");
    communicator.send("PIECE");
    communicator.send("PIECE");
    //Listens for messages from communicator and handles the command
    communicator.addListener(message -> Platform.runLater(() -> messageHandle(message)));

  }

  /**
   * handle the received messages
   *
   * @param message the message from server
   */
  protected void messageHandle(String message) {
    message = message.trim();

    if (message.contains("PIECE")) {
      logger.info("Adding new piece");
      message = message.replace("PIECE ", "");
      //create new value the server gives
      int number = Integer.parseInt(message);
      GamePiece gamePiece = GamePiece.createPiece(number);
//      addNewPiece(gamePiece);
      if (currentPiece == null) {
        currentPiece = gamePiece; //First Piece
      } else if (followingPiece == null) {
        //if we have the currentPiece but don't have the following piece,we set the following piece with new piece
        followingPiece(gamePiece);
      } else if (currentPiece != null) {
        // we have both piece,we add it to the queue,waiting to add to the pieceBoard
        queue.add(gamePiece);//Creates Queue
      }
    }
  }


  /**
   * used to assist addNewPiece
   *
   * @param piece the following piece
   */
  public void followingPiece(GamePiece piece) {
    followingPiece = piece; //Second Piece
    //update pieceBoard
    nextPieceListener.nextPiece(currentPiece, followingPiece);
  }


  /**
   * override the old method which could load followingPiece with the piece in the queue
   */
  @Override
  public void nextPiece() {
    currentPiece = followingPiece;
    //we set followingPiece with queue
    next();
  }

  /**
   * assist nextPiece to get next piece
   */
  public void next() {
    followingPiece = queue.remove();
    //update the pieceBoard
    nextPieceListener.nextPiece(currentPiece, followingPiece);
    //request for the next piece
    communicator.send("PIECE");
  }


  /**
   * override the score,similar to old one,but send the score to server
   *
   * @param lines  the number of lines we cleared
   * @param blocks the number of blocks we cleared
   */
  @Override
  public void score(int lines, int blocks) {
    //if someone gain a new score, they have to send it to server
    super.score(lines, blocks);
    //update my current score
    communicator.send("SCORE " + this.scoreProperty().get());
    logger.info("our score gonna send is " + this.scoreProperty().get());

  }

  /**
   * override the gameLoop because we have to send lives to update the current lives of the game
   */
  @Override
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
}
