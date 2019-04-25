import java.util.Comparator;
import java.util.Iterator;
import java.util.Random;
import java.util.function.BiConsumer;
import java.io.PrintWriter;

public class SkipListExpt {
  public static void main(String[] args) {  
    Comparator<Integer> comp = new Comparator<Integer>() {

      @Override
      public int compare(Integer i1, Integer i2) {
        return i1 - i2;
      }

    };
    
    // These are our cost analysis tests
    // LINK TO GRAPH/TABLE: https://docs.google.com/spreadsheets/d/1r5SE8zE0NX_u_9QgU_hTFw718Oyfs8Myfxm5xfXCGaA/edit?usp=sharing
    // To our eyes, the graphs appear to be logarithmic, and so the major functions are all probably in O(log(n)) (on average).
    // NOTE: In the following loop, we switched which print statement was commented to get the counts of 'set' and 'get'.
    SkipList<Integer, Integer> sl = new SkipList<Integer, Integer>(comp);
    for (int i = 1; i < 1000; i++) {
      sl.set(i, i);
      sl.get(i);
      System.out.println(sl.setCount);
      //System.out.println(sl.getCount);
    }
    
    SkipList<Integer, Integer> slr = new SkipList<Integer, Integer>(comp);
    for (int i = 1; i < 1000; i++) {
      slr.set(i, i);
      slr.remove(i);
      slr.set(i, i);
      System.out.println(slr.removeCount);
    }
    
    /*
    The following comment includes basic tests
  
    SkipList<Integer, String> sl = new SkipList<Integer, String>(comp);

    sl.set(1, "test");
    sl.dump(new PrintWriter(System.out, true));
    sl.set(2, "word");
    sl.dump(new PrintWriter(System.out, true));
    sl.set(1, "newtest");
    sl.dump(new PrintWriter(System.out, true));
    sl.set(5, "continue");
    sl.dump(new PrintWriter(System.out, true));
    sl.set(3, "banana");
    sl.dump(new PrintWriter(System.out, true));
    sl.set(2, "howdy");
    sl.dump(new PrintWriter(System.out, true));
    sl.set(-1, "negative");
    sl.dump(new PrintWriter(System.out, true));
    System.out.println(sl.height);
    
    System.out.println(sl.get(2));
    System.out.println(sl.get(6));
    System.out.println(sl.get(2));
    System.out.println(sl.get(-1));
    System.out.println(sl.get(5));
    // BiConsumer to compare both lists
    BiConsumer<Integer, String> inc = (a, b) -> {
      if (a == 2) {
        System.out.println("it is 2");
      } else {
        System.out.println("it is not 2");
      }
    };

    sl.dump(new PrintWriter(System.out, true));
    
    sl.forEach(inc);
    Iterator<SLNode<Integer, String>> it = sl.nodes();

    while (it.hasNext()) {
      SLNode<Integer, String> temp = it.next();
      pen.println(temp.key + ", " + temp.value);
    } // while
    */
  } // main
} // class
