package game.engine.exceptions;

public class InvalidTurnException extends Exception {
	static final String MSG = "Action done on wrong turn";
	public InvalidTurnException(){
		super(MSG);
	}
	
	public InvalidTurnException(String message){
		super(message);
	}
	
	
}
