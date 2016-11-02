package kullervo16.checklist.rest.mappers;

import kullervo16.checklist.exceptions.TemplateReferencedByAnotherTemplateException;
import kullervo16.checklist.model.ErrorMessage;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class TemplateReferencedByAnotherTemplateExceptionMapper implements ExceptionMapper<TemplateReferencedByAnotherTemplateException> {

    @Override
    public Response toResponse(final TemplateReferencedByAnotherTemplateException exception) {

        return Response.status(Response.Status.CONFLICT)
                       .entity(new ErrorMessage("The template " + exception.getTemplateId()
                                                + " is referenced by the template " + exception.getParentTemplateId()
                                                + ". Please remove the link between those 2 templates before deleting " + exception.getTemplateId() + '.',
                                                ErrorMessage.Severity.MAJOR, null))
                       .type(MediaType.APPLICATION_JSON_TYPE)
                       .build();
    }
}
