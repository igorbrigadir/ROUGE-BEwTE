package mathalgo;

/**
 * Fibonacci heap
 * See "Introduction to Algorithms" by Cormen, Leiserson, and Rivest 
 *
 */
public class FHeap {

	private int numNodes;
	private int min = -1;
	private double[] key;
	private int[] degree;
	
	private int[] parent;
	private int[] child;
	private int[] left;
	private int[] right;
	private boolean[] mark;

	public FHeap(int maxSize, double[] keys) {
		this.key = keys;
		degree = new int[maxSize];
		parent = new int[maxSize];
		child = new int[maxSize];
		left = new int[maxSize];
		right = new int[maxSize];
		mark = new boolean[maxSize];
	}

	public int minimum() {
		return min;
	}

	public void  link(int y, int x) {
	

		// remove y from root list of heap
	        right[left[y]] = right[y];
	        left[right[y]] = left[y];

	        // make y a child of x
	        parent[y] = x;

	        if (child[x] == -1) {
	            child[x] = y;
	            right[y] = y;
	            left[y] = y;
	        } else {
	            left[y] = child[x];
	            right[y] = right[child[x]];
	            right[child[x]] = y;
	            left[right[y]] = y;
	        }

	        // increase degree[x]
	        degree[x]++;

	        // set mark[y] false
	        mark[y] = false;
	}

	 

	protected void consolidate()
    {
		
		int[] A = new int[numNodes+1];
		for(int i=0; i < A.length; i++) {
		     A[i] = -1;
		}

		int numRoots = 0;
		int x = min;
		 if (x != -1) {
	         numRoots++;
	         x = right[x];

	         while (x != min) {
	             numRoots++;
	             x = right[x];
	         }
	     }	
    
        // For each node in root list do...
        while (numRoots > 0) {
            // Access this node's degree..
            int d = degree[x];
            int next = right[x];

            // ..and see if there's another of the same degree.
            while (A[d] != -1) {
                // There is, make one of the nodes a child of the other.
                int y = A[d];

                // Do this based on the key value.
                // exchange
                if (key[x] > key[y]) {
                    int temp = y;
                    y = x;
                    x = temp;
                }

                // FibonacciHeapNode<T> y disappears from root list.
                link(y, x);

                // We've handled this degree, go to next one.
                A[d] = -1;
                d++;
            }

            // Save this node for later when we might encounter another
            // of the same degree.
            A[d] = x;

            // Move forward through list.
            x = next;
            numRoots--;
        }

        // Set min to null (effectively losing the root list) and
        // reconstruct the root list from the array entries in array[].
        min = -1;

        for (int i = 0; i < A.length; i++) {
            if (A[i] != -1) {
                // We've got a live one, add it to root list.
                if (min != -1) {
                    // First remove node from root list.
                    right[left[A[i]]] = right[A[i]];
                    left[right[A[i]]] = left[A[i]];

                    // Now add to root list, again.
                    left[A[i]] = min;
                    right[A[i]] = right[min];
                    right[min] = A[i];
                    left[right[A[i]]] = A[i];

                    // Check if this is a new min.
                    if (key[A[i]] < key[min]) {
                        min = A[i];
                    }
                } else {
                    min = A[i];
                }
            }
        }
    }

	public void insert(int x) {
		degree[x] = 0;
		parent[x] = -1;
		child[x] = -1;
		left[x] = x;
		right[x] = x;
		mark[x] = false;
		if(min != -1) {
			left[x] = min;
			right[x] = right[min];
			right[min] = x;
			left[right[x]] = x;
			if(key[x] < key[min]) {
				min = x;
			}
		}
		else {
			min = x;
		}
		
		numNodes++;
	}

	 

	public int extractMin() {
	int z = min;
	if (z != -1) {
		  int numChildren = degree[z];
		  
		  int x = child[z];
		  int tempR;
	      for (int i = numChildren; i > 0; i--) {
	    	  tempR = right[x];
	    	  right[left[x]] = right[x];
	    	  left[right[x]] = left[x];
	    	  left[x] = min;
	    	  right[x] = right[min];
	    	  right[min] = x;
	    	  // ?
	    	  left[right[x]] = x;
	    	  parent[x] = -1;
	    	  x = tempR;
	      }
	       
	      right[left[z]] = right[z];
	      left[right[z]] = left[z];
	      
	           if( z == right[z]) {
	              min = -1;
	           }
	           else {
	            	min = right[z];
	               consolidate();
	           }
	           numNodes--;
	     }
	return z;
	}
         

	public void decreaseKey(int x, double newKey) {
		if (newKey > key[x]) {
	   		System.err.println("new key is greater than current key");
		}

		key[x] = newKey;
		int y = parent[x];
		if(y != -1 && key[x]<key[y]) {
	   		cut(x, y);
	   		cascadingCut(y);    
		}

		if(key[x]<key[min]) {
	   		min = x;
		}
	}

		public void cut(int x, int y) {
			//Remove x from the child list of y, decrementing degree[y]
			right[left[x]] = right[x];
			left[right[x]] = left[x];
			degree[y]--;
			
			if(degree[y] == 0) {
				child[y] = -1;
			}
			else if(child[y] == x) {
				child[y] = right[x];
			}
			
			//Add x to the root list of H
			left[x] = min;
			right[x] = right[min];
			right[min] = x;
			left[right[x]] = x;
			parent[x] = -1;
			mark[x] = false;
		}
		
		public boolean isEmpty() {
			return min == -1;
		}

		public void cascadingCut(int y) {
			int z = parent[y];
			if(z != -1) {
	  			if(!mark[y]) {
	    		    mark[y] = true;
	  			}
	  			else {
	    		    cut(y, z);
		            cascadingCut(z);
			  	}
			}
		}

		public void delete(int x) {
			decreaseKey(x,Double.NEGATIVE_INFINITY);
			extractMin();
		}
	
}