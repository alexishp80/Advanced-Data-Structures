package cmsc420_f22; // Do not delete this line

import java.util.ArrayList;

public class LeftistHeap<Key extends Comparable<Key>, Value> {
	private class LHNode { // a node in the tree
		Key key; //key
		Value value; //value
		LHNode left, right; //children
		int npl; //null path length
	}
	
	private LHNode root; //root of tree
	public LeftistHeap() { //constructor
		root = null; 
	}
	
	public boolean isEmpty() { //checks if heap is empty, true if empty false if not
		if (this.root == null) {
			return true;
		} else {
			return false;
		}
	}
	
	public void clear() { //removes all existing contents from the heap
		this.root = null;
	}
	/*Inserts a key-value pair into the heap
	 * @param x the key
	 * @param v the value */
	public void insert(Key x, Value v) {
		LHNode node = new LHNode(); //creating new node for this key-value pair
		node.key = x; //assigning key
		node.value = v; //assigning value
		node.left= null; //setting children to null
		node.right = null;
		node.npl = 0; //setting npl to 0, as children are null
		if (isEmpty()) { //if the tree is empty, this node becomes the root
			this.root = node;
		} else { //tree is not empty
			LeftistHeap<Key, Value> h2 = new LeftistHeap<Key, Value>(); //create new heap
			h2.root = node; //set new heap's root to new node
			this.mergeWith(h2); //merge heaps, handles sorting and updating npl values
		}
	}
	/*Merges current heap with h2. has no effect if h2 is null 
	 * or this == h2. Otherwise, both heaps are merged into the current
	 * onem and h2 becomes empty. 
	 * @param h2 another heap to be merged with the current one
	 * 
	 * */
	public void mergeWith(LeftistHeap<Key, Value> h2) { 
		if (h2 == null || this == h2) {
			return;
		} else {
			this.root = merge(this.root, h2.root);
			h2.clear();
		}
	}
	
	/* Private recursive helper for mergeWith*/
	private LHNode merge(LHNode u, LHNode v) {
		//if one is empty, return the other
		if(u == null) {
			return v;
		}
		if (v == null) {
			return u;
		}
		
		if (u.key.compareTo(v.key) > 0) {
			//swap so that u has smaller key
			LHNode w = v;
			v = u;
			u = w;
		}
		
		if (u.left == null) {
			u.left = v; //put v in u.left to preserve leftist property
		} else { //merge v on the right subtree
			u.right = merge(u.right, v); //recursively merge on the right subtree
			if (u.left.npl < u.right.npl) { //if leftist property fails
				LHNode w = u.left; //swap 
				u.left = u.right;
				u.right = w;
			}
			u.npl = u.right.npl + 1; //npl value update
		}
		return u; //return root of final tree
	}
	
	/*Given a key x, this method splits the heap into two heaps. One heap contains
	 * all elements whose keys are less than or equal to x, and a new heap contains
	 * all entries whose keys are greater than x. This new heap is then returned. */
	public LeftistHeap<Key, Value> split(Key x) { 
		ArrayList<LHNode> list = new ArrayList<LHNode>(); //create list of roots of heaps to merge
		this.root = splitHelper(list, x, this.root); //recursive helper function
		//Now we have a messed up tree and a list of subtrees
		fix(this.root); //recursive helper function
		LeftistHeap<Key, Value> heap = new LeftistHeap<Key, Value>(); //instantiate new heap
		//merge subtrees
		if (list.isEmpty()) { //no roots in list, no trees to merge, conditions are satisfied in original heap
			heap.root = null; //empty heap
			return heap;
		} else {
			//merge
			heap.root = list.get(0); //set root to first root in list
			LeftistHeap<Key, Value> h2 = new LeftistHeap<Key, Value>(); //helper heap
			for(int i = 1; i < list.size(); i++) { //for every root in the list
				h2.root = list.get(i); //set root as the root of the helper heap
				heap.mergeWith(h2); //merge with heap
				//heap acts as a working heap
			}
			
			return heap; //returns the new heap
		}
	}
	
	//Private helper method that fixes the original heap by swapping subtrees and fixing npl values
	private void fix(LHNode u) { 
		
		if(u == null) { //nothing to fix
			return;
		} else { //need to start from the bottom of the tree, recurse to bottom
			//preorder traversal
			fix(u.left); 
			fix(u.right);
		}
		
		if(u.left == null || u.right == null) { //if either child is null
			u.npl = 0; //null path length is zero
		} 
		
		if(u.left == null && u.right != null) { //no left child, but there is a right child
			u.left = u.right; //swap!
			u.right = null;
			
		} else { //there are two children
		
			if (u.right != null && u.left.npl < u.right.npl) { //leftist property violated
				LHNode w = u.left; //swap!
				u.left = u.right;
				u.right = w;
			}
			
	
		}
		
		if (u.right != null) { //right child exists
			u.npl = u.right.npl + 1; //update npl based on right child (otherwise it will be zero from above)
		}
	}
	
	//Private helper method to split. Returns null if the subtree is added to the second heap, returns u if it remains in the heap
	private LHNode splitHelper(ArrayList<LHNode> list, Key x, LHNode u) {
		if(u == null) { //the node can't be portioned to either heap if it doesn't exist
			return u;
		}
		if (u.key.compareTo(x) <= 0) { //checking split property
			//recurses down the subtrees if u does not get added to the other heap
			//preorder traversal
			u.left = splitHelper(list, x, u.left); //sets the children values to the return of these recursive calls
			u.right = splitHelper(list, x, u.right);
		} else {
			//u and it's subtree belong in the other heap
			list.add(u);
			return null; //unlink subtree from original tree
		}
		
		return u; //returns to split
		
		
		
	}
	
	//Returns smallest key in the heap, makes no changes to heap structure or content
	public Key getMinKey() { 
		if (isEmpty()) { //empty tree 
			return null; 
		} else {
			return this.root.key; //smallest key in the leftist heap is the root
		}
	}
	
	/*removes and returns smallest key in the heap.
	 * Throws exception if the heap is empty. Locates minimum key value, 
	 * deletes the entry, and returns the value. */
	public Value extractMin() throws Exception { 
		if (isEmpty()) { //null check
			throw new Exception("Empty heap"); //throw exception
		}
		//smallest key in leftist heap is the root, need to get a heap with the subtrees
		LeftistHeap<Key, Value> left = new LeftistHeap<Key, Value>(); //create new heap for left subtree
		left.root = this.root.left;
		LeftistHeap<Key, Value> right = new LeftistHeap<Key, Value>(); //create new heap for right subtree
		right.root = this.root.right;
		left.mergeWith(right); //merge left and right subtrees together, now all nodes are under "left" heap
		Value ret = this.root.value; //save min value
		this.root = left.root; //assign the heap as the new updated heap
		return ret; //return min value
	}
	
	/*Lists contents of the tree based on right-to-left traversal*/
	public ArrayList<String> list() { 
		ArrayList<String> list = new ArrayList<String>(); //create list to put strings in
		listHelper(list, this.root); //call to helper
		return list;
	}
	
	/*Private recursive helper that takes in the working list and the current node.*/
	private void listHelper(ArrayList<String> list, LHNode u) {
		if (u == null) { //if node is null
			list.add("[]"); //given string format for null node
		} else {
			list.add("(" + u.key + ", " + u.value + ") [" + u.npl + "]"); //given string format for nonnull node
			listHelper(list, u.right); //recurse on right
			listHelper(list, u.left); //recurse on left
		}
	}
}
