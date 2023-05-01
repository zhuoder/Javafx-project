package uk.ac.soton.comp1206.media;

import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * this class is used for add music in the game.
 */
public class Multimedia {
  /**
   * * set new logger
   */
  private static final Logger logger = LogManager.getLogger(Multimedia.class);
  /**
   * used to play audio
   */
  private static MediaPlayer audioPlayer;

  /**
   * used to play background music
   */
  private static MediaPlayer backgroundPlayer;

  /**
   * the volume of background music
   */
  private static double backgroundVolume;

  /**
   * the volume of audio
   */
  private static double audioVolume;

  /**
   * constructor of multiMedia
   */
  public Multimedia() {
  }

  /**
   * used to play audio
   *
   * @param audio the name of the audio file
   */
  public void playAudio(String audio) {
    String audioToPlay = Multimedia.class.getResource("/sounds/" + audio).toExternalForm();
    try {
      Media media = new Media(audioToPlay);
      audioPlayer = new MediaPlayer(media);
      if (audioVolume != 0) {
        audioPlayer.setVolume(audioVolume);
      }
      audioPlayer.play();
      logger.info("now is playing " + audio);
    } catch (Exception e) {
      e.printStackTrace();
      logger.error(e.toString());
    }

  }

  /**
   * set the background music
   *
   * @param volume the volume in double type
   */
  public static void setBackgroundVolume(double volume) {
    Multimedia.backgroundVolume = volume;
  }

  /**
   * set the audio volume
   *
   * @param audioVolume the volume in double type
   */
  public static void setAudioVolume(double audioVolume) {
    Multimedia.audioVolume = audioVolume;
  }

  /**
   * used to play background music
   *
   * @param music the file name of background music
   */
  public void playBackgroundMusic(String music) {
    String musicToPlay = Multimedia.class.getResource("/music/" + music).toExternalForm();

    try {
      Media media = new Media(musicToPlay);
      backgroundPlayer = new MediaPlayer(media);
      //set cycle play
      backgroundPlayer.setAutoPlay(true);
      backgroundPlayer.setCycleCount(MediaPlayer.INDEFINITE);
      if (backgroundVolume != 0) {
        backgroundPlayer.setVolume(backgroundVolume);
      }
      backgroundPlayer.play();
      logger.info("now is playing Background Music: " + music);
    } catch (Exception e) {
      e.printStackTrace();
      logger.error(e.toString());
    }
  }

  /**
   * stop playing background music
   */
  public void stopPlaying() {
    backgroundPlayer.stop();
    logger.info("background music stop");
  }

  /**
   * getter, used to get backgroundPlayer
   *
   * @return current backgroundPlayer
   */
  public static MediaPlayer getBackgroundPlayer() {
    return backgroundPlayer;
  }

  /**
   * getter, used to get audioPlayer
   * @return current audioPlayer
   */
  public static MediaPlayer getAudioPlayer() {
    return audioPlayer;
  }
}
