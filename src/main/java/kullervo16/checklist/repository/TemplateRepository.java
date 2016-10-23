package kullervo16.checklist.repository;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import kullervo16.checklist.model.ErrorMessage;
import kullervo16.checklist.model.Template;
import kullervo16.checklist.model.TemplateInfo;
import kullervo16.checklist.model.persist.TemplatePersister;
import org.apache.commons.io.IOUtils;

/**
 * Repository that parses the directory template structure.
 *
 * @author jeve
 */

public enum TemplateRepository {
    INSTANCE;

    // Create a ref here to make sure the actors get initialized
    static ActorRepository actors = new ActorRepository();

    private static Map<String, Template> data = new HashMap<>();

    private static final String TEMPLATE_DIR = "/opt/checklist/templates";

    private static final String BACKUP_DIR = "/opt/checklist/backup";

    static {
        // fixed path ... target is to work in a docker container, you can mount it via a volume to whatever you want
        loadData(TEMPLATE_DIR);
    }

    /**
     * This method scans the directory structure and determines which templates are available.
     *
     * @param folder the directory to scan
     */

    public static void loadData(final String folder) {

        final Map<String, Template> newData = new HashMap<>();

        scanDirectoryForTemplates(new File(folder), "", newData);
        data = newData;
    }


    private static void scanDirectoryForTemplates(final File startDir, final String prefix, final Map<String, Template> newModel) {

        final File[] files = startDir.listFiles();

        if (files != null) {

            for (final File f : files) {

                if (f.isDirectory()) {

                    scanDirectoryForTemplates(f, prefix + '/' + f.getName(), newModel);

                } else {

                    final Template template = new Template(f);
                    final String id = prefix + '/' + f.getName().substring(0, f.getName().lastIndexOf("."));

                    template.setDisplayName(id);
                    template.setId(id);
                    newModel.put(id, template);
                }
            }
        }
    }


    public List<String> getTemplateNames() {
        return new LinkedList<>(data.keySet());
    }


    public Template getTemplate(final String folder, final String templateName) {
        return data.get('/' + folder + '/' + templateName);
    }


    public Template getTemplate(final String fullName) {
        return data.get(fullName);
    }


    public List<TemplateInfo> getTemplateInformation() {

        final List<TemplateInfo> result = new LinkedList<>();
        final Map<String, Template> snapshot = Collections.unmodifiableMap(data);

        for (final Entry<String, Template> entry : snapshot.entrySet()) {

            final TemplateInfo ti = new TemplateInfo();

            ti.setId(entry.getKey());
            ti.setDescription(entry.getValue().getDescription());
            ti.setMilestones(entry.getValue().getMilestones());
            ti.setTags(entry.getValue().getTags());
            ti.setSubchecklistOnly(entry.getValue().isSubchecklistOnly());

            result.add(ti);
        }

        Collections.sort(result);

        return result;
    }


    public List<ErrorMessage> validateAndUpdate(String name, final InputStream inputStream) throws IOException {

        final String content = IOUtils.toString(inputStream);
        final List<ErrorMessage> errors = TemplatePersister.validateTemplate(content);

        if ("".equals(name) || "undefined".equals(name)) {
            errors.add(new ErrorMessage("No name given", ErrorMessage.Severity.CRITICAL, "You should give a name... structure can be applied by using /"));
        }

        if (!name.startsWith("/")) {
            name = '/' + name;
        }

        for (final ErrorMessage err : errors) {

            if (!err.getSeverity().equals(ErrorMessage.Severity.WARNING)) {
                // warning is the highest severity we allow and still update
                return errors;
            }
        }

        final File targetFile;

        if (data.containsKey(name)) {

            // update of an existing template

            targetFile = data.get(name).getPersister().getFile();

            // take a backup before proceeding

            final File backupDir = new File(BACKUP_DIR);

            if (!backupDir.exists()) {
                // TODO: the result of mkdirs is not checked
                backupDir.mkdirs();
            }

            final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
            final File backupFile = new File(backupDir, name + '.' + sdf.format(new Date()));

            // TODO: the result of mkdirs is not checked
            // TODO: we should create an utility class to do this or reuse an existing one
            new File(backupFile.getParent()).mkdirs();
            IOUtils.copy(new FileReader(targetFile), new FileWriter(backupFile));

        } else {

            // new file
            String fileName = name;
            final String[] split = name.split("/");

            if (!split[split.length - 1].contains(".")) {
                fileName += ".yml";
            }

            targetFile = new File(TEMPLATE_DIR + '/' + fileName);
        }

        // now check for the parent directory

        final File parent = targetFile.getParentFile();

        if (!parent.exists()) {
            // no need to check. if creation failed, the file will be unwritable and the proper error is generated below.
            //noinspection ResultOfMethodCallIgnored
            parent.mkdirs();
        }

        try (FileOutputStream fos = new FileOutputStream(targetFile)) {

            final Template t;

            IOUtils.write(content, fos);
            t = new Template(targetFile);
            data.put(name, t);
            t.setId(name);

        } catch (final IOException ioe) {
            errors.add(new ErrorMessage("Cannot write template to file", ErrorMessage.Severity.CRITICAL, ioe.getMessage()));
        }

        return errors;
    }


    public void deleteTemplate(final Template t) {

        data.remove(t.getId());

        if (t.getPersister().getFile() != null) {
            // TODO: the result of the delete call is ignored
            t.getPersister().getFile().delete();
        }
    }
}
