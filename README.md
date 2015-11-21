# checklist
Simple GUI application that allows you to handle yaml-described checklists. A simple webapplication exposes the content and statistics to let you detect weak spots in your processes.

##Templates

The templates are defined in  YAML. This is the syntax :

```yaml
steps :    
    - id: createSecureGit
      responsible: middleware
      action: request secure GIT to JDSS. Request URL
      check: on the deployment station, perform git clone with the URL from JDSS
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
```

## GUI

This simple GUI allows you to select a template and instantiate it in a checklist. The checklist is simply a copy of the YAML template
you select where the GUI allows you to update the status. This enables people without development skills that would be distracted
by the YAML syntax to fill out the checklist.

It applies also some governance : 
 * you cannot change steps marked as done
 * you need to confirm every check point
 * a comment is required for each failing step in order to gather the weaknesses in your process so you can adapt your checklists accordingly

The GUI is written in SWING, so it will run on any platform that supports Java.

## GIT backend (TODO)

The idea is to use GIT as a backend. The GUI will commit and push after each update, so that the GIT is always up to date and 
can be used to switch control from 1 party to another. On the other hand, since GIT works without a central repo, you can
also use the checklists when in detached mode.

The Web frontend will be called by a webhook and pull the updates that will then be reflected directly in the website.

## Web frontend (TODO)

The Web frontend serves 2 purposes :
 * allow you to monitor the progress of a given checklist (f.e. on a television screen in your operations room)
 * provide statistics on usage
 
 The statistics will help you see which templates are widely used, but also where the most errors occur. This may point to
 a spot in your process where there is either something unclear, or your previous steps lack proper checks to make them less
 error prone.
