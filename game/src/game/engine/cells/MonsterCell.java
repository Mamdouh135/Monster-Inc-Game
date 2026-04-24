package game.engine.cells;

import game.engine.monsters.*;

public class MonsterCell extends Cell {
	private Monster cellMonster;

	public MonsterCell(String name, Monster cellMonster) {
		super(name);
		this.cellMonster = cellMonster;
	}

	public Monster getCellMonster() {
		return cellMonster;
	}
	
	
	public void onLand(Monster landingMonster, Monster opponentMonster){
		super.onLand(landingMonster, opponentMonster);
		if(this.getCellMonster()== null)
			return;
		if(this.getMonster().getRole()==this.getCellMonster().getRole())
			this.getMonster().executePowerupEffect(opponentMonster);
		
		else{
			if(this.getMonster().getEnergy()>this.getCellMonster().getEnergy()){
				int change=this.getMonster().getEnergy()-this.getCellMonster().getEnergy();
				this.getCellMonster().setEnergy(this.getMonster().getEnergy());
				this.getMonster().alterEnergy(-1*change);
				
			}
			
		}
	}

}
