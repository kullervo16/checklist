/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.netbeans.rest.application.config;

import java.util.Set;
import javax.ws.rs.core.Application;

/**
 * @author jef
 */
@javax.ws.rs.ApplicationPath("webresources")
public class ApplicationConfig extends Application {

    @Override
    public Set<Class<?>> getClasses() {

        final Set<Class<?>> resources = new java.util.HashSet<>();

        addRestResourceClasses(resources);

        return resources;
    }


    /**
     * Do not modify addRestResourceClasses() method.
     * It is automatically populated with
     * all resources defined in the project.
     * If required, comment out calling this method in getClasses().
     */
    private void addRestResourceClasses(Set<Class<?>> resources) {
        resources.add(kullervo16.checklist.rest.ChecklistService.class);
        resources.add(kullervo16.checklist.rest.MilestoneService.class);
        resources.add(kullervo16.checklist.rest.TagService.class);
        resources.add(kullervo16.checklist.rest.TemplateService.class);
        resources.add(kullervo16.checklist.rest.UserInfoService.class);
        resources.add(kullervo16.checklist.rest.mappers.TemplateReferencedByAnotherTemplateExceptionMapper.class);
    }

}
