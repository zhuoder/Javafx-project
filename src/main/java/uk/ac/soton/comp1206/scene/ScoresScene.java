package uk.ac.soton.comp1206.scene;

import javafx.application.Platform;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Pos;
import javafx.scene.control.TextInputDialog;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.util.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.component.ScoresList;
import uk.ac.soton.comp1206.game.Game;
import uk.ac.soton.comp1206.game.MultiplayerGame;
import uk.ac.soton.comp1206.media.Multimedia;
import uk.ac.soton.comp1206.network.Communicator;
import uk.ac.soton.comp1206.ui.GamePane;
import uk.ac.soton.comp1206.ui.GameWindow;


import java.io.*;
import java.util.ArrayList;
import java.util.Optional;

/**
 * this class is used to show the score list after a game
 */
public class ScoresScene extends BaseScene {

  /**
   * the game we just finished
   */
  private Game game;

  /**
   * score we just get in game we just finished
   */
  private int scores;

  /**
   * the name of the player
   */
  private String name;
  /**
   * object of multimedia
   */
  private Multimedia multimedia = new Multimedia();

  /**
   * logger of score scene
   */
  private Logger logger = LogManager.getLogger(ScoresScene.class);

  /**
   * new communicator
   */
  private Communicator communicator;

  /**
   * used to hold the scores of all players in the game we just finished
   */
  private SimpleListProperty<Pair<String, Integer>> multiplayerScores;


  /**
   * used to hold online scores
   */
  private ScoresList onlineScoreList;

  /**
   * record the local scores
   */
  private SimpleListProperty<Pair<String, Integer>> localScores = new SimpleListProperty<>();

  /**
   * record the online scores
   */
  private SimpleListProperty<Pair<String, Integer>> remoteScores = new SimpleListProperty<>();


  /**
   * used to show local scores
   */
  private VBox localScore;

  /**
   * used to show online scores
   */
  private VBox onlineScore;


  /**
   * Create a new scene, passing in the GameWindow the scene will be displayed in
   *
   * @param gameWindow the game window
   */
  public ScoresScene(GameWindow gameWindow, Game game) {
    super(gameWindow);
    logger.info("new score scene");
    this.game = game;
    //get the game score
    scores = game.scoreProperty().get();
    //set score property to observable property
    localScores.set(FXCollections.observableArrayList(new ArrayList<>()));
    remoteScores.set(FXCollections.observableArrayList(new ArrayList<>()));
    communicator = gameWindow.getCommunicator();
  }

  /**
   * the constructor which contains three paras, which is to load the online scores
   *
   * @param gameWindow gameWindow
   * @param game       the game just finished
   * @param scores     the online score
   */
  public ScoresScene(GameWindow gameWindow, Game game, SimpleListProperty<Pair<String, Integer>> scores, String name) {
    super(gameWindow);
    this.game = game;
    this.scores = game.scoreProperty().get();
    //Sets the local ScoreList to the scores in multiplayer
    this.localScores.set(FXCollections.observableArrayList(new ArrayList<>()));
    this.remoteScores.set(FXCollections.observableArrayList(new ArrayList<>()));
    multiplayerScores = scores;
    this.name = name;
    logger.info("Creating Scores Scene");
    communicator = gameWindow.getCommunicator();

  }


  /**
   * used to initialize the scoreScene
   */
  @Override
  public void initialise() {
    multimedia.playAudio("explode.wav");
    multimedia.playBackgroundMusic("end.wav");
    //after we build the scene,we would initialise the scene,we have to load the previous data and show

    if (!(game instanceof MultiplayerGame)) {
      //if we are in single player mode,we would load local data from the file
      loadScores();
      addCurrentScore(name, scores);
    } else {
      //else we are in multiPlayer game, we would load online score and sort them manually
      //the online scores here is essentially the scores of every gamer in one game
      this.localScores.addAll(multiplayerScores);
      this.localScores.sort((a, b) -> b.getValue() - a.getValue());


    }
    scene.setOnKeyPressed(keyEvent -> {
      if (keyEvent.getCode() == KeyCode.ESCAPE) {
        multimedia.playAudio("transition.wav");
        multimedia.stopPlaying();
        gameWindow.startMenu();
        logger.info("Escape pressed");
        if (!(game instanceof MultiplayerGame)) {
          communicator.send("QUIT");
        }
      }
    });

    loadOnlineScores();

    //add a new listener to communicator which send us online score list with correspond protocol
    communicator.addListener(message -> Platform.runLater(() -> receiveCommunication(message.trim())));

  }

  /**
   * used to build the scene
   */
  @Override
  public void build() {

    logger.info("Building " + this.getClass().getName());

    root = new GamePane(gameWindow.getWidth(), gameWindow.getHeight());

    var scorePane = new StackPane();
    scorePane.setMaxWidth(gameWindow.getWidth());
    scorePane.setMaxHeight(gameWindow.getHeight());
    scorePane.getStyleClass().add("menu-background");
    root.getChildren().add(scorePane);

    //Contains both scoreLists
    var scores = new HBox();
    scores.setAlignment(Pos.CENTER);
    scorePane.getChildren().add(scores);

    //Local leaderboard
    localScore = new VBox();
    localScore.setAlignment(Pos.CENTER);
    scores.getChildren().add(localScore);


    //Public leaderboard on the server
    onlineScore = new VBox();
    onlineScore.setAlignment(Pos.CENTER);
    scores.getChildren().add(onlineScore);

    //Game over title text
    Text highScores = new Text("High Scores");
    highScores.setTextAlignment(TextAlignment.CENTER);
    highScores.getStyleClass().add("title");
    scorePane.getChildren().add(highScores);
    StackPane.setAlignment(highScores, Pos.TOP_CENTER);

    //Local leaderboard text
    var scoreText = new Text("Local Scores");
    scoreText.getStyleClass().add("heading");
    localScore.getChildren().add(scoreText);
    scoreText.setTranslateX(-70);

    //local leaderboard scoreList
    var scoresList = new ScoresList();
    localScore.getChildren().add(scoresList);
    scoresList.setAlignment(Pos.CENTER);
    scoresList.setTranslateX(-70);
    this.localScores.bind(scoresList.localProperty());

    //Online leaderboard text and list
    var onlineScoreText = new Text("Online Scores");
    onlineScoreText.getStyleClass().add("heading");
    onlineScoreText.setTranslateX(70);
    onlineScoreList = new ScoresList();
    onlineScoreList.setAlignment(Pos.CENTER);
    onlineScoreList.setTranslateX(70);
    onlineScore.getChildren().add(onlineScoreText);
    onlineScore.getChildren().add(onlineScoreList);
    //bind remoteScores with onlineScoreList property,therefore,there could share the list data
    //and shown on the scene
    remoteScores.bind(onlineScoreList.localProperty());

    localScore.setVisible(false);
    onlineScore.setVisible(false);

  }

  /**
   * when game ends, we load the previous game data from a file
   */
  public void loadScores() {
    File file = new File("scores.txt");
    try {
      if (!file.exists()) {
        writeScores();
      } else {
        BufferedReader reader = new BufferedReader(new FileReader(file));
        String line;
        while ((line = reader.readLine()) != null) {
          String[] nameScore = line.split(":");
          String name = nameScore[0];
          int score = Integer.parseInt(nameScore[1]);
          Pair<String, Integer> entry = new Pair<>(name, score);
          localScores.add(entry);
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  /**
   * if we don't have a file, we create a new file and write
   * the default data in the file ,called by the loadScore method.
   */
  public void writeScores() {
    File file = new File("scores.txt");
    try {
      ArrayList<Pair<String, Integer>> scores = new ArrayList<>();
      scores.add(new Pair<>("momo", 1000));
      scores.add(new Pair<>("momo", 1200));
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
        localScores.add(pair);
        bufferedWriter.write(nameScore);
        bufferedWriter.write("\n");
      }
      bufferedWriter.close();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }


  /**
   * the loadScore above could only load previous data,for the game data we have just generated,
   * we have to use this method and sort the whole data
   *
   * @param name  username of the score
   * @param score current score
   */
  public void addCurrentScore(String name, int score) {
    File file = new File("scores.txt");
    try {
      //write data to score.txt
      BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(file, true));

      //sort the score after adding current scores
      this.localScores.sort((a, b) -> b.getValue() - a.getValue());

      if (scores > localScores.get(localScores.size() - 1).getValue()) {
        if (!(game instanceof MultiplayerGame)) {
          var nameDialog = new TextInputDialog();
          nameDialog.setTitle("New Score");
          nameDialog.setContentText("Enter Your Name");
          Optional<String> result = nameDialog.showAndWait();
          this.name = result.orElse("mage");
        }
        bufferedWriter.write(this.name + ":" + score);
        bufferedWriter.write("\n");
        this.localScores.add(new Pair<>(this.name, this.scores));
        //sort again
        this.localScores.sort((a, b) -> b.getValue() - a.getValue());
        //Asks the user for their name

      }
      bufferedWriter.close();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  /**
   * load online scores
   */
  public void loadOnlineScores() {
    communicator.send("HISCORES");
  }

  /**
   * write local scores to online server
   */
  public void writeOnlineScore() {
    logger.info("send my high score");
    communicator.send("HISCORE " + name + ":" + scores);
  }

  /**
   * handle received messages
   *
   * @param message received messages
   */
  public void receiveCommunication(String message) {
    try {

      if (message.contains("HISCORES")) {
        //replace the HISCORE with empty,therefore we could split it easier
        message = message.replace("HISCORES", "");
        //use \n as separator
        String[] pairs = message.split("\n");

        for (String pair : pairs) {
          //adds scores and name to remoteScoresList
          String[] scoreName = pair.split(":");
          //for every pair in the pairs,we use : as separator and put them into remoteScore
          remoteScores.add(new Pair<>(scoreName[0], Integer.parseInt(scoreName[1])));
        }
      }
      if (message.contains("NEWSCORE")) {
        remoteScores.clear();
        loadOnlineScores();
        scores = 0;
      } else if (remoteScores.get(remoteScores.size() - 1).getValue() < scores) {
        if (!(game instanceof MultiplayerGame)) {
          writeOnlineScore();
          logger.info("send current new high score");
        }
        // If the score is greater than the lowest score on the online score list
        // we would add the current data into online high score
        localScore.setVisible(true);
        onlineScore.setVisible(true);
      } else {
        localScore.setVisible(true);
        onlineScore.setVisible(true);
      }
    } catch (Exception e) {
      System.out.println("Exception ignored: " + e.getMessage());
    }
  }
}


