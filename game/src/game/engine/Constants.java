package game.engine;

public final class Constants {
	// board constants
	public static int BOARD_SIZE = 100;
	public static int BOARD_ROWS = 10;
	public static int BOARD_COLS = 10;
	public static int WINNING_POSITION = 99;
	public static int STARTING_POSITION = 0;
	
	// special cells positions constants
	public static int[] MONSTER_CELL_INDICES = { 2, 18, 34, 54, 82, 88 };
	public static int[] CONVEYOR_CELL_INDICES = { 6, 22, 44, 52, 66 };
	public static int[] SOCK_CELL_INDICES = { 32, 42, 74, 84, 98 };
	public static int[] CARD_CELL_INDICES = { 4, 12, 28, 36, 48, 56, 60, 76, 86, 90 };
	
	// Energy constants 
	public static int WINNING_ENERGY = 1000;
	public static int MIN_ENERGY = 0;
	
	// cell constants
	public static int SLIP_PENALTY = 100;
	
	// power constants
	public static int POWERUP_COST = 500;
	
	
	
}
