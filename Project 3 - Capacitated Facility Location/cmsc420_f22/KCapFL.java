package cmsc420_f22; // Do not delete this line

import java.util.ArrayList;

public class KCapFL<LPoint extends LabeledPoint2D> {
	private int capacity; //This is the maximum capacity of any service center,
	private XkdTree<LPoint> tree; //a tree to store the points
	private LeftistHeap<Double, ArrayList<LPoint>> heap; //leftist heap for storing key-value pairs. Each pair is of the form (ri,Li), where ri is the squared radius of this cluster of points and Li is the set of points in the cluster. 
	
	/*
	 * Constructor for KCapFL. Initializes a tree with the given bucketSize and bbox. Sets the capacity. 
	 */
	public KCapFL(int capacity, int bucketSize, Rectangle2D bbox) {
		this.capacity = capacity;
		tree = new XkdTree<LPoint>(bucketSize, bbox);
		heap = new LeftistHeap<Double, ArrayList<LPoint>>();
	}
	
	/*
	 * Clears the structure by clearing the tree and heap
	 */
	public void clear() {
		tree.clear();
		heap.clear();
	}
	
	/*
	 * Initializes the structure. Checks if the number of points is > 0 and divisible by the capacity. If so, it bulk inserts the points into
	 * the tree. Finally, it creates the initial radii by running the knn query on every point and adding the results to the heap.
	 * Throws an exception if the point set size is invalid.  
	 */
	public void build(ArrayList<LPoint> pts) throws Exception {
		if(((pts.size() <= 0) || (pts.size()%capacity != 0))) { //checks point set size
			throw new Exception("Invalid point set size");
		} else {
			tree.bulkInsert(pts); //bulkInsert
			for(int i = 0; i < pts.size(); i++) { //goes through pts list
				ArrayList<LPoint> knn;
				//runs knn query on every point
				knn = tree.kNearestNeighbor(pts.get(i).getPoint2D(), capacity);
				//inserts radii and query result into heap
				heap.insert(knn.get(0).getPoint2D().distanceSq(knn.get(capacity-1).getPoint2D()), knn);
			}
			
		}
	}
	
	/*
	 * Returns null if kd tree is empty. Repeats the following steps: extact next cluster from the heap, check if every point is in the tree. 
	 * If so, delete the points from the cluster in the tree, and return the cluster. Else, check if the first point of the list is in the tree. 
	 * If so, compute a new knn using it and insert it back into the heap. Else, do nothing. Continue extracting until all the points of a cluster are in the tree. 
	 */
	public ArrayList<LPoint> extractCluster() {
		ArrayList<LPoint> lst = new ArrayList<LPoint>();
		if(tree.size() == 0) {
			//no points left
			return null;
		}
		
		boolean check = true; //checks whether points are in the tree
		try {
			lst = heap.extractMin(); //gets next cluster
		} catch(Exception e) {
			//
		}
		
		//checks if all points of cluster are in tree
		for(int i = 0; i < lst.size(); i++) {
			if(tree.find(lst.get(i).getPoint2D()) == null) {
				//this point is not in the tree, meaning this cluster has failed and needs to be fixed
				check = false;
			} 
		}
		
		if(check) {
			//all points are in the tree, delete all points
			for(int i = 0; i < lst.size(); i++) {
				try {
					tree.delete(lst.get(i).getPoint2D());
				} catch (Exception e) {
			
				}
			}
			//returns the list of points from knn query
			return lst;
		} else {
			//not all the points are in the list
			LPoint pt = lst.get(0); //first element is used to build new query
			if(tree.find(pt.getPoint2D()) != null) {
				ArrayList<LPoint> knn = tree.kNearestNeighbor(pt.getPoint2D(), capacity);
				Double radius = pt.getPoint2D().distanceSq(knn.get(capacity-1).getPoint2D());
				heap.insert(radius, knn); //new cluster inserted
			}
		}
		
		return extractCluster();
	}
	//uses xkdtree list function
	public ArrayList<String> listKdTree() {
		return tree.list();
	}
	//uses leftistheap list function
	public ArrayList<String> listHeap() {
		return heap.list();
	}
}
