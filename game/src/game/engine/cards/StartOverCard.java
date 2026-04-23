package game.engine.cards;

import game.engine.monsters.Monster;

public class StartOverCard extends Card {

	public StartOverCard(String name, String description, int rarity, boolean lucky) {
		super(name, description, rarity, lucky);
	}
	public void performAction(Monster player, Monster opponent)
	{
		// Check the card's flag: Is it a lucky card?
	    if (this.isLucky()) {
	        
	        // Scenario A: Lucky! Send the horse (opponent) back to the start.
	        opponent.setPosition(0);
	        
	    } else {
	        
	        // Scenario B: Unlucky! Send yourself (player) back to the start.
	        player.setPosition(0);
	        
	    }
	}
}
