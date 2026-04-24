package game.engine.cells;

import game.engine.interfaces.CanisterModifier;
import game.engine.monsters.Monster;
import game.engine.*;

public class ContaminationSock extends TransportCell implements CanisterModifier {

	public ContaminationSock(String name, int effect) {
		super(name, effect);
	}
	

	public void modifyCanisterEnergy(Monster monster, int canisterValue){
		monster.alterEnergy(canisterValue);
		
	}
	
	public void onLand(Monster landingMonster, Monster opponentMonster){
		super.onLand(landingMonster, opponentMonster);
		this.modifyCanisterEnergy(this.getMonster(),-1*Math.abs(Constants.SLIP_PENALTY ));
	}
	

	public void transport(Monster monster){
		monster.move(-1*Math.abs(this.getEffect()));
		
	}
	

}

