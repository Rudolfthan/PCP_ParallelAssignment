//Copyright M.M.Kuttel 2024 CSC2002S, UCT
package ParallelAbelianSandpile;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.RecursiveTask;

import javax.imageio.ImageIO;

//This class is for the grid for the Abelian Sandpile cellular automaton
public class parallelGrid extends RecursiveTask<Boolean>{
	private int rows, columns;
	private int [][] grid; //grid 
	private int [][] updateGrid;//grid for next time step
	private int lo, hi;
	private static final  int SEQUENTIAL_CUTOFF = 100;

    
	public parallelGrid(int w, int h) {
		rows = w+2; //for the "sink" border
		columns = h+2; //for the "sink" border
		grid = new int[this.rows][this.columns];
		updateGrid=new int[this.rows][this.columns];
		/* grid  initialization */
		for(int i=0; i<this.rows; i++ ) {
			for( int j=0; j<this.columns; j++ ) {
				grid[i][j]=0;
				updateGrid[i][j]=0;
			}
		}
	}
	public parallelGrid(int [][] grid ,int [][] updateGrid , int lo, int hi) {
		this.grid=grid;
		this.updateGrid=updateGrid;
		this.lo=lo;
		this.hi=hi;
		this.rows=grid.length;
		this.columns=grid[0].length;


	}

	public parallelGrid(int[][] newGrid) {
		this(newGrid.length,newGrid[0].length); //call constructor above
		//don't copy over sink border
		for(int i=1; i<rows-1; i++ ) {
			for( int j=1; j<columns-1; j++ ) {
				this.grid[i][j]=newGrid[i-1][j-1];
			}
		}
		
	}
	public parallelGrid(parallelGrid copyGrid) {
		this(copyGrid.rows,copyGrid.columns); //call constructor above
		/* grid  initialization */
		for(int i=0; i<rows; i++ ) {
			for( int j=0; j<columns; j++ ) {
				this.grid[i][j]=copyGrid.get(i,j);
			}
		}
	}
	
	public int getRows() {
		return rows-2; //less the sink
	}

	public int getColumns() {
		return columns-2;//less the sink
	}


	int get(int i, int j) {
		return this.grid[i][j];
	}
	public int[][] getGrid(){
		return grid;
	}

	public int[][] getUpdateGrid(){
		return updateGrid;
	
	}

	void setAll(int value) {
		//borders are always 0
		for( int i = 1; i<rows-1; i++ ) {
			for( int j = 1; j<columns-1; j++ ) 			
				grid[i][j]=value;
			}
	}
	

	//for the next timestep - copy updateGrid into grid
	public void nextTimeStep() {
		for(int i=1; i<rows-1; i++ ) {
			for( int j=1; j<columns-1; j++ ) {
				this.grid[i][j]=updateGrid[i][j];
			}
		}
	}
	
     
	protected Boolean compute() {

        // do not update border
		// checks if task should be done sequentially or in parallel
        if (hi - lo <= SEQUENTIAL_CUTOFF) {
            boolean change = false;
            for (int i = lo; i < hi; i++) {
                for (int j = 1; j < columns - 1; j++) {
                    updateGrid[i][j] = (grid[i][j] % 4) +
                            (grid[i - 1][j] / 4) +
                            grid[i + 1][j] / 4 +
                            grid[i][j - 1] / 4 +
                            grid[i][j + 1] / 4;
                    if (grid[i][j] != updateGrid[i][j]) {
                        change = true;
                    }
                }
            } 
            return change;
        } else {
			//for divide a problem into two sub problems
            int mid = (lo + hi) / 2;
			//creating tasks
            parallelGrid left = new parallelGrid(grid,updateGrid, lo, mid);
            parallelGrid right = new parallelGrid(grid,updateGrid , mid, hi);
			//fork left task to run in parallel
			left.fork();
			// compute right task sequentially
            boolean rightAns = right.compute(); 
			// join left task
            boolean leftAns = left.join(); 
            return leftAns || rightAns; 
            


        }
    }

	
	
	
	//display the grid in text format
	void printGrid( ) {
		int i,j;
		//not border is not printed
		System.out.printf("Grid:\n");
		System.out.printf("+");
		for( j=1; j<columns-1; j++ ) System.out.printf("  --");
		System.out.printf("+\n");
		for( i=1; i<rows-1; i++ ) {
			System.out.printf("|");
			for( j=1; j<columns-1; j++ ) {
				if ( grid[i][j] > 0) 
					System.out.printf("%4d", grid[i][j] );
				else
					System.out.printf("    ");
			}
			System.out.printf("|\n");
		}
		System.out.printf("+");
		for( j=1; j<columns-1; j++ ) System.out.printf("  --");
		System.out.printf("+\n\n");
	}
	
	//write grid out as an image
	void gridToImage(String fileName) throws IOException {
        BufferedImage dstImage =
                new BufferedImage(rows, columns, BufferedImage.TYPE_INT_ARGB);
        //integer values from 0 to 255.
        int a=0;
        int g=0;//green
        int b=0;//blue
        int r=0;//red

		for( int i=0; i<rows; i++ ) {
			for( int j=0; j<columns; j++ ) {
			     g=0;//green
			     b=0;//blue
			     r=0;//red

				switch (grid[i][j]) {
					case 0:
		                break;
		            case 1:
		            	g=255;
		                break;
		            case 2:
		                b=255;
		                break;
		            case 3:
		                r = 255;
		                break;
		            default:
		                break;
				
				}
		                // Set destination pixel to mean
		                // Re-assemble destination pixel.
		              int dpixel = (0xff000000)
		                		| (a << 24)
		                        | (r << 16)
		                        | (g<< 8)
		                        | b; 
		              dstImage.setRGB(i, j, dpixel); //write it out

			
			}}
		
        File dstFile = new File(fileName);
        ImageIO.write(dstImage, "png", dstFile);
	}
	
	


}
