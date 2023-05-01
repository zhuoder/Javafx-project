package uk.ac.soton.comp1206.scene;


import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.media.Multimedia;
import uk.ac.soton.comp1206.network.Communicator;
import uk.ac.soton.comp1206.ui.GamePane;
import uk.ac.soton.comp1206.ui.GameWindow;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;


/**
 * this class is lobbyScene which is used to show the lobby of the game and provide a entrance to
 * play with others and join or host a multi-game
 */
public class LobbyScene extends BaseScene {
  /**
   * the logger of lobbyScene
   */
  private static final Logger logger = LogManager.getLogger(LobbyScene.class);
  /**
   * used to send message constantly
   */
  protected Timer timer;

  /**
   * communicator used
   */
  protected Communicator communicator;
  /**
   * show current available channel
   */
  protected Text channelTitle;

  /**
   * show current channel
   */
  protected VBox channelBox = new VBox();
  /**
   * show the message in the channel
   */
  protected VBox messages;

  /**
   * button used to change the name of the player
   */
  protected Button nickNameChange;
  /**
   * object of multimedia
   */
  protected Multimedia multimedia = new Multimedia();

  /**
   * button for start a game
   */
  protected Button startGame;

  /**
   * button used for exit the channel
   */
  protected Button exitChannel;

  /**
   * create new border pane
   */
  protected BorderPane borderPane;

  /**
   * stack pane used to hold all the message
   */
  protected StackPane messagesPane;

  /**
   * hBox used to set the send field
   */
  protected HBox sendField;
  /**
   * nickname of the player
   */
  protected String nickName;
  /**
   * show the UI inside the channel
   */
  protected VBox channelUI;

  /**
   * list of current players
   */
  protected VBox gamerList;
  /**
   * the set of all the gamers
   */
  protected HashSet<String> gamers = new HashSet<>();

  /**
   * the main pane which used to hold all the element
   */
  private StackPane lobbyPane;

  /**
   * changePane used for change the background
   */
  public static StackPane changePane = new StackPane();

  /**
   * Create a new scene, passing in the GameWindow the scene will be displayed in
   *
   * @param gameWindow the game window
   */
  public LobbyScene(GameWindow gameWindow) {
    super(gameWindow);
    communicator = gameWindow.getCommunicator();
  }

  /**
   * initialise the scene
   */
  @Override
  public void initialise() {
    multimedia.playBackgroundMusic("menu.mp3");
    timer = new Timer();
    //ask for channel from time to time
    timer.scheduleAtFixedRate(new TimerTask() {
      //ask for channel from serve every 2 seconds
      @Override
      public void run() {
        Platform.runLater(() -> communicator.send("LIST"));
      }
    }, 20, 1000);
    //add message to communicator to handle received messages
    communicator.addListener(message -> Platform.runLater(() -> handleListen(message.trim())));

    scene.setOnKeyPressed(keyEvent -> {
      if (keyEvent.getCode() == KeyCode.ESCAPE) {
        communicator.send("PART");
        messages.getChildren().clear();
        exitChannel.setVisible(false);
        nickNameChange.setVisible(false);
        sendField.setVisible(false);
        messagesPane.setVisible(false);
        gamerList.setVisible(false);
        startGame.setVisible(false);
        channelUI.setVisible(false);
        channelTitle.setText(" ");
        logger.info("Escape Pressed");
        gameWindow.startMenu();
      }
    });

  }

  /**
   * build the scene
   */
  @Override
  public void build() {
    logger.info("Building " + this.getClass().getName());

    root = new GamePane(gameWindow.getWidth(), gameWindow.getHeight());

    lobbyPane = new StackPane();
    lobbyPane.setMaxWidth(gameWindow.getWidth());
    lobbyPane.setMaxHeight(gameWindow.getHeight());
    root.getChildren().add(lobbyPane);

    borderPane = new BorderPane();
    borderPane.getStyleClass().add("lobbyBackground");
    lobbyPane.getChildren().add(borderPane);
    borderPane.setMaxSize(lobbyPane.getMaxWidth(), lobbyPane.getMaxHeight());


    Pane darkPane = new StackPane();
    darkPane.getStyleClass().setAll("lobby-background");
    Pane lightPane = new StackPane();
    lightPane.getStyleClass().setAll("lobby-background-light");
    ObservableList<String> challengeStyleClasses = changePane.getStyleClass();
    ObservableList<String> darkStyleClasses = darkPane.getStyleClass();
    ObservableList<String> lightStyleClasses = lightPane.getStyleClass();
    if (challengeStyleClasses.equals(darkStyleClasses)) {
      borderPane.getStyleClass().setAll("lobby-background");
    } else if (challengeStyleClasses.equals((lightStyleClasses))) {
      borderPane.getStyleClass().setAll("lobby-background-light");
    } else {
      borderPane.getStyleClass().setAll("lobby-background");
    }


    //set new UI for the channel
    VBox channel = new VBox(20);
    borderPane.setLeft(channel);
    channel.setTranslateX(30);

    //add new start button to open a channel
    Button start = new Button("Start");
    start.getStyleClass().add("menuItem");

    //use HBox to hold start button and textField which is used to get the name of channel
    HBox startBox = new HBox(10);
    startBox.getChildren().add(start);
    TextField startField = new TextField();
    startField.setPromptText("enter your channel's name");
    startBox.getChildren().add(startField);
    startField.setVisible(false);
    startField.getStyleClass().add("TextField");
    startField.setOnKeyPressed(keyEvent -> {
      if (keyEvent.getCode() == KeyCode.ENTER) {
        String text = startField.getText();
        startField.clear();
        communicator.send("CREATE " + text);
        startField.setVisible(false);
      }
    });

    start.setOnAction(event -> startField.setVisible(true));

    var scrollChannelPane = new ScrollPane();
    scrollChannelPane.getStyleClass().add("scroller");
    scrollChannelPane.setPrefSize(channelBox.getWidth(), 600);

    scrollChannelPane.setContent(channelBox);

    channel.getChildren().addAll(startBox, scrollChannelPane);

    nickNameChange = new Button("NickName");
    nickNameChange.setOnAction(event -> {
      var dialog = new TextInputDialog();
      dialog.setTitle("Nickname");
      dialog.setContentText("Enter your Nickname: ");
      Optional<String> nickName = dialog.showAndWait();
      nickName.ifPresent(string -> communicator.send("NICK " + string));
    });

    exitChannel = new Button("Leave");
    exitChannel.setOnAction(event -> {
      communicator.send("PART");
      messages.getChildren().clear();
      exitChannel.setVisible(false);
      nickNameChange.setVisible(false);
      sendField.setVisible(false);
      messagesPane.setVisible(false);
      gamerList.setVisible(false);
      startGame.setVisible(false);
      channelUI.setVisible(false);
      channelTitle.setText(" ");
    });


    channelUI = new VBox();
    channelUI.setSpacing(10);
    VBox channelUp = new VBox();
    channelUp.getStyleClass().add("gameBox");
    VBox channelDown = new VBox();
    channelDown.getStyleClass().add("gameBox");

    channelUI.getStyleClass().add("gameBox");
    borderPane.setRight(channelUI);


    //use to hold the messages
    messagesPane = new StackPane();
    messagesPane.setPrefSize(gameWindow.getWidth() / 2.50, gameWindow.getHeight() / 2.50);

    //ScrollPane allows for chat to scroll down
    var scrollPane = new ScrollPane();
    scrollPane.getStyleClass().add("scroller");
    scrollPane.setPrefSize(messagesPane.getWidth(), messagesPane.getHeight());

    messages = new VBox();
    messages.getStyleClass().add("messages");
    messages.setPrefSize(scrollPane.getPrefWidth(), scrollPane.getPrefHeight());
    //let scrollPane to scroll vertically
    VBox.setVgrow(scrollPane, Priority.ALWAYS);

    scrollPane.setContent(messages);
    scrollPane.setFitToWidth(true);
    messagesPane.getChildren().add(scrollPane);
    TextField messageToSend = new TextField();
    messageToSend.getStyleClass().add("text-field");
    var sendMessage = new Button("Send");
    sendMessage.getStyleClass().add("lobbyButton");
    messageToSend.setOnKeyPressed(keyEvent -> {
      if (keyEvent.getCode() == KeyCode.ENTER) {
        String message = messageToSend.getText();
        multimedia.playAudio("message.wav");
        if (message != null) {
          //if it's not empty,we send the message to communicator
          communicator.send("MSG " + message);
          messageToSend.clear();
        }
      }
    });
    //set send button to send message
    sendMessage.setOnAction(event -> {
      String message = messageToSend.getText();
      if (message != null) {
        communicator.send("MSG " + message);
        messageToSend.clear();
      }
    });

    //use a HBox to construct send field
    sendField = new HBox();
    sendField.getChildren().addAll(messageToSend, sendMessage);
    HBox.setHgrow(messageToSend, Priority.ALWAYS);

    //add new startGame button
    startGame = new Button("Start Game");
    startGame.getStyleClass().add("startButton");
    startGame.setAlignment(Pos.CENTER);
    startGame.setOnAction(event -> {
      //when we press the start button,we then send the message and open a game as a host
      communicator.send("START");
    });
    //initialise the title
    channelTitle = new Text();
    channelTitle.getStyleClass().add("heading");
    //initialise the gamerList
    gamerList = new VBox(5);

    HBox exitNick = new HBox(10, exitChannel, nickNameChange, startGame);
    exitNick.setAlignment(Pos.CENTER);
    exitChannel.getStyleClass().add("lobbyButton");
    nickNameChange.getStyleClass().add("lobbyButton");
    exitNick.getStyleClass().add("");

    channelUp.getChildren().addAll(channelTitle, exitNick);
    channelDown.getChildren().addAll(messagesPane, sendField);


    channelUI.getChildren().addAll(channelUp, channelDown, gamerList);
    channelUp.setTranslateY(-50);
    channelDown.setTranslateY(-50);
    gamerList.setTranslateY(-50);
    channelUI.setAlignment(Pos.CENTER);
    //at first the UI should be invisible
    channelUI.setVisible(false);


  }

  /**
   * used to handle the received messages
   *
   * @param message received messages
   */
  public void handleListen(String message) {


    if (message.contains("CHANNELS")) { //displays all channels available
      channelBox.getChildren().clear();
      message = message.replace("CHANNELS", "").trim();
      String[] channelArray = message.split("\n");

      for (String channel : channelArray) {
        Text textChannel = new Text(channel);
        textChannel.setOnMouseClicked(mouseEvent -> communicator.send("JOIN " + channel));

        textChannel.setOnMouseEntered(mouseEvent -> textChannel.setStyle("-fx-text-fill: blue"));
        textChannel.setOnMouseExited(mouseEvent -> textChannel.setStyle("-fx-text-fill: white"));

        textChannel.getStyleClass().add("channelItem");
        channelBox.getChildren().add(textChannel);
      }
    } else if (message.contains("JOIN")) {
      //join the channel
      String[] joinArray = message.split(" ");
      Join(joinArray[1]);
    } else if (message.contains("MSG")) {
      message = message.replace("MSG ", "");
      LocalTime currentTime = LocalTime.now();
      DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
      String formattedTime = currentTime.format(formatter);
      String messageSend = "[" + formattedTime + "] " + message;
      Text messageText = new Text(messageSend);
      messages.getChildren().add(messageText);
    } else if (message.contains("HOST")) {
      //we are the host,therefore we could start a game
      startGame.setVisible(true);
    } else if (message.contains("USERS")) {
      message = message.replace("USERS ", "");
      showGamer(message);
    } else if (message.contains("START")) {
      //if we get start message,we then open a game
      startMultiPlay();
    } else if (message.contains("NICK")) {
      message = message.replace("NICK ", "");
      nickName = message;
    } else if (message.contains("ERROR")) {
      //handle if receive error
      message = message.replace("ERROR ", "");
      logger.error(message);

      Stage errorWindow = new Stage();       // Create a new stage for the pop-up window
      var box = new HBox();
      box.setAlignment(Pos.CENTER);
      box.getChildren().addAll(new Label(message));
      Scene scene = new Scene(box, 200, 100);     // Create a scene for the pop-up window
      errorWindow.setScene(scene);      // Set the scene for the pop-up window
      errorWindow.show();

    }
  }

  /**
   * join a new channel,which would show the essential UI
   *
   * @param channelName the name of channel
   */
  protected void Join(String channelName) {
    channelUI.setVisible(true);
    nickNameChange.setVisible(true);
    exitChannel.setVisible(true);
    channelBox.setVisible(true);
    gamerList.setVisible(true);
    //we can't set startGame into visible,because only host could open a game
    startGame.setVisible(false);
    sendField.setVisible(true);
    messagesPane.setVisible(true);
    gamerList.setVisible(true);
    multimedia.playAudio("pling.wav");
    channelTitle.setText("Current Channel: " + channelName);
  }

  /**
   * show all gamers in current game
   *
   * @param gamer the gamer message
   */
  public void showGamer(String gamer) {

    //initialise the list and set
    gamerList.getChildren().clear();
    gamers.clear();
    String[] playerArr = gamer.split("\n");
    for (String s : playerArr) {
      gamers.add(s);
      Text text = new Text(s);
      text.getStyleClass().add("heading");
      gamerList.getChildren().add(text);
    }

  }

  /**
   * open a new multi-game
   */
  public void startMultiPlay() {
    multimedia.playAudio("transition.wav");
    multimedia.stopPlaying();
    gameWindow.loadScene(new MultiplayerScene(gameWindow, gamers, nickName));
  }


  /**
   * used to return the game list
   *
   * @return gamer list
   */
  public HashSet<String> getGamers() {
    return gamers;
  }
}
