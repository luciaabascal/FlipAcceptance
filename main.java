import java.util.ArrayList;
import java.util.Collections;

public class main {

	private static boolean debug = false;

	public static void main(String[] args) {
		
		float jktc = 0.5f*(float)Math.log(1.0f+Math.sqrt(2.0f));
		
		float Jkt = jktc/0.95f;
		
		int L = 30;
		int[][] lattice;
		
		lattice = new int[L][];
	    
	    for(int i=0; i<L; i++){
	      lattice[i] = new int[L];
	    }

		for(int i = 0; i<L; i++) {
	      for (int j = 0; j<L; j++) {
	        lattice[i][j] = -1;
	      }
	    }

		float[] J = {Jkt};
		
		IFlipAcceptance twest = new IFlipAcceptance(lattice, J);
		for(int k = 0; k<200; k++ ) {
			for (int i = 0; i< L; i++) {
				for (int j=0; j<L; j++) {
					if (twest.doIAccept(i, j)) {
						lattice[i][j] *=-1;
					}
				}
			}
		}
		for (int i = 0; i < L; i++) {
			for (int j = 0; j < L; j++) {
				System.out.print(lattice[i][j] + " ");
			}
			System.out.println("");
		}
	
	}
}