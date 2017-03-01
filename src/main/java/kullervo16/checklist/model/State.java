/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kullervo16.checklist.model;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

/**
 * Class to represent a state of a step or a complete checklist.
 *
 * @author jeve
 */
public enum State implements Comparable<State> {
    NOT_YET_APPLICABLE(false, false, false, false),
    UNKNOWN(false, false, false, false),
    NOT_APPLICABLE(false, true, false, false),
    OK(false, true, true, false),
    IN_PROGRESS(true, false, false, false),
    EXECUTED(true, false, false, false),
    EXECUTION_FAILED_NO_COMMENT(true, false, false, true),
    EXECUTION_FAILED(false, true, true, true),
    CHECK_FAILED_NO_COMMENT(true, false, false, true),
    CHECK_FAILED(false, true, true, true),
    ABORTED(false, true, false, true);

    private static final Set<State> COMPLETE_WITH_ERRORS;

    private static final Set<State> COMPLETE;

    private final boolean open;

    private final boolean complete;

    private final boolean reopenable;

    private final boolean error;

    static {
        final ArrayList<State> statesList = new ArrayList<>();

        for (final State stateWalker : State.values()) {

            if (stateWalker.isError() && stateWalker.isComplete()) {
                statesList.add(stateWalker);
            }
        }

        COMPLETE_WITH_ERRORS = new HashSet<>(statesList);
    }


    static {
        final ArrayList<State> statesList = new ArrayList<>();

        for (final State stateWalker : State.values()) {

            if (stateWalker.isComplete()) {
                statesList.add(stateWalker);
            }
        }

        COMPLETE = new HashSet<>(statesList);
    }


    State(final boolean open, final boolean complete, final boolean reopenable, final boolean error) {

        // Coherence checks: should always be false
        // Some checks are duplicates. It is intentional. Those tests will not have performance issues.
        {
            if (open && (complete||reopenable)) {
                throw new RuntimeException("A state cannot be open and complete, reopenable at the same time !");
            }

            if (complete && open) {
                throw new RuntimeException("A state cannot be complete and open at the same time !");
            }

            if (reopenable && (open || !complete)) {
                throw new RuntimeException("A state cannot be reopenable and open, not complete at the same time !");
            }
        }

        this.complete = complete;
        this.reopenable = reopenable;
        this.error = error;
        this.open = open;
    }


    public boolean isComplete() {
        return this.complete;
    }


    public boolean isReopenable() {
        return this.reopenable;
    }


    public boolean isError() {
        return this.error;
    }


    public boolean isOpen() {
        return this.open;
    }


    public static Set<State> getCompleteWithErrors() {
        return COMPLETE_WITH_ERRORS;
    }


    public static Set<State> getComplete() {
        return COMPLETE;
    }
}
