import java.util.ArrayList;
import java.util.Collections;

public class main {

	private static boolean debug = false;

	public static void main(String[] args) {
		int[][] L = {
				{-1,1,-1,1},
				{1,1,1,1},
				{-1,1,-1,1},
				{1,1,-1,1}
		};

		float[] J = {1, 0.5f, 0.2f};
		IFlipAcceptance twest = new IFlipAcceptance(L, J);
		for(int k = 0; k<200; k++ ) {
			for (int i = 0; i< 4; i++) {
				for (int j=0; j<4; j++) {
					if (twest.doIAccept(i, j)) {
						L[i][j] *=-1;
					}
				}
			}
		}
		for (int i = 0; i< 4; i++) {
			for (int j=0; j<4; j++) {
				System.out.print(L[i][j] + " ");
			}
			System.out.println("");
		}
	
	}
}