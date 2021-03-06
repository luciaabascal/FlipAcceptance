
package r2ms.flip.modW;



import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;
import java.lang.Math;

public class W {
	private float J[] = null;
	private int MIDDLE_VALUE = 4;
	private int NUM_OF_NEIGHBORS = 0;

	// Debug variable to print info messages
	// DEFAULT: false
	private boolean debug = false;
	
	// Number of neighbors array for each neighbor level
	private int[] numOfNeighborsArray  = null;
	
	// Energy Array for each neighbor and position level
	// Neighborhoods X energy
	private float energy[][] = null;

	// Lattice pointer
	private int lattice[][] = null;
	
	// Lattice size
	private int L = 0;
	
	// External magnetic field
	private float H = 0;
	
	// Random number generator
	private Random RandomGenerator = null;
	
	// neighbors position to obtain the energy
	// This array is composed by raw data in this order:
	// First neighbors xy position for each spin
	// Second neight...
	private ArrayList<Integer> neighborsPosition = new ArrayList<Integer>();
	
	private float energyChange = 0;
	
	/**
	 * IFlipAcceptance
	 * Constructor
	 * @param lattice Lattice pointer
	 * @param J Array of J values for each neighbor level
	 */

	public W (int[][] lattice, float []J, float H) {
		this.J = J;
		
		this.L = lattice[0].length;
		
		this.H = H;
		
		this.lattice = lattice;
		
		this.NUM_OF_NEIGHBORS = J.length;
		
		this.RandomGenerator = new Random(6969);
		
		this.energy = this.computeEnergyArray(J);
	}

	
	/**
	 * 
	 */
	private ArrayList<Integer> radiusOfNeighbors(int NUMBER_OF_NEIGHBORS) {
		
		System.out.print("Getting for  " + NUMBER_OF_NEIGHBORS + " neighbors");
		ArrayList<Integer> neighborsR = new ArrayList<Integer>();
	
		int ini = 0;
		int max = 1;
		while (neighborsR.size() < NUMBER_OF_NEIGHBORS) {
			
			for (int a = ini; a < max; a++) {
				for (int b = ini; b < max; b++) {
					int r = a * a + b * b;
					
					// If radius is zero, its the central spin
					if (r == 0) {
						continue;
					}
		
					// Add to neighbors only if its not added 
					if ( !neighborsR.contains(r) ) {
						neighborsR.add(r);
					} 
				}
			}
			ini = max;
			max += max;
		}
		
		return neighborsR;
		
	}
	
	public float getEnergyChange() {
		return this.energyChange;
	}
	/**
	 * getNumberOfNeighbors
	 * Return a array with the number of neighbors in each position, for position 0, first
	 * neighbors number.
	 * For example [4,4,4,8] for 1st, 2nd, 3th, 4th neighbors 
	 * @return{int[]} Array with the number of neighbors in each position.
	 */
	private int[] getNumberOfNeighbors() {
		// Get neighborhood radius
		

		ArrayList<Integer> neighborhoodsR = this.radiusOfNeighbors(this.NUM_OF_NEIGHBORS);
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
				if (a * a + b * b == rSquared) {
					
					// If a or b is 0, then we have only 'a half' of spin
					if (a == 0 || b == 0) {
						numOfNeighbors += 0.5;
					} else {
						numOfNeighbors++;
					}
					
					// Neighbor determination array
					
					// In case of a or b is zero, the neighbors generation 
					// may be generate once for all quadrants. In other case
					// it will counted twice.
					
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
						continue; // Next step
						
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
	
	// Generate array of energy for each neighbor:
	/**
	 * computeEnergyArray
	 * Computes the probability due to energy for each neighbor level and possible value of spin configuration.
	 * Possibles positions are (for 8 neighbors):
	 * Spin Energy probability
	 * -8	1
	 * -6	1
	 * -4	1
	 * -2	1
	 *  0	1
	 *  2	exp(-2J*2)
	 *  4	exp(-2J*4)
	 *  6	exp(-2J*6)
	 *  8	exp(-2J*8)
	 * @param{float} 		J Array of J value for each neighbor level 
	 * @return{float[][]} 	A two dimension float array of neighbor level X energy
	 */

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

			for (int j = -nOfNeighbors; j<=nOfNeighbors; j+=2) {
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
	
	/**
	 * doIAccept
	 * Decides whether a given spin in lattice should flip or not
	 * @param{int} x position of spin in lattice
	 * @param{int} y position of spin in lattice
	 * @return{Boolean} Whether spin flips or not
	 */
	public boolean doIAccept(int x, int y) {
		
		int neightborPositionPointer = 0;
		
		float EChange = 0;
		
		float totalProbability = 1;
		// Compute probability for each neighborhood
		for (int i = 0; i<this.NUM_OF_NEIGHBORS; i++) {
			
			// For each neighborhood, compute the energy (get the value of each neighbor)
			int NUM_OF_NEIGH = this.numOfNeighborsArray[i];
			
			int ici = this.lattice[x][y];
			int ien = 0;
			for (int j = 0; j<NUM_OF_NEIGH; j++ ) {
				int xPos = x + this.neighborsPosition.get(neightborPositionPointer++);
				int yPos = y + this.neighborsPosition.get(neightborPositionPointer++);
				
				//System.out.println(dx + "," + dy);
				
				// Ciclic boundary conditions
				if (xPos >= this.L) {
					xPos -= this.L;
				}
				
				if (xPos < 0) {
					xPos += this.L;
				}
				
				if (yPos >= this.L) {
					yPos -= this.L;
				}
				
				if (yPos < 0) {
					yPos += this.L;
				}
				
				//System.out.println("Neightbor = " + xPos + ", " + yPos);
				ien += this.lattice[xPos][yPos];
				
				
				
				
				//System.out.println("["+x+","+y+"][" + xPos + ", " + yPos + "] " + this.lattice[xPos][yPos]);
			}

			EChange += 2 * J[i] * ien;
			
			totalProbability *= this.energy[i][this.MIDDLE_VALUE + ien * ici];
			
		}
		
		this.energyChange = EChange;
	
		//System.out.println("Probability = "+ totalProbability);
		return this.RandomGenerator.nextFloat() < totalProbability;
	}
}
