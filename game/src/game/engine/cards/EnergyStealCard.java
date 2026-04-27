package game.engine.cards;

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
		// Use alterEnergy to properly respect shields and Dynamo passives!
		monster.alterEnergy(canisterValue);
	}
	
	public void performAction(Monster player, Monster opponent) {
		// 1. If the opponent has a shield, they block the steal and lose their shield
		if (opponent.isShielded()) {
			opponent.setShielded(false);
		} 
		// 2. If they don't have a shield, steal the energy!
		else {
			int opponentEnergy = opponent.getEnergy();
			int cardValue = this.getEnergy();
			
			// Calculate exactly how much we can steal
			int actualStolen = (cardValue > opponentEnergy) ? opponentEnergy : cardValue;
			
			// Player gains the energy
			player.setEnergy(player.getEnergy() + actualStolen);
			
			// Opponent takes damage equal to the stolen amount
			this.modifyCanisterEnergy(opponent, -1 * actualStolen);
		}
	}
}