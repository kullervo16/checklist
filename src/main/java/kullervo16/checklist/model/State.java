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
    NOT_YET_APPLICABLE(false, false, false),
    UNKNOWN(false, false, false),
    NOT_APPLICABLE(true, false, false),
    OK(true, true, false),
    ON_HOLD(false, false, false),
    EXECUTED(false, false, true),
    EXECUTION_FAILED_NO_COMMENT(false, true, true),
    EXECUTION_FAILED(true, true, true),
    CHECK_FAILED_NO_COMMENT(false, true, true),
    CHECK_FAILED(true, true, true),
    ABORTED(true, false, true);

    private final boolean complete;

    private final boolean reopenable;

    private final boolean error;


    State(final boolean complete, final boolean reopenable, final boolean error) {
        this.complete = complete;
        this.reopenable = reopenable;
        this.error = error;
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
}
