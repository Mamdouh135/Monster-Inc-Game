package game.engine.monsters;

import game.engine.*;

public class Dynamo extends Monster {
	
	public Dynamo(String name, String description, Role role, int energy) {
		super(name, description, role, energy);
	}
  
	public void executePowerupEffect(Monster opponentMonster){
		opponentMonster.setFrozen(true);
		
		
	}
	
	public void setEnergy(int energy) {
		int change = energy - this.getEnergy();

		int doubleChange=2*change;
		super.setEnergy(this.getEnergy()+doubleChange);
		
	}
	
	
	
	
	
}
