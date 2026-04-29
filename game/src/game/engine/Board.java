package game.engine;

import java.util.ArrayList;
import java.util.Collections;

import game.engine.cards.Card;
import game.engine.cells.*;
import game.engine.exceptions.InvalidMoveException;
import game.engine.monsters.Monster;

public class Board {
	private Cell[][] boardCells;
	private static ArrayList<Monster> stationedMonsters; 
	private static ArrayList<Card> originalCards;
	public static ArrayList<Card> cards;
	
	public Board(ArrayList<Card> readCards) {
		this.boardCells = new Cell[Constants.BOARD_ROWS][Constants.BOARD_COLS];
		stationedMonsters = new ArrayList<Monster>();
		originalCards = readCards;
		cards = new ArrayList<Card>();
		
		setCardsByRarity();
		reloadCards();
	}
	
	public Cell[][] getBoardCells() { return boardCells; }
	public static ArrayList<Monster> getStationedMonsters() { return stationedMonsters; }
	public static void setStationedMonsters(ArrayList<Monster> stationedMonsters) { Board.stationedMonsters = stationedMonsters; }
	public static ArrayList<Card> getOriginalCards() { return originalCards; }
	public static ArrayList<Card> getCards() { return cards; }
	public static void setCards(ArrayList<Card> cards) { Board.cards = cards; }

	private int[] indexToRowCol(int index){
		int col = index % 10;
		int row = index / 10;
		if(row % 2 == 1) col = 9 - col;
		return new int[]{row, col};
	}

	private Cell getCell(int index){
		int[] pair = indexToRowCol(index);
		return boardCells[pair[0]][pair[1]];
	}

	private void setCell(int index, Cell cell){
		int[] pair = indexToRowCol(index);
		boardCells[pair[0]][pair[1]] = cell;
	}

	public void initializeBoard(ArrayList<Cell> specialCells){
		  ArrayList<Monster> sMonsters = getStationedMonsters();
		  int[] monsterI = Constants.MONSTER_CELL_INDICES;
		  ArrayList<Cell> monstercell = new ArrayList<Cell>();
		  for (int i = 0; i < sMonsters.size(); i++) {
		   sMonsters.get(i).setPosition(monsterI[i]);
		   monstercell.add(new MonsterCell(sMonsters.get(i).getName(), sMonsters.get(i)));
		  }
		  
		  ArrayList<Cell> d = new ArrayList<>();
		  ArrayList<Cell> cs = new ArrayList<>();
		  ArrayList<Cell> cb = new ArrayList<>();
		  
		  for (Cell c : specialCells) {
		   if(c instanceof DoorCell)
		   {
		    d.add(c);
		   }
		   else if(c instanceof ContaminationSock)
		   {
		    cs.add(c);
		   }
		   else if(c instanceof ConveyorBelt)
		   {
		    cb.add(c);
		   }
		  }
		  
		  //get the constant indices arrays
		  int[] cardI = Constants.CARD_CELL_INDICES;
		  int[] convI = Constants.CONVEYOR_CELL_INDICES;
		  int[] contI = Constants.SOCK_CELL_INDICES;
		  
		  for (int index = 0; index < 100; index++) {
		   if(index%2 == 1)
		   {
		    if(d.size()>0)
		    setCell(index, d.remove(0));
		    else setCell(index, new Cell("Cell:" + index));
		   }
		   else{
		    if(contains(monsterI, index))
		    {
		     if(monstercell.size()>0)
		     setCell(index, monstercell.remove(0));
		     else setCell(index, new Cell("Cell:" + index));
		    }
		    else if(contains(cardI, index))
		    {
		     setCell(index, new CardCell("Card Cell: "+index));
		    }
		    else if(contains(convI, index))
		    {
		     if(cb.size()>0)
		     setCell(index, cb.remove(0));
		     else setCell(index, new Cell("Cell:" + index));
		    }
		    else if(contains(contI, index))
		    {
		     if(cs.size()>0)
		     setCell(index, cs.remove(0));
		     else setCell(index, new Cell("Cell:" + index));
		    }
		    else{
		     setCell(index, new Cell("Normal Cell"));
		    }
		   }
		  }
		  
		 }


	private boolean contains(int[] array, int value) {
		for (int i : array) {
			if (i == value) return true;
		}
		return false;
	}

	private void setCardsByRarity(){
		ArrayList<Card> expanded = new ArrayList<>();
		for (Card card : originalCards) {
			for (int j = 0; j < card.getRarity(); j++) {
				expanded.add(card);
			}
		}
		originalCards = expanded;
	}

	public static void reloadCards(){
	    cards.clear();
		cards.addAll(originalCards); 
		Collections.shuffle(cards);
	}

	public static Card drawCard(){
		if(cards.isEmpty()) reloadCards();
		return cards.remove(0);
	}

	public void moveMonster(Monster currentMonster, int roll, Monster opponentMonster) throws InvalidMoveException {
		int oldPos = currentMonster.getPosition();
		
		// 1. Let the monster handle its own movement (Supports Dasher!)
		currentMonster.move(roll);
		int newPos = currentMonster.getPosition(); 
		
		// 2. Initial collision check before triggering the cell
		if (newPos == opponentMonster.getPosition()) {
			currentMonster.setPosition(oldPos); // Revert the move
			throw new InvalidMoveException();
		}
		
		Cell landedCell = getCell(newPos);
		
		// 3. Capture the CURRENT monster's confusion status BEFORE landing
		int playerConf = currentMonster.getConfusionTurns();
		
		// 4. Trigger the cell effect
		landedCell.onLand(currentMonster, opponentMonster);
		
		// 5. Final collision check (in case a swapper or transport caused a collision)
		if (currentMonster.getPosition() == opponentMonster.getPosition()) {
			currentMonster.setPosition(oldPos); // Revert the move
			throw new InvalidMoveException();
		}
		
		// 6. THE FIX: Decrement confusion ONLY for the current monster that just moved!
		// The opponent's confusion will be decremented on their own turn.
		if (playerConf > 0) {
			currentMonster.decrementConfusion();
		}
		
		// 7. Synchronize the board
		updateMonsterPositions(currentMonster, opponentMonster);
	}
	
	private void updateMonsterPositions(Monster player, Monster opponent){
		int pIndex = player.getPosition();
		int oIndex = opponent.getPosition();
		for (int i = 0; i < 100; i++) {
			Cell c = getCell(i);
			if(i == pIndex) c.setMonster(player);
			else if(i == oIndex) c.setMonster(opponent);
			else c.setMonster(null);
		}
	}
}