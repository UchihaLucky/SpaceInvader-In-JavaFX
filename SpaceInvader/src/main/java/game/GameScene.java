package game;


import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.File;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;


public class GameScene extends Application {

    private final int SCENE_WIDTH = 1080;
    private final int SCENE_HEIGHT = 720;
    private double SPACE_SPEED = 2.0;
    private double ENEMIES_SPEED = 3;

    //Base Pane of our Game Screen.
    private StackPane stackPane = new StackPane();
    private PauseScreen pauseScreen = new PauseScreen();

    //Only for Game play (Bullets, Hero, Enemies, Space).
    private Pane pane = new Pane();
    private List<Space> elements = new ArrayList<>();
    private List<Enemies> enemiesList = new ArrayList<>();
    private List<HeroBullets> bulletsList = new ArrayList<>();
    //position of Enemies.
    private int[] posX = new int[]{200, 300, 400, 500, 600, 700, 100};
    protected static boolean GAME_OVER = false;
    private Random random = new Random();
    private Enemies enemies;
    private Space space;
    private HeroBullets bullets;
    private boolean isPause = false;
    private int SCORE;
    private Text score = new Text();
    private Hero hero;
    private AnimationTimer timer;
    private GameScene gameScene = this;
    private boolean collide = true;
    private boolean START = false;
    private  int SHIPCASE = 6;
    private Media media;
    private MediaPlayer background;
    private VFXSound vfxSound;

    public void gameReset(){
        Iterator<Space> elementsItr = elements.iterator();
        while (elementsItr.hasNext()) {
            Space space = elementsItr.next();
            pane.getChildren().remove(space);
            elementsItr.remove();

        }
        Iterator<HeroBullets> bulletsItr = bulletsList.iterator();
        while (bulletsItr.hasNext()){
            HeroBullets heroBullets = bulletsItr.next();
            pane.getChildren().remove(heroBullets);
            bulletsItr.remove();
        }
        Iterator<Enemies> iterator = enemiesList.iterator();
        while (iterator.hasNext()) {
          Enemies enemies = iterator.next();
          pane.getChildren().remove(enemies);
          iterator.remove();

        }

        elements.clear();
        bulletsList.clear();
        enemiesList.clear();
        collide = true;
        SCORE = 0;
        ENEMIES_SPEED = 3;
        SPACE_SPEED = 2.0;
        pane.getChildren().remove(score);
        score.setText("SCORE : " + SCORE);
        pane.getChildren().add(score);

    }

    public void gameScene() throws Exception {


            hero = new Hero();
            pane.getChildren().addAll(hero, score);
            score.setFont(new Font("Monospaced", 25));
            score.setFill(Color.rgb(255, 255, 255, 0.7));
            score.setX(1);
            score.setY(25);
            score.setText("SCORE : " + SCORE);


            timer = new AnimationTimer() {
            @Override
            public void handle(long now) {

                if (elements.size() < 50 || now % 2 == 0) {
                    space = new Space();
                    pane.getChildren().add(space);
                    elements.add(space);
                }
                for (Space elements : elements) {
                    elements.move(SPACE_SPEED);
                }
                if (START){
                    Iterator<Space> elementsItr = elements.iterator();
                    while (elementsItr.hasNext()) {
                        Space space = elementsItr.next();
                        if (space.getCenterY() > SCENE_HEIGHT) {
                            pane.getChildren().remove(space);
                            elementsItr.remove();
                        }
                    }

                    for (Enemies enemies : enemiesList) {
                        enemies.move(ENEMIES_SPEED);
                        if (enemies.collide(hero) && collide) {
                            pane.getChildren().remove(hero);
                            GAME_OVER = true;
                        }
                    }

                    Iterator<Enemies> itr = enemiesList.iterator();
                    while (itr.hasNext()) {
                        Enemies enemies = itr.next();

                        Iterator<HeroBullets> iterator = bulletsList.iterator();
                        while (iterator.hasNext()) {
                            HeroBullets bullets = iterator.next();
                            if (bullets.collide(enemies)) {
                                SCORE += 1;
                                score.setText("SCORE : " + SCORE);
                                iterator.remove();
                                itr.remove();
                                pane.getChildren().remove(bullets);
                                pane.getChildren().remove(enemies);
                                vfxSound.bombSound();
                            }
                        }
                        if (enemies.getPosY() > SCENE_HEIGHT) {

                            pane.getChildren().remove(enemies);
                            itr.remove();
                        }

                    }
                    if (enemiesList.size() < 5 || now % 71 == 1) {
                        try {
                            enemies = new Enemies(posX[random.nextInt(7)], -10, 50, 90);
                        } catch (MalformedURLException e) {
                            e.printStackTrace();
                        }
                        enemiesList.add(enemies);
                        pane.getChildren().add(enemies.show());
                    }

                    for (HeroBullets bullets : bulletsList) {
                        bullets.move();
                    }

                    if (GAME_OVER) {
                        GameOverScreen gameOverScreen = new GameOverScreen(SCORE, gameScene, stackPane);
                        stackPane.getChildren().add(gameOverScreen);
                        for (HeroBullets bullets : bulletsList) {
                            pane.getChildren().remove(bullets);
                        }
                        bulletsList.clear();
                        GAME_OVER = false;
                        collide = false;
                    }
                    if(SCORE%10 == 0){
                        SPACE_SPEED += 0.002;
                        ENEMIES_SPEED += 0.002;
                    }
                }

            }


        };
        timer.start();


    }

    public void gameStartMenu(){
        StartMenu startMenu = new StartMenu(stackPane, gameScene,pane,hero,vfxSound);
    }


    @Override
    public void start(Stage primaryStage) throws Exception {


        vfxSound = new VFXSound();
        gameScene();


        stackPane.getChildren().addAll(pane);
        stackPane.setStyle("-fx-background-color: #000000");

        Scene scene =new Scene(stackPane, SCENE_WIDTH, SCENE_HEIGHT);
        scene.setOnKeyPressed(e ->{
	    if(!GAME_OVER && collide && START){
           	 if (e.getCode() == KeyCode.P)
              	   checkPause();
           	 else
               	 controls(e.getCode(), hero);
	    }

        });


        primaryStage.setScene(scene);
        primaryStage.show();

        gameStartMenu();
    }

    public static void main(String[] args) {
        launch(args);
    }
    public void checkPause() {
        if (isPause == false) {
            stackPane.getChildren().add(pauseScreen);
            timer.stop();
            isPause = true;
            pauseScreen.setVisible(true);
        }else if (isPause == true){
            isPause = false;
            pauseScreen.setVisible(false);
            stackPane.getChildren().remove(pauseScreen);
            timer.start();

        }
    }

    public void controls(KeyCode keyCode, Hero hero){
        if (keyCode == KeyCode.RIGHT)
            hero.setTranslateX(hero.getHeroX() + 10);
        if (keyCode == KeyCode.LEFT)
            hero.setTranslateX(hero.getHeroX() - 10);

        if (keyCode == KeyCode.SPACE) {
            bullets = new HeroBullets(hero);
            bullets.shipCase(SHIPCASE);
            bulletsList.add(bullets);
            pane.getChildren().add(bullets);
            vfxSound.fireSound();
            }
        }
    public void setSHIPCASE(int x){
        SHIPCASE = x;
    }

    public void setStart(boolean flag){
        START = flag;
    }


}
