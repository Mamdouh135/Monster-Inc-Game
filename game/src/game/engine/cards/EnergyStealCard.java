package game.engine.cards;

import game.engine.exceptions.GameActionException;
import game.engine.interfaces.CanisterModifier;
import game.engine.monsters.Monster;

public class EnergyStealCard extends Card implements CanisterModifier {
	private int energy;

	public EnergyStealCard(String name, String description, int rarity, int energy) {
		super(name, description, rarity, true);
		this.energy = energy;
	}
	
	public int getEnergy() {
		return energy;
	}

	@Override
	public void modifyCanisterEnergy(Monster monster, int canisterValue) {
		monster.alterEnergy(-1*canisterValue);
	}
	public  void performAction(Monster player, Monster opponent){
	
		if(!opponent.isShielded()){
			int opponentEnergy = opponent.getEnergy();
			int cardValue = this.getEnergy();
			int actualStolen = (cardValue>opponentEnergy)?opponentEnergy:cardValue;
			player.setEnergy(player.getEnergy()+actualStolen);
		}
		this.modifyCanisterEnergy(opponent, this.getEnergy());
		
		
	};
	
}
