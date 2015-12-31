/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kullervo16.checklist.model;

import kullervo16.checklist.model.dto.StepDto;

/**
 *
 * @author jef
 */
public class Condition {
    private final StepDto step;
    private final String selectedOption;

    public Condition(StepDto step, String selectedOption) {
        this.step = step;
        this.selectedOption = selectedOption;
    }
    
    public boolean isConditionUnreachable() {        
        return this.step.getSelectedOption() != null && !this.selectedOption.equals(this.step.getSelectedOption());
    }

    public StepDto getStep() {
        return step;
    }

    public String getSelectedOption() {
        return selectedOption;
    }
    
    
}
