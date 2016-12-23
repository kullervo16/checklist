
package kullervo16.checklist.messages;

/**
 * This request serves as a heartbeat to determine whether the persistence system still works..
 * @author jef
 */
public class HeartBeat {
    
    private long timestamp;

    public HeartBeat() {
        this.timestamp = System.currentTimeMillis();
    }

    public long getTimestamp() {
        return timestamp;
    }
        

}
