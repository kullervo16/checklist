package kullervo16.checklist.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonIgnore;

import static java.util.Collections.singletonList;
import static kullervo16.checklist.model.State.NOT_APPLICABLE;
import static kullervo16.checklist.model.State.NOT_YET_APPLICABLE;
import static kullervo16.checklist.model.State.UNKNOWN;
import static kullervo16.checklist.utils.CollectionUtils.isCollectionNullOrEmpty;
import static kullervo16.checklist.utils.StringUtils.isStringNullOrEmptyOrBlank;

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

    protected List<Condition> conditions;

    protected String question;

    protected String answerType;

    protected List<String> answers;

    private String child;

    protected boolean reopenable;

    private String user;


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
        question = step.getQuestion();
        answerType = step.getAnswerType();
        child = step.getChild();
        reopenable = step.reopenable;

        // Copy conditions
        {
            final List<Condition> originalConditions = step.getConditions();

            conditions = new ArrayList<>(originalConditions.size());

            if (!originalConditions.isEmpty()) {

                for (final Condition conditionWalker : originalConditions) {

                    final Step originalSelectPoint = conditionWalker.getStep();
                    Step selectionPoint = null;

                    for (final Step stepWalker : stepList) {

                        if (stepWalker.equals(originalSelectPoint)) {
                            selectionPoint = stepWalker;
                            break;
                        }
                    }

                    state = NOT_YET_APPLICABLE;
                    conditions.add(new Condition(selectionPoint, conditionWalker.getExpectedAnswers()));
                }
            }
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
        user = (String) stepMap.get("user");

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

        try {
            state = stepMap.containsKey("state") ? State.valueOf((String) stepMap.get("state")) : UNKNOWN;
        }catch(IllegalArgumentException iae) {
            // ok, so there is a state that we do not know (anymore). If it's one we know of, it is handled here, otherwise we rethrow
            switch((String)stepMap.get("state")) {
                case "ON_HOLD":
                    state = State.IN_PROGRESS;
                    break;
                case "CLOSED":
                    state = State.ABORTED;
                    break;
                default:
                    throw iae;
            }            
        }

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

        // If it is an action with options
        // then we convert it into a question
        if (!isStringNullOrEmptyOrBlank(action) && !isCollectionNullOrEmpty(options)) {
            question = action;
            answerType = "onlyOne";
            action = null;
        }

        if (!isStringNullOrEmptyOrBlank(question) && isStringNullOrEmptyOrBlank(answerType)) {
            answerType = "onlyOne";
        }

        // Process condition and conditions
        // We keep condition for backward compatibility
        {
            int nbConditions = 0;
            final List<Map> conditionFromMap = (List<Map>) stepMap.get("condition");
            final List<Map<String, Object>> conditionsFromMap = (List<Map<String, Object>>) stepMap.get("conditions");

            if (conditionFromMap != null) {
                nbConditions++;
            }

            if (conditionsFromMap != null) {
                nbConditions += conditionsFromMap.size();
            }

            this.conditions = new ArrayList<>(nbConditions);

            if (conditionFromMap != null) {

                Step selectionPoint = null;

                for (final Step stepWalker : stepList) {

                    if (stepWalker.getId().equals(conditionFromMap.get(0).get("selectionPoint"))) {
                        selectionPoint = stepWalker;
                    }
                }

                if (selectionPoint == null) {
                    throw new IllegalStateException("Unable to meet condition for step " + id);
                }

                this.conditions.add(new Condition(selectionPoint,
                                                  conditionFromMap.size() == 1 ? null
                                                                               : singletonList((String) conditionFromMap.get(1).get("option"))));
            }

            if (conditionsFromMap != null) {

                for (final Map<String, Object> conditionsFromMapWalker : conditionsFromMap) {

                    final String stepId = (String) conditionsFromMapWalker.get("stepId");
                    final Object expectedfAnswersObject = conditionsFromMapWalker.get("expectedfAnswers");
                    Step conditionStep = null;
                    List<String> expectedfAnswers = null;

                    // If there is no stepId defined in the condition: bad format
                    if (stepId == null) {
                        throw new IllegalStateException("No stepId defined for at least one condition");
                    }

                    // Look for the step by its id
                    for (final Step stepWalker : stepList) {

                        if (stepWalker.getId().equals(stepId)) {
                            conditionStep = stepWalker;
                            break;
                        }
                    }

                    if (conditionStep == null) {
                        throw new IllegalStateException("Unable to meet condition for step " + id);
                    }

                    if (expectedfAnswersObject != null) {

                        // We accept a single string and a string list for conveniency
                        if (expectedfAnswers instanceof List) {
                            expectedfAnswers = (List<String>) expectedfAnswersObject;
                        } else {
                            expectedfAnswers = singletonList((String) expectedfAnswersObject);
                        }
                    }

                    this.conditions.add(new Condition(conditionStep, expectedfAnswers));
                }
            }
        }

        if (stepMap.containsKey("lastUpdate")) {
            lastUpdate = new Date(Long.valueOf((String) stepMap.get("lastUpdate")));
        }

        if (stepMap.containsKey("selectedOption") && answers.isEmpty()) {
            answers.add((String) stepMap.get("selectedOption"));
            stepMap.remove("selectedOption");
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
    }


    public void setExecutor(final String executor) {
        this.executor = executor;
    }


    public boolean isComplete() {

        // TODO: a state can be null ????  If not, remove the check this.state != null
        return this.state != null && this.state.isComplete();
    }


    public boolean isActionExpected() {
        return this.state == UNKNOWN || this.state.isOpen();
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

    @JsonIgnore
    public List<Condition> getConditions() {
        return this.conditions;
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


    public boolean isReopenable() {
        return this.reopenable;
    }


    public void setReopenable(final boolean reopenable) {
        this.reopenable = reopenable;
    }


    public boolean dependsOn(final Step step) {

        if (this.conditions.isEmpty()) {
            return false;
        }

        for (final Condition condition : this.conditions) {

            if (condition.getStep() == step) {
                return true;
            }
        }

        return false;
    }

    public void setUser(String userName) {
        this.user = userName;
    }

    public String getUser() {
        return user;
    }


    /**
     * Update the status of the step if the conditions are reachable or unrecheable.
     *
     * @return {@code true} if the status has been updated or {@code false} if the status has not been updated.
     */
    public boolean updateStateDependingOnConditions() {

        switch (state) {

            // Do not update the status because the state does not allow the status to be updated
            case OK:
            case IN_PROGRESS:
            case EXECUTED:
            case EXECUTION_FAILED_NO_COMMENT:
            case EXECUTION_FAILED:
            case CHECK_FAILED_NO_COMMENT:
            case CHECK_FAILED:
                return false;

            case NOT_YET_APPLICABLE:
            case NOT_APPLICABLE:
            case UNKNOWN:
                break;

            default:
                throw new UnsupportedOperationException();
        }

        State potentiallyNewState = null;

        for (final Condition condition : conditions) {

            if (!condition.isConditionReachable()) {

                if (condition.getStep().getState().isComplete()) {
                    potentiallyNewState = NOT_APPLICABLE;
                    break;
                } else if (potentiallyNewState == null) {
                    potentiallyNewState = NOT_YET_APPLICABLE;
                }
            }
        }

        if (potentiallyNewState == null && (state == NOT_YET_APPLICABLE || state == NOT_APPLICABLE)) {
            state = UNKNOWN;
            return true;
        }

        if (potentiallyNewState != null) {
            state = potentiallyNewState;
            return true;
        }

        return false;
    }
}
