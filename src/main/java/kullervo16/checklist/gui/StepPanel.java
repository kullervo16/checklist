/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kullervo16.checklist.gui;

import java.awt.Color;
import java.beans.PropertyChangeListener;
import java.text.SimpleDateFormat;
import javax.swing.JDialog;
import javax.swing.JFrame;
import kullervo16.checklist.model.Step;

/**
 * The GUI for a single step in a checklist. When updated, it will launch a modal
 * UpdateStepFrame to capture the update information.
 * 
 * @author jeve
 */
public class StepPanel extends javax.swing.JPanel {

    private Step step;
    private SimpleDateFormat sdf = new SimpleDateFormat("YYYY/MM/DD HH:mm");
    private final JFrame parentFrame;
    /**
     * Creates new form StepPanel
     */
    public StepPanel(JFrame parent,Step step) {
        this.parentFrame = parent;
        this.step = step;
        initComponents();     
        this.updateButton.setVisible(!this.step.isComplete());
        setVisible(true);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        responsible = new javax.swing.JLabel();
        action = new javax.swing.JLabel();
        executed = new javax.swing.JLabel();
        updateButton = new javax.swing.JButton();
        id = new javax.swing.JLabel();

        setBackground(getBackgroundColor());
        setPreferredSize(new java.awt.Dimension(700, 115));

        responsible.setFont(new java.awt.Font("Tahoma", 2, 18)); // NOI18N
        responsible.setText(this.step.getResponsible());

        action.setFont(new java.awt.Font("Tahoma", 0, 24)); // NOI18N
        action.setText(this.step.getAction());

        executed.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        executed.setText(getExecutedText());

        updateButton.setText("Update");
        updateButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                updateStep(evt);
            }
        });

        id.setFont(new java.awt.Font("Tahoma", 0, 24)); // NOI18N
        id.setText(this.step.getId());

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(executed, javax.swing.GroupLayout.PREFERRED_SIZE, 349, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(updateButton)
                        .addGap(0, 154, Short.MAX_VALUE))
                    .addComponent(action, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(id, javax.swing.GroupLayout.PREFERRED_SIZE, 254, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(responsible, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(id)
                    .addComponent(responsible))
                .addGap(4, 4, 4)
                .addComponent(action)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(executed)
                    .addComponent(updateButton, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(16, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void updateStep(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_updateStep
        
        JDialog frame = new JDialog(this.parentFrame, "Check", true);
        UpdateStepPanel usf = new UpdateStepPanel(this.step, frame);
        frame.getContentPane().add(usf);
        frame.pack();
        frame.setVisible(true);
        
        System.err.println("#################");
        System.err.println(step.getState() + " : "+ step.getComment());
        
        // now update the GUI
        this.setBackground(this.getBackgroundColor());
        this.executed.setText(getExecutedText());
        this.updateButton.setVisible(!this.step.isComplete());
        this.parentFrame.firePropertyChange("progress", 0, 1);
    }//GEN-LAST:event_updateStep


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel action;
    private javax.swing.JLabel executed;
    private javax.swing.JLabel id;
    private javax.swing.JLabel responsible;
    private javax.swing.JButton updateButton;
    // End of variables declaration//GEN-END:variables

    private Color getBackgroundColor() {
        switch(this.step.getState()) {
            case OK:
                return new Color(25, 140, 79);
            case NOK:
                return new Color(209, 31, 31);
            case NOT_APPLICABLE:
                return Color.WHITE;
            case ON_HOLD:
                return new Color(237, 233, 12);
            case UNKNOWN:
            default:
                return new Color(214, 214, 214);
        }
    }

    private String getExecutedText() {
        if(this.step.isComplete()) {
            String dateStr = this.step.getLastUpdate() != null ? " at "+sdf.format(this.step.getLastUpdate()) : "";
            return "Executed by "+this.step.getExecutor()+ dateStr;
        }
        return "";
    }
}