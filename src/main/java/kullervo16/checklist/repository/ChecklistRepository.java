package kullervo16.checklist.repository;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import kullervo16.checklist.messages.PersistenceRequest;
import kullervo16.checklist.model.Checklist;
import kullervo16.checklist.model.ChecklistInfo;
import kullervo16.checklist.model.Milestone;
import kullervo16.checklist.model.TagcloudEntry;
import kullervo16.checklist.model.Template;
import org.jboss.resteasy.logging.Logger;

/**
 * Repository that parses the directory checklist structure.
 *
 * @author jeve
 */

public enum ChecklistRepository {
    INSTANCE;

    private static Map<String, Checklist> data = new HashMap<>();

    private static File mainFolder;

    private final static Object lock = new Object();

    static {
        // fixed path... target is to work in a docker container, you can mount it via a volume to whatever you want
        loadData("/opt/checklist/checklists");
    }

    /**
     * This method scans the directory structure and determines which templates are available.
     *
     * @param folder the directory to scan
     */
    public static void loadData(final String folder) {

        final Map<String, Checklist> newData = new HashMap<>();

        synchronized (lock) {
            mainFolder = new File(folder);
            scanDirectoryForTemplates(mainFolder, newData);
            data = newData;
        }
    }


    private static void scanDirectoryForTemplates(final File startDir, final Map<String, Checklist> newModel) {

        final File[] files = startDir.listFiles();

        if (files != null) {

            for (final File f : files) {

                try {

                    if (f.isDirectory()) {

                        scanDirectoryForTemplates(f, newModel);

                    } else {

                        final Checklist cl = new Checklist(f);

                        cl.setId(f.getName());
                        newModel.put(cl.getId(), cl);
                    }
                } catch (final Exception e) {
                    Logger.getLogger(ChecklistRepository.class).error("Error during loading " + f.getName(), e);
                }
            }
        }
    }


    public List<String> getChecklistNames() {
        return  new LinkedList<>(data.keySet());
    }


    public Checklist getChecklist(final String name) {
        return data.get(name);
    }


    public String createFromTemplate(final String folder, final String templateName, final Template template, final String parent) {
        return createFromTemplate('/' + folder + '/' + templateName, template, parent);
    }


    /**
     * This method creates a new checklist from an existing template.
     *
     * @param template
     * @return a UUID
     */
    public String createFromTemplate(final String templateId, final Template template, final String parent) {

        Checklist parentCL = null;

        if (parent != null) {

            parentCL = data.get(parent);

            if (parentCL == null) {
                throw new IllegalArgumentException(parent + " is not a known checklist... can't use it as parent");
            }
        }

        final String uuid = UUID.randomUUID().toString();
        final File subfolder = new File(mainFolder, templateId);

        synchronized (lock) {

            subfolder.mkdirs();

            final Checklist checklist = new Checklist(uuid, template, new File(subfolder, uuid), parentCL);

            checklist.setCreationTime(System.currentTimeMillis());
            checklist.setUniqueTagcombination(isTagCombinationUnique(checklist.getTags(), null));
            data.put(uuid, checklist);
        }

        // TODO : send message to stats to signal updated content        
        ActorRepository.getPersistenceActor().tell(new PersistenceRequest(uuid), null);

        return uuid;
    }


    public List<ChecklistInfo> getChecklistInformation(final List<String> tags, final List<String> milestones, boolean hideSubchecklists) {

        final List<ChecklistInfo> result = new LinkedList<>();

        synchronized (lock) {

            for (final Entry<String, Checklist> clEntry : data.entrySet()) {

                if (hideSubchecklists && clEntry.getValue().isSubchecklist()) {
                    continue;
                }

                if (!matchesTag(tags, clEntry.getValue())) {
                    continue;
                }

                if (!matchesMilestone(milestones, clEntry.getValue())) {
                    continue;
                }

                final ChecklistInfo cli = new ChecklistInfo(clEntry.getValue());

                cli.setUuid(clEntry.getKey());
                result.add(cli);
            }
        }

        Collections.sort(result); // sort outside the lock

        return result;
    }


    public Iterable<Checklist> getChecklistsForTemplate(final String id) {

        final List<Checklist> result = new LinkedList<>();

        synchronized (lock) {

            for (final Entry<String, Checklist> clEntry : data.entrySet()) {

                if (id.equals(clEntry.getValue().getTemplate())) {
                    result.add(clEntry.getValue());
                }
            }
        }

        return result;
    }


    /**
     * Get information about the tags on the checklists
     *
     * @param filter If specified, only return info from checklists that contain all tags specified in this comma separated list
     * @return
     */
    public List<TagcloudEntry> getTagInfo(final String filter) {

        List<String> filterList = new LinkedList<>();

        if (filter != null) {
            filterList = Arrays.asList(filter.split(","));
        }

        final Map<String, Integer> tagMap = new HashMap<>();

        synchronized (lock) {

            for (final Checklist cl : data.values()) {

                if (!filterList.isEmpty()) {

                    if (!matchesTag(filterList, cl)) {
                        continue;
                    }
                }

                for (final String tag : cl.getTags()) {

                    if (filterList.contains(tag)) {
                        // no need to repeat the ones in the filter (this only disturbs the tagcloud)
                        continue;
                    }

                    if (tagMap.containsKey(tag)) {
                        tagMap.put(tag, tagMap.get(tag) + 1);
                    } else {
                        tagMap.put(tag, 1);
                    }
                }
            }
        }

        final List<TagcloudEntry> result = new LinkedList<>();

        // TODO: replace with collect
        for (final Entry<String, Integer> tmEntry : tagMap.entrySet()) {
            result.add(new TagcloudEntry(tmEntry.getKey(), tmEntry.getValue()));
        }

        return result;
    }


    private boolean matchesTag(final List<String> filterList, final Checklist cl) {

        if (filterList == null || filterList.isEmpty()) {
            return true;
        }

        boolean match = true;

        for (final String tag : filterList) {

            if (!tag.equals("")) {
                match &= cl.getTags().contains(tag);
            }
        }

        return match;
    }


    private boolean matchesMilestone(final List<String> filterList, final Checklist cl) {

        if (filterList == null || filterList.isEmpty()) {
            return true;
        }

        boolean match = true;

        // TODO: quit the loop if !match
        for (final String milestoneName : filterList) {

            if (!milestoneName.equals("")) {
                match &= cl.getMilestones().contains(new Milestone(milestoneName, true));
            }
        }

        return match;
    }


    public List<TagcloudEntry> getMilestoneInfo(final String filter) {

        List<String> filterList = new LinkedList<>();

        if (filter != null) {
            filterList = Arrays.asList(filter.split(","));
        }

        final Map<String, Integer> msMap = new HashMap<>();

        synchronized (lock) {

            for (final Checklist cl : data.values()) {

                if (!matchesMilestone(filterList, cl)) {
                    continue;
                }

                for (final Milestone ms : cl.getMilestones()) {

                    if (filterList.contains(ms.getName())) {
                        // no need to repeat the ones in the filter (this only disturbs the tagcloud)
                        continue;
                    }

                    if (!ms.isReached()) {
                        continue;
                    }

                    msMap.put(ms.getName(), msMap.containsKey(ms.getName()) ? msMap.get(ms.getName()) + 1 : 1);
                }
            }
        }

        final List<TagcloudEntry> result = new LinkedList<>();

        // TODO: replace with collect
        for (final Entry<String, Integer> tmEntry : msMap.entrySet()) {
            result.add(new TagcloudEntry(tmEntry.getKey(), tmEntry.getValue()));
        }

        return result;
    }


    public void deleteChecklist(final Checklist cl) {

        data.remove(cl.getId());

        if (cl.getPersister().getFile() != null) {
            // TODO: delete result ignored
            cl.getPersister().getFile().delete();
        }
    }


    /**
     * Checks if a tag combination is unique
     *
     * @param tags
     * @param id   the id of the checklist in question (is not taken into account during comparison)
     * @return
     */
    public boolean isTagCombinationUnique(final List<String> tags, final String id) {

        synchronized (lock) {

            for (final Checklist clWalker : data.values()) {

                if (clWalker.getId().equals(id)) {
                    // only if id specified and equal to the candidate we would want to check
                    continue;
                }

                boolean different = false;

                for (final String tagWalker : tags) {

                    if (!clWalker.getTags().contains(tagWalker)) {
                        // tag is not in, so no need to test further
                        different = true;
                        break;
                    }
                }

                if (!different) {
                    return false;
                }
            }
        }

        return true;
    }
}