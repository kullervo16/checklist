package kullervo16.checklist.model;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Data object to model a step in a template/checklist.
 *
 * @author jeve
 */
public class Step {

    protected String id;

    protected String responsible;

    protected String action;

    protected List<String> checks;

    protected State state;

    protected String executor;

    protected Date lastUpdate;

    protected String comment;

    protected Milestone milestone;

    protected List<String> errors;

    protected int weight;

    protected String documentation;

    protected String subChecklist;

    protected List<String> options;

    protected String selectionOption;

    protected Condition condition;

    protected String question;

    protected String answerType;

    protected List<String> answers;

    private String child;


    public Step() {
    }


    /**
     * copy constructor.
     *
     * @param step
     */
    Step(final Step step, final List<Step> stepList) {

        action = step.getAction();
        checks = new LinkedList<>(step.getChecks());
        comment = step.getComment();
        executor = step.getExecutor();
        id = step.getId();
        lastUpdate = step.getLastUpdate();
        milestone = step.getMilestone() == null ? null : new Milestone(step.getMilestone().getName(), step.getMilestone().isReached());
        responsible = step.getResponsible();
        state = step.getState();
        errors = new LinkedList<>(step.getErrors());
        answers = new LinkedList<>(step.getAnswers());
        weight = step.getWeight();
        documentation = step.getDocumentation();
        subChecklist = step.getSubChecklist();
        options = step.getOptions();
        selectionOption = step.getSelectedOption();
        question = step.getQuestion();
        answerType = step.getAnswerType();
        child = step.getChild();

        if (step.getCondition() != null) {

            Step selectionPoint = null;

            for (final Step walker : stepList) {

                if (walker.equals(step.getCondition().getStep())) {
                    selectionPoint = walker;
                }
            }

            state = State.NOT_APPLICABLE;
            condition = new Condition(selectionPoint, step.getCondition().getSelectedOption());
        }
    }


    /**
     * Yaml constructor
     *
     * @param stepMap
     * @param stepList list with the other steps, needed for wiring
     */
    public Step(final Map stepMap, final List<Step> stepList) {

        id = (String) stepMap.get("id");

        for (final Step walker : stepList) {
            if (walker.getId().equals(id)) {
                throw new IllegalStateException("Duplicate step id " + id);
            }
        }

        responsible = (String) stepMap.get("responsible");
        action = (String) stepMap.get("action");
        checks = new LinkedList<>();
        executor = (String) stepMap.get("executor");
        documentation = (String) stepMap.get("documentation");
        subChecklist = (String) stepMap.get("subchecklist");
        question = (String) stepMap.get("question");
        answerType = (String) stepMap.get("answerType");
        child = (String) stepMap.get("child");
        weight = stepMap.get("weight") == null ? 1 : Integer.parseInt(stepMap.get("weight").toString());

        if (stepMap.get("check") != null) {

            if (stepMap.get("check") instanceof String) {

                // convert String to list (this makes it a lot easier to configure the yaml
                checks.add((String) stepMap.get("check"));

            } else {

                for (final Map<String, String> entry : (List<Map>) stepMap.get("check")) {
                    checks.add(entry.get("step"));
                }
            }
        }

        errors = new LinkedList<>();

        if (stepMap.get("errors") != null) {

            if (stepMap.get("errors") instanceof String) {

                // convert String to list (this makes it a lot easier to configure the yaml
                errors.add((String) stepMap.get("errors"));

            } else {

                for (final String error : (List<String>) stepMap.get("errors")) {
                    errors.add(error);
                }
            }
        }

        answers = new LinkedList<>();

        if (stepMap.get("answers") != null) {

            try {

                if (stepMap.get("answers") instanceof String) {

                    // convert String to list (this makes it a lot easier to configure the yaml
                    answers.add((String) stepMap.get("answers"));

                } else {

                    for (final String error : (List<String>) stepMap.get("answers")) {
                        answers.add(error);
                    }
                }
            } catch (final Exception e) {

                // answers are sent as plain text... if some clever guy sends some JSON, this screws up the parsing...
                answers.add(stepMap.get("answers").toString());
            }
        }

        state = stepMap.containsKey("state") ? State.valueOf((String) stepMap.get("state")) : State.UNKNOWN;

        if (stepMap.containsKey("milestone")) {

            final Milestone ms;

            if (stepMap.get("milestone") instanceof String) {

                ms = new Milestone((String) stepMap.get("milestone"), false);

            } else {

                final String name = (String) ((List<Map>) stepMap.get("milestone")).get(0).get("name");
                final boolean reached = ((List<Map>) stepMap.get("milestone")).get(1).get("reached").equals("true");

                ms = new Milestone(name, reached);
            }

            milestone = ms;
        }

        if (stepMap.containsKey("options")) {
            options = new LinkedList<>((List<String>) stepMap.get("options"));
        }

        if (stepMap.containsKey("condition")) {

            Step selectionPoint = null;

            for (final Step walker : stepList) {

                if (walker.getId().equals(((List<Map>) stepMap.get("condition")).get(0).get("selectionPoint"))) {
                    selectionPoint = walker;
                }
            }

            if (selectionPoint == null) {
                throw new IllegalStateException("Unable to meet condition for step " + id);
            }

            condition = new Condition(selectionPoint, (String) ((List<Map>) stepMap.get("condition")).get(1).get("option"));
        }

        if (stepMap.containsKey("lastUpdate")) {
            lastUpdate = new Date(Long.valueOf((String) stepMap.get("lastUpdate")));
        }

        if (stepMap.containsKey("selectedOption")) {
            selectionOption = (String) stepMap.get("selectedOption");
        }
    }


    public String getId() {
        return id;
    }


    public void setId(final String id) {
        this.id = id;
    }


    public void setResponsible(final String responsible) {
        this.responsible = responsible;
    }


    public void setAction(final String action) {
        this.action = action;
    }


    public void setChecks(final List<String> checks) {
        this.checks = checks;
    }


    public void setLastUpdate(final Date lastUpdate) {
        this.lastUpdate = lastUpdate;
    }


    public String getResponsible() {
        return responsible;
    }


    public String getAction() {
        return action;
    }


    public List<String> getChecks() {
        return checks;
    }


    public State getState() {
        return state;
    }


    public String getExecutor() {
        return executor;
    }


    public Date getLastUpdate() {
        return lastUpdate;
    }


    public Milestone getMilestone() {
        return milestone;
    }


    public void setMilestone(final Milestone milestone) {
        this.milestone = milestone;
    }


    /**
     * @param state
     */
    public void setState(final State state) {
        this.state = state;
        lastUpdate = new Date();
    }


    public void setExecutor(final String executor) {
        this.executor = executor;
    }


    public boolean isComplete() {

        if (state == null) {
            return false;
        }

        switch (state) {

            case UNKNOWN:
            case ON_HOLD:
            case EXECUTED:
            case CHECK_FAILED_NO_COMMENT:
            case EXECUTION_FAILED_NO_COMMENT:

                return false;

            default:

                return true;
        }
    }


    public void setComment(final String text) {
        comment = text;
    }


    public String getComment() {
        return comment;
    }


    @Override
    public int hashCode() {
        int hash = 5;
        hash = 97 * hash + Objects.hashCode(id);
        return hash;
    }


    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Step other = (Step) obj;
        if (!Objects.equals(id, other.id)) {
            return false;
        }
        return true;
    }


    @Override
    public String toString() {
        return "StepDto{" + "id=" + id
                + ", responsible=" + responsible
                + ", action=" + action
                + ", checks=" + checks
                + ", state=" + state
                + ", executor=" + executor
                + ", lastUpdate=" + lastUpdate
                + ", comment=" + comment
                + ", milestone=" + milestone + '}';
    }


    public List<String> getErrors() {
        return errors;
    }


    public void setErrors(final List<String> errors) {
        this.errors = errors;
    }


    public int getWeight() {
        return weight;
    }


    public void setWeight(final int weight) {
        this.weight = weight;
    }


    public String getDocumentation() {
        return documentation;
    }


    public void setDocumentation(final String documentation) {
        this.documentation = documentation;
    }


    public String getSubChecklist() {
        return subChecklist;
    }


    public void setSubChecklist(final String subChecklist) {
        this.subChecklist = subChecklist;
    }


    public List<String> getOptions() {
        return options;
    }


    public void setOptions(final List<String> options) {
        this.options = options;
    }


    public String getSelectedOption() {
        return selectionOption;
    }


    public void setSelectedOption(final String selectionOption) {
        this.selectionOption = selectionOption;
    }


    public Condition getCondition() {
        return condition;
    }


    public void setCondition(final Condition condition) {
        this.condition = condition;
    }


    public String getQuestion() {
        return question;
    }


    public void setQuestion(final String question) {
        this.question = question;
    }


    public String getAnswerType() {
        return answerType;
    }


    public void setAnswerType(final String answerType) {
        this.answerType = answerType;
    }


    public List<String> getAnswers() {
        return answers;
    }


    public void setAnswers(final List<String> answers) {
        this.answers = answers;
    }


    public void setChild(final String childUUID) {
        child = childUUID;
    }


    public String getChild() {
        return child;
    }

}
