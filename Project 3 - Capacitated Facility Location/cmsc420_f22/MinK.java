package cmsc420_f22; // Do not delete this line
/* Author: Alexis Paul, apaul127, 117031522*/
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;


/*
 * The keys will be squared distances to the query point (of type
 *	Double) and the values will be points of type LPoint.
 */
public class MinK<Key extends Comparable<Key>, Value> {
	private class Node { // a node in the tree
		Key key; //key
		Value value; //value
	}
	
	//defines how nodes are compared, based on their keys
	private class keyComp implements Comparator<Node> {
		public int compare(Node k1, Node k2 ) {
			//System.out.println(k1.key);
			//System.out.println(k2.key);
			return k1.key.compareTo(k2.key);
		}
	}
	
	private ArrayList<Node> heap; //the MinK structure
	//private Node[] heap;
    private int size; //the number of elements in the structure
    private int capacity; //the max number of elements in the structure
    private Key maxKey; //the maximum key of the heap, infinity unless at max capacity
    
    private Integer getLeft(int i) {
    	if((2*i)+1 < heap.size()) {
    		return (2*i)+1;
    	} else {
    		return null;
    	}
    }
    
    private Integer getRight(int i) {
    	if((2*i)+2 < heap.size()) {
    		return (2*i)+2;
    	} else {
    		return null;
    	}
    }
    
    private Integer getPar(int i) {
    	if(i >=1) {
    		return (i-1)/2;
    	} else {
    		return null;
    	}
    }
    /**
     * MinK constructor, initializes a new heap, sets size to 0. 
     * @param k -> capacity
     * @param maxKey -> maxKey
     */
	public MinK(int k, Key maxKey) { 
		heap = new ArrayList<Node>();
		size = 0;
		capacity = k;
		this.maxKey = maxKey;
	}
	/*
	 * Size getter
	 */
	public int size() { 
		return size;
	}
	
	/*
	 * Removes all elements in the MinK structure by initializing a new heap. Sets size to 0. 
	 */
	public void clear() { 
		heap = new ArrayList<Node>();
		size = 0;
	}
	
	/**
	 * If the structure has k elements, this returns the maximum key value among
	 * these elements. Otherwise, it returns maxKey value given in the constructor
	 * @return
	 */
	public Key getKth() { 
		if(size == capacity) {
			return heap.get(0).key;
		} else {
			return this.maxKey;
		}
			
	}
	
	/*
	 * This adds the given key-value pair to the current set. If the
	 * structure has fewer than k element, this entry is added, increasing the size by one. If
	 * the structure has k elements and x is greater than or equal to the largest, the operation
	 * is ignored. If the structure has k elements and x is less than the largest, the pair (x, v)
	 * is added, and the previous largest is removed.
	 */
	public void add(Key x, Value v) {
		
		Node t = new Node(); //make new node to be added
		t.key = x;
		t.value = v;
		
		if(size < capacity) { //not full
			heap.add(t);
			siftUp(size, t);
			size++;
		} else if(size == capacity && (x.compareTo(this.getKth()) < 0)) {
			//add
			heap.set(0, t);
			siftDown(0, t);
			//sift down
			//size remains the same
		} 

		
	}
	
	/*
	 * Swaps nodes in the heap
	 */
	private void swap(int i, int j) {
		Node temp = heap.get(i);
		heap.set(i, heap.get(j));
		heap.set(j, temp);
	}
	
	/*
	 * sifts an element up to where it belongs in the heap
	 */
	private void siftUp(Integer i, Node k) {
		while(getPar(i) != null && k.key.compareTo(heap.get(getPar(i)).key) > 0) {
			//while the key is greater than its parent, swap
			swap(i, getPar(i));
			i = getPar(i);
		}
	
	}
	
	//sifts an element down to where it belongs in the heap
	private  void siftDown(int i, Node k) { 
		while(getLeft(i) != null) {
			Integer u = getLeft(i); //left child
			Integer v = getRight(i); //right child
			//Node swap;
			//find the greater child and swap
			
			if(v != null && heap.get(v).key.compareTo(heap.get(u).key) > 0) {
				//v is the greater child, so we swap so we can just reference u
				u = v; 
			}
			
			//if u is greater than the current key, we must swap them
			if(u != null && heap.get(u).key.compareTo(heap.get(i).key) > 0) {
				swap(i, u);
				i = u;
			} else {
				break;
			}
		
		}
		
	}
	
	

	/*
	 * Create a ArrayList of the values in the structure, sorted in
	 * increasing order by their key values.
	 */
	public ArrayList<Value> list() { 
		ArrayList<Node> lst = new ArrayList<Node>();
		for(int i = 0; i < this.heap.size(); i++) {
			lst.add(this.heap.get(i));
		}
		
		Collections.sort(lst, new keyComp()); //sorts the list by keys
		
		ArrayList<Value> val = new ArrayList<Value>();
		for(int i = 0; i < this.heap.size(); i++) {
			val.add(lst.get(i).value);
		}
		
		return val;
	}
}
