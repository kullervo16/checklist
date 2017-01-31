/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kullervo16.checklist.model;

import java.util.List;

/**
 * @author jef
 */
public class Condition {

    private final Step step;

    private final List<String> expectedAnswers;


    public Condition(final Step step, final List<String> expectedAnswers) {
        this.step = step;
        this.expectedAnswers = expectedAnswers == null || expectedAnswers.isEmpty() ? null : expectedAnswers;
    }


    public boolean isConditionReachable() {

        // If the step state is NOT_APPLICABLE and there is no answer to match, we consider the condition is reachable
        if (this.step.getState() == State.NOT_APPLICABLE && this.expectedAnswers == null) {
            return true;
        }

        // If the step state is not OK, we consider the step is not reachable
        if (this.step.getState() != State.OK) {
            return false;
        }

        // If there is no answers defined in the condition, we consider the condition is reachable
        if (this.expectedAnswers == null) {
            return true;
        }

        // If there is no answers defined by the user, we consider the step is not reachable
        if (this.step.getAnswers() == null) {
            return false;
        }

        // Indicate if at least one the expected answers is in the answers list
        boolean reachable = false;

        for (final String expectedAnswerWalker : this.expectedAnswers) {

            // If the expected answer is in the answers list
            if (this.step.getAnswers().contains(expectedAnswerWalker)) {
                reachable = true;
                break;
            }
        }

        return reachable;
    }


    public Step getStep() {
        return step;
    }


    public List<String> getExpectedAnswers() {
        return expectedAnswers;
    }
}
