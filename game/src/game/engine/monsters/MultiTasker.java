package game.engine.monsters;

import game.engine.Role;
import game.engine.Constants;;

public class MultiTasker extends Monster {
	private int normalSpeedTurns;
	
	public MultiTasker(String name, String description, Role role, int energy) {
		super(name, description, role, energy);
		this.normalSpeedTurns = 0;
	}

	public int getNormalSpeedTurns() {
		return normalSpeedTurns;
	}

	public void setNormalSpeedTurns(int normalSpeedTurns) {
		this.normalSpeedTurns = normalSpeedTurns;
	}
	
	public void setEnergy(int energy) {
		
		super.setEnergy(energy+Constants.MULTITASKER_BONUS);
		
	}
	public void move(int distance){
		if(this.getNormalSpeedTurns() >0){
			super.move(distance);
			this.setNormalSpeedTurns(this.getNormalSpeedTurns()-1);
		}
		
		else
			super.move(distance/2);
			
	}
	
	public void executePowerupEffect(Monster opponentMonster){
		this.setNormalSpeedTurns(2);
	}

}