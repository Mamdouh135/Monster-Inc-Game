package game.engine.exceptions;

public class OutOfEnergyException extends Exception {
	 static final String MSG = "Not Enough Energy for Power Up";
	 public OutOfEnergyException(){
		 super(MSG);
	 }
	 OutOfEnergyException(String message){
		 super(message);
	 }
	
	
}
