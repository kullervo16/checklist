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

    private final String selectedOption;


    public Condition(final Step step, final String selectedOption) {
        this.step = step;
        this.selectedOption = selectedOption;
    }


    public boolean isConditionReachable() {

        // If the step state is NOT_APPLICABLE, we consider the condition is reachable
        if (this.step.getState() == State.NOT_APPLICABLE) {
            return true;
        }

        // If the step state is not OK, we consider the step is not reachable
        if (this.step.getState() != State.OK) {
            return false;
        }

        return this.selectedOption == null || this.selectedOption.equals(this.step.getSelectedOption());
    }


    public Step getStep() {
        return step;
    }


    public String getSelectedOption() {
        return selectedOption;
    }
}
