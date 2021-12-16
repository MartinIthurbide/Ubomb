package fr.ubx.poo.ubomb.go;

import fr.ubx.poo.ubomb.game.Direction;
import fr.ubx.poo.ubomb.game.Game;
import fr.ubx.poo.ubomb.game.Position;
import fr.ubx.poo.ubomb.go.character.Player;
import fr.ubx.poo.ubomb.go.decor.Explosion;
import fr.ubx.poo.ubomb.go.decor.Stone;
import fr.ubx.poo.ubomb.go.decor.Tree;
import fr.ubx.poo.ubomb.go.decor.bonus.Bonus;
import fr.ubx.poo.ubomb.view.SpriteBomb;

import java.util.ArrayList;
import java.util.Map;

public class Bomb extends GameObject{

    private final int CONST = 50;
    private int range;
    private int cptBomb;
    private int etatBomb;
    private boolean exploded;
    public Bomb(Game game, Position position) {
        super(game, position);
        this.etatBomb = 3;
        cptBomb = CONST;
        exploded = false;
    }

    public Bomb(Position position) {
        super(position);
    }

    @Override
    public boolean isWalkable(Player player) {
        return true;
    }

    private void reinitCpt(int cpt) {
        cptBomb = cpt;
    }

    public void update() {
        if(isExploded() != true) {
            cptBomb--;
            if (cptBomb <= 0) {
                setModified(true);
                reinitCpt(CONST); // delai entre chaque changement d'etat
                etatBomb--;
                System.out.println("etat bombe : "+etatBomb+"\n");

                    //todo
                    // faire passer l'explosion en tant que décor pour l'explosion
                    // arreter le programme
                    //explosion();
            }
        }


    }

    public int getRange() {
        return range;
    }

    public int getEtatBomb() {
        return etatBomb;
    }

    public boolean isExploded(){
        return exploded;
    }

    @Override
    public void remove() {
        super.remove();
    }

    public Explosion[] explosion() {
        System.out.println("Explosion\n");
        int cpt = 0;
        exploded = true;
        //todo
        Position posBomb = getPosition(); // definition de la position suivante de la bombe
        Direction[] direction = Direction.values();

        Explosion[] explosions = new Explosion[direction.length+1];
        // todo : gérer les cas pour la range
        for( int i = 0; i < direction.length ; i++){
            Position nextPos =  direction[i].nextPosition(posBomb);
            if(game.inside(nextPos)){
                if (game.getGrid().get(nextPos) == null || (game.getGrid().get(nextPos) instanceof Bonus)){ // Door et Box pas prise en compte
                    System.out.println("Boom à : " + nextPos +"\n");
                    cpt++;
                    explosions[cpt] = new Explosion(nextPos);
                }
            }
       }
        return explosions;
    }
}
