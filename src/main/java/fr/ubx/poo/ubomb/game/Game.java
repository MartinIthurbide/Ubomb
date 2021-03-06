/*
 * Copyright (c) 2020. Laurent Réveillère
 */

package fr.ubx.poo.ubomb.game;


import fr.ubx.poo.ubomb.go.GameObject;
import fr.ubx.poo.ubomb.go.character.Monster;
import fr.ubx.poo.ubomb.go.character.Player;
import fr.ubx.poo.ubomb.go.decor.Door;
import fr.ubx.poo.ubomb.go.decor.Explosion;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

public class Game {

    public final int bombBagCapacity;
    public final int monsterVelocity;
    public int playerLives;
    public int levels;
    public boolean changeLevelState = false;
    
    public int monsterLives;
    public final long playerInvisibilityTime;
    public final long monsterInvisibilityTime;
    private Grid grid;
    private final Player player;
    private final ArrayList<Monster> monsters = new ArrayList<>();
    private final ArrayList<Explosion> explosions = new ArrayList<>();
    private final ArrayList<GridRepo> tabLevels = new ArrayList<>();

    public int currentLevel = 1;
    public int nbKeys = 0;
    public int playerHearts;
    public int bombRange = 1;
    public int bombCapacity = 1;

    public boolean won = false;

    public Game(String worldPath) {
        try (InputStream input = new FileInputStream(new File(worldPath, "config.properties"))) {
            Properties prop = new Properties();
            // load the configuration file
            prop.load(input);
            bombBagCapacity = Integer.parseInt(prop.getProperty("bombBagCapacity", "3"));
            monsterVelocity = Integer.parseInt(prop.getProperty("monsterVelocity", "10"));
            levels = Integer.parseInt(prop.getProperty("levels", "1"));
            playerLives = Integer.parseInt(prop.getProperty("playerLives", "3"));
            playerHearts = playerLives;
            playerInvisibilityTime = Long.parseLong(prop.getProperty("playerInvisibilityTime", "4000"));
            monsterInvisibilityTime = Long.parseLong(prop.getProperty("monsterInvisibilityTime", "1000"));
            monsterLives = Integer.parseInt(prop.getProperty("monsterLives", "1"));

            // Load the world
            String prefix = prop.getProperty("prefix");
            String suffix = ".txt";

            for (int i = 0; i < levels; i++){
                tabLevels.add(new GridRepoFile(this));
                //tabLevels.get(i).load(i,prefix+i+suffix);
            }

            this.grid = tabLevels.get(0).load(1, prefix + 1 + suffix);

            // Create the player
            String[] tokens = prop.getProperty("player").split("[ :x]+");
            if (tokens.length != 2)
                throw new RuntimeException("Invalid configuration format");
            Position playerPosition = new Position(Integer.parseInt(tokens[0]), Integer.parseInt(tokens[1]));
            player = new Player(this, playerPosition, playerLives);
        }
            catch (IOException ex) {
            System.err.println("Error loading configuration");
            throw new RuntimeException("Invalid configuration format");
        }
    }

    public Grid getGrid() {
        return grid;
    }

    // Returns the player, monsters and bombs at a given position
    public List<GameObject> getGameObjects(Position position) {
        List<GameObject> gos = new LinkedList<>();
        if (getPlayer().getPosition().equals(position))
            gos.add(player);
        for (Monster m : getMonsters()) {
            if(m.getPosition().equals(position)) {
                gos.add(m);
            }
        }
        return gos;
    }

    public Player getPlayer() {
        return this.player;
    }

    public ArrayList<Monster> getMonsters() {
        return monsters;
    }

    public ArrayList<Explosion> getExplosions() {
        return explosions;
    }

    public void addExplosions(Explosion e) {
        explosions.add(e);
    }


    public void removeExplosions (Explosion e){
        explosions.remove(e);
        e.remove();
    }

    public void addMonster(Monster m) {
        monsters.add(m);
    }

    public boolean getChangeLevelState() {
        return changeLevelState;
    }

    public void setChangeLevelState(boolean changeLevelState) {
        this.changeLevelState = changeLevelState;
    }


    public void changeLevel (int currentLevel, Door d) throws IOException {

        player.blockDoor = true;
        for (Monster m: getMonsters()
             ) {
            grid.remove(m.getPosition());
            m.remove();
        }

        this.grid = tabLevels.get(currentLevel-1).load(currentLevel,"level"+currentLevel+".txt");

        player.setPosition(tabLevels.get(currentLevel-1).getGame().spawnPlayer(d.getSens())); // todo : position du joueur dans le level suivant
        //System.out.println("Position joueur : "+player.getPosition());
        setChangeLevelState(true);
    }

    public Position spawnPlayer (int sens) {
        if(sens == Door.BACK) {
            for (int i = 0; i < getGrid().getWidth(); i++){
                for (int j = 0; j < getGrid().getHeight(); j++) {
                    if (grid.get(new Position(i,j)) instanceof Door) {
                        Door d = (Door) grid.get(new Position(i,j));
                        if (d.getSens() == Door.NEXT)
                            return new Position(i,j);
                    }
                }
            }
        }
        if(sens == Door.NEXT) {
            for (int i = 0; i < getGrid().getWidth(); i++){
                for (int j = 0; j < getGrid().getHeight(); j++) {
                    if (grid.get(new Position(i,j)) instanceof Door) {
                        Door d = (Door) grid.get(new Position(i,j));
                        if (d.getSens() == Door.BACK)
                            return new Position(i,j);
                    }
                }
            }
        }

        return null;
    }


    public boolean inside(Position position) {
        int H = grid.getHeight();
        int W = grid.getWidth();
        if(position.getX() < W && position.getX() >= 0)
            return position.getY() < H && position.getY() >= 0;
        return false;
    }

}
