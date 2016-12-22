package kullervo16.checklist.model.persist;

import java.io.File;
import java.io.PrintWriter;
import java.util.LinkedList;
import java.util.Map;

import kullervo16.checklist.model.Checklist;
import kullervo16.checklist.model.Milestone;
import kullervo16.checklist.model.State;
import kullervo16.checklist.model.Step;

/**
 * Data object class to persist a Checklist... it is backed by a YAML file and adds more data to the template persister.
 *
 * @author jeve
 */
public class ChecklistPersister extends TemplatePersister {

    public ChecklistPersister(final File file, final Checklist cl) {
        super(file, cl);
    }


    @Override
    protected void serializeHeader(final PrintWriter writer) {

        super.serializeHeader(writer);

        final Checklist cl = (Checklist) template;

        if (cl.getParent() != null) {
            writer.append("parent: ").append(cl.getParent()).append('\n');
        }

        writer.append("template: ").append(cl.getTemplate()).append('\n')
              .append("specificTagSet: ").append(Boolean.toString(cl.isSpecificTagSet())).append('\n')
              .append("uniqueTagcombination: ").append(Boolean.toString(cl.isUniqueTagcombination())).append('\n');
    }


    @Override
    protected void handleData(final Map templateMap, final LinkedList<Step> steps, final LinkedList<Milestone> milestones, final LinkedList<String> tags) {

        super.handleData(templateMap, steps, milestones, tags);

        final Checklist cl = (Checklist) template;

        if (templateMap.containsKey("template")) {
            cl.setTemplate((String) templateMap.get("template"));
        }

        if (templateMap.containsKey("specificTagSet")) {
            cl.setSpecificTagSet(templateMap.get("specificTagSet").equals("true"));
        }

        if (templateMap.containsKey("parent")) {
            cl.setParent((String) templateMap.get("parent"));
        }

        if (templateMap.containsKey("uniqueTagcombination")) {
            cl.setUniqueTagcombination("true".equalsIgnoreCase((String) templateMap.get("uniqueTagcombination")));
        } else {
            // backwards compatibility : if not set, say true...
            cl.setUniqueTagcombination(true);
        }               
    }


    @Override
    protected void serializeStep(final Step step, final PrintWriter writer) {

        super.serializeStep(step, writer);

        if (!step.getErrors().isEmpty()) {

            writer.append(SEPARATOR_1).append("errors:\n");

            step.getErrors().forEach((error) -> {
                appendEscaped(writer.append(SEPARATOR_1).append(SEPARATOR_2), error).append('\n');
            });
        }

        if (!step.getAnswers().isEmpty()) {

            writer.append(SEPARATOR_1).append("answers:\n");

            step.getAnswers().forEach((answer) -> {
                appendEscaped(writer.append(SEPARATOR_1).append(SEPARATOR_2), answer).append('\n');
            });
        }
        printLine(writer, "  user", step.getUser());
    }


    @Override
    protected void afterRead() {

        super.afterRead();

        ((Checklist)template).updateStepsReopenable();
    }
}
