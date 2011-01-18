/* Simulation functions, holds the host population */

public class Simulation {

	// fields
	private HostPopulation hostPop;
	private int day = 0;
	
	// constructor
	public Simulation() {
		hostPop = new HostPopulation();
	}
	
	// methods
	
	public void printState() {
		System.out.println("Day " + day);
		System.out.println("N: " + hostPop.getN());
		System.out.println("S: " + hostPop.getS());
		System.out.println("I: " + hostPop.getI());		
		System.out.println();	
	}
	
	public void stepForward() {
		
		// check to see if host population exists...
	
		hostPop.grow();
		hostPop.decline();
		hostPop.contact();
		hostPop.recover();
		day++;
		
	}

}