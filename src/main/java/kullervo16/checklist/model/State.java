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
    NOT_YET_APPLICABLE(false, false),
    UNKNOWN(false, false),
    NOT_APPLICABLE(true, false),
    OK(true, true),
    ON_HOLD(false, false),
    EXECUTED(false, false),
    EXECUTION_FAILED_NO_COMMENT(false, true),
    EXECUTION_FAILED(true, true),
    CHECK_FAILED_NO_COMMENT(false, true),
    CHECK_FAILED(true, true),
    ABORTED(true, false);

    private final boolean complete;

    private final boolean reopenable;


    State(final boolean complete, final boolean reopenable) {
        this.complete = complete;
        this.reopenable = reopenable;
    }


    public boolean isComplete() {
        return this.complete;
    }


    public boolean isReopenable() {
        return this.reopenable;
    }
}
