package game.engine.monsters;

import game.engine.*;

public class Schemer extends Monster {
	
	public Schemer(String name, String description, Role role, int energy) {
		super(name, description, role, energy);
	}
	public void executePowerupEffect(Monster opponentMonster){
		int totalStolen=this.stealEnergyFrom(opponentMonster);
		for (Monster stationedMonster : Board.getStationedMonsters()) {
	        totalStolen+= stealEnergyFrom(stationedMonster);
	    }
		this.setEnergy(totalStolen+this.getEnergy());
	}
	 private int stealEnergyFrom(Monster target){
		 int stealBonus=0;
		 if(target.getEnergy()<Constants.SCHEMER_STEAL){
			 stealBonus=target.getEnergy();
			 target.setEnergy(Constants.MIN_ENERGY);
			
		 }
		 else{
			 stealBonus=Constants.SCHEMER_STEAL;
			 target.setEnergy(target.getEnergy()-Constants.SCHEMER_STEAL);
			
		 }
		 
		 return stealBonus;
	 }
	 
	 public void setEnergy(int energy){
		
			super.setEnergy(energy+10);

	 }
	
}
