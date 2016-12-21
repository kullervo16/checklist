package kullervo16.checklist.model.persist;

import kullervo16.checklist.model.*;
import com.esotericsoftware.yamlbeans.YamlException;
import com.esotericsoftware.yamlbeans.YamlReader;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

import kullervo16.checklist.model.Step;
import kullervo16.checklist.repository.TemplateRepository;

/**
 * Data object to model a template. It is backed by a YAML file.
 *
 * @author jeve
 */
public class TemplatePersister {

    private static final String CHECK = "check";

    private static final String SUBCHECKLIST = "subchecklist";

    private static final String STEPS = "steps";

    private static final String DESCRIPTION = "description";

    private static final String TAGS = "tags";

    private static final String DISPLAY_NAME = "displayName";

    private static final String SUBCHECKLIST_ONLY = "subchecklistOnly";

    protected static final String SEPARATOR_2 = "  - ";

    protected static final String SEPARATOR_1 = "    ";

    private final File file;

    protected Template template;

    private boolean writing;

    private boolean loaded;

    // variables for cache control
    private long lastCheck;

    private long fileModifTime;


    public TemplatePersister(final File file, final Template t) {
        this.file = file;
        template = t;
        loaded = false;
    }


    public void checkAndLoadDataFromFile() {

        if (!writing && file != null && System.currentTimeMillis() - lastCheck > 1000 && file.lastModified() - fileModifTime > 0) {

            final YamlReader reader;

            try (FileReader fileReader = new FileReader(file)) {

                // init
                final LinkedList<Step> steps = new LinkedList<>();
                final LinkedList<String> tags = new LinkedList<>();
                final LinkedList<Milestone> milestones = new LinkedList<>();

                reader = new YamlReader(fileReader);

                final Map templateMap = (Map) reader.read();

                if (templateMap != null) {
                    handleData(templateMap, steps, milestones, tags);
                }

                template.setTags(tags);
                template.setMilestones(milestones);
                template.setSteps(steps);
                lastCheck = System.currentTimeMillis();
                fileModifTime = file.lastModified();
                template.setCreationTime(fileModifTime);
                template.setUser((String) templateMap.get("user"));
                afterRead();
            } catch (final Exception ex) {

                Logger.getLogger(TemplatePersister.class.getName()).log(Level.SEVERE, "Error while parsing " + file, ex);
                loaded = false;

                return;
            }
        }

        loaded = true;
    }


    protected void handleData(final Map templateMap, final LinkedList<Step> steps, final LinkedList<Milestone> milestones, final LinkedList<String> tags) {

        for (final Map stepMap : (List<Map>) templateMap.get(STEPS)) {

            final Step step = new Step(stepMap, steps);

            steps.add(step);

            if (step.getMilestone() != null) {
                milestones.add(step.getMilestone());
            }
        }

        template.setDescription((String) templateMap.get(DESCRIPTION));

        if (templateMap.get(TAGS) != null) {

            tags.addAll((List<String>) templateMap.get(TAGS));

            // sanity check... sometimes people code stuff that gets interpreted as yaml... if a tag is not a string, convert it to one
            final List removeList = new LinkedList();

            for (final Object o : tags) {
                if (!o.getClass().equals(String.class)) {
                    removeList.add(o);
                }
            }

            for (final Object toBeRemoved : removeList) {
                tags.remove(toBeRemoved);
                tags.add(toBeRemoved.toString());
            }
        }

        if (templateMap.get(DISPLAY_NAME) != null) {
            template.setDisplayName((String) templateMap.get(DISPLAY_NAME));
        }

        if (templateMap.get(SUBCHECKLIST_ONLY) != null) {
            template.setSubchecklistOnly("true".equalsIgnoreCase((String) templateMap.get(SUBCHECKLIST_ONLY)));
        }
    }


    /**
     * This method makes sure the template adheres to the proper structure
     *
     * @param content
     * @return a list of warnings and errors
     */
    public static List<ErrorMessage> validateTemplate(final String content) {

        final LinkedList<ErrorMessage> result = new LinkedList<>();

        try {

            final YamlReader reader = new YamlReader(content);
            Map templateMap = null;

            try {
                templateMap = (Map) reader.read();
            } catch (final ClassCastException cce) {
                result.add(new ErrorMessage("Invalid YAML.", ErrorMessage.Severity.CRITICAL, "No YAML structure found."));
            }

            if (templateMap != null) {

                // step 1 : check presence of main level tags
                checkTag(templateMap, DISPLAY_NAME, "/", result, DataType.STRING, false);
                checkTag(templateMap, DESCRIPTION, "/", result, DataType.STRING, true);
                checkTag(templateMap, TAGS, "/", result, DataType.LIST, true);
                checkTag(templateMap, STEPS, "/", result, DataType.LIST, true);

                // step 2 : check tags
                if (templateMap.containsKey(TAGS)) {

                    try {

                        final List<String> currentTagList = new LinkedList<>();

                        for (final String tag : (List<String>) templateMap.get(TAGS)) {

                            if (currentTagList.contains(tag)) {
                                result.add(new ErrorMessage("Duplicate tag : " + tag, ErrorMessage.Severity.WARNING, "You used the same tag twice in the template"));
                            } else {
                                currentTagList.add(tag);
                            }
                        }

                    } catch (final ClassCastException cce) {
                        result.add(new ErrorMessage("Wrong data in Tags", ErrorMessage.Severity.MAJOR, "/tags : value should be simple strings"));
                    }
                }

                // step 3 : check steps
                if (templateMap.containsKey(STEPS)) {

                    final List<Map> stepList = (List<Map>) templateMap.get(STEPS);
                    final List<String> currentIdList = new LinkedList<>();
                    int pos = 1;

                    for (final Map stepMap : stepList) {

                        checkStep(stepMap, pos++, result);

                        final String stepId = (String) stepMap.get("id");

                        if (stepId != null) {

                            if (currentIdList.contains(stepId)) {
                                result.add(new ErrorMessage("Duplicate id : " + stepId, ErrorMessage.Severity.MAJOR, "You used the same id twice in the template"));
                            } else {
                                currentIdList.add(stepId.toString());
                            }

                            // Check the condition if any
                            {
                                final Object conditionsObject = stepMap.get("condition");

                                if (conditionsObject != null) {

                                    if ((conditionsObject instanceof List)) {

                                        final List<Map<String, String>> conditions = (List<Map<String, String>>) conditionsObject;

                                        if (!conditions.isEmpty()) {

                                            final Map<String, String> condition = conditions.get(0);

                                            if (condition != null) {

                                                final String selectionPoint = condition.get("selectionPoint");

                                                if (selectionPoint == null) {

                                                    result.add(new ErrorMessage("The step " + stepId + " condition must depend on a step", ErrorMessage.Severity.MAJOR, "The property \"selectionPoint\" must be defined for the condition."));

                                                } else {

                                                    if (!currentIdList.contains(selectionPoint)) {
                                                        result.add(new ErrorMessage("The step " + stepId + " must be defined after the " + selectionPoint + "  step", ErrorMessage.Severity.MAJOR, "A dependent step can never be defined before the step it depends on."));
                                                    }
                                                }
                                            }
                                        }
                                    } else {
                                        result.add(new ErrorMessage("The step " + stepId + " condition must depend on a step", ErrorMessage.Severity.MAJOR, "The property \"selectionPoint\" must be defined for the condition."));
                                    }
                                }
                            }
                        } else {
                            result.add(new ErrorMessage("Each step should have an ID", ErrorMessage.Severity.MAJOR, "You have to define the \"id\" property for each step."));
                        }
                    }
                }
            }
        } catch (final YamlException e) {
            result.add(new ErrorMessage("Invalid YAML.", ErrorMessage.Severity.CRITICAL, e.getMessage()));
        }

        return result;
    }


    private enum DataType {
        STRING,
        URL,
        NUMBER,
        POSITIVE_NUMBER,
        MAP,
        LIST,
        STRING_OR_LIST
    }


    private static void checkTag(final Map data, final String tagName, final String path, final List<ErrorMessage> errorList, final DataType dataType, final boolean mandatory) {

        if (!data.containsKey(tagName)) {

            if (mandatory) {
                errorList.add(new ErrorMessage("Missing tag : " + tagName, ErrorMessage.Severity.MAJOR, path + "/" + tagName));
            }

        } else {

            switch (dataType) {

                case STRING:

                    // TODO: replace this by a instance of block
                    try {
                        // Keep the .length() call to be sure the code optimizer will not remove the cast.
                        ((String) data.get(tagName)).length();
                    } catch (final ClassCastException cce) {
                        errorList.add(new ErrorMessage("Invalid tag value: " + tagName, ErrorMessage.Severity.MAJOR, path + "/" + tagName + " should be a valid String"));
                    }

                    break;

                case MAP:

                    // TODO: replace this by a instance of block
                    try {
                        // Keep the .size() call to be sure the code optimizer will not remove the cast.
                        ((Map) data.get(tagName)).size();
                    } catch (final ClassCastException cce) {
                        errorList.add(new ErrorMessage("Invalid tag value: " + tagName, ErrorMessage.Severity.MAJOR, path + "/" + tagName + " should be a valid Map"));
                    }

                    break;

                case LIST:

                    // TODO: replace this by a instance of block
                    try {
                        // Keep the .size() call to be sure the code optimizer will not remove the cast.
                        ((List) data.get(tagName)).size();
                    } catch (final ClassCastException cce) {
                        errorList.add(new ErrorMessage("Invalid tag value: " + tagName, ErrorMessage.Severity.MAJOR, path + "/" + tagName + " should be a valid List"));
                    }

                    break;

                case STRING_OR_LIST:

                    // TODO: replace this by a instance of block
                    try {
                        // Keep the .length() call to be sure the code optimizer will not remove the cast.
                        ((String) data.get(tagName)).length();
                    } catch (final ClassCastException cce) {
                        // TODO: replace this by a instance of block
                        // second try : maybe it's a list
                        try {
                            // Keep the .size() call to be sure the code optimizer will not remove the cast.
                            ((List) data.get(tagName)).size();
                        } catch (final ClassCastException cce2) {
                            errorList.add(new ErrorMessage("Invalid tag value: " + tagName, ErrorMessage.Severity.MAJOR, path + "/" + tagName + " should be a valid String or List"));
                        }
                    }

                    break;

                case NUMBER:

                    try {
                        // TODO: try to remove the value variable
                        final Integer value = Integer.valueOf(data.get(tagName).toString());
                    } catch (final NumberFormatException cce) {
                        errorList.add(new ErrorMessage("Invalid tag value: " + tagName, ErrorMessage.Severity.MAJOR, path + "/" + tagName + " should be a valid Integer"));
                    }

                    break;

                case POSITIVE_NUMBER:

                    try {

                        final Integer value = Integer.valueOf(data.get(tagName).toString());

                        if (value < 0) {
                            errorList.add(new ErrorMessage("Invalid tag value: " + tagName, ErrorMessage.Severity.MAJOR, path + "/" + tagName + " should be a valid positive Integer"));
                        }
                    } catch (final NumberFormatException cce) {
                        errorList.add(new ErrorMessage("Invalid tag value: " + tagName, ErrorMessage.Severity.MAJOR, path + "/" + tagName + " should be a valid Integer"));
                    }

                    break;
            }
        }
    }


    private static void checkStep(final Map stepMap, final int pos, final LinkedList<ErrorMessage> result) {

        // step 1 : all mandatory/optional tags and their types
        {
            checkTag(stepMap, "id", "/steps/" + pos, result, DataType.STRING, true);
            checkTag(stepMap, "responsible", "/steps/" + pos, result, DataType.STRING, true);
            checkTag(stepMap, "options", "/steps/" + pos, result, DataType.LIST, false);
            checkTag(stepMap, "milestone", "/steps/" + pos, result, DataType.STRING, false);
            checkTag(stepMap, "condition", "/steps/" + pos, result, DataType.LIST, false);
            checkTag(stepMap, "action", "/steps/" + pos, result, DataType.STRING, false);
            checkTag(stepMap, "documentation", "/steps/" + pos, result, DataType.URL, false);
            checkTag(stepMap, SUBCHECKLIST, "/steps/" + pos, result, DataType.STRING, false);
            checkTag(stepMap, CHECK, "/steps/" + pos, result, DataType.STRING_OR_LIST, false);
            checkTag(stepMap, "weight", "/steps/" + pos, result, DataType.POSITIVE_NUMBER, false);
        }

        // step 2 : either subchecklist, question or action must be present
        {
            int count = 0;

            if (stepMap.containsKey(SUBCHECKLIST)) count++;
            if (stepMap.containsKey("action")) count++;
            if (stepMap.containsKey("question")) count++;

            if (count == 0) {
                result.add(new ErrorMessage("Subchecklist, question or action is mandatory", ErrorMessage.Severity.MAJOR, "/steps/" + pos + " contains neither subchecklist, question nor action. One of the 2 is required."));
            } else if (count != 1) {
                result.add(new ErrorMessage("More than 1 of (subchecklist, question, action) specified", ErrorMessage.Severity.MAJOR, "/steps/" + pos + " contains more than 1 of (subchecklist, question,action). Only one is allowed."));
            }
        }

        // step 3 : if subchecklist, see if it exists
        if (stepMap.containsKey(SUBCHECKLIST)) {

            final String subchecklist = (String) stepMap.get(SUBCHECKLIST);

            if (TemplateRepository.INSTANCE.getTemplate(subchecklist) == null) {
                result.add(new ErrorMessage("Referenced subchecklist " + subchecklist + " does not (yet) exist", ErrorMessage.Severity.WARNING, "/steps/" + pos + " Unless you add this subchecklist, instantiation will fail at runtime."));
            }
        }

        // step 4 : check for checks
        if (stepMap.containsKey(CHECK)) {

            if (stepMap.get(CHECK) instanceof List) {

                // check elements are map entries with a step key
                for (final Object o : (List) stepMap.get(CHECK)) {

                    if (o instanceof Map) {

                        if (!((Map) o).containsKey("step")) {
                            result.add(new ErrorMessage("Invalid check message", ErrorMessage.Severity.CRITICAL, "/steps/" + pos + " Check is invalid. Either a simple string or a list of step elements should be present."));
                        }

                    } else {
                        result.add(new ErrorMessage("Invalid check message", ErrorMessage.Severity.CRITICAL, "/steps/" + pos + " Check is invalid. Either a simple string or a list of step elements should be present."));
                    }
                }
            } else if (!(stepMap.get(CHECK) instanceof String)) {

                result.add(new ErrorMessage("Invalid check message", ErrorMessage.Severity.CRITICAL, "/steps/" + pos + " Check is invalid. Either a simple string or a list of step elements should be present."));
            }
        }
    }


    protected void serializeStep(final Step step, final PrintWriter writer) {

        printLine(writer, "- id", step.getId());
        printLine(writer, "  responsible", step.getResponsible());
        printLine(writer, "  action", step.getAction());
        printLine(writer, "  state", step.getState().toString());
        printLine(writer, "  executor", step.getExecutor());
        printLine(writer, "  comment", step.getComment());
        printLine(writer, "  documentation", step.getDocumentation());
        printLine(writer, "  subchecklist", step.getSubChecklist());
        printLine(writer, "  weight", "" + step.getWeight());
        printLine(writer, "  selectedOption", step.getSelectedOption());
        printLine(writer, "  question", step.getQuestion());
        printLine(writer, "  answerType", step.getAnswerType());
        printLine(writer, "  child", step.getChild());

        if (step.getLastUpdate() != null) {
            printLine(writer, "  lastUpdate", "" + step.getLastUpdate().getTime());
        }

        if (step.getChecks().size() == 1) {
            printLine(writer, "  check", step.getChecks().get(0));
        } else if (step.getChecks().size() > 1) {
            writer.append(SEPARATOR_1).append("check: \n");
            step.getChecks().forEach((check) -> {
                writer.append("      -").append(" step: ").append(check).append('\n');
            });
        }

        if (step.getMilestone() != null) {
            writer.append(SEPARATOR_1)
                  .append("milestone: \n")
                  .append(SEPARATOR_1).append(SEPARATOR_2)
                  .append("name: ").append(step.getMilestone().getName()).append('\n')
                  .append(SEPARATOR_1).append(SEPARATOR_2)
                  .append("reached: ").append(Boolean.toString(step.getMilestone().isReached()))
                  .append('\n');
        }

        if (step.getOptions() != null) {
            writer.append(SEPARATOR_1).append("options:\n");
            step.getOptions().forEach((option) -> {
                writer.append(SEPARATOR_1).append(SEPARATOR_2).append(option).append('\n');
            });
        }

        {
            final Condition condition = step.getCondition();

            if (condition != null) {

                writer.append(SEPARATOR_1).append("condition:\n");
                writer.append(SEPARATOR_1).append(SEPARATOR_2).append("selectionPoint: ").append(condition.getStep().getId()).append('\n');

                if (condition.getSelectedOption() != null) {
                    writer.append(SEPARATOR_1).append(SEPARATOR_2).append("option: ").append(condition.getSelectedOption()).append('\n');
                }
            }
        }
    }




    protected void printLine(final PrintWriter writer, final String name, final String value) {

        if (value != null) {
            appendEscaped(writer.append("  ").append(name).append(": "), value.replaceAll("\n", "##NEWLINE##").replaceAll("\"", "'")).append('\n');
        }
    }


    protected PrintWriter appendEscaped(final PrintWriter writer, final String content) {        
        return writer.append("\"").append(content != null ? content.replaceAll("\"", "'") : "").append("\"");
    }


    /**
     * Method can be overwritten by subclasses to present the header.
     *
     * @param writer
     */
    protected void serializeHeader(final PrintWriter writer) {

        if (template.getDisplayName() != null) {
            appendEscaped(writer.append("displayName: "), template.getDisplayName()).append('\n');
        }

        if (template.getDescription() != null) {
            appendEscaped(writer.append("description: "), template.getDescription()).append('\n');
        }

        if (!template.getTags().isEmpty()) {
            writer.append("tags: ").append('\n');
            template.getTags().forEach((tag) -> {
                appendEscaped(writer.append("    - "), tag).append('\n');
            });
        }
        appendEscaped(writer.append("user: "), template.getUser()).append('\n');
    }


    public void serialize(boolean force) {

        if (!loaded && !force) {
            // do this to prevent an unloaded version to be erased !
            checkAndLoadDataFromFile();
        }

        writing = true;

        try (PrintWriter writer = new PrintWriter(file);) {

            serializeHeader(writer);
            writer.append("steps:\n");

            for (final Step step : template.getSteps()) {
                if (step != null) {
                    serializeStep((Step) step, writer);
                }
            }

            writer.flush();

        } catch (final IOException ioe) {
            ioe.printStackTrace();
        }

        lastCheck = System.currentTimeMillis();
        fileModifTime = file.lastModified();
        writing = false;
    }


    @Override
    public int hashCode() {

        int hash = 7;

        checkAndLoadDataFromFile();
        hash = 89 * hash + template.hashCode();

        return hash;
    }


    @Override
    public boolean equals(final Object obj) {

        checkAndLoadDataFromFile();

        if (obj == null) {
            return false;
        }

        if (getClass() != obj.getClass()) {
            return false;
        }

        final TemplatePersister other = (TemplatePersister) obj;

        return Objects.equals(template, other.template);
    }


    public File getFile() {
        return file;
    }


    protected void afterRead() {

    }
}
