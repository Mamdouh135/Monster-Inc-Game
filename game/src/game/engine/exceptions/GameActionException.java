package game.engine.exceptions;

public abstract class GameActionException extends Exception {
	public GameActionException(){
		super();
	}
	GameActionException(String message){
		super(message);
	}
	
	
}
