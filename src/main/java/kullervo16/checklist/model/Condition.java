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


    public boolean isConditionUnreachable() {
        return this.step.getSelectedOption() != null
               && (this.step.getSelectedOption() == null
                   || this.step.getSelectedOption().isEmpty()
                   || !this.selectedOption.equals(this.step.getSelectedOption()));
    }


    public Step getStep() {
        return step;
    }


    public String getSelectedOption() {
        return selectedOption;
    }
}
