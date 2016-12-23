
package kullervo16.checklist.actors;

import akka.actor.UntypedActor;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.logging.Level;
import java.util.logging.Logger;
import kullervo16.checklist.messages.HeartBeat;
import kullervo16.checklist.messages.PersistenceRequest;
import kullervo16.checklist.repository.ChecklistRepository;

/**
 * Actor to handle persistence.
 *
 * @author jef
 */
public class PersistenceActor extends UntypedActor {
    private static final Logger logger = Logger.getLogger(PersistenceActor.class.getName());

    @Override
    public void onReceive(final Object work) throws Exception {

        if (work instanceof PersistenceRequest) {
            this.persist(((PersistenceRequest) work).getUuid());
        } else if(work instanceof HeartBeat) {
            this.handleHeartBeat((HeartBeat)work);
        } else {
            unhandled(work);
        }
    }


    private void persist(final String uuid) {
        ChecklistRepository.INSTANCE.getChecklist(uuid).persist(false);
    }

    private void handleHeartBeat(HeartBeat heartBeat) {
        try(PrintWriter writer = new PrintWriter(new File("/opt/checklist/heartbeat"));) {
            Long delay = System.currentTimeMillis() - heartBeat.getTimestamp();
            writer.append("Actual persistence delay = ").append(delay.toString()).append(" ms");
            writer.flush();
            writer.close();
        }catch(IOException ioe) {
            logger.log(Level.SEVERE, "Cannot write hearbeat", ioe);
        }
    }
    
}
