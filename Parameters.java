/* Stores parameters for use across simulation */
/* Start with parameters in source, implement input file later */
/* A completely static class.  */

public class Parameters {

	// host parameters
	public static final int initialN = 100;				// in individuals
	public static final double birthRate = 0.01;		// in births per individual per day, 1/30 years = 1/(30*365)
	public static final double deathRate = 0.01;		// in deaths per individual per day, 1/30 years = 1/(30*365)
	
	// epidemiological parameters
	public static final int initialI = 1;				// in individuals	
	public static final double beta = 0.5;				// in contacts per individual per day
	public static final double nu = 0.1;				// in recoveries per individual per day

}