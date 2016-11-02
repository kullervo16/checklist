package kullervo16.checklist.exceptions;

public class TemplateReferencedByAnotherTemplateException extends Exception {

    private final String templateId;

    private final String parentTemplateId;


    public TemplateReferencedByAnotherTemplateException(final String templateId, final String parentTemplateId) {
        this.templateId = templateId;
        this.parentTemplateId = parentTemplateId;
    }


    public String getTemplateId() {
        return templateId;
    }


    public String getParentTemplateId() {
        return parentTemplateId;
    }
}
