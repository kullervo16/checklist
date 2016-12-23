
package kullervo16.checklist.health;

import java.util.Date;
import javax.ejb.Schedule;
import javax.ejb.Singleton;
import kullervo16.checklist.messages.HeartBeat;
import kullervo16.checklist.messages.PersistenceRequest;
import kullervo16.checklist.repository.ActorRepository;

/**
 * Class that will periodically send a health persistence message. The result will be used to determine
 * the health of the system.
 * 
 * @author jef
 */
@Singleton
public class PersistenceCheckerSessionBean {

    @Schedule(dayOfWeek = "*", month = "*", hour = "*", dayOfMonth = "*", year = "*", minute = "*", second = "0", persistent = false)    
    public void myTimer() {        
        ActorRepository.getPersistenceActor().tell(new HeartBeat(), null);
    }
}
    
