package game.engine.cells;

public class Cell {
	private String name;
	private Monster monster;
	
	public Cell(String name){
		this.name = name;
		this.monster = null;
	}

	public Monster getMonster() {
		return monster;
	}

	public void setMonster(Monster monster) {
		this.monster = monster;
	}

	public String getName() {
		return name;
	}
}
