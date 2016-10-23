
package kullervo16.checklist.repository;

import akka.actor.ActorPath;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;

import java.util.HashMap;
import java.util.Map;

import kullervo16.checklist.actors.PersistenceActor;

/**
 * @author jef
 */

public class ActorRepository {

    private static final ActorSystem system;

    private static final Map<Class, ActorPath> pathIndex = new HashMap<>();

    static {

        system = ActorSystem.create("ChecklistActors");

        createActor(PersistenceActor.class, "persistenceActor");

    }

    private static ActorRef createActor(final Class type, final String name) {

        // create the actors
        final ActorRef ref = system.actorOf(Props.create(PersistenceActor.class), name);

        pathIndex.put(PersistenceActor.class, ref.path());

        return ref;
    }


    public static ActorRef getPersistenceActor() {
        return system.actorFor(pathIndex.get(PersistenceActor.class));
    }
}

