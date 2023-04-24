// Author: Alexis Paul, apaul127, 117031522
// For: CMSC 420
// Date: Fall 2022
/**
 * This program implements an extended kd-tree, storing points in 2-dimensional 
 * space. Operations for this tree include find, insert, bulkInsert, and nearestNeighbor.
 */
package cmsc420_f22; // Do not delete this line

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;


public class XkdTree<LPoint extends LabeledPoint2D> {
	
	//comparator to sort points lexicographically by x then y
	private class ByXThenY implements Comparator<LPoint> {
		public int compare(LPoint pt1, LPoint pt2) {
			if(pt1.getX() > pt2.getX()) {
				return 1;
			} else if(pt1.getX() < pt2.getX()) {
				return -1;
			} else { //sort by y
				if(pt1.getY() > pt2.getY()) {
					return 1;
				} else if(pt1.getY() < pt2.getY()) {
					return -1;
				} else {
					return 0;
				}
			}
		}
	}
	
	//comparator to sort points lexicographically by y then x
	private class ByYThenX implements Comparator<LPoint> {
		public int compare(LPoint pt1, LPoint pt2) {
			if(pt1.getY() > pt2.getY()) {
				return 1;
			} else if(pt1.getY() < pt2.getY()) {
				return -1;
			} else { //sort by y
				if(pt1.getX() > pt2.getX()) {
					return 1;
				} else if(pt1.getX() < pt2.getX()) {
					return -1;
				} else {
					return 0;
				}
			}
			
		}
	}
	
	private abstract class Node { // generic node (purely abstract)
	abstract LPoint find(Point2D pt); // find helper - abstract
	abstract ArrayList<String> listHelper(ArrayList<String> lst); //list helper - abstract
	abstract Node bulkInsertHelper(ArrayList<LPoint> pts); // bulkInsert helper - abstract
	abstract LPoint nearestNeighborHelper(Point2D center, Rectangle2D cell, LPoint best); //nearestNeighbor helper - abstract
	abstract void delete(Point2D pt) throws Exception; //delete - abstract
	abstract Node fixBucket(Point2D pt); //fixBucket - abstract
	abstract void kNearestNeighborhelper(Point2D center, Rectangle2D cell, MinK<Double, LPoint> heap); ////knearestNeighbor helper - abstract
	}
	
	/**
	 * InternalNode stores splitting information, cutDim and cutVal.
	 *
	 */
	private class InternalNode extends Node {
		int cutDim; // the cutting dimension (0 = x, 1 = y)
		double cutVal; // the cutting value
		Node left, right; // children
		/**find helper for internal nodes  
		 * Parameter: pt, the point to find
		 * Returns: the pt if found or null if it doesn't exist
		 */
		LPoint find(Point2D pt) { 
			LPoint ret = null;
			if((cutDim == 0 && cutVal == pt.getX()) || (cutDim == 1 && cutVal == pt.getY())) {
				// point lies on cutting value, can be in either tree
				if(left!=null) {
					ret = left.find(pt);
				}
				if(ret == null && right != null) {
					return right.find(pt);
				} else {
					return ret;
				}
			} else if((cutDim == 0 && cutVal > pt.getX()) || (cutDim == 1 && cutVal > pt.getY())) {
				// in left subtree
				if(left!=null) {
					return left.find(pt);
				}
			} else {
				// in right subtree
				if(right!=null) {
					return right.find(pt);
				}
			}
			
			return null; //this is not eight?
			
		}
		
		/** list helper for internal nodes
		 * Parameters: lst, a buffer to add the strings to
		 * Returns: lst
		 */ 
		ArrayList<String> listHelper(ArrayList <String> lst) {
			
			String s = new String();
			if(cutDim == 0) {
				s = s + ("(x=" + cutVal + ")");
			} else {
				s = s + ("(y=" + cutVal + ")");
			}
			lst.add(s);
			if(right!=null) {
				lst = right.listHelper(lst);
			}
			if(left!=null) {
				lst = left.listHelper(lst);
			}
			return lst;
		}
		
		/* bulkInsert helper for internal nodes 
		 * Paramaters: pts, a list of points to insert
		 * Returns: the root of the subtree/tree after insert operations
		 */
		Node bulkInsertHelper(ArrayList<LPoint> pts) { 
			//sort points by cutting dim
			//split points by cutting dim
			//recurse on each side
			
			if(pts.size() == 0) {
				//there are no points to insert
				return this;
			}
			if(this.cutDim == 0) { //x is cutting dimension
				//sort by cutting dimension x, use y to break ties
				Collections.sort(pts, new ByXThenY());				
				ArrayList<LPoint> left = new ArrayList<LPoint>();
				ArrayList<LPoint> right = new ArrayList<LPoint>();
				
				//partition lists
				for(int i = 0; i < pts.size(); i++) {
					if(pts.get(i).getX() < cutVal) {
						left.add(pts.get(i));
					} else {
						right.add(pts.get(i));
					}
				
				}
				
				//recurse on children
				this.left = this.left.bulkInsertHelper(left);
				this.right = this.right.bulkInsertHelper(right);
				return this;
			} else { // y is cutting dimension
				//sort by cutting dimension y, use x to break ties
				Collections.sort(pts, new ByYThenX());	
				ArrayList<LPoint> left = new ArrayList<LPoint>();
				ArrayList<LPoint> right = new ArrayList<LPoint>();
				
				//partition lists
				for(int i = 0; i < pts.size(); i++) {
					if(pts.get(i).getY() < cutVal) {
						left.add(pts.get(i));
					} else {
						right.add(pts.get(i));
				
					} 
				}
				
				//left and right will be sorted because points are sorted
				this.left = this.left.bulkInsertHelper(left);
				this.right = this.right.bulkInsertHelper(right);
				return this;
			}
			
		}
		/** nearestNeighbor helper for internal nodes
		 * Parameters: center (the query point), cell (the rectangle we are querying), best (the current nearestNeighbor)
		 * Returns: best (closest neighbor found)
		 */
		LPoint nearestNeighborHelper(Point2D center, Rectangle2D cell, LPoint best) {
			Rectangle2D leftpart = cell.leftPart(cutDim, cutVal); //left child's cell
			Rectangle2D rightpart = cell.rightPart(cutDim, cutVal); //right child's cell
			double distance = 0; //distance var
			if(best == null) {
				distance = Double.POSITIVE_INFINITY; //distance needs to be compared even if best is null
			} else {
				distance = center.distanceSq(best.getPoint2D());
			}
			if(cutDim == 0) {
				//cutting dim is x
				if(center.getX() < this.cutVal) { //center is closer to left
					best = this.left.nearestNeighborHelper(center, leftpart, best);
					if(best!=null) { //update distance
						distance = center.distanceSq(best.getPoint2D());
					}
					if(rightpart.distanceSq(center) < distance) { //is right viable?
						best = this.right.nearestNeighborHelper(center, rightpart, best);
					}
				} else { //center is closer to right
					best = this.right.nearestNeighborHelper(center, rightpart, best); 
					if(best!=null) { //update distance
						distance = center.distanceSq(best.getPoint2D());
					}
					if(leftpart.distanceSq(center) < distance) { //is left viable?
						best = this.left.nearestNeighborHelper(center, leftpart, best);
					}
				}
				
				return best;
			} else {
				//cutting dim is y
				if(center.getY() < this.cutVal) { //center is closer to left
					best = this.left.nearestNeighborHelper(center, leftpart, best);
					if(best!=null) { //update distance
						distance = center.distanceSq(best.getPoint2D());
					}
					if(rightpart.distanceSq(center) < distance) { //is right viable?
						best = this.right.nearestNeighborHelper(center, rightpart, best);
					}
				} else { //center is closer to right
					best = this.right.nearestNeighborHelper(center, rightpart, best);
					if(best!=null) { //update distance
						distance = center.distanceSq(best.getPoint2D());
					}
					if(leftpart.distanceSq(center) < distance) { //is left viable
						best = this.left.nearestNeighborHelper(center, leftpart, best); 
					}
				}
				
				return best;
			}
		}

		/*
		 * Delete for internal nodes. 
		 * Internal works to find the node. 
		 */
		void delete(Point2D pt) throws Exception {
			if((cutDim == 0 && cutVal == pt.getX()) || (cutDim == 1 && cutVal == pt.getY())) {
				//can be in either subtree, must check both
				try {
					this.right.delete(pt);
				} catch(Exception e) {
					
				}
				
				try {
					this.left.delete(pt);
				} catch(Exception t) {
						
				}
			} else if((cutDim == 0 && cutVal > pt.getX()) || (cutDim == 1 && cutVal > pt.getY())) {
				// in left subtree
				this.left.delete(pt);
				
			} else {
				// in right subtree
				this.right.delete(pt);
				
			}
			
			
			
		}

		/*
		 * Fixes the tree after a deletion, given the point that was deleted. 
		 * Follows the same path as delete.
		 */
		XkdTree<LPoint>.Node fixBucket(Point2D pt) {
			if((cutDim == 0 && cutVal == pt.getX()) || (cutDim == 1 && cutVal == pt.getY())) {
				//could be in either subtree, must fix both sides
					if(this.right!=null) {
						this.right = this.right.fixBucket(pt);
					}
					if(this.left!=null) {
						this.left = this.left.fixBucket(pt);
					}
			} else if((cutDim == 0 && cutVal > pt.getX()) || (cutDim == 1 && cutVal > pt.getY())) {
				// in left subtree, fix the left
				if(this.left!=null) {
					this.left = this.left.fixBucket(pt);
				}
				
			} else {
				// in right subtree, fix the right
				if(this.right!=null) {
					this.right = this.right.fixBucket(pt);
				}
				
			}
			
			//occurs if the bucket is completely empty, returns the other child. 
			if(this.right == null) {
				return this.left;
			} else if (this.left == null) {
				return this.right;
			}
			
			return this;
		}

		@Override
		/** nearestNeighbor helper for internal nodes
		 * Parameters: center (the query point), cell (the rectangle we are querying), heap (the MinK data structure that holds the k best points)
		 * Returns: void
		 */
		void kNearestNeighborhelper(Point2D center, Rectangle2D cell, MinK<Double, LPoint> heap) {
			if (cell.distanceSq(center) > heap.getKth()) {
				return;
			}
				Rectangle2D leftpart = cell.leftPart(cutDim, cutVal); //left child's cell
				Rectangle2D rightpart = cell.rightPart(cutDim, cutVal); //right child's cell
				if(cutDim == 0) {
					//cutting dim is x
					if(center.getX() < this.cutVal) { //center is closer to left
						this.left.kNearestNeighborhelper(center, leftpart, heap);
						if(rightpart.distanceSq(center) < heap.getKth()) { //is right viable?
							this.right.kNearestNeighborhelper(center, rightpart, heap);
						}
					} else { //center is closer to right
						this.right.kNearestNeighborhelper(center, rightpart, heap); 
						if(leftpart.distanceSq(center) < heap.getKth()) { //is left viable?
							this.left.kNearestNeighborhelper(center, leftpart, heap);
						}
					}
				} else {
					//cutting dim is y
					if(center.getY() < this.cutVal) { //center is closer to left
						this.left.kNearestNeighborhelper(center, leftpart, heap);
						if(rightpart.distanceSq(center) < heap.getKth()) { //is right viable?
							this.right.kNearestNeighborhelper(center, rightpart, heap);
						}
					} else { //center is closer to right
						this.right.kNearestNeighborhelper(center, rightpart, heap);
						if(leftpart.distanceSq(center) < heap.getKth()) { //is left viable
							this.left.kNearestNeighborhelper(center, leftpart, heap); 
						}
					}
				}
		}
	
	}
	
	/**
	 * ExternalNodes stores a list of points that lie within this node (contained within bucket)
	 * The number of points is limited by bucketSize (provided during construction)
	 *
	 */
	private class ExternalNode extends Node {
		ArrayList<LPoint> bucket = new ArrayList<LPoint>();; // the bucket
		
		/** nearestNeighbor helper for external nodes
		 * Parameters: center (the query point), cell (the rectangle we are querying), best (the current nearestNeighbor)
		 * Returns: best (closest neighbor found)
		 */
		LPoint nearestNeighborHelper(Point2D center, Rectangle2D cell, LPoint best) {
			//Goes through bucket and finds closest point
			double distance = 0;
			if(best == null) {
				distance = Double.POSITIVE_INFINITY; //distance needs to be compared even if best is null
			} else {
				distance = center.distanceSq(best.getPoint2D());
			}
			for(int i = 0; i < bucket.size(); i++) {
				if(center.distanceSq(bucket.get(i).getPoint2D()) < distance) {
					best = bucket.get(i);
					distance = center.distanceSq(best.getPoint2D());
				}
			}
			
			return best;
		}
		
		/**find helper for external nodes  
		 * Parameter: pt, the point to find
		 * Returns: the pt if found or null if it doesn't exist
		 */
		LPoint find(Point2D pt) { 
			//goes through bucket to find point
			for(int i = 0; i < bucket.size(); i++) {
				if(bucket.get(i).getPoint2D().equals(pt)) {
					return bucket.get(i);
				}
			}
			return null; 
		}
		
		/** list helper for external nodes
		 * Parameters: lst, a buffer to add the strings to
		 * Returns: lst
		 */ 
		ArrayList<String> listHelper(ArrayList<String> lst) {
			
			ArrayList<String> buf = new ArrayList<String>();
			String s = new String(); //the string buffer
			s = s + "[ ";
			
			//collect points using the string
			for(int i = 0; i < bucket.size(); i++) {
				buf.add("{" + bucket.get(i).getLabel() + ": "+ bucket.get(i).getPoint2D().toString() + "} "); 
			}
			
			Collections.sort(buf); //sort that arraylist. show it who's boss. lexicographical sorting
			
			for(int i = 0; i < buf.size(); i++) {
				s = s + buf.get(i);
			}
			s = s + "]"; //close string
			
			buf.clear();
			buf.add(s);
			lst.add(s);
			return lst;
			
		}
		
		
		/* bulkInsert helper for external nodes 
		 * Paramaters: pts, a list of points to insert
		 * Returns: the root of the subtree/tree after insert operations
		 */
		Node bulkInsertHelper(ArrayList<LPoint> pts) {
			//add to bucket
			//sort bucket
			//if bucket is over size split
			//when splitting, create new internal node, two external nodes, send lists to external nodes
			//internal node cutting dim based on (x.max-x.min) vs (y.max-y.min)
			
			bucket.addAll(pts);
			
			if(this.bucket.size() > bucketSize) {
				//split
				
				//determine cutting dim
				double maxX = bbox.getLow().getX();
				double maxY = bbox.getLow().getY();
				double minX = bbox.getHigh().getX();;
				double minY = bbox.getHigh().getY();
				
				//find max and min for x and y
				for(int i = 0; i < bucket.size(); i++) {
					if(bucket.get(i).getX() > maxX) {
						maxX = bucket.get(i).getX();
					}
					if(bucket.get(i).getY() > maxY) {
						maxY = bucket.get(i).getY();
					}
					if(bucket.get(i).getX() < minX) {
						minX = bucket.get(i).getX();
					}
					if(bucket.get(i).getY() < minY) {
						minY = bucket.get(i).getY();
					}
				}


				
				
				InternalNode node = new InternalNode(); //this is the new node we want to use
				ArrayList<LPoint> left = new ArrayList<LPoint>();
				ArrayList<LPoint> right = new ArrayList<LPoint>(); //left and right subtree lists
				if((maxX - minX) >= (maxY - minY)) {
					//cutting dim is x
					node.cutDim = 0;
					//sort by cutting dim x, break ties by y
					Collections.sort(bucket, new ByXThenY());	
					
					//get median
					int median = bucket.size()/2;
					if(bucket.size()%2 != 0) {
						//odd, the true median
						node.cutVal = bucket.get(median).getX();
					} else {
						//even, funky math median
						node.cutVal = (bucket.get(median-1).getX()+bucket.get(median).getX())/2;
					}
					
					//add points to left and right lists
					for(int i = 0; i < median; i++) {
						left.add(bucket.get(i));
					}
					for(int i = median; i < bucket.size(); i++) {
						right.add(bucket.get(i));
					}
					
				} else {
					
					//cutting dim is y
					node.cutDim = 1;
					//sort by cutting dim y, break ties by x
					Collections.sort(bucket, new ByYThenX());	
					//median
					int median = bucket.size()/2;
					if(bucket.size()%2 != 0) {
						//odd, the true median
						node.cutVal = bucket.get(median).getY();
					} else {
						//even, funky math median
						node.cutVal = (bucket.get(median-1).getY()+bucket.get(median).getY())/2;
					}
					
					//add points to left and right lists
					for(int i = 0; i < median; i++) {
						left.add(bucket.get(i));
					}
					for(int i = median; i < bucket.size(); i++) {
						right.add(bucket.get(i));
					}
					
				}
				

				node.left = new ExternalNode(); //create new external node for left subtree
				node.left = node.left.bulkInsertHelper(left); //recurse
				node.right = new ExternalNode(); //create new external node for right subtree
				node.right = node.right.bulkInsertHelper(right); //recurse
				return node;
				
			} 

			return this;
			
		}
		
		/**
		 * Delete for internal nodes. If the point is found it is deleted. 
		 * Else an exception is thrown. 
		 */
		void delete(Point2D pt) throws Exception {
			int size2 = bucket.size(); //saves size to compare later
			for(int i = 0; i < bucket.size(); i++) {
				if(bucket.get(i).getPoint2D().equals(pt)) {
					//delete
					bucket.remove(i);
					size = size - 1; //decrement size
					return;
				}
			}
			
			if(bucket.size() == size2) { //checks if something was deleted
				//this means the point is not here
				throw new Exception("Deletion of nonexistent point");
			}
			
			
		}

		@Override
		/**
		 * Fixbucket for internal node, just makes the internal node null
		 * if there are no more elements in the bucket.
		 */
		XkdTree<LPoint>.Node fixBucket(Point2D pt) {
			if(this.bucket.size() == 0) {
				return null;
			} else {
				return this;
			}
		}

		@Override
		/** nearestNeighbor helper for external nodes
		 * Parameters: center (the query point), cell (the rectangle we are querying), heap (the MinK data structure that holds the k best points)
		 * Returns: void
		 */
		void kNearestNeighborhelper(Point2D center, Rectangle2D cell, MinK<Double, LPoint> heap) {
			//goes through the bucket for a point that can be added to the heap and adds it
			for(int i = 0; i < bucket.size(); i++) {
				if (bucket.get(i).getPoint2D().distanceSq(center) < heap.getKth()) {
					heap.add(bucket.get(i).getPoint2D().distanceSq(center), bucket.get(i));
				}
			}
			
			
		}
	}
	
	// ... the rest of the class
	int size; //number of elements in the tree
	int bucketSize; //max size of buckets
	Node root; //root of the tree
	Rectangle2D bbox; //the region of points that can exist in the tree
	
	//Constructor for XkdTree. An empty tree is represented setting the root to point to a single external node
	//whose bucket is empty.
	//bucketSize is the max size of the buckets, bbox is the bounding box
	public XkdTree(int bucketSize, Rectangle2D bbox) { 
		size = 0;
		ExternalNode node = new ExternalNode();
		node.bucket = new ArrayList<LPoint>();
		root = node;
		this.bbox = bbox;
		this.bucketSize = bucketSize;
		
	}
	
	//clear all entries from the tree
	public void clear() { 
		this.size = 0;
		ExternalNode node = new ExternalNode();
		node.bucket = new ArrayList<LPoint>();;
		this.root = node;
		//bounding box remains
	}
	
	//size of tree
	public int size() {
		return size;
	}
	
	//find element q, returns the point when found or null if not found
	public LPoint find(Point2D q) {
		return this.root.find(q);
	}
	
	//inserts point pt.
	//If pt is outside the bounding box, throws an exception
	public void insert(LPoint pt) throws Exception { /* ... */ 
		ArrayList<LPoint> pts = new ArrayList<LPoint>();
		pts.add(pt);
		bulkInsert(pts);
	}
	
	//Inserts lists of points pt
	//If any pt is outside the bounding box, throws an exception
	public void bulkInsert(ArrayList<LPoint> pts) throws Exception { /* ... */ 
		//check if points are in bounding box
		for(int i = 0; i < pts.size(); i++) {
			if((pts.get(i).getX() > this.bbox.high.getX()) || (pts.get(i).getX() < this.bbox.low.getX()) 
					|| (pts.get(i).getY() > this.bbox.high.getY()) || (pts.get(i).getY() < this.bbox.low.getY())) {
				throw new Exception("Attempt to insert a point outside bounding box");
			}
		}
		//call bulkinsert on the node
		root = root.bulkInsertHelper(pts);
		//increment size
		size = size + pts.size();
		
	}
	
	/*
	 * Generates a left to right preorder traversal of the tree.
	 * Returns an arraylist of strings
	 */
	public ArrayList<String> list() { /* ... */ 
		ArrayList<String> lst = new ArrayList<String>();
		if(root == null) {
			lst.add("[ ]");
			return lst;
		}
		return root.listHelper(lst); 
	}
	
	/*
	 * Given a query point, and it computes the closest point. If the tree is empty, it
	 * returns null. Otherwise, it returns a reference to the closest LPoint in the kd-tree.
	 */
	public LPoint nearestNeighbor(Point2D center) { /* ... */ 
		if(size == 0) { //empty tree
			return null;
		} else {
			LPoint best = null; //set best to null
			return root.nearestNeighborHelper(center, bbox, best);
		}
	}

	public void delete(Point2D pt) throws Exception { /* ... */
		if(root == null || this.find(pt) == null) {
			throw new Exception("Deletion of nonexistent point");
		}
		root.delete(pt);
		root = root.fixBucket(pt);
	}
	public ArrayList<LPoint> kNearestNeighbor(Point2D center, int k) { 
		if(size == 0) { //empty tree
			return new ArrayList<LPoint>();
		} else {
			MinK<Double, LPoint> heap = new MinK<Double, LPoint>(k , Double.POSITIVE_INFINITY); //set best to null
			root.kNearestNeighborhelper(center, bbox, heap);
			return heap.list();
		}
	
	}
}
