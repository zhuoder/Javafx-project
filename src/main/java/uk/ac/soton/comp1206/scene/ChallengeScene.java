package uk.ac.soton.comp1206.scene;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.util.Duration;
import javafx.util.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.component.*;
import uk.ac.soton.comp1206.game.Game;
import uk.ac.soton.comp1206.game.MultiplayerGame;
import uk.ac.soton.comp1206.media.Multimedia;
import uk.ac.soton.comp1206.ui.GamePane;
import uk.ac.soton.comp1206.ui.GameWindow;

import java.io.*;
import java.util.*;

/**
 * The Single Player challenge scene. Holds the UI for the single player challenge mode in the game.
 */
public class ChallengeScene extends BaseScene {

  /**
   * logger for ChallengeScene
   */
  private static final Logger logger = LogManager.getLogger(MenuScene.class);

  /**
   * current game
   */
  protected Game game;
  /**
   * used to play sound in the game
   */
  protected Multimedia multimedia = new Multimedia();

  /**
   * x coordinate of keyBoard control
   */
  protected int currentBlockX = 0;

  /**
   * y coordinate of keyBoard control
   */
  protected int currentBlockY = 0;

  /**
   * the property of X coordinate
   */
  protected IntegerProperty getX = new SimpleIntegerProperty();

  /**
   * the property of Y coordinate
   */
  protected IntegerProperty getY = new SimpleIntegerProperty();
  /**
   * boolean variable used for judge if we could add new piece to our grid
   */
  protected boolean challengeChat = true;

  /**
   * create new piece board which is showed on the side of the screen
   */
  protected PieceBoard pieceBoard = new PieceBoard(3, 3, 100, 100);

  /**
   * create new following piece board which is showed below the pieceBoard
   */
  protected PieceBoard followingPieceBoard = new PieceBoard(3, 3, 70, 70);

  /**
   * main gameBoard
   */
  protected GameBoard board;

  /**
   * create a new rectangle of timer
   */
  protected Rectangle timer;


  /**
   * boolean variable which is used for change the background pictures
   */
  protected boolean imageShow = false;
  /**
   * integer property which used to show the high score value
   */
  public SimpleIntegerProperty highScoreValue = new SimpleIntegerProperty(0);

  /**
   * new stack pane which used as an intermediaries to change the background which response to the setting
   */
  public static StackPane changePane = new StackPane();

  /**
   * main board of showing all the element in this scene
   */
  private StackPane challengePane;
  /**
   * used to set all the element in a correct position
   */
  protected BorderPane mainPane;


  /**
   * Create a new Single Player challenge scene
   *
   * @param gameWindow the Game Window
   */
  public ChallengeScene(GameWindow gameWindow) {
    super(gameWindow);
    logger.info("Creating Challenge Scene");
  }


  /**
   * Build the Challenge window
   */
  @Override
  public void build() {
    logger.info("Building " + this.getClass().getName());

    setupGame();


    root = new GamePane(gameWindow.getWidth(), gameWindow.getHeight());

    challengePane = new StackPane();
    challengePane.setMaxWidth(gameWindow.getWidth());
    challengePane.setMaxHeight(gameWindow.getHeight());
    root.getChildren().add(challengePane);

    //set the challengePane
    Pane darkPane = new StackPane();
    darkPane.getStyleClass().setAll("challenge-background");
    Pane lightPane = new StackPane();
    lightPane.getStyleClass().setAll("challenge-background-light");
    ObservableList<String> challengeStyleClasses = changePane.getStyleClass();
    ObservableList<String> darkStyleClasses = darkPane.getStyleClass();
    ObservableList<String> lightStyleClasses = lightPane.getStyleClass();
    if (challengeStyleClasses.equals(darkStyleClasses)) {
      challengePane.getStyleClass().setAll("challenge-background");
    } else if (challengeStyleClasses.equals((lightStyleClasses))) {
      challengePane.getStyleClass().setAll("challenge-background-light");
    } else {
      challengePane.getStyleClass().setAll("challenge-background");
    }

    board = new GameBoard(game.getGrid(), gameWindow.getWidth() / 2.0, gameWindow.getWidth() / 2.0);
    //bind coordinates with gameBoard
    board.boardXProperty().bind(getX);
    board.boardYProperty().bind(getY);

    //the UI of score
    Text score = new Text("Score: ");
    Text scoreValue = new Text("0");
    scoreValue.textProperty().bind(game.scoreProperty().asString());
    //用于跟踪game类里的scoreProperty，当game类里的property更新后，该类里的几个value也会更新，从而实现
    //显示实时分数的效果
    TextFlow scoreFlow = new TextFlow(score, scoreValue);
    score.getStyleClass().add("heading");
    scoreValue.getStyleClass().add("heading");


    //the UI of level
    Text level = new Text("Level: ");
    Text levelValue = new Text("0");
    levelValue.textProperty().bind(game.levelProperty().asString());
    TextFlow levelFlow = new TextFlow(level, levelValue);
    level.getStyleClass().add("heading");
    levelValue.getStyleClass().add("heading");

    //the UI of lives
    Text lives = new Text("Lives: ");
    Text livesValue = new Text("3");
    livesValue.textProperty().bind(game.livesProperty().asString());
    TextFlow livesFlow = new TextFlow(lives, livesValue);
    lives.getStyleClass().add("heading");
    livesValue.getStyleClass().add("heading");

    //the UI of multiplier
    Text multiplier = new Text("Multiplier: ");
    Text multiplierValue = new Text("1");
    multiplierValue.textProperty().bind(game.multiplierProperty().asString());
    TextFlow multiplierFlow = new TextFlow(multiplier, multiplierValue);
    multiplier.getStyleClass().add("heading");
    multiplierValue.getStyleClass().add("heading");

    HBox statusUI = new HBox(25, scoreFlow, levelFlow, livesFlow, multiplierFlow);
    statusUI.setAlignment(Pos.CENTER);
    challengePane.getChildren().add(statusUI);

    //the UI of highScore

    Text highScoreText = new Text("HighScore: ");
    Text highScoreValue = new Text("0");
    highScoreText.getStyleClass().add("heading");
    highScoreValue.getStyleClass().add("heading");
    highScoreValue.textProperty().bind(this.highScoreValue.asString());
    HBox highScoreHbox = new HBox();
    highScoreHbox.getChildren().addAll(highScoreText, highScoreValue);

    mainPane = new BorderPane();
    challengePane.getChildren().add(mainPane);

    //set onMouseClick, when we click mainBoard for twice,we then change the background picture
    mainPane.setOnMouseClicked(Event -> {
      if (Event.getClickCount() == 2) {
        if (!imageShow) {
          challengePane.getStyleClass().setAll("challenge-background");
          imageShow = true;
        } else {
          challengePane.getStyleClass().setAll("menu-background");
          imageShow = false;
        }
      }
    });


    //set the game area to the center of the viewpoint
    mainPane.setCenter(board);
    //set the hover on main board
    board.Hover();

    //create a VBox to hold the pieceBoard
    VBox pieceBoardVBox = new VBox(20);
    pieceBoardVBox.getChildren().addAll(highScoreHbox, pieceBoard, followingPieceBoard);
    //只在中心的block上设置center为true
    pieceBoard.paintCentre();
    pieceBoardVBox.setAlignment(Pos.CENTER);
    pieceBoardVBox.setTranslateX(-50);
    mainPane.setRight(pieceBoardVBox);

    //Handle block on gameboard grid being clicked
    board.setOnBlockClick(this::blockClicked);

    //implement timer
    timer = new Rectangle(gameWindow.getWidth(), 15);
    StackPane stackPane = new StackPane(timer);
    mainPane.setTop(stackPane);
    stackPane.setTranslateY(30);

    pieceBoard.setOnBlockClick(block -> {
      game.rotateCurrentPiece();
      pieceBoard.setPiece(game.getCurrentPiece());
    });

    board.setOnRightClicked(block -> {
      game.rotateCurrentPiece();
      pieceBoard.setPiece(game.getCurrentPiece());
    });


    //implement the nextPiece listener when nextPiece changes
    //将listener设置在这里是为了向这里的pieceBoard添加piece，针对game的响应,
//而且只有这里才能向pieceBoard添加，并且在此重写方法，将重写的listener传入game类中
//触发条件是重写的方法被调用，参数就是game类调用该重写方法时传入的参数
    game.setNextPieceListener((currentPiece, followingGamePiece) -> {
      pieceBoard.setPiece(currentPiece);
      followingPieceBoard.setPiece(followingGamePiece);
    });

    followingPieceBoard.setOnBlockClick(block -> {
      game.swapCurrentPiece();
      pieceBoard.setPiece(game.getCurrentPiece());
      followingPieceBoard.setPiece(game.getFollowingPiece());
    });

    game.setLineFadeListener(gameBlockCoordinates -> {
      multimedia.playAudio("clear.wav");
      //显示出删除效果
      board.fadeOut(gameBlockCoordinates);
    });
    game.setOnGameLoopListener(time -> {
      timer.widthProperty().set(gameWindow.getWidth());
      Timeline timerBar = createTime(time);
      timerBar.play();
    });
    game.setGameEndListener(game -> {
      logger.info("Game Over");
      timer.setVisible(false);
      game.endGame();
      multimedia.stopPlaying();
      multimedia.playAudio("transition.wav");
      gameWindow.startScore(game);
    });

    game.scoreProperty().addListener((observableValue, number, t1) -> getHighScore());
  }

  /**
   * create a new timeLine
   *
   * @param delay the delay time of timeline
   * @return new line we creat
   */
  private Timeline createTime(int delay) {
    int numColors = 3; // Number of colors to use

    // Duration for each color change
    double durationPerColor = (double) delay / numColors;

    Timeline timeline = new Timeline();

    // Set initial color and width
    timeline.getKeyFrames().add(new KeyFrame(Duration.ZERO,
            new KeyValue(timer.fillProperty(), Color.GREEN)));
    timeline.getKeyFrames().add(new KeyFrame(Duration.ZERO,
            new KeyValue(timer.widthProperty(), timer.getWidth())));

    // Change to yellow color and reduce width to 2/3 over the same duration
    timeline.getKeyFrames().add(new KeyFrame(Duration.millis(durationPerColor),
            new KeyValue(timer.fillProperty(), Color.YELLOW)));
    timeline.getKeyFrames().add(new KeyFrame(Duration.millis(durationPerColor),
            new KeyValue(timer.widthProperty(), timer.getWidth() / 3 * 2)));

    // Change to red color and reduce width to 1/3 over the same duration
    timeline.getKeyFrames().add(new KeyFrame(Duration.millis(2 * durationPerColor),
            new KeyValue(timer.fillProperty(), Color.RED)));
    timeline.getKeyFrames().add(new KeyFrame(Duration.millis(2 * durationPerColor),
            new KeyValue(timer.widthProperty(), timer.getWidth() / 3)));

    // Reduce width to 0 over the remaining duration
    timeline.getKeyFrames().add(new KeyFrame(Duration.millis(delay),
            new KeyValue(timer.widthProperty(), 0)));

    return timeline;
  }

  /**
   * Handle when a block is clicked, this is for set a block on the gameBoard
   *
   * @param gameBlock the Game Block that was clocked
   */
  private void blockClicked(GameBlock gameBlock) {
    if (game.blockClicked(gameBlock)) {
      multimedia.playAudio("place.wav");
    } else {
      multimedia.playAudio("fail.wav");
    }
  }

  /**
   * Set up the game object and model
   */
  public void setupGame() {
    logger.info("Starting a new challenge");

    //Start new game
    game = new Game(5, 5);
  }

  /**
   * Initialise the scene and start the game
   */
  @Override
  public void initialise() {
    logger.info("Initialising Challenge");
    game.start();
    this.multimedia.playBackgroundMusic("game.wav");
    scene.setOnKeyPressed(this::keyboardSetting);
    getHighScore();
  }

  /**
   * end up the game
   */
  public void gameEnd() {
    //end game only when single player
    if (!(game instanceof MultiplayerGame)) {
      game.endGame();
      showEnd();
    }
  }

  /**
   * used to assist gameEnd
   */
  public void showEnd() {
    timer.setVisible(false);
    multimedia.stopPlaying();
    multimedia.playAudio("transition.wav");
    gameWindow.startMenu();
  }

  /**
   * get the current high score
   */
  public void getHighScore() {

    try {
      int highScore;
      File file = new File("scores.txt");
      if (file.exists()) {
        ArrayList<Pair<String, Integer>> scores = new ArrayList<>();
        BufferedReader reader = new BufferedReader(new FileReader(file));
        String line;
        while ((line = reader.readLine()) != null) {
          String[] parts = line.split(":");
          String name = parts[0];
          int score = Integer.parseInt(parts[1]);
          scores.add(new Pair<>(name, score));
        }
        reader.close();
        scores.sort((a, b) -> b.getValue() - a.getValue());
        highScore = scores.get(0).getValue();
      } else {
        File scoreFile = new File("scores.txt");
        try {
          ArrayList<Pair<String, Integer>> scores = new ArrayList<>();
          scores.add(new Pair<>("momo", 300));
          scores.add(new Pair<>("momo", 200));
          scores.add(new Pair<>("lolo", 300));
          scores.add(new Pair<>("zyh", 200));
          scores.add(new Pair<>("lzy", 100));
          scores.add(new Pair<>("lyx", 120));
          scores.add(new Pair<>("bl", 320));
          scores.add(new Pair<>("zjl", 190));
          scores.add(new Pair<>("mage", 120));
          scores.add(new Pair<>("tem", 120));
          BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(file));

          for (Pair<String, Integer> pair : scores) {
            String nameScore = pair.getKey() + ":" + pair.getValue();
            bufferedWriter.write(nameScore);
            bufferedWriter.write("\n");
          }
          bufferedWriter.close();

        } catch (Exception e) {
          e.printStackTrace();
        }
        highScore = 320;
      }
      highScoreValue.set(Math.max(game.scoreProperty().get(), highScore));
    } catch (Exception e) {
      e.printStackTrace();
    }
  }


  /**
   * this is used to bind with scene,which could give feedback to keyboard input
   *
   * @param keyEvent the input key event
   */
  public void keyboardSetting(KeyEvent keyEvent) {
    int oldBlockX = currentBlockX;
    int oldBlockY = currentBlockY;
    boolean ifMoved = false;

    switch (keyEvent.getCode()) {
      case ESCAPE -> {
        gameEnd();
        logger.info("Escape Pressed");
      }
      case Q, Z, OPEN_BRACKET -> {
        for (int i = 0; i < 3; i++) {
          game.rotateCurrentPiece(); // Rotates the current piece left
        }
        pieceBoard.setPiece(game.getCurrentPiece());
      }
      case E, C, CLOSE_BRACKET -> {
        game.rotateCurrentPiece(); // Rotates the current piece right
        pieceBoard.setPiece(game.getCurrentPiece());
      }
      case SPACE, R -> {
        game.swapCurrentPiece();
        pieceBoard.setPiece(game.getCurrentPiece());
        followingPieceBoard.setPiece(game.getFollowingPiece());
      }
      case ENTER, X -> {
        if (challengeChat) {
          blockClicked(board.getBlock(currentBlockX, currentBlockY)); //Clicks piece
        }
      }
      case UP, W -> {
        logger.info("move up");
        if (currentBlockY > 0) {
          currentBlockY = currentBlockY - 1;
          getY.set(currentBlockY);
          ifMoved = true;
        } else {
          multimedia.playAudio("fail.wav");
        }
      }
      case LEFT, A -> {
        logger.info("move left");
        if (currentBlockX > 0) {
          currentBlockX = currentBlockX - 1;
          getX.set(currentBlockX);
          ifMoved = true;
        } else {
          multimedia.playAudio("fail.wav");
        }
      }
      case DOWN, S -> {
        logger.info("move down");
        if (currentBlockY < 4) {
          currentBlockY = currentBlockY + 1;
          getY.set(currentBlockY);
          ifMoved = true;
        } else {
          multimedia.playAudio("fail.wav");
        }
      }
      case RIGHT, D -> {
        logger.info("move right");
        if (currentBlockX < 4) {
          currentBlockX = currentBlockX + 1;
          getX.set(currentBlockX);
          ifMoved = true;
        } else {
          multimedia.playAudio("fail.wav");
        }
      }
      default -> {
      }
    }

    if (ifMoved) {
      for (int i = 0; i < board.getBlocks().length; i++) {
        for (int j = 0; j < board.getBlocks()[i].length; j++) {
          if (board.getBlocks()[i][j].getHover()) {
            board.getBlocks()[i][j].paint();
          }
        }
      }

      board.getBlock(oldBlockX, oldBlockY).paint(); //Removes cursor from previous grid position
      board.getBlock(currentBlockX, currentBlockY).hoverPaint(); //Adds cursor to current grid position
    }
  }
}