//Author Griffin McCool
package cs1501_p4;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;
import java.util.ArrayList;

public class NetAnalysis implements NetAnalysis_Inter{
	private Node[] graph;
	
    // Constructor
    public NetAnalysis(String filename){
		try {
			int start;
			int end;
			String type;
			int bandwidth;
			int length;
			File file = new File(filename);
			Scanner scan = new Scanner(file);
			// initializes adjacency list
			graph = new Node[scan.nextInt()];
			scan.nextLine();
			while (scan.hasNextLine()){
				// breaks out of loop since last line in provided text files are blank
				if (!scan.hasNextInt()){
					break;
				}
				start = scan.nextInt();
				end = scan.nextInt();
				type = scan.next();
				bandwidth = scan.nextInt();
				length = scan.nextInt();
				// add edge to the graph (start-end)
				if (graph[start] != null){
					Node n = graph[start];
					while (n.getNext() != null){
						n = n.getNext();
					}
					n.setNext(new Node(start, end, type, bandwidth, length));
				} else {
					graph[start] = new Node(start, end, type, bandwidth, length);
				}
				// add edge to the graph (end-start)
				if (graph[end] != null){
					Node n = graph[end];
					while (n.getNext() != null){
						n = n.getNext();
					}
					n.setNext(new Node(end, start, type, bandwidth, length));
				} else {
					graph[end] = new Node(end, start, type, bandwidth, length);
				}
			}
			scan.close();
		} catch (FileNotFoundException fnfe){
			System.out.println("File not found.");
		}
    }

    /**
	 * Find the lowest latency path from vertex `u` to vertex `w` in the graph
	 *
	 * @param	u Starting vertex
	 * @param	w Destination vertex
	 *
	 * @return	ArrayList<Integer> A list of the vertex id's representing the
	 * 			path (should start with `u` and end with `w`)
	 * 			Return `null` if no path exists
	 */
	public ArrayList<Integer> lowestLatencyPath(int u, int w){
		float[] time = new float[graph.length];
		int[] via = new int[graph.length];
		boolean[] marked = new boolean[graph.length];
		Node[] pq = new Node[graph.length * graph.length];
		int insertPosition = 1;
		// if either vertex is greater than the greatest vertex, there is no path
		if (u >= graph.length || w >= graph.length) return null;
		
		for (int i = 0; i < time.length; i++){
			time[i] = Float.MAX_VALUE;
			marked[i] = false;
		}
		time[u] = 0;
		marked[u] = true;

		// add start vertex to the PQ
		pq = add(pq, new Node(u, 0), insertPosition);
		insertPosition++;

		while(pq[1] != null){
			// set cur to vertex on top of PQ
			int cur = pq[1].getEnd();
			Node n = graph[cur];

			// pop the last value off pq
			insertPosition--;
			pq = pop(pq, insertPosition);

			// consider all edges from n (cur)
			while (n != null){
				// add n to the pq if it hasnt been added before
				if (!marked[n.getEnd()]){
					pq = add(pq, new Node(n.getEnd(), n.getTime() + time[n.getStart()]), insertPosition);
					insertPosition++;
				}
				// if a vertex is not marked yet, update the time and edge (via) arrays
				if (!marked[n.getEnd()]){
					time[n.getEnd()] = n.getTime() + time[n.getStart()];
					via[n.getEnd()] = n.getStart();
					marked[n.getEnd()] = true;
				} else {
					// if a vertex is marked, check if the current path is shorter than the stored path
					float newTime = time[n.getStart()] + n.getTime();
					if(newTime < time[n.getEnd()]){
						time[n.getEnd()] = newTime;
						via[n.getEnd()] = n.getStart();
						// update the pq if needed
						update(pq, n.getEnd(), insertPosition, newTime);
					}
				}
				n = n.getNext();
			}
		}
		// if w isn't marked, return null as it was not found
		if (!marked[w]) return null;

		ArrayList<Integer> aList = new ArrayList<Integer>();
		ArrayList<Integer> aList2 = new ArrayList<Integer>();
		int check = w;
		while (check != u){
			aList.add(check);
			check = via[check];
		}
		aList.add(u);
		// aList is now in the reverse order that we need, so we reverse it
		for (int i = aList.size() - 1; i >= 0; i--){
			aList2.add(aList.get(i));
		}
        return aList2;
    }

	/**
	 * Helper function to maintain min pq's (adds to pq)
	 *
	 * @param	pq the pq sent in, modified, and returned
	 * @param	newNode node to be added to the PQ
	 * @param	insPos insert position in the PQ
	 *
	 * @return	Node[] the pq
	 */
	private Node[] add(Node[] pq, Node newNode, int insPos){
		if (pq[1] == null) {
			pq[1] = newNode;
			return pq;
		}
		Node temp;
		pq[insPos] = newNode;
		int i = insPos;
		// while newNode can move up, keep swapping with parent
		while ((i > 1) && (newNode.getTime() < pq[i/2].getTime())){
			temp = pq[i/2];
			pq[i/2] = newNode;
			pq[i] = temp;
			i = i/2;
		}
		return pq;
	}

	/**
	 * Helper function to maintain min pq's (pops off min value and maintains heap properties)
	 *
	 * @param	pq the pq sent in, modified, and returned
	 * @param	insPos insert position in the PQ
	 *
	 * @return	Node[] the pq
	 */
	private Node[] pop(Node[] pq, int insPos){
		if (pq[2] == null){
			pq[1] = null;
			return pq;
		}
		int lci;
		Node lc;
		Node temp;
		Node cur = pq[insPos];
		pq[1] = cur;
		pq[insPos] = null;
		// takes min of both children (if insPos < 4 there is only one child)
		if (insPos > 3){
			lc = (pq[2].getTime() <= pq[3].getTime()) ? pq[2] : pq[3];
			lci = (pq[2].getTime() <= pq[3].getTime()) ? 2 : 3;
		} else {
			lc = pq[2];
			lci = 2;
		}
		int i = 1;
		while ((2 * i < insPos) && (cur.getTime() > lc.getTime())){
			temp = lc;
			pq[lci] = cur;
			pq[i] = temp;
			
			i = lci;

			// takes min of both children 
			if (2 * i < insPos){
				if ((2 * i) + 1 < insPos){
					lc = (pq[2 * i].getTime() <= pq[2 * i + 1].getTime()) ? pq[2 * i] : pq[2 * i + 1];
					lci = (pq[2 * i].getTime() <= pq[2 * i + 1].getTime()) ? 2 * i : 2 * i + 1;
				} else {
					lc = pq[2 * i];
					lci = 2 * i;
				}
			}
		}
		return pq;
	}

	/**
	 * Helper function to maintain min pq's (updates then maintains heap properties)
	 *
	 * @param	pq the pq sent in, modified, and returned
	 * @param	v the vertex to be updated
	 * @param	insPos insert position in the PQ
	 * @param	time new time value that vertex is to be updated with
	 *
	 * @return	Node[] the pq
	 */
	private Node[] update(Node[] pq, int v, int insPos, float time){
		int i = 1;
		Node temp;
		Node lc  = null;
		int lci = 0;
		// find v
		while (i < insPos && pq[i].getEnd() != v){
			i++;
		}
		// if i == insPos, v wasn't found so we just return as no update is needed
		if (i == insPos) return pq;
		pq[i].setTime(time);

		// while v can move up, keep swapping with parent
		while ((i > 1) && (pq[i].getTime() < pq[i/2].getTime())){
			temp = pq[i/2];
			pq[i/2] = pq[i];
			pq[i] = temp;
			i = i/2;
		}

		// while v can move down, keep swapping with min child
		// takes min of both children 
		if (2 * i < insPos){
			if ((2 * i) + 1 < insPos){
				lc = (pq[2 * i].getTime() <= pq[2 * i + 1].getTime()) ? pq[2 * i] : pq[2 * i + 1];
				lci = (pq[2 * i].getTime() <= pq[2 * i + 1].getTime()) ? 2 * i : 2 * i + 1;
			} else {
				lc = pq[2 * i];
				lci = 2 * i;
			}
		}
		while ((2 * i < insPos) && (pq[i].getTime() > lc.getTime())){
			temp = lc;
			pq[lci] = pq[i];
			pq[i] = temp;
			
			i = lci;

			// takes min of both children 
			if (2 * i < insPos){
				if ((2 * i) + 1 < insPos){
					lc = (pq[2 * i].getTime() <= pq[2 * i + 1].getTime()) ? pq[2 * i] : pq[2 * i + 1];
					lci = (pq[2 * i].getTime() <= pq[2 * i + 1].getTime()) ? 2 * i : 2 * i + 1;
				} else {
					lc = pq[2 * i];
					lci = 2 * i;
				}
			}
		}
		return pq;
	}

	/**
	 * Find the bandwidth available along a given path through the graph
	 * (the minimum bandwidth of any edge in the path). Should throw an
	 * `IllegalArgumentException` if the specified path is not valid for
	 * the graph.
	 *
	 * @param	ArrayList<Integer> A list of the vertex id's representing the
	 * 			path
	 *
	 * @return	int The bandwidth available along the specified path
	 */
	public int bandwidthAlongPath(ArrayList<Integer> p) throws IllegalArgumentException{
		int min = Integer.MAX_VALUE;
		Node n;
		int to;
		// if the first vertex is greater than the greatest vertex in the graph, path is invalid
		if (p.get(0) >= graph.length) throw new IllegalArgumentException("Invalid path.");
		// if the path is only 1 vertex, bandwidth is 0
		if (p.size() <= 1) return 0;

		for (int i = 0; i < p.size(); i++){
			int v = p.get(i);
			// if the vertex is greater than the greatest vertex in the graph, path is invalid
			if (v >= graph.length) throw new IllegalArgumentException("Invalid path.");
			if (i < p.size() - 1){
				to = p.get(i + 1);
			} else break;
			n = graph[v];
			// if n is null, the vertex wasn't added to the graph, and path is invalid
			if (n == null) throw new IllegalArgumentException("Invalid path.");
			while (n.getEnd() != to){
				if (n.getNext() == null) throw new IllegalArgumentException("Invalid path.");
				n = n.getNext();
			}
			min = (min <= n.getBandwidth()) ? min : n.getBandwidth();
		}
        return min;
    }

	/**
	 * Return `true` if the graph is connected considering only copper links
	 * `false` otherwise
	 *
	 * @return	boolean Whether the graph is copper-only connected
	 */
	public boolean copperOnlyConnected(){
		boolean[] marked = new boolean[graph.length];
		// if the graph is empty, it is not connected
		if (graph.length == 0) return false;
		marked = copperDFS(0, marked);
        // make sure each node is marked
		for (int i = 0; i < marked.length; i++){
			if (marked[i] == false) return false;
		}
		return true;
    }

	/**
	 * Helper function for copperOnlyConnected() that performs DFS and marks all copper connected verticies
	 * 
	 * @param	v current start vertex
	 * @param	marked marked array
	 * 
	 * @return	marked array
	 */
	private boolean[] copperDFS(int v, boolean[] marked){
		Node cur = graph[v];
		while (cur != null){
			// if there is a copper connection & end node hasn't been marked yet, go to next vertex and mark both
			if (cur.getType().equals("copper") && !marked[cur.getEnd()]){
				marked[v] = true;
				marked[cur.getEnd()] = true;
				marked = copperDFS(cur.getEnd(), marked);
			}
			cur = cur.getNext();
		}
		return marked;
	}

	/**
	 * Return `true` if the graph would remain connected if any two vertices in
	 * the graph would fail, `false` otherwise
	 *
	 * @return	boolean Whether the graph would remain connected for any two
	 * 			failed vertices
	 */
	public boolean connectedTwoVertFail(){
		// if there are less than 3 verticies in the graph, it would not stay connected
		if (graph.length < 3) return false;
		// if there are exactly 3 verticies, the graph will only have 1 vertex left and is connected
		if (graph.length == 3) return true;
		int count = 0;
		int start;
		boolean[] marked = new boolean[graph.length];
		// for each pair of verticies, check if each vertex is reachable
		for (int i = 0; i < graph.length; i++){
			for (int j = 1; j < graph.length; j++){
				// if i == j, we're only removing 1 vertex
				if (i != j) {
					marked = new boolean[graph.length];
					count = 0;
					if (i == 0){
						start = j+1;
						if (j == graph.length-1) start = j - 1;
					}
					else start = 0;
					marked = connectedDFS(i, j, marked, start);
					// make sure we've marked every vertex except for the two removed
					for (int x = 0; x < marked.length; x++){
						if (marked[x] == true) count++;
					}
					if (count != (graph.length - 2)) return false;
				}
			}
		}
		return true;
    }

	/**
	 * Helper function for connectedTwoVertFail() that performs DFS and marks all connected verticies after removing two
	 * 
	 * @param	v1 first removed vertex
	 * @param	v2 second removed vertex
	 * @param	marked marked array
	 * @param	start vertex to start DFS at
	 * 
	 * @return	marked array
	 */
	private boolean[] connectedDFS(int v1, int v2, boolean[] marked, int start){
		Node cur = graph[start];
		while (cur != null){
			if (cur.getStart() != v1 && cur.getStart() != v2 && cur.getEnd() != v1 && cur.getEnd() != v2 && !marked[cur.getEnd()]){
				marked[start] = true;
				marked[cur.getEnd()] = true;
				marked = connectedDFS(v1, v2, marked, cur.getEnd());
			}
			cur = cur.getNext();
		}
		return marked;
	}

	/**
	 * Find the lowest average (mean) latency spanning tree for the graph
	 * (i.e., a spanning tree with the lowest average latency per edge). Return
	 * it as an ArrayList of STE edges.
	 *
	 * Note that you do not need to use the STE class to represent your graph
	 * internally, you only need to use it to construct return values for this
	 * method.
	 *
	 * @return	ArrayList<STE> A list of STE objects representing the lowest
	 * 			average latency spanning tree
	 * 			Return `null` if the graph is not connected
	 */
	public ArrayList<STE> lowestAvgLatST(){
		// if the graph is empty, it is not connected
		if (graph.length == 0) return null;
		ArrayList<STE> aList = new ArrayList<STE>();
		boolean[] marked = new boolean[graph.length];
		int numVertsAdded = 0;
		int lastNum;
		int i = 0;
		float curMin = Float.MAX_VALUE;
		Node curMinEdge = null;
		Node cur;
		// mark 0 because we start there
		marked[0] = true;
		numVertsAdded++;
		while (numVertsAdded != marked.length){
			// goes through all marked nodes and checks edges
			for (int j = 0; j < marked.length; j++){
				// if it's marked, check for a min edge
				if (marked[j]){
					cur = graph[j];
					// check thru all edges starting from current vertex
					while (cur != null){
						// if we find a new min that isn't already marked, update curMin
						if (!marked[cur.getEnd()] && cur.getTime() < curMin){
							curMin = cur.getTime();
							curMinEdge = cur;
						}
						cur = cur.getNext();
					}
				}
			}
			// if a vertex was already marked that we are going to mark again, that means we couldn't
			// find a new edge, and not all verticies are marked, thus the graph is not connected
			if (curMinEdge == null || marked[curMinEdge.getEnd()]) return null;
			// otherwise, mark the vertex
			marked[curMinEdge.getEnd()] = true;
			numVertsAdded++;
			aList.add(new STE(curMinEdge.getStart(), curMinEdge.getEnd()));
			curMin = Float.MAX_VALUE;
		}
        return aList;
    }

	// linked list node definiton
	class Node{
		// MAKE PRIVATE NO CAP
		private int start;
		private int end;
		private String type;
		private int bandwidth;
		private int length;
		private Node next;
		private float time;

		// Node constructor for adjacency list
		public Node(int from, int dest, String t, int bw, int l){
			start = from;
			end = dest;
			type = t;
			bandwidth = bw;
			length = l;
			if (type.equals("copper")) time = (float)length/(float)230000000;
			else time = (float)length/(float)200000000;
		}

		public Node(int to, float t){
			end = to;
			time = t;
		}

		public int getStart(){
			return start;
		}

		public int getEnd(){
			return end;
		}

		public String getType(){
			return type;
		}

		public int getBandwidth(){
			return bandwidth;
		}

		public int getLength(){
			return length;
		}

		public Node getNext(){
			return next;
		}

		public float getTime(){
			return time;
		}

		public void setNext(Node n){
			next = n;
		}

		public void setTime(float f){
			time = f;
		}
	}
}