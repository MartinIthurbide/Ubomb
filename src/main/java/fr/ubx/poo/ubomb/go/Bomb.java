package fr.ubx.poo.ubomb.go;

import fr.ubx.poo.ubomb.game.Direction;
import fr.ubx.poo.ubomb.game.Game;
import fr.ubx.poo.ubomb.game.Position;
import fr.ubx.poo.ubomb.go.character.Character;
import fr.ubx.poo.ubomb.go.character.Monster;
import fr.ubx.poo.ubomb.go.character.Player;
import fr.ubx.poo.ubomb.go.decor.*;
import fr.ubx.poo.ubomb.go.decor.bonus.Bonus;

import java.util.ArrayList;

public class Bomb extends GameObject{

    private final int CONST = 50;
    private final int CONSTEXP = 30;
    private int range;
    private int cptBomb;
    private int cptExplode;
    private int etatBomb;
    private boolean exploded;
    public Bomb(Game game, Position position) {
        super(game, position);
        this.etatBomb = 3;
        cptBomb = CONST;
        cptExplode = CONSTEXP;
        exploded = false;
        range = game.bombRange;
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
            }
        }

    }

    public void clearExplosions(ArrayList<Explosion> explosions){
        cptExplode --;
        if (cptExplode <= 0){
            for (int i = 0; i < explosions.size();i++){
                game.removeExplosions(explosions.get(i));
                //explosions.get(i).remove();
            }
        }
        reinitCpt(CONSTEXP);
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

    public ArrayList<Explosion> explosion() {
        System.out.println("Explosion\n");
        exploded = true;
        int range = getRange();
        Position posBomb = getPosition(); // definition de la position suivante de la bombe
        Direction[] direction = Direction.values();

        game.addExplosions(new Explosion(getPosition()));
        for( int i = 0; i < direction.length; i++){
            Position nextPos =  posBomb;


            for (int r = 1; r <= range; r++){
                nextPos = direction[i].nextPosition(nextPos);
                GameObject nextObj = game.getGrid().get(nextPos);

                if(game.inside(nextPos)){
                    if (!(nextObj instanceof Stone) && !(nextObj instanceof Tree) && !(nextObj instanceof Door)){ // Door et Box pas prise en compte
                        if (nextObj != null) {
                            if (nextPos == game.getPlayer().getPosition()) {
                                damagePlayer();
                                System.out.println("PlayerHeart : " + game.playerHearts + "\n");
                                // todo : probleme damage player et monster
                            }
                            game.getGrid().get(nextPos).remove();
                        }
                            System.out.println("Boom à : " + nextPos +"\n");
                            game.addExplosions(new Explosion(nextPos));
                            game.getExplosions().get(i).getPosition();
                    }
                }
            }
       }
        game.bombCapacity ++;
        return game.getExplosions();
    }

    public void damagePlayer(){
        if (game.playerHearts == 0)
            System.out.println("Joueur mort\n");
            // todo: game over et on detruit le player ?
        else
            game.playerHearts --;
    }
}
