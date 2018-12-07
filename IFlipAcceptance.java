import java.util.ArrayList;
import java.util.Collections;

public class IFlipAcceptance {
	private float J[] = null;
	private int MIDDLE_VALUE = 4;
	private int NUM_OF_NEIGHBORS = 0;

	private float energy[][] = null; // Array of Neighborhoods X energy

	private int lattice[][] = null;

	public IFlipAcceptance (int[][] lattice, float []J) {
		this.J = J;
		this.NUM_OF_NEIGHBORS = J.length;

		

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

				if (debug) {
					System.out.println("[DEBUG] " + rSquared + " <=>b = " + b + ", a=" +a);
				}

				// Check if a^2+b^2 = r^2 
				if (a*a+b*b == rSquared) {
					if (a == 0 || b == 0) {
						if (debug) {
							System.out.println("[DEBUG] IN" );
						}
						numOfNeighbors += 0.5;
					} else {
						numOfNeighbors++ ;
					}
					
				}
			}

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

			if ( numOfNeighbors*4 > MAX_VAL_OF_NEIGHBORS) { MAX_VAL_OF_NEIGHBORS = numOfNeighbors*4; }
		}

		this.MIDDLE_VALUE = MAX_VAL_OF_NEIGHBORS;

	}

	private float[][] computeEnergyArray (float []J) {

		// Get the number of neighbors for each 
		int[] numOfNeighbors = this.getNumberOfNeighbors(this.NUM_OF_NEIGHBORS);

		// Compute energy for each neighbors
		int len = numOfNeighbors.length;
		float[][] energyArray = new float[len][];

		// Compute energy for each neighbors
		for (int i = 0; i<len; i++) {

			// Num of neighbors of i neighborhood
			int nOfNeighbors = numOfNeighbors[i];
			
			energyArray[i] = new float[2*nOfNeighbors + 1];

			for (int j = -nOfNeighbors; j<=nOfNeighbors; j++) {
				if (j > 0) {
					energyArray[i][this.MIDDLE_VALUE + j] = Math.exp(-2 * J[i] * (j - nOfNeighbors));
				} else {
					energyArray[i][this.MIDDLE_VALUE + j] = 1;
				}
			}
		}

		return energyArray;

	}
}