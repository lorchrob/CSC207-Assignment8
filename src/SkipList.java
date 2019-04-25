import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Random;
import java.util.function.BiConsumer;

/**
 * An implementation of skip lists.
 */
public class SkipList<K, V> implements SimpleMap<K, V> {

  // +-----------+---------------------------------------------------
  // | Constants |
  // +-----------+

  /**
   * The initial height of the skip list.
   */
  static final int MAX_HEIGHT = 15;

  // +---------------+-----------------------------------------------
  // | Static Fields |
  // +---------------+

  static Random rand = new Random();

  // +--------+------------------------------------------------------
  // | Fields |
  // +--------+

  /**
   * Pointers to all the front elements.
   */
  SLNode<K, V> front;

  /**
   * The comparator used to determine the ordering in the list.
   */
  Comparator<K> comparator;

  /**
   * The number of values in the list.
   */
  int size;

  /**
   * The current height of the skiplist.
   */
  int height;

  /**
   * The probability used to determine the height of nodes.
   */
  double prob = 0.5;

  /**
   * Record the cost of the 'get' method
   */
  int getCount = 0;

  /**
   * Record the cost of the 'set' method
   */
  int setCount = 0;

  /**
   * Record the cost of the 'remove' method
   */
  int removeCount = 0;

  // +--------------+------------------------------------------------
  // | Constructors |
  // +--------------+

  /**
   * Create a new skip list that orders values using the specified comparator.
   */
  public SkipList(Comparator<K> comparator) {
    this.front = new SLNode<K, V>(null, null, MAX_HEIGHT);
    for (int i = 0; i <= MAX_HEIGHT; i++) {
      front.next.set(i, null);
    } // for
    this.comparator = comparator;
    this.size = 0;
    this.height = 0;
  } // SkipList(Comparator<K>)

  /**
   * Create a new skip list that orders values using a not-very-clever default comparator.
   */

  public SkipList() {
    this((k1, k2) -> k1.toString().compareTo(k2.toString()));
  } // SkipList()

  // +-------------------+-------------------------------------------
  // | SimpleMap methods |
  // +-------------------+

  @Override
  /**
   * Method to update/add values in the SkipList.
   * 
   * @param K key, the key of the element being added or updated
   *        V value, the value of the element being added or updated
   *        
   *@return V oldValue, returns the old value that was updated (null if a new element was added)
   *
   *@pre  Key shouldn't be null
   *@post Key is now associated with V value in the SkipList
   *      Key order is preserved in the SkipList (and no additional items are added or deleted)
   */
  public V set(K key, V value) {
    this.setCount = 0;

    ArrayList<SLNode<K, V>> update = new ArrayList<SLNode<K, V>>(MAX_HEIGHT + 1); // keep track of nodes
                                                                              // to be updated
    SLNode<K, V> x = this.front; // node pointer used to search for desired element

    // fill 'update' with null values so it can be used with 'set'
    for (int i = 0; i <= MAX_HEIGHT; i++) {
      update.add(null);
    }

    // make as much "progress" as possible on each level, noting
    // pointers we will have to update
    for (int i = this.height; i >= 0; i--) {
      while (x.next.get(i) != null && comparator.compare(x.next.get(i).key, key) < 0) {
        x = x.next.get(i);
        setCount++;
      } // while
      setCount++;
      update.set(i, x);
    } // for

    // when no more progress can be made at the first level, we must be
    // directly before the node that possibly contains the desired element

    x = x.next.get(0);
    setCount++;

    // if the keys match, we update!
    if (x != null && comparator.compare(x.key, key) == 0) {
      V temp = x.value;
      x.value = value;
      return temp; // return affected value
    } // if

    // otherwise, we insert a new node
    else {
      int newLevel = randomLevel();
      if (newLevel > this.height) {
        // take care of update values between current and new list height
        for (int i = this.height + 1; i <= newLevel; i++) {
          update.set(i, front);
          setCount++;
        }
        this.height = newLevel;
      }

      // make our new node
      x = new SLNode<K, V>(key, value, newLevel);

      // update/add pointers to make list properly connected
      for (int i = 0; i <= newLevel; i++) {
        x.next.set(i, update.get(i).next.get(i));
        update.get(i).next.set(i, x);
        setCount++;
      } // for
      size++;
    } // else
    return null; // new value was added, so return null
  } // set(K,V)


  // helper function to get a random level for a new node between 0 and MAX_HEIGHT
  private int randomLevel() {
    int newLevel = 0;
    while (rand.nextDouble() < this.prob) {
      newLevel++;
    } // while
    return Math.min(newLevel, MAX_HEIGHT);
  }

  @Override
  /**
   * Method to get values in the SkipList.
   * 
   * @param K key, the key associated with the value you want to get
   *        
   *@return V value, the value associated with key (null if key isn't in the SkipList)
   *
   *@pre  Key shouldn't be null
   *@post SkipList is not modified
   */
  public V get(K key) {
    this.getCount = 0;
    
    if (key == null) {
      throw new NullPointerException("null key");
    } // if
    SLNode<K, V> x = this.front; // node pointer used to search for desired element

    // make as much "progress" as possible on each level
    for (int i = this.height; i >= 0; i--) {
      while (x.next.get(i) != null && comparator.compare(x.next.get(i).key, key) < 0) {
        x = x.next.get(i);
        getCount++;
      } // while
      getCount++;
    } // for

    // when no more progress can be made at the first level, we must be
    // directly before the node that possibly contains the desired element

    x = x.next.get(0);
    getCount++;

    // if the keys match, we return the value
    if (x != null && comparator.compare(x.key, key) == 0) {
      return x.value; // return value
    } // if
    return null;
  } // get(K,V)

  @Override
  public int size() {
    return this.size;
  } // size()

  @Override
  public boolean containsKey(K key) {
    Iterator<K> it = keys();
    while (it.hasNext()) {
      if (comparator.compare(it.next(), key) == 0) {
        return true;
      }
    }
    return false;
  } // containsKey(K)

  @Override
  /**
   * Method to remove elements in the SkipList.
   * 
   * @param K key, the key associated with the value you want to remove
   *        
   *@return V value, the value associated with the element you've removed (null if there is no
   *        element with key)
   *
   *@pre  Key shouldn't be null
   *@post Element with key is no longer in the SkipList (unless key isn't in the SkipList, in which 
   *      there is no change)
   *      Order is preserved
   */
  public V remove(K key) {
    this.removeCount = 0;
    
    ArrayList<SLNode<K, V>> update = new ArrayList<SLNode<K, V>>(MAX_HEIGHT + 1); // keep track of nodes
                                                                              // to be updated
    SLNode<K, V> x = this.front; // node pointer used to search for desired element

    // fill 'update' with null values so it can be used with 'set'
    for (int i = 0; i <= MAX_HEIGHT; i++) {
      update.add(null);
    }

    // make as much "progress" as possible on each level, noting
    // pointers we will have to update
    for (int i = this.height; i >= 0; i--) {
      while (x.next.get(i) != null && comparator.compare(x.next.get(i).key, key) < 0) {
        x = x.next.get(i);
        removeCount++;
      } // while
      update.set(i, x);
      removeCount++;
    } // for

    // when no more progress can be made at the first level, we must be
    // directly before the node that possibly contains the desired element

    x = x.next.get(0);
    removeCount++;

    // if the keys match, we remove!
    if (x != null && comparator.compare(x.key, key) == 0) {
      for (int i = 0; i <= this.height; i++) {
        if (update.get(i).next.get(i) != x) {
          break;
        }
        removeCount++;
        update.get(i).next.set(i, x.next.get(i));// remove element
      }
      while (this.height > 1 && this.front.next.get(this.height) == null) {
        this.height--;
        removeCount++;
      }
      size--;
      return x.value; // return removed value
    } // if
    return null;


  } // remove(K)

  @Override
  public Iterator<K> keys() {
    return new Iterator<K>() {
      Iterator<SLNode<K, V>> nit = SkipList.this.nodes();

      @Override
      public boolean hasNext() {
        return nit.hasNext();
      } // hasNext()

      @Override
      public K next() {
        return nit.next().key;
      } // next()

      @Override
      public void remove() {
        nit.remove();
      } // remove()
    };
  } // keys()

  @Override
  public Iterator<V> values() {
    return new Iterator<V>() {
      Iterator<SLNode<K, V>> nit = SkipList.this.nodes();

      @Override
      public boolean hasNext() {
        return nit.hasNext();
      } // hasNext()

      @Override
      public V next() {
        return nit.next().value;
      } // next()

      @Override
      public void remove() {
        nit.remove();
      } // remove()
    };
  } // values()

  @Override
  // Method to apply an action to each node of the SkipList
  public void forEach(BiConsumer<? super K, ? super V> action) {
    Iterator<SLNode<K, V>> i = nodes();
    while (i.hasNext()) {
      SLNode<K, V> n = i.next();
      action.accept(n.key, n.value);
    }

  } // forEach

  // +----------------------+----------------------------------------
  // | Other public methods |
  // +----------------------+

  /**
   * Dump the tree to some output location.
   */
  public void dump(PrintWriter pen) {
    String leading = "          ";

    SLNode<K, V> current = front.next.get(0);

    // Print some X's at the start
    pen.print(leading);
    for (int level = 0; level <= this.height; level++) {
      pen.print(" X");
    } // for
    pen.println();
    printLinks(pen, leading);

    while (current != null) {
      // Print out the key as a fixed-width field.
      // (There's probably a better way to do this.)
      String str;
      if (current.key == null) {
        str = "<null>";
      } else {
        str = current.key.toString();
      } // if/else
      if (str.length() < leading.length()) {
        pen.print(leading.substring(str.length()) + str);
      } else {
        pen.print(str.substring(0, leading.length()));
      } // if/else

      // Print an indication for the links it has.
      for (int level = 0; level < current.next.size(); level++) {
        pen.print("-*");
      } // for
      // Print an indication for the links it lacks.
      for (int level = current.next.size(); level <= this.height; level++) {
        pen.print(" |");
      } // for
      pen.println();
      printLinks(pen, leading);

      current = current.next.get(0);
    } // while

    // Print some O's at the start
    pen.print(leading);
    for (int level = 0; level <= this.height; level++) {
      pen.print(" O");
    } // for
    pen.println();
  } // dump(PrintWriter)

  /**
   * Print some links (for dump).
   */
  void printLinks(PrintWriter pen, String leading) {
    pen.print(leading);
    for (int level = 0; level <= this.height; level++) {
      pen.print(" |");
    } // for
    pen.println();
  } // printLinks


  // +---------+-----------------------------------------------------
  // | Helpers |
  // +---------+

  /**
   * Pick a random height for a new node.
   */
  int randomHeight() {
    int result = 1;
    while (rand.nextDouble() < prob) {
      result = result + 1;
    }
    return result;
  } // randomHeight()

  /**
   * Get an iterator for all of the nodes. (Useful for implementing the other iterators.)
   */
  Iterator<SLNode<K, V>> nodes() {
    return new Iterator<SLNode<K, V>>() {

      /**
       * A reference to the next node to return.
       */
      SLNode<K, V> next = SkipList.this.front.next.get(0);

      @Override
      public boolean hasNext() {
        return this.next != null;
      } // hasNext()

      @Override
      public SLNode<K, V> next() {
        if (this.next == null) {
          throw new IllegalStateException();
        }
        SLNode<K, V> temp = this.next;
        this.next = this.next.next.get(0);
        return temp;
      } // next();
    }; // new Iterator
  } // nodes()

  // +---------+-----------------------------------------------------
  // | Helpers |
  // +---------+

} // class SkipList


/**
 * Nodes in the skip list.
 */
class SLNode<K, V> {

  // +--------+------------------------------------------------------
  // | Fields |
  // +--------+

  /**
   * The key.
   */
  K key;

  /**
   * The value.
   */
  V value;

  /**
   * Pointers to the next nodes.
   */
  ArrayList<SLNode<K, V>> next;

  // +--------------+------------------------------------------------
  // | Constructors |
  // +--------------+

  /**
   * Create a new node of height n with the specified key and value.
   */
  public SLNode(K key, V value, int n) {
    this.key = key;
    this.value = value;
    this.next = new ArrayList<SLNode<K, V>>(n + 1);
    for (int i = 0; i <= n; i++) {
      this.next.add(null);
    } // for
  } // SLNode(K, V, int)

  // +---------+-----------------------------------------------------
  // | Methods |
  // +---------+

} // SLNode<K,V>
