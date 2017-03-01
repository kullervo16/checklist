/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kullervo16.checklist.model;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author jef
 */
public class Condition {

    private final Step step;

    private final List<String> expectedAnswers;

    private final Set<State> expectedStates;


    public Condition(final Step step, final List<String> expectedAnswers, final Set<State> expectedStates) {
        this.step = step;
        this.expectedAnswers = expectedAnswers == null || expectedAnswers.isEmpty() ? null : expectedAnswers;
        this.expectedStates = expectedStates == null || expectedStates.isEmpty() ? null : expectedStates;
    }


    public boolean isConditionReachable() {

        // If there is at least one expected answers defined in the condition and if the step is OK
        if (this.expectedAnswers != null && this.step.getState() == State.OK) {

            final List<String> stepAnswers = this.step.getAnswers();

            // If there is at least one answer defined by the user
            if (stepAnswers != null) {

                for (final String expectedAnswerWalker : this.expectedAnswers) {

                    // If the expected answer is in the answers list
                    if (stepAnswers.contains(expectedAnswerWalker)) {

                        return true;
                    }
                }
            }
        }

        // If there is at least one expected state in the condition
        if (expectedStates != null) {

            final State stepState = step.getState();

            for (final State expectedStateWalker : expectedStates) {

                if (stepState == expectedStateWalker) {

                    return true;
                }
            }

            return false;
        }

        // We will reach this line only if no expectedAnswer and no expectedState is defined (should not happen if the condition is correctly written)
        return this.expectedAnswers == null;
    }


    public Step getStep() {
        return step;
    }


    public List<String> getExpectedAnswers() {
        return expectedAnswers;
    }


    public Set<State> getExpectedStates() {
        return expectedStates;
    }
}
