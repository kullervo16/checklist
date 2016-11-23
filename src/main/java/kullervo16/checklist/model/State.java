/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kullervo16.checklist.model;

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
    EXECUTED(true, false, false, true),
    EXECUTION_FAILED_NO_COMMENT(true, false, true, true),
    EXECUTION_FAILED(false, true, true, true),
    CHECK_FAILED_NO_COMMENT(true, false, true, true),
    CHECK_FAILED(false, true, true, true),
    ABORTED(false, true, false, true);

    private final boolean complete;

    private final boolean reopenable;

    private final boolean error;

    private final boolean open;


    State(final boolean open, final boolean complete, final boolean reopenable, final boolean error) {
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
}
