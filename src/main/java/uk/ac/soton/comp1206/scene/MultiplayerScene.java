package uk.ac.soton.comp1206.scene;

import javafx.application.Platform;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Pos;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.util.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.component.Leaderboard;
import uk.ac.soton.comp1206.game.MultiplayerGame;
import uk.ac.soton.comp1206.network.Communicator;
import uk.ac.soton.comp1206.ui.GameWindow;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * multiplayerScene which is used to show a multi-game scene, this could show all the useful UI in a multi-game
 */
public class MultiplayerScene extends ChallengeScene {
  /**
   * the logger of this class
   */
  private static final Logger logger = LogManager.getLogger(MultiplayerScene.class);
  /**
   * Communicator used to send messages to the server, and receive messages from the server
   */
  protected Communicator communicator;

  /**
   * Holds scores of all players in the game
   */
  protected SimpleListProperty<Pair<String, Integer>> multiplayerScores = new SimpleListProperty<>();

  private SimpleListProperty<Pair<String, Integer>> submitScores = new SimpleListProperty<>();

  /**
   * TextField used as a chat input
   */
  protected TextField textField = new TextField();

  /**
   * use to hold all the chat message in playing game
   */
  protected VBox chatBox;

  /**
   * used to show the side board which could show all players' status
   */
  protected Leaderboard leaderboard;
  /**
   * the name of the player
   */
  private String name;
  /**
   * the VBox which contains lives
   */
  private VBox livesBox;

  /**
   * the set of gamers
   */
  protected HashSet<String> gamerSet;
  /**
   * used to send scores continuously
   */
  private ScheduledThreadPoolExecutor scheduledThreadPoolExecutor = new ScheduledThreadPoolExecutor(1);

  /**
   * Create a multiPlayer challenge scene
   *
   * @param gameWindow the Game Window
   */
  public MultiplayerScene(GameWindow gameWindow, HashSet<String> gamerSet, String name) {
    super(gameWindow);
    this.multiplayerScores.set(FXCollections.observableArrayList(new ArrayList<>()));
    this.submitScores.set(FXCollections.observableArrayList(new ArrayList<>()));
    this.gamerSet = gamerSet;
    this.name = name;
  }

  /**
   * initialise the scene
   */
  @Override
  public void initialise() {
    super.initialise();
    communicator = gameWindow.getCommunicator();
    communicator.addListener(message -> Platform.runLater(() -> handleListen(message.trim())));
    //we have ask for online scores when we create the
    scheduledThreadPoolExecutor.scheduleAtFixedRate(() -> communicator.send("SCORES"), 1000, 2000, TimeUnit.MILLISECONDS);

  }

  /**
   * build the scene
   */
  @Override
  public void build() {
    super.build();

    //add a pane to hold all the messages chatting in the game
    var messagesPane = new BorderPane();
    messagesPane.setPrefSize(gameWindow.getWidth() / 6.0, gameWindow.getHeight() / 6.0);
    var scrollPane = new ScrollPane();
    scrollPane.getStyleClass().add("scroller");
    scrollPane.setPrefSize(messagesPane.getWidth(), messagesPane.getHeight());
    chatBox = new VBox();
    chatBox.getStyleClass().add("messages");
    chatBox.setPrefSize(scrollPane.getPrefWidth(), scrollPane.getPrefHeight());
    scrollPane.setContent(chatBox);
    messagesPane.setCenter(scrollPane);

    //add a field to chat in game
    var chatTitle = new Text("Press T To Chat");
    chatTitle.setTextAlignment(TextAlignment.CENTER);
    chatTitle.getStyleClass().add("heading");

    var chatBox = new HBox(chatTitle, textField);
    chatBox.setMaxWidth(gameWindow.getWidth());
    textField.setPrefWidth(chatTitle.getLayoutBounds().getWidth() + 50);
    //we can't see it at first,only when we type q ,we could see it
    textField.setVisible(false);
    textField.setAlignment(Pos.CENTER);


    var chatField = new VBox(chatBox, messagesPane);

    //add a new scoreList to show everyone's score
    leaderboard = new Leaderboard();
    leaderboard.setAlignment(Pos.CENTER);
    this.multiplayerScores.bind(leaderboard.localProperty());

    HBox scoreLives = new HBox();
    livesBox = new VBox();
    scoreLives.getChildren().addAll(leaderboard, livesBox);


    var sideUI = new VBox(20);
    sideUI.getChildren().addAll(scoreLives, pieceBoard, followingPieceBoard);
    sideUI.setAlignment(Pos.CENTER_RIGHT);
    mainPane.setRight(sideUI);
    mainPane.setBottom(chatField);

    //Setting GameEndListener
    game.setGameEndListener(game -> {
      gameEnd();
      game.endGame();
      communicator.send("DIE");
      timer.setVisible(false);
      multimedia.stopPlaying();
      multimedia.playAudio("transition.wav");
      gameWindow.loadScene(new ScoresScene(gameWindow, game, this.submitScores, name));
    });

  }


  /**
   * handle the received message
   *
   * @param message the received messages from server
   */
  public void handleListen(String message) {

    if (message.contains("MSG")) {
      //add received message to box
      message = message.replace("MSG", "");
      Text messageText = new Text(message);
      messageText.getStyleClass().add("messages Text");
      chatBox.getChildren().add(messageText);
    } else if (message.contains("SCORES")) {
      //if we received online scores
      message = message.replace("SCORES ", "");
      String[] scoreArr = message.split("\n");
      multiplayerScores.clear();
      submitScores.clear();
      for (String s : scoreArr) {
        String[] state = s.split(":");

        var submit = new Pair<>(state[0], Integer.parseInt(state[1]));
        submitScores.add(submit);

        if (s.contains("DEAD")) {
          leaderboard.cross(state[0] + ": " + state[1] + " lives: ");
          var entry = new Pair<>(state[0] + ": " + state[1] + " lives: ", 0);
          this.multiplayerScores.add(entry);
        } else {
          var entry = new Pair<>(state[0] + ": " + state[1] + " lives: ", Integer.parseInt(state[2]));
          this.multiplayerScores.add(entry);
        }

      }
    }
  }

  /**
   * create a new multi-game
   */
  @Override
  public void setupGame() {
    game = new MultiplayerGame(5, 5, this.gameWindow);
    logger.info("Starting a new multiplayer game");
  }

  /**
   * override the keyboardInput,which could handle chat in playing game
   *
   * @param keyEvent the input key event
   */
  @Override
  public void keyboardSetting(KeyEvent keyEvent) {
    super.keyboardSetting(keyEvent);
    KeyCode keyCode = keyEvent.getCode();
    switch (keyCode) {
      case T -> {
        if (!textField.isVisible()) {
          textField.setVisible(true);
          String message = textField.getText();
          if (!message.trim().isEmpty()) { // Check if the trimmed text is not empty
            communicator.send("MSG " + message);
            textField.clear();
          }
        } else {
          textField.setVisible(false);
          textField.clear();
        }
        challengeChat = false;
      }
      case ESCAPE -> {
        logger.info("Escape Pressed");
        multimedia.stopPlaying();
        game.endGame();
        multimedia.playAudio("transition.wav");
        gameEnd();
        gameWindow.startMenu();
        communicator.send("DIE");
      }
      case ENTER -> {
        if (textField.isVisible()) {
          String message = textField.getText();
          if (message != null) {
            communicator.send("MSG " + message);
            textField.clear();
          }
          textField.setVisible(false);
        }
        challengeChat = true;
      }
      default -> {
      }
      // Do nothing for other key events
    }
  }

  /**
   * override of the gameEnd
   */
  @Override
  public void gameEnd() {
    //end game only when single player
    if (!(game instanceof MultiplayerGame)) {
      game.endGame();
      timer.setVisible(false);
      multimedia.stopPlaying();
      multimedia.playAudio("transition.wav");
      gameWindow.startMenu();
    }
    scheduledThreadPoolExecutor.shutdown();
  }
}