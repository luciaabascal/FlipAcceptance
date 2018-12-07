import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;
import java.lang.Math;

public class IFlipAcceptance {
	private float J[] = null;
	private int MIDDLE_VALUE = 4;
	private int NUM_OF_NEIGHBORS = 0;

	private boolean debug = true;
	
	private int[] numOfNeighborsArray  = null;
	
	private float energy[][] = null; // Array of Neighborhoods X energy

	private int lattice[][] = null;
	private int L = 0;
	private Random generator = null;
	
	// neighbors position to obtain the energy
	// This array is composed by raw data in this order:
	// First neighbors xy position for each spin
	// Second neight...
	private ArrayList<Integer> neighborsPosition = new ArrayList<Integer>();
	

	public IFlipAcceptance (int[][] lattice, float []J) {
		this.J = J;
		
		this.L = lattice[0].length-1;
		
		this.lattice = lattice;
		
		this.NUM_OF_NEIGHBORS = J.length;
		
		this.generator = new Random(6969);
		
		this.energy = this.computeEnergyArray(J);
	}

	private int[] getNumberOfNeighbors() {
		// Get neighborhood radius
		int max = 10;
		ArrayList<Integer> neighborhoodsR = new ArrayList<Integer>();

		for (int a = 0; a < max; a++) {
			for (int b = 0; b < max; b++) {
				int r = a * a + b * b;
				if (r == 0) { // Pasar del 0, soy yo
					continue;
				}

				if ( !neighborhoodsR.contains(r) ) {
					neighborhoodsR.add(r);
				} 
			}
		}

		Collections.sort(neighborhoodsR);

		int[] numOfNeighborsArr = new int[this.NUM_OF_NEIGHBORS];

		// For each neighborhood, get how many neighbors we have
		//Collections.max(
		int MAX_VAL_OF_NEIGHBORS = 0;
		
		
		
		for (int i = 0; i < this.NUM_OF_NEIGHBORS; i++) {
			float numOfNeighbors = 0;

			// Get the maximun neighbor
			int maxNumber = (int)Math.round(Math.sqrt(neighborhoodsR.get(i)));

			int rSquared = neighborhoodsR.get(i);

			// For each integer, check if it is in lattice:
			for(int a = 0; a <= maxNumber; a++) {



				// Exact square root checker
				int b = (int)Math.round(Math.sqrt(rSquared - a * a));

				if (this.debug) {
					System.out.println("[DEBUG] " + rSquared + " <=>b = " + b + ", a=" +a);
				}

				// Check if a^2+b^2 = r^2 
				if (a*a+b*b == rSquared) {
					if (a == 0 || b == 0) {
						if (this.debug) {
							System.out.println("[DEBUG] IN" );
						}
						numOfNeighbors += 0.5;
					} else {
						numOfNeighbors++ ;
					}
					
					if (a == 0) {
						continue;
					}
					
					if (b == 0) {
						// First quadrant
						neighborsPosition.add(a); // X position
						neighborsPosition.add(b); // Y position
						
						// Second quadrant
						neighborsPosition.add(b); // X position
						neighborsPosition.add(a); // Y position
						
						// Third quadrant
						neighborsPosition.add(b); // X position
						neighborsPosition.add(-a); // Y position
						
						// Fourth quadrant
						neighborsPosition.add(-a); // X position
						neighborsPosition.add(b); // Y position
						continue;
						
						
					}
					// First quadrant
					neighborsPosition.add(a); // X position
					neighborsPosition.add(b); // Y position
					
					// Second quadrant
					neighborsPosition.add(-a); // X position
					neighborsPosition.add(b); // Y position
					
					// Third quadrant
					neighborsPosition.add(a); // X position
					neighborsPosition.add(-b); // Y position
					
					// Fourth quadrant
					neighborsPosition.add(-a); // X position
					neighborsPosition.add(-b); // Y position
					
					
				}
			}
			

			if (this.debug) {
				switch ((i+1)%10) {
					
					case 1:
						System.out.format("%dst neighbors, with R^2 =\t %d", (i+1), rSquared);
						break;
					
					case 2:
						System.out.format("%dnd neighbors, with R^2 =\t %d", (i+1), rSquared);
						break;
					
					case 3:
						System.out.format("%drd neighbors, with R^2 =\t %d", (i+1), rSquared);
						break;
	
					default:
						System.out.format("%dth neighbors, with R^2 =\t %d", (i+1), rSquared);
						break;
	
				}
				System.out.format("\t : \t %2.0f \t neighbors \n", numOfNeighbors*4);
			}
			numOfNeighborsArr[i] = (int)numOfNeighbors*4;
			if ( numOfNeighbors*4 > MAX_VAL_OF_NEIGHBORS) { MAX_VAL_OF_NEIGHBORS = (int)numOfNeighbors*4; }
		}

		this.MIDDLE_VALUE = MAX_VAL_OF_NEIGHBORS;
		return numOfNeighborsArr;
	}

	private float[][] computeEnergyArray (float []J) {

		// Get the number of neighbors for each
		this.numOfNeighborsArray = this.getNumberOfNeighbors();
		
		int[] numOfNeighbors = numOfNeighborsArray;

		// Compute energy for each neighbors
		int len = numOfNeighbors.length;
		float[][] energyArray = new float[len][];

		// Compute energy for each neighbors
		for (int i = 0; i<len; i++) {

			// Num of neighbors of i neighborhood
			int nOfNeighbors = numOfNeighbors[i];
			
			energyArray[i] = new float[2*this.MIDDLE_VALUE + 1];

			for (int j = -nOfNeighbors; j<=nOfNeighbors; j++) {
				if (j > 0) {
					energyArray[i][this.MIDDLE_VALUE + j] = (float)Math.exp(-2 * J[i] * j);
				} else {
					energyArray[i][this.MIDDLE_VALUE + j] = 1;
				}
				
				if(debug) {
					System.out.format("i = %d, J[i] = %f, nOfNeighbors = %d\n",i,  J[i], nOfNeighbors);
					System.out.format("Energy j = %d , E = %f\n", j, energyArray[i][this.MIDDLE_VALUE + j]);
				}
			}
		}

		return energyArray;

	}
	
	public boolean doIAccept(int x, int y) {
		
		int neightborPositionPointer = 0;
		
		float totalProbability = 1;
		// Compute probability for each neighborhood
		for (int i = 0; i<this.NUM_OF_NEIGHBORS; i++) {
			
			// For each neighborhood, compute the energy (get the value of each neighbor)
			int NUM_OF_NEIGH = this.numOfNeighborsArray[i];
			
			int ici = this.lattice[(this.L + x)%this.L][(this.L + y)%this.L];
			int ien = 0;
			for (int j = 0; j<NUM_OF_NEIGH; j++ ) {
				int dx = this.neighborsPosition.get(neightborPositionPointer++);
				int dy = this.neighborsPosition.get(neightborPositionPointer++);
				
				//sSystem.out.println("Dx = "+ dx + ", dy = " + dy + " val = " + this.lattice[x + dx][y + dy]);
				
				ien += this.lattice[(this.L + (x + dx))%this.L][(this.L + (y + dy))%this.L];
			}
		//	System.out.println("IEN = " + ien + ", ICI = " + ici);
			totalProbability*=this.energy[i][this.MIDDLE_VALUE + ien*ici];
		}
	
		//System.out.println("Probability = "+ totalProbability);
		return this.generator.nextFloat() < totalProbability;
	}
}
