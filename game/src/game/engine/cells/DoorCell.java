package game.engine.cells;

import game.engine.Board;
import game.engine.Role;
import game.engine.interfaces.CanisterModifier;
import game.engine.monsters.Monster;

public class DoorCell extends Cell implements CanisterModifier {
	private Role role;
	private int energy;
	private boolean activated;
	
	public DoorCell(String name, Role role, int energy) {
		super(name);
		this.role = role;
		this.energy = energy;
		this.activated = false;
	}
	
	public Role getRole() {
		return role;
	}
	
	public int getEnergy() {
		return energy;
	}
	
	public boolean isActivated() {
		return activated;
	}

	public void setActivated(boolean isActivated) {
		this.activated = isActivated;
	}
	
	public void modifyCanisterEnergy(Monster monster, int canisterValue) {
	    
	    if (monster.getRole().equals(this.getRole())){
	        monster.alterEnergy(canisterValue);
	    } 

	    else {
	        monster.alterEnergy(-1 * canisterValue);
	    }
	}
	
	
	private boolean energyChanged (Monster monster,int beforeEnergy,boolean currentChangeStatus){
		if(monster.getEnergy()!=beforeEnergy)
			return true;
		else
			return currentChangeStatus;
	}
	

	
	public void onLand(Monster landingMonster, Monster opponentMonster){
        super.onLand(landingMonster, opponentMonster);
       
        if(!(this.isActivated())){
        	boolean energyActuallyChanged=false;
        	int beforeEnergy=this.getMonster().getEnergy();
        	this.modifyCanisterEnergy(this.getMonster(), this.getEnergy());
        	energyActuallyChanged=this.energyChanged(this.getMonster(),beforeEnergy,energyActuallyChanged);
        	for (Monster stationedMonster : Board.getStationedMonsters()){
        		
        		if(stationedMonster.getRole().equals(this.getMonster().getRole())){
        			beforeEnergy=stationedMonster.getEnergy();
        	this.modifyCanisterEnergy(stationedMonster, this.getEnergy());
        			energyActuallyChanged=this.energyChanged(stationedMonster,beforeEnergy,energyActuallyChanged);
        		}    
        	}
        	if(energyActuallyChanged)
        	this.setActivated(true);
        	
}
      
	
	
}




}
