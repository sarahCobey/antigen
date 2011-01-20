/* A population of host individuals */

import java.util.*;

public class HostPopulation {

	// fields
	private List<Host> susceptibles = new ArrayList<Host>();
	private List<Host> infecteds = new ArrayList<Host>();	
	private int cases = 0;
	
	// constructors
	public HostPopulation() {
		// fill population with Host objects
		int initialS = Parameters.initialN - Parameters.initialI;
		for (int i = 0; i < initialS; i++) {
			Host h = new Host();
			
			// sometimes start with an immunity to 0.0
			double chanceOfSuccess = Parameters.initialRecovered;
			if (Random.nextBoolean(chanceOfSuccess)) {
				Phenotype p = new Phenotype(-0.25);
				List<Phenotype> history = h.getHistory();
				history.add(p);
			}
			
			susceptibles.add(h);
		}
		// infect some individuals
		Virus urV = new Virus();	// ur-Virus
		for (int i = 0; i < Parameters.initialI; i++) {
			Virus v = new Virus(urV);
			Host h = new Host(v);
			infecteds.add(h);
		}		
	}
	
	// accessors
	public int getN() {
		return susceptibles.size() + infecteds.size();
	}
	public int getS() {
		return susceptibles.size();
	}
	public int getI() {
		return infecteds.size();
	}
	public double getPrS() {
		return (double) getS() / (double) getN();
	}
	public double getPrI() {
		return (double) getI() / (double) getN();
	}
	public int getRandomN() {
		return Random.nextInt(0,getN()-1);
	}
	public int getRandomS() {
		return Random.nextInt(0,getS()-1);
	}
	public int getRandomI() {
		return Random.nextInt(0,getI()-1);
	}	
	
	public void resetCases() {
		cases = 0;
	}
	public int getCases() {
		return cases;
	}	
	
	// draw a Poisson distributed number of births and add these hosts to the end of the population list
	public void grow() {
		double totalBirthRate = getN() * Parameters.birthRate;
		int births = Random.nextPoisson(totalBirthRate);
		for (int i = 0; i < births; i++) {
			Host h = new Host();
			susceptibles.add(h);
		}
	}
	
	// draw a Poisson distributed number of deaths and remove random hosts from the population list
	public void decline() {
		// deaths in susceptible class
		double totalDeathRate = getS() * Parameters.deathRate;
		int deaths = Random.nextPoisson(totalDeathRate);
		for (int i = 0; i < deaths; i++) {
			if (getS()>0) {
				int sndex = getRandomS();
				susceptibles.remove(sndex);
			}
		}		
		// deaths in infectious class		
		totalDeathRate = getI() * Parameters.deathRate;
		deaths = Random.nextPoisson(totalDeathRate);
		for (int i = 0; i < deaths; i++) {
			if (getI()>0) {
				int index = getRandomI();
				infecteds.remove(index);
			}
		}		
	}

	// draw a Poisson distributed number of contacts and move from S->I based upon this
	public void contact() {

		// each infected makes I->S contacts on a per-day rate of beta * S/N
		double totalContactRate = getI() * getPrS() * Parameters.beta;
		int contacts = Random.nextPoisson(totalContactRate);
		for (int i = 0; i < contacts; i++) {
			if (getS()>0 && getI()>0) {
		
				// get indices and objects
				int index = getRandomI();
				int sndex = getRandomS();
				Host iH = infecteds.get(index);
				Host sH = susceptibles.get(sndex);			
				Virus v = iH.getInfection();
				
				// attempt infection
				Phenotype p = v.getPhenotype();
				List<Phenotype> history = sH.getHistory();
				double chanceOfSuccess = p.riskOfInfection(history);
				if (Random.nextBoolean(chanceOfSuccess)) {
					sH.infect(v);
					susceptibles.remove(sndex);
					infecteds.add(sH);
					cases++;
				}
			
			}
		}		
		
	}
	
	// draw a Poisson distributed number of recoveries and move from I->S based upon this
	public void recover() {
		// each infected recovers at a per-day rate of nu
		double totalRecoveryRate = getI() * Parameters.nu;
		int recoveries = Random.nextPoisson(totalRecoveryRate);
		for (int i = 0; i < recoveries; i++) {
			if (getI()>0) {
				int index = getRandomI();
				Host h = infecteds.get(index);
				h.clearInfection();
				infecteds.remove(index);
				susceptibles.add(h);
			}
		}			
	}
	
	// draw a Poisson distributed number of mutations and mutate based upon this
	// mutate should not impact other Virus's Phenotypes through reference
	public void mutate() {
		// each infected mutates at a per-day rate of mu
		double totalMutationRate = getI() * Parameters.muPhenotype;
		int mutations = Random.nextPoisson(totalMutationRate);
		for (int i = 0; i < mutations; i++) {
			if (getI()>0) {
				int index = getRandomI();
				Host h = infecteds.get(index);
				h.mutate();
			}
		}			
	}	
	
	// draw a Poisson distributed number of samples and add them to the VirusSample
	public void sample() {
		double totalSamplingRate = Parameters.tipSamplingRate;
		int samples = Random.nextPoisson(totalSamplingRate);
		for (int i = 0; i < samples; i++) {
			if (getI()>0) {
				int index = getRandomI();
				Host h = infecteds.get(index);
				Virus v = h.getInfection();
				VirusSample.add(v);
			}
		}			
	}
	
	// through current infected population assigning ancestry as trunk
	public void makeTrunk() {
		for (int i = 0; i < getI(); i++) {
			Host h = infecteds.get(i);
			Virus v = h.getInfection();
			v.makeTrunk();
			while (v.getParent() != null) {
				v = v.getParent();
			//	if (v.isTrunk()) {
			//		break;
			//	} else {
					v.makeTrunk();
			//	}
			}
		}
	}	
	
	public double getDiversity(int sampleCount) {
		double meanDiversity = 0.0;
		for (int i = 0; i < sampleCount; i++) {
			if (getI()>0) {
				int index = getRandomI();
				Host hA = infecteds.get(index);
				Virus vA = hA.getInfection();
				index = getRandomI();
				Host hB = infecteds.get(index);
				Virus vB = hB.getInfection();
				meanDiversity += vA.distance(vB);
			}
		}	
		meanDiversity /= (double) sampleCount;
		return meanDiversity;
	}
	
	// output list of extant antigenic phenotypes
	public void printPhenotypes() {
		if (getI()>0) {
			for (int i = 0; i < getI(); i++) {
				Host h = infecteds.get(i);
				Virus v = h.getInfection();
				Phenotype p = v.getPhenotype();
				System.out.print(p + "\t");
			}
			System.out.println();
		}
	}

}