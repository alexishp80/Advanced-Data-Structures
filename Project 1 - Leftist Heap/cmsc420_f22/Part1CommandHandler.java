package cmsc420_f22;

// YOU SHOULD NOT MODIFY THIS FILE

import java.util.Scanner;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

/**
 * Command handler. This reads a single command line, processes the command (by
 * invoking the appropriate method(s) on the data structure), and returns the
 * result as a string.
 */

public class Part1CommandHandler {

	private HashMap<String, LeftistHeap<Integer, String>> heaps; // map of heaps

	/**
	 * Default constructor.
	 */
	public Part1CommandHandler() {
		heaps = new HashMap<String, LeftistHeap<Integer, String>>(); // initialize heaps
	}

	/**
	 * Process a single command and return the string output. Each command begins
	 * with a command followed by a list of arguments. The arguments are separated
	 * by colons (":").
	 */
	public String processCommand(String inputLine) throws Exception {
		Scanner line = new Scanner(inputLine);
		line.useDelimiter(":"); // use ":" to separate arguments
		String output = new String(); // for storing summary output
		String cmd = (line.hasNext() ? line.next() : ""); // next command
		try {
			// -----------------------------------------------------
			// COMMENT string
			// - comment line for the output
			// -----------------------------------------------------
			if (cmd.compareTo("comment") == 0) {
				String comment = line.next(); // read the comment
				output += "[" + comment + "]" + System.lineSeparator();
			}
			// -----------------------------------------------------
			// INSERT ID label key
			// - add item with given label and key and save its location
			// -----------------------------------------------------
			else if (cmd.compareTo("insert") == 0) {
				String heapID = line.next(); // read heap ID
				String label = line.next(); // read the label
				int key = line.nextInt(); // read the key
				LeftistHeap<Integer, String> heap = getHeap(heapID, true); // get/create the heap
				output += "insert(" + key + ", " + label + ") into [" + heapID + "]: ";
				
				heap.insert(key, label); // add to heap
				output += "successful" + System.lineSeparator();
			}
			// -----------------------------------------------------
			// MERGE ID1 ID2
			// - merge heaps with ID1 and ID2, result stored in ID1
			// -----------------------------------------------------
			else if (cmd.compareTo("merge") == 0) {
				String heapID1 = line.next(); // read heap IDs
				String heapID2 = line.next();
				LeftistHeap<Integer, String> heap1 = getHeap(heapID1, false); // get the heaps
				LeftistHeap<Integer, String> heap2 = getHeap(heapID2, false); 
				output += "merge[" + heapID1 + "] with [" + heapID2 + "]: ";
				
				heap1.mergeWith(heap2); // merge them
				output += "successful" + System.lineSeparator();
			}
			// -----------------------------------------------------
			// SPLIT ID1 key ID2
			// - split heap ID1 about key putting larger keys in ID2
			// -----------------------------------------------------
			else if (cmd.compareTo("split") == 0) {
				String heapID1 = line.next(); // read ID of heap to split
				Integer key = line.nextInt(); // read the key
				String heapID2 = line.next(); // read the ID of new heap
				LeftistHeap<Integer, String> heap1 = getHeap(heapID1, false); // get heap1
				output += "split[" + heapID1 + "] at " + key + " forming [" + heapID2 + "]: ";
				if (heaps.containsKey(heapID2)) { // heapID2 already exists?
					throw new Exception("Attempt to split into an existing heap: " + heapID2);
				}
				
				LeftistHeap<Integer, String> heap2 = heap1.split(key); // split it
				heaps.put(heapID2, heap2); // add new heap to the map
				output += "successful" + System.lineSeparator();
			}
			// -----------------------------------------------------
			// LIST ID
			// - list the heap contents, with each tree in reverse preorder
			// -----------------------------------------------------
			else if (cmd.compareTo("list") == 0) {
				String heapID = line.next(); // read heap ID
				LeftistHeap<Integer, String> heap = getHeap(heapID, false); // get the heap
				
				ArrayList<String> list = heap.list();
				if (list == null) {
					throw new Exception("list returned a null result");
				}
				output += "list[" + heapID + "]:" + System.lineSeparator();
				for (String s : list) {
					output += "  " + s + System.lineSeparator();
				}
				output += heapStructure(list); // summarize heap contents (indented)
			}
			// -----------------------------------------------------
			// GET-MIN-KEY ID
			// - get the minimum element from the heap
			// -----------------------------------------------------
			else if (cmd.compareTo("get-min-key") == 0) {
				String heapID = line.next(); // read heap ID
				LeftistHeap<Integer, String> heap = getHeap(heapID, false); // get the heap
				output += "get-min-key[" + heapID + "]: ";

				Integer key = heap.getMinKey(); // get the minimum
				output += (key == null ? "null" : key) + System.lineSeparator();
			}
			// -----------------------------------------------------
			// EXTRACT-MIN ID
			// - extract the minimum element from the heap
			// -----------------------------------------------------
			else if (cmd.compareTo("extract-min") == 0) {
				String heapID = line.next(); // read heap ID
				LeftistHeap<Integer, String> heap = getHeap(heapID, false); // get the heap
				output += "extract-min[" + heapID + "]: ";
				
				String label = heap.extractMin(); // extract the minimum
				output += label + System.lineSeparator();
			}
			// -----------------------------------------------------
			// CLEAR ID
			// -----------------------------------------------------
			else if (cmd.compareTo("clear") == 0) {
				String heapID = line.next(); // read heap ID
				LeftistHeap<Integer, String> heap = getHeap(heapID, false); // get the heap
				output += "clear[" + heapID + "]: ";
				
				heap.clear(); // clear the heap
				output += "successful" + System.lineSeparator();
			}
			// -----------------------------------------------------
			// IS-EMPTY ID
			// -----------------------------------------------------
			else if (cmd.compareTo("is-empty") == 0) {
				String heapID = line.next(); // read heap ID
				LeftistHeap<Integer, String> heap = getHeap(heapID, false); // get the heap
				output += "is-empty[" + heapID + "]: ";
				
				boolean isEmpty = heap.isEmpty(); // determine its status
				output += (isEmpty ? "yes" : "no") + System.lineSeparator();
			}
			// -----------------------------------------------------
			// Unrecognized command
			// -----------------------------------------------------
			else {
				if (cmd.compareTo("") == 0)
					System.err.println("Error: Empty command line (Ignored)");
				else
					System.err.println("Error: Invalid command - \"" + cmd + "\" (Ignored)");
			}
			line.close();
		} catch (Exception e) { // exception thrown?
			output += "Failure due to exception: \"" + e.getMessage() + "\"" + System.lineSeparator();
		} catch (Error e) { // error occurred?
			System.err.print("Operation failed due to error: " + e.getMessage());
			e.printStackTrace(System.err);
		} finally { // always executed
			line.close(); // close the input scanner
		}
		return output; // return summary output
	}

	/**
	 * Get the heap associated with a given identifier. Optionally, the heap
	 * is created if it does not exist.
	 */
	private LeftistHeap<Integer, String> getHeap(String heapID, boolean create) throws Exception {
		LeftistHeap<Integer, String> heap = heaps.get(heapID); // get the heap
		if (heap == null) {
			if (create) {
				heap = new LeftistHeap<Integer, String>(); // create the heap
				heaps.put(heapID, heap); // add it to the map
			} else {
				throw new Exception("Attempt to access nonexistant heap: " + heapID);
			}
		}
		return heap;
	}

	/**
	 * Print the heap contents with indentation.
	 */
	private static String heapStructure(ArrayList<String> entries) {
		String output = "Formatted structure:" + System.lineSeparator();
		Iterator<String> iter = entries.iterator(); // iterator for the list
		if (iter.hasNext()) { // tree is nonempty
			output += heapStructureHelper(iter, "  "); // print everything
		}
		return output;
	}

	/**
	 * Recursive helper for treeStructure. The argument iterator specifies the next
	 * node from the preorder list to be printed, and the argument indent indicates
	 * the indentation to be performed (of the form "| | | ...").
	 */
	private static String heapStructureHelper(Iterator<String> iter, String indent) {
		final String levelIndent = "| "; // the indentation for each level of the tree
		String output = "";
		if (iter.hasNext()) {
			String entry = iter.next(); // get the next entry
			Boolean isExtern = (entry.length() > 0 && entry.charAt(0) == '['); // external?
			if (!isExtern) { // not external
				output += heapStructureHelper(iter, indent + levelIndent); // print left subtree
				output += indent + entry + System.lineSeparator(); // print this node
				output += heapStructureHelper(iter, indent + levelIndent); // print right subtree
			}
		} else {
			System.err.println("Unexpected trailing elements in entries list"); // shouldn't get here!
		}
		return output;
	}
}
