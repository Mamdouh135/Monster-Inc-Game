package game.engine;
import java.util.ArrayList; 
public class Board {
	private Cell[][] boardCells;
	/*A 2D array of cells representing the game board with dimensions BOARD ROWS
	x BOARD COLS.*/
	
	private static ArrayList<Monster> stationedMonsters;
	/*  containing monsters stationed on the board
	at monster cell*/
	
	private static ArrayList<Card> originalCards; 
	// ArrayList containing the original cards read from CSV
	
	
	private static ArrayList<Card> cards;
	// ArrayList containing the current available cards
	
	
	
	// initializng the constructor
	public Board(ArrayList<Card> readCards){
		boardCells = new Cell[Constants.BOARD_ROWS][Constants.BOARD_COLS];
		
		//  "initializes the stationedMonsters as an empty ArrayList"
        stationedMonsters = new ArrayList<>();

        //  "initializes cards as an empty ArrayList"
        cards = new ArrayList<>();
        
        // initializes original cards with the value of readCards
        originalCards = readCards;
        
       
        
        
	}
	
	
	
	
	//intialzing getters and setters.
	public static ArrayList<Card> getCards() {
		return cards;
	}
	public static void setCards(ArrayList<Card> newcards) {
		cards = newcards;
	}
	public static ArrayList<Monster> getStationedMonsters() {
		return stationedMonsters;
	}
	public Cell[][] getBoardCells() {
		return boardCells;
	}
	public static ArrayList<Card> getOriginalCards() {
		return originalCards;
	}
	public static void setStationedMonsters(ArrayList<Monster> newMonsters) {
		stationedMonsters = newMonsters;
	}
	
	

}
