/*
 * Copyright (c) 2020. Laurent Réveillère
 */

package fr.ubx.poo.ubomb.engine;

import com.sun.javafx.font.CompositeStrike;
import fr.ubx.poo.ubomb.game.Direction;
import fr.ubx.poo.ubomb.game.Game;
import fr.ubx.poo.ubomb.game.Position;
import fr.ubx.poo.ubomb.go.Bomb;
import fr.ubx.poo.ubomb.go.GameObject;
import fr.ubx.poo.ubomb.go.character.Monster;
import fr.ubx.poo.ubomb.go.character.Player;
import fr.ubx.poo.ubomb.go.decor.Box;
import fr.ubx.poo.ubomb.go.decor.Decor;
import fr.ubx.poo.ubomb.go.decor.Door;
import fr.ubx.poo.ubomb.go.decor.Explosion;
import fr.ubx.poo.ubomb.go.decor.bonus.Bonus;
import fr.ubx.poo.ubomb.view.*;
import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;

import javax.management.monitor.MonitorSettingException;
import java.io.IOException;
import java.util.*;

import static fr.ubx.poo.ubomb.view.ImageResource.*;


public final class GameEngine {

    private static AnimationTimer gameLoop;
    private final String windowTitle;
    private final Game game;
    private final Player player;
    private final List<Sprite> sprites = new LinkedList<>();
    private final ArrayList<Bomb> bombs = new ArrayList<>();
    private final Set<Sprite> cleanUpSprites = new HashSet<>();
    private final Stage stage;
    private StatusBar statusBar;
    private Pane layer;
    private Input input;
    private boolean bombInput = false;
    private int cptExplode = 200;


    public GameEngine(final String windowTitle, Game game, final Stage stage) {
        this.stage = stage;
        this.windowTitle = windowTitle;
        this.game = game;
        this.player = game.getPlayer();
        initialize();
        buildAndSetGameLoop();
    }

    private void initialize() {
        Group root = new Group();
        layer = new Pane();

        int height = game.getGrid().getHeight();
        int width = game.getGrid().getWidth();
        int sceneWidth = width * Sprite.size;
        int sceneHeight = height * Sprite.size;
        Scene scene = new Scene(root, sceneWidth, sceneHeight + StatusBar.height);
        scene.getStylesheets().add(getClass().getResource("/css/application.css").toExternalForm());

        stage.setTitle(windowTitle);
        stage.setScene(scene);
        stage.setResizable(false);
        stage.sizeToScene();
        stage.show();

        input = new Input(scene);
        root.getChildren().add(layer);
        statusBar = new StatusBar(root, sceneWidth, sceneHeight, game);

        // Create sprites
        for (Decor decor : game.getGrid().values()) {
            sprites.add(SpriteFactory.create(layer, decor));
            decor.setModified(true);
        }

        // Creates monsters
        for (Monster m : game.getMonsters()) {
            sprites.add(new SpriteMonster(layer,m));
            m.setModified(true);
        }

        sprites.add(new SpritePlayer(layer, player));
        player.setModified(true);
    }

    void buildAndSetGameLoop() {
        gameLoop = new AnimationTimer() {
            public void handle(long now) {
                // Check keyboard actions
                processInput(now);

                // Do actions
                update(now);
                createNewBombs(now);
                try {
                    checkCollision(now);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                checkExplosions();
                invincibility(now);
                player.update(now);

                for (Monster m: game.getMonsters()
                     ) {
                    m.update(now);
                }

                // Graphic update
                cleanupSprites();
                render();
                statusBar.update(game);
                //sprites.add(SpriteFactory.updateFactory(layer,game)
            }
        };
    }

    private void checkExplosions() {
        for (Bomb b : bombs) {
            b.update();
            if (b.getEtatBomb() == 0 && b.isExploded() != true) {
                b.remove();
                b.explosion();
                for (int i = 0; i < game.getExplosions().size(); i++) {
                    sprites.add(new SpriteFactory(layer, EXPLOSION.getImage(), game.getExplosions().get(i)));
                }
            }
            else if(b.isExploded() == true)
                b.clearExplosions(game.getExplosions());
        }
    }

    private void createNewBombs(long now) {
        if (player.dropBomb() && bombInput){
            System.out.println("Je suis dans createBomb\n");
            Bomb bombe = new Bomb(game,player.getPosition());
            bombs.add(bombe);
            sprites.add(new SpriteBomb(layer,BOMB_0.getImage(),bombe));
            game.bombCapacity --;
        }
        bombInput = false;
    }

    private void openDoor (){
        if(game.nbKeys > 0){
            if (player.takeDoor(1)){
                Door door = new Door(game, player.getDirection().nextPosition(player.getPosition()));
                sprites.add(new SpriteFactory(layer,DOOR_OPENED.getImage(),door));
                door.isWalkable(player);
                game.nbKeys --;
            }
        }
    }

    private void checkCollision(long now) throws IOException {
        //verifier direction tout autour du player

            if (game.getGrid().get(player.getPosition()) instanceof Door) {
                Door d = (Door) game.getGrid().get(player.getPosition());
                d.takenBy(player);
                player.goToNextLevel(game.currentLevel, d);
            }

            for (Monster m : game.getMonsters())
                player.playerCollision(m);

            for (Explosion e : game.getExplosions()) {
                for (Monster m : game.getMonsters())
                    m.monsterCollision(e);

                player.playerCollision(e);
            }
        }

    private void invincibility(long now){
        if (player.isInvincible() == true)
            player.updateInvincibility();
    }

    private void processInput(long now) {
        if (input.isExit()) {
            gameLoop.stop();
            Platform.exit();
            System.exit(0);
        } else if (input.isMoveDown()) {
            player.requestMove(Direction.DOWN);
        } else if (input.isMoveLeft()) {
            player.requestMove(Direction.LEFT);
        } else if (input.isMoveRight()) {
            player.requestMove(Direction.RIGHT);
        } else if (input.isMoveUp()) {
            player.requestMove(Direction.UP);
            input.clear();
        } else if (input.isKey()){
            openDoor();
        } else if (input.isBomb()){
            player.dropBomb();
            bombInput = true;
        }
        input.clear();
    }

    private void showMessage(String msg, Color color) {
        Text waitingForKey = new Text(msg);
        waitingForKey.setTextAlignment(TextAlignment.CENTER);
        waitingForKey.setFont(new Font(60));
        waitingForKey.setFill(color);
        StackPane root = new StackPane();
        root.getChildren().add(waitingForKey);
        Scene scene = new Scene(root, 400, 200, Color.WHITE);
        stage.setTitle(windowTitle);
        stage.setScene(scene);
        input = new Input(scene);
        stage.show();
        new AnimationTimer() {
            public void handle(long now) {
                processInput(now);
            }
        }.start();
    }


    private void update(long now) {
        player.update(now);

        if (player.getLives() == 0) {
            gameLoop.stop();
            showMessage("Perdu!", Color.RED);
        }

        for (Monster m: game.getMonsters()) {
            if (m.getLives() == 0){
                m.remove();
            }
        }

        if (player.isWinner()) {
            gameLoop.stop();
            showMessage("Gagné", Color.BLUE);
        }
    }

    public void cleanupSprites() {
        sprites.forEach(sprite -> {
            if (sprite.getGameObject().isDeleted()) {
                System.out.println(sprite.getPosition()+"\n");
                game.getGrid().remove(sprite.getPosition());
                cleanUpSprites.add(sprite);
            }
        });
        cleanUpSprites.forEach(Sprite::remove);
        sprites.removeAll(cleanUpSprites);
        cleanUpSprites.clear();
    }

    private void render() {
        if(game.changeLevelState){
            sprites.clear();
            stage.close();
            initialize();
            sprites.forEach(Sprite::render);
            game.setChangeLevelState(false);
        }
        else{
            sprites.forEach(Sprite::render);
        }
    }

    public void start() {
        gameLoop.start();
    }
}
