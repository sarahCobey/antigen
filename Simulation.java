/* Simulation functions, holds the host population */

import java.util.*;
import java.io.*;

import com.javamex.classmexer.*;

public class Simulation {

	// fields
	private List<HostPopulation> demes = new ArrayList<HostPopulation>();
	private double diversity;
	private double tmrca;
	private double netau;
	
	// constructor
	public Simulation() {
		for (int i = 0; i < Parameters.demeCount; i++) {
			if (Parameters.restartFromCheckpoint) {
				HostPopulation hp = new HostPopulation(i, true);
				demes.add(hp);
			}
			else {
				HostPopulation hp = new HostPopulation(i);
				demes.add(hp);
			}
		}
	}
	
	// methods
	
	public int getN() {
		int count = 0;
		for (int i = 0; i < Parameters.demeCount; i++) {
			HostPopulation hp = demes.get(i);
			count += hp.getN();
		}
		return count;
	}
	
	public int getS() {
		int count = 0;
		for (int i = 0; i < Parameters.demeCount; i++) {
			HostPopulation hp = demes.get(i);
			count += hp.getS();
		}
		return count;
	}	
	
	public int getI() {
		int count = 0;
		for (int i = 0; i < Parameters.demeCount; i++) {
			HostPopulation hp = demes.get(i);
			count += hp.getI();
		}
		return count;
	}	
	
	public int getR() {
		int count = 0;
		for (int i = 0; i < Parameters.demeCount; i++) {
			HostPopulation hp = demes.get(i);
			count += hp.getR();
		}
		return count;
	}		
	
	public int getCases() {
		int count = 0;
		for (int i = 0; i < Parameters.demeCount; i++) {
			HostPopulation hp = demes.get(i);
			count += hp.getCases();
		}
		return count;
	}	
	
	public double getDiversity() {
		return diversity;
	}		
	
	public double getNetau() {
		return netau;
	}	
	
	public double getTmrca() {
		return tmrca;
	}			
	
	// proportional to infecteds in each deme
	public int getRandomDeme() {
		int n = Random.nextInt(0,getN()-1);
		int d = 0;
		int target = (demes.get(0)).getN();
		while (n < target) {
			d += 1;
			target += (demes.get(d)).getN();
		}
		return d;
	}
	
	// return random virus proportional to worldwide prevalence
	public Virus getRandomInfection() {
	
		Virus v = null;
		
		if (getI() > 0) {
	
			// get deme proportional to prevalence
			int n = Random.nextInt(0,getI()-1);
			int d = 0;
			int target = (demes.get(0)).getI();
			while (d < Parameters.demeCount) {
				if (n < target) {
					break;
				} else {
					d++;
					target += (demes.get(d)).getI();
				}	
			}
			HostPopulation hp = demes.get(d);
					
			// return random infection from this deme
			if (hp.getI()>0) {
				Host h = hp.getRandomHostI();
				v = h.getInfection();
			}
		
		}
		
		return v;
		
	}
	
	// return random host from random deme
	public Host getRandomHost() {
		int d = Random.nextInt(0,Parameters.demeCount-1);
		HostPopulation hp = demes.get(d);
		return hp.getRandomHost();
	}
	
	public double getAverageRisk(Phenotype p) {
		
		double averageRisk = 0;
		for (int i = 0; i < 10000; i++) {
			Host h = getRandomHost();
			Phenotype[] history = h.getHistory();
			averageRisk += p.riskOfInfection(history);
		}
		averageRisk /= 10000.0;
		return averageRisk;
		
	}
	
	public void printImmunity() {
	
		try {
			File immunityFile = new File("out.immunity");
			immunityFile.delete();
			immunityFile.createNewFile();
			PrintStream immunityStream = new PrintStream(immunityFile);
			
			for (double x = VirusTree.xMin; x <= VirusTree.xMax; x += 0.5) {
				for (double y = VirusTree.yMin; y <= VirusTree.yMax; y += 0.5) {
				
					Phenotype p = PhenotypeFactory.makeArbitaryPhenotype(x,y);
					double risk = getAverageRisk(p);
					immunityStream.printf("%.4f,", risk);
				
				}
				immunityStream.println();
			}
			
			immunityStream.close();
		} catch(IOException ex) {
			System.out.println("Could not write to file"); 
			System.exit(0);
		}
	
	}
	
	public void printHostPopulation() {
	
		try {
			File hostFile = new File("out.hosts");
			hostFile.delete();
			hostFile.createNewFile();
			PrintStream hostStream = new PrintStream(hostFile);
			for (int i = 0; i < Parameters.demeCount; i++) {
				HostPopulation hp = demes.get(i);
				hp.printHostPopulation(hostStream);
			}
			hostStream.close();
		} catch(IOException ex) {
			System.out.println("Could not write to file"); 
			System.exit(0);
		}
	
	}	
		
	public void makeTrunk() {
		for (int i = 0; i < Parameters.demeCount; i++) {
			HostPopulation hp = demes.get(i);
			hp.makeTrunk();
		}
	}	
	
	public void printState() {
	
		System.out.printf("%d\t%.3f\t%.3f\t%.3f\t%d\t%d\t%d\t%d\t%d\n", Parameters.day, getDiversity(), getTmrca(),  getNetau(), getN(), getS(), getI(), getR(), getCases());
		
		if (Parameters.memoryProfiling && Parameters.day % 10 == 0) {
			long noBytes = MemoryUtil.deepMemoryUsageOf(this);
			System.out.println("Total: " + noBytes);
			HostPopulation hp = demes.get(1);
			noBytes = MemoryUtil.deepMemoryUsageOf(hp);
			System.out.println("One host population: " + noBytes);
			Host h = hp.getRandomHostS();
			noBytes = MemoryUtil.deepMemoryUsageOf(h);
			System.out.println("One susceptible host with " +  h.getHistoryLength() + " previous infection: " + noBytes);
			//h.printHistory();
			if (getI() > 0) {
				Virus v = getRandomInfection();
				noBytes = MemoryUtil.memoryUsageOf(v);
				System.out.println("One virus: " + noBytes);
				noBytes = MemoryUtil.deepMemoryUsageOf(VirusTree.getTips());
				System.out.println("Virus tree: " + noBytes);
			}
		}
		
	}

	public void printHeader(PrintStream stream) {
		stream.print("date\tdiv\ttmrca\tnetau\ttotalN\ttotalS\ttotalI\ttotalR\ttotalCases");
		for (int i = 0; i < Parameters.demeCount; i++) {
			String name = Parameters.demeNames[i];
			stream.printf("\t%sDiv\t%sTmrca\t%sNetau\t%sN\t%sS\t%sI\t%sR\t%sCases", name, name, name, name, name, name, name, name);
		}
		stream.println();
	}
	
	public void printState(PrintStream stream) {
		if (Parameters.day > Parameters.burnin) {
			stream.printf("%.4f\t%.4f\t%.4f\t%.4f\t%d\t%d\t%d\t%d\t%d", Parameters.getDate(), getDiversity(), getTmrca(), getNetau(), getN(), getS(), getI(), getR(), getCases());
			for (int i = 0; i < Parameters.demeCount; i++) {
				HostPopulation hp = demes.get(i);
				hp.printState(stream);
			}
			stream.println();
		}
	}	
		
	public void updateDiversity() {
		diversity = 0.0;
		tmrca = 0.0;
		int sampleCount = Parameters.diversitySamplingCount;
		for (int i = 0; i < sampleCount; i++) {
			Virus vA = getRandomInfection();
			Virus vB = getRandomInfection();
			if (vA != null && vB != null) {
				double dist = vA.distance(vB);
				diversity += dist;
				if (dist > tmrca) {
					tmrca = dist;
				}
			}
		}	
		diversity /= (double) sampleCount;
		tmrca /= 2.0;
	}	
	
	public void updateNetau() {
		double coalCount = 0.0;	
		double coalOpp = 0.0;
		double coalWindow = Parameters.netauWindow / 365.0;
		int sampleCount = Parameters.netauSamplingCount;
		for (int i = 0; i < sampleCount; i++) {
			Virus vA = getRandomInfection();
			Virus vB = getRandomInfection();
			if (vA != null && vB != null) {
				coalOpp += coalWindow;
				coalCount += vA.coalescence(vB, coalWindow);
			}
		}	
		netau = coalOpp / coalCount;
	}		
		
	public void resetCases() {
		for (int i = 0; i < Parameters.demeCount; i++) {	
			HostPopulation hp = demes.get(i);
			hp.resetCases();
		}
	}
		
	public void stepForward() {
				
		for (int i = 0; i < Parameters.demeCount; i++) {		
			HostPopulation hp = demes.get(i);
			hp.stepForward();
			for (int j = 0; j < Parameters.demeCount; j++) {
				if (i != j) {
					HostPopulation hpOther = demes.get(j);
					hp.betweenDemeContact(hpOther);
				}
			}
		}
							
		Parameters.day++;
		
	}
	
	public void run() {
	
		try {
		
			File seriesFile = new File("out.timeseries");		
			seriesFile.delete();
			seriesFile.createNewFile();
			PrintStream seriesStream = new PrintStream(seriesFile);
			System.out.println("day\tdiv\ttmrca\tnetau\tN\tS\tI\tR\tcases");
			printHeader(seriesStream);
							
			for (int i = 0; i < Parameters.endDay; i++) {
				
				stepForward();
				
				if (Parameters.day % Parameters.printStep == 0) {
					updateDiversity();
					updateNetau();
					printState();
					printState(seriesStream);
					resetCases();
				}
				
				if (getI()==0) {
					if (Parameters.repeatSim) {
						reset();
						i = 0; 
						seriesFile.delete();
						seriesFile.createNewFile();
						seriesStream = new PrintStream(seriesFile);
						printHeader(seriesStream);
					} else {
						break;
					}
				}
			}
			
			seriesStream.close();
		} catch(IOException ex) {
			System.out.println("Could not write to file"); 
			System.exit(0);
		}	
					
		// tree reduction
		VirusTree.pruneTips();
		VirusTree.markTips();		
	
		// tree prep
		makeTrunk();
		VirusTree.fillBackward();			
		VirusTree.sortChildrenByDescendants();
		VirusTree.setLayoutByDescendants();
		VirusTree.streamline();			
		
		// rotation
		if (Parameters.pcaSamples) {
			VirusTree.rotate();
			VirusTree.flip();
		}
		
		// tip and tree output
		VirusTree.printTips();			
		VirusTree.printBranches();	
		
		// mk output
		VirusTree.printMK();
		
		// immunity output
		if (Parameters.phenotypeSpace == "geometric") {
			VirusTree.updateRange();
			VirusTree.printRange();
			if (Parameters.immunityReconstruction) {
				printImmunity();
			}
		}		
		
		// detailed output
		if (Parameters.detailedOutput) {
			printHostPopulation();
		}	
		
	}
	
	public void reset() {
		Parameters.day = 0;
		diversity = 0;
		for (int i = 0; i < Parameters.demeCount; i++) {
			HostPopulation hp = demes.get(i);
			hp.reset();
		}
		VirusTree.clear();
	}

}