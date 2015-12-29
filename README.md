# checklist
Simple GUI application that allows you to handle yaml-described checklists. A simple webapplication exposes the content and statistics to let you detect weak spots in your processes.

It has support for both tags and milestones :

 * tags will organise your templates and checklists. You use them to retrieve them and to group the statistics
 * milestones define a status in the process the checklist supports. You reach a milestone if all checks before that milestone are ok. Via the milestone, you can see which instances of the checklists reached a given state.

##Templates

The templates are defined in  YAML. This is the syntax :

```yaml
description: Checklist to monitor a first deployment of an application
tags:
    - deployment
    - software
steps :    
    - id: createSecureGit
      responsible: middleware
      action: request secure GIT to JDSS. Request URL
      check: on the deployment station, perform git clone with the URL from JDSS
      milestone: readyForDeployment
    - id: createApplication
      responsible: middleware
      action: create the application in the proper zone
      check:
          - step: verify proper gear type (must match environment)
          - step: rhc ssh <application> -n <domain>      
    - id: odtInit
      responsible: middleware
      action: perform odt init
      check: project is on disk
      milestone: deployed
```

## Web GUI (WIP)

This simple GUI allows you to select a template and instantiate it in a checklist. The checklist is simply a copy of the YAML template
you select where the GUI allows you to update the status. This enables people without development skills that would be distracted
by the YAML syntax to fill out the checklist.

It applies also some governance : 
 * you cannot change steps marked as done
 * you need to confirm every check point
 * a comment is required for each failing step in order to gather the weaknesses in your process so you can adapt your checklists accordingly

The Web frontend also serves 2 other purposes :
 * allow you to monitor the progress of a given checklist (f.e. on a television screen in your operations room)
 * provide statistics on usage
 
 The statistics will help you see which templates are widely used, but also where the most errors occur. This may point to
 a spot in your process where there is either something unclear, or your previous steps lack proper checks to make them less
 error prone.

## GIT backend (TODO)

The idea is to use a file based backend with GIT as a remote synchronisation store. The backend will commit and push after each update, so that the GIT is always up to date.
If you loose your data (or you are using a 100% container approach and do not use persistent storage at all), the system will pull from the GIT to restore its state.


