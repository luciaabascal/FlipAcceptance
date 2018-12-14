package r2ms;
import java.util.Random;
import java.util.Scanner;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.File;

/**

*
*/

public class Ising {

  /**
   * Main
   * @param args
   */
  public static void main(String[] args) {
    
    //  Define and initialize some of the variables. 
      
    int[][] lattice; //Spin map of the system
    int[] ip,  im; //map of first neighbours
    float[] w = new float[9]; // Array from -4 to 4 and probability exp^-2(E/kT)

    float Jkt;
    float external_magnetic_field; //External magnetic field
    float energy; //Energy
    
    int i, j, L;
    int nsample, ndelta, nequil, mcsmax; //Simulation steps; Ndelta steps; Nequil steps for thermalization; mcsmax Maximum number of MC Steps.

    int ien, ici, m; //Initial magnetic moment

    float mag, ave_mag, ave_mag2, ji; //Magnetization; average of magnetization and average of the square of the magnetization.
    float acc_mag, acc_mag2, acc_energy, acc_energy2; //Accumulated values for the magnetization, the square of the magnetization, the total energy and the square of the energy
    float jktc, red_temp, invsize; //Jkt = jktc/red_temp; Reduced temperature; Inverse of the surface


    int iseed = 21;

    /* In order to obtain the values of our system, we need to
     * ask user to insert these values using System input 
     */ 
    Scanner sc = new Scanner(System.in);

    Random RandomGen = new Random(iseed);
    

    jktc = 0.5f*(float)Math.log(1.0f+Math.sqrt(2.0f));
    System.out.format("JkTc=%f\n", jktc);

    // Read the number of spins per cartesian direction
    System.out.format("System size L: ");
    L = sc.nextInt();

    // Define the lattice of spins. In each position of the first array we put other array
    
    lattice = new int[L][];
    
    for(i=0; i<L; i++){
      lattice[i] = new int[L];
    }
           
    ip = new int[L];
    im = new int[L];

    /*
     *  Read values from user
     */
    // Ask user for reduced temperature and store it in red_temp variable
    System.out.format("Reduced temperature (T/Tc): ");
    red_temp = sc.nextFloat();

    Jkt = jktc/red_temp;
    
    System.out.format("Maximum number of MC Steps (Mcsmax):\n");
    System.out.format("Use for average every (Ndelta) steps:\n");
    System.out.format("Neglect first (Nequil) steps for thermalization:\n");
    System.out.format("Enter mcsmax: ");
    mcsmax = sc.nextInt();

    System.out.format("ndelta: ");
    ndelta = sc.nextInt();
    
    System.out.format("nequil: ");
    nequil = sc.nextInt();

    // Read the external magnetic field
    System.out.format("External magnetic field, H: ");
    external_magnetic_field = sc.nextFloat();
    

    //
    // Initialize lattice. For the first steps, all the spins are pointing down
    //
    for(i = 0; i<L; i++) {
      for (j = 0; j<L; j++) {
        lattice[i][j] = -1;
      }
    }
    

    // If all the spins are aligned initially pointed downwards,
    // then the initial magnetic moment is equal to the total number
    // of spins (L * L).
    // The minus sign comes from the fact of the spin direction (downwards)
    m = -L*L;

    // The magnetization (in 2D) is defined as the 
    // magnetic moment per unit of surface.
    // Here we define the inverse of the surface.
    invsize = 1.0f/(float)(L*L);

    // We define the magnetization here
    mag = invsize*m;

    // We define the zero of energy for this particular configuration
    // The zero of energy is always ill-defined, so we can set this
    // value to whatever configuration we want.
    // The only important thing is the difference of energy between
    // different configurations
    energy = 0;

    //
    // Simulation
    //

    nsample = 0;

    // We initialize the accumulated values for
    // the magnetization:               acc_mag
    // the square of the magnetization: acc_mag2
    // the total energy               : acc_energy
    // the square of the energy       : acc_energy2
    acc_mag     = 0;
    acc_mag2    = 0;
    acc_energy  = 0;
    acc_energy2 = 0;
    
    
    // IFlipAcceptance
    float[] JJ = {Jkt, (float)0.1 * Jkt }; 
    IFlipAcceptance flipAcceptance = new IFlipAcceptance(lattice, JJ, external_magnetic_field);
    
    // Main loop in order to calculate the energy for each sweep
    for(int mcs = 1; mcs <= mcsmax; mcs++) {

      // For each loop, compute the lattice energy for first neightbor
     for (i = 0; i<L; i++) {

        for (j = 0; j<L; j++) {



            // Check if spin flips
            if (flipAcceptance.doIAccept(i, j)) {
            	ici = lattice[i][j];
              // Spin flip if is accepted
              lattice[i][j] = -ici;

              // Update the value of the energy
              // The factor of 2 is for the double counting
              energy = energy + flipAcceptance.getEnergyChange();

              // Update the value of the magnetic moment
              // If initially all the magnetic moments are pointing down,
              // then the magnetic dipole of the "cluster" is -5.
              // If one is reverted, then there are four pointing down
              // and one pointing up, so the new magnetic moment
              // of the cluster is - 4 (down) + 1 (up) = -3,
              // i. e. we have changed the magnetization by a value of +2,
              // In this particular case, assuming the initial dipole
              // moment pointing down, then ici = -1 and
              // m = m - 2*ici = m +2
              m = m - 2*ici;
           }
         }
        }

      //    Perform the averages only if we have finished the equilibration
      if (mcs > nequil) {

        if (mcs % ndelta == 0) {
           nsample++;

           mag = invsize * m;
           System.out.format("%6d\t %8.6f\t %10.6f\n", nsample, invsize * energy, mag);
           acc_mag = acc_mag + mag;
           acc_mag2 = acc_mag2 + mag * mag;
         }
      }
    }

    ave_mag = acc_mag/nsample;
    ave_mag2 = acc_mag2/nsample;

    //
    // Susceptibility
    // This is given by the fluctuation dissipation theorem,
    // see, for instance, Eq. (2.13) of the book by Landau and Binder, page 11
    // $k_{\rm B} T \chi = \langle M^{2} \rangle - \langle M \rangle^{2}
    ji = Jkt * L*L * ( ave_mag2 - ave_mag*ave_mag);


    // Create file SAMPLES if not exists
    File sample_file = new File("SAMPLES");

    try {
      sample_file.createNewFile();
    } catch (IOException e) {
      System.out.print("Error: file file can not be created!");
      // Print error in screen 
      e.printStackTrace();
    }

    try {
      PrintWriter writer = new PrintWriter(sample_file);
      writer.format("%3d, %8.4f, M: %10.4f, Suscep: %10.4f, %d, %d, %d \n", L, red_temp, ave_mag, ji, mcsmax, ndelta, nequil);
      writer.close();
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    }


    // Create file PHOTO if not exists
    File photo_file = new File("PHOTO");
    try {
      photo_file.createNewFile();
    } catch (IOException e) {
      System.out.print("Error: file file can not be created!");
      // Print error in screen 
      e.printStackTrace();
    }

    try {
      PrintWriter writer = new PrintWriter(photo_file);
      for(i = 0; i<L; i++){
        for(j = 0; j<L; j++){
          writer.format("%d", lattice[i][j]+1);
        }
        writer.println("");
      }
      writer.close();
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    }

  }

}