/**
 * A driver for CS1501 Project 4
 * @author	Dr. Farnan
 */
package cs1501_p4;
import java.util.ArrayList;

public class App {
    public static void main(String[] args) {
        NetAnalysis na = new NetAnalysis("build/resources/main/network_data2.txt");
        // System.out.println("Edge from 6 end: " + na.graph[6].next.next.next.next.getEnd());
        // System.out.println("Edge from 6 length: " + na.graph[6].next.next.next.next.getLength());
        // System.out.println("Edge from 6 type: " + na.graph[6].next.next.next.next.getType());
        // System.out.println("Edge from 6 time: " + na.graph[6].next.next.next.next.getTime());
        System.out.println("Would it still work? " + na.connectedTwoVertFail());
        ArrayList<STE> list3 = na.lowestAvgLatST();
        System.out.println("Min spanning tree: ");
        if(list3 != null) {
            for (int j = 0; j < list3.size(); j++) System.out.println(list3.get(j).toString());
        } else System.out.println("List3 is null");
        System.out.println("______________________");
        System.out.println("Connected with copper? " + na.copperOnlyConnected());
        
        ArrayList<Integer> list = na.lowestLatencyPath(0, 4);
        if (list != null){
            for (int i = 0; i < list.size(); i++){
                System.out.print(list.get(i) + ":");
            }
        } else System.out.println("List is null. No path.");
        System.out.println();
        ArrayList<Integer> list2 = new ArrayList<Integer>();
        list2.add(0);
        list2.add(2);
        list2.add(5);
        list2.add(3);
        list2.add(7);
        list2.add(6);
        list2.add(4);
        list2.add(1);
        // list2.add(1);
        // list2.add(2);
        // list2.add(4);
        // list2.add(0);
        // list2.add(3);
        System.out.println("Lowest bandwidth: " + na.bandwidthAlongPath(list2));
    }
}
