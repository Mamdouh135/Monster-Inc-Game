package game.engine.cells;

public class MonsterCell extends Cell{
	private Monster cellMonster;
	
	public MonsterCell(String name, Monster cellMonster){
		super(name);
		this.cellMonster = cellMonster;
	}

	public Monster getCellMonster() {
		return cellMonster;
	}
	
}
