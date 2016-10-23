
package kullervo16.checklist.messages;

/**
 * Message signaling that a checklist needs persisting. Note that we pass the UUID
 * of the element to be persisted, so under high load, it is possible that we have multiple
 * modifications per message... that's no problem (otherwise we should be transporting a clone
 * which seems overkill, especially with the expected amount of traffic.. which in general
 * will still result in 1 save action per update).
 *
 * @author jef
 */
public class PersistenceRequest {

    private final String uuid;


    public PersistenceRequest(final String uuid) {
        this.uuid = uuid;
    }


    public String getUuid() {
        return uuid;
    }
}
