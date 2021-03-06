package fr.ubx.poo.ubomb.go.decor.bonus;

import fr.ubx.poo.ubomb.game.EntityCode;
import fr.ubx.poo.ubomb.game.Position;
import fr.ubx.poo.ubomb.go.Takeable;
import fr.ubx.poo.ubomb.go.character.Player;

public class BombNumberInc extends Bonus implements Takeable {
    public BombNumberInc(Position position) {
        super(position);
    }

    @Override
    public void takenBy(Player player) {
        player.takeBombNumberInc();
        explode();
    }
}
