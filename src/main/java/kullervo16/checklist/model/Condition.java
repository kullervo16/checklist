/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kullervo16.checklist.model;

/**
 * @author jef
 */
public class Condition {

    private final Step step;

    private final String answer;


    public Condition(final Step step, final String answer) {
        this.step = step;
        this.answer = answer;
    }


    public boolean isConditionReachable() {

        // If the step state is NOT_APPLICABLE and there is no answer to match, we consider the condition is reachable
        if (this.step.getState() == State.NOT_APPLICABLE && this.answer == null) {
            return true;
        }

        // If the step state is not OK, we consider the step is not reachable
        if (this.step.getState() != State.OK) {
            return false;
        }

        return this.answer == null || (this.step.getAnswers() != null && this.step.getAnswers().contains(this.answer));
    }


    public Step getStep() {
        return step;
    }


    public String getAnswer() {
        return answer;
    }
}
