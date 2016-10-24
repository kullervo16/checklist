
package kullervo16.checklist.actors;

import akka.actor.UntypedActor;
import kullervo16.checklist.messages.PersistenceRequest;
import kullervo16.checklist.repository.ChecklistRepository;

/**
 * Actor to handle persistence.
 *
 * @author jef
 */
public class PersistenceActor extends UntypedActor {

    @Override
    public void onReceive(final Object work) throws Exception {

        if (work instanceof PersistenceRequest) {
            this.persist(((PersistenceRequest) work).getUuid());
        } else {
            unhandled(work);
        }
    }


    private void persist(final String uuid) {
        ChecklistRepository.INSTANCE.getChecklist(uuid).persist();
    }
}
