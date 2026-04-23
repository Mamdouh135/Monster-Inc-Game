package game.engine;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

import game.engine.dataloader.DataLoader;
import game.engine.exceptions.InvalidMoveException;
import game.engine.exceptions.OutOfEnergyException;
import game.engine.monsters.*;
import game.engine.Board;

public class Game {
	private Board board;
	private ArrayList<Monster> allMonsters; 
	private Monster player;
	private Monster opponent;
	private Monster current;
	
	public Game(Role playerRole) throws IOException {
		this.board = new Board(DataLoader.readCards());
		
		this.allMonsters = DataLoader.readMonsters();
		
		this.player = selectRandomMonsterByRole(playerRole);
		this.opponent = selectRandomMonsterByRole(playerRole == Role.SCARER ? Role.LAUGHER : Role.SCARER);
		this.current = player;
		ArrayList<Monster> copy = new ArrayList<>(allMonsters);
		copy.remove(player);
		copy.remove(opponent);
		Board.setStationedMonsters(copy);
		this.board.initializeBoard(DataLoader.readCells());
	}
	
	public Board getBoard() {
		return board;
	}
	
	public ArrayList<Monster> getAllMonsters() {
		return allMonsters; 
	}
	
	public Monster getPlayer() {
		return player;
	}
	
	public Monster getOpponent() {
		return opponent;
	}
	
	public Monster getCurrent() {
		return current;
	}
	
	public void setCurrent(Monster current) {
		this.current = current;
	}
	
	private Monster selectRandomMonsterByRole(Role role) {
		Collections.shuffle(allMonsters);
	    return allMonsters.stream()
	    		.filter(m -> m.getRole() == role)
	    		.findFirst()
	    		.orElse(null);
	}
	
	private Monster getCurrentOpponent(){
		 if (this.current == player)
			 return opponent;
		 else
			 return player;
	 }
	 
	private int rollDice(){
		 return (int) (Math.random() * 6) + 1;
	 }
	
	public void usePowerup() throws OutOfEnergyException {
		if (current.getEnergy() >= Constants.POWERUP_COST){
			current.setEnergy(current.getEnergy() - Constants.POWERUP_COST);
			current.executePowerupEffect(getCurrentOpponent());
		}
		else
			throw new OutOfEnergyException();
	}
	
	public void playTurn() throws InvalidMoveException{
		if (current.isFrozen())
			current.setFrozen(false);
		else{
			int moves = rollDice();
			board.moveMonster(current, moves, getCurrentOpponent());
		}
		switchTurn();
	}
	
	private void switchTurn(){
		if (this.current == player)
			 this.current = opponent;
		 else
			 this.current = player;
	}
	
	private boolean checkWinCondition(Monster monster){
		if (monster.getPosition() == Constants.WINNING_POSITION && monster.getEnergy() >= Constants.WINNING_ENERGY)
			return true;
		else
			return false;
	}
	
	public Monster getWinner(){
		if (checkWinCondition(player))
			return player;
		else if (checkWinCondition(opponent))
			return opponent;
		else
			return null;
	}
	
}