package kullervo16.checklist.repository;

import java.io.File;
import java.util.*;
import java.util.Map.Entry;

import kullervo16.checklist.messages.PersistenceRequest;
import kullervo16.checklist.model.*;
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
        clearAndLoad();
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

                if (f.getName().startsWith(".")) {
                    continue;
                }

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
        return new LinkedList<>(data.keySet());
    }


    public Checklist getChecklist(final String name) {
        return data.get(name);
    }


    public String createFromTemplate(final String folder, final String templateName, final Template template, final String parent, final String parentStepId,
                                     final String userName, final String userId) {
        return createFromTemplate('/' + folder + '/' + templateName, template, parent, parentStepId, userName, userId);
    }


    /**
     * This method creates a new checklist from an existing template.
     *
     * @param templateId
     * @param template
     * @param parent
     * @return a UUID
     */
    public String createFromTemplate(final String templateId, final Template template, final String parent, final String parentStepId, final String userName,
                                     final String userId) {

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

            // TODO: delete result ignored
            subfolder.mkdirs();

            final Checklist checklist = new Checklist(uuid, template, new File(subfolder, uuid), parentCL, parentStepId);

            checklist.setCreationTime(System.currentTimeMillis());
            checklist.setUser(userName);
            checklist.addUserTag(userId);
            checklist.setUniqueTagcombination(isTagCombinationUnique(checklist.getTags(), null));
            data.put(uuid, checklist);
        }

        // TODO : send message to stats to signal updated content        
        ActorRepository.getPersistenceActor().tell(new PersistenceRequest(uuid), null);

        return uuid;
    }


    public List<ChecklistInfo> getChecklistInformation(final List<String> tags, final List<String> milestones, final boolean hideSubchecklists) {

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
     * Get information about the tags on the checklists.
     *
     * @param filter If specified, only return info from checklists that contain all tags specified in this comma separated list
     * @return
     */
    public Tagcloud getTagInfo(final String filter) {

        final Set<String> filterSet = filter == null ? new HashSet<>(5) : new HashSet<>(Arrays.asList(filter.split(",")));
        final Map<String, Integer> tagMap = new HashMap<>();
        List<String> commonTags = null;

        synchronized (lock) {

            for (final Checklist cl : data.values()) {

                if (cl.isSubchecklist()) {
                    continue;
                }

                if (filter != null && !filterSet.isEmpty()) {

                    if (!matchesTag(filterSet, cl)) {
                        continue;
                    }
                }

                final Set<String> clTags = cl.getTags();

                // If it is the first checklist
                if (commonTags == null) {

                    // Copy the tags from the checklist
                    commonTags = new ArrayList<>(clTags);

                    // Remove the filter tags
                    commonTags.removeAll(filterSet);

                } else {

                    for (int i = 0; i < commonTags.size(); ) {

                        // If the common tag exists in the checklist
                        if (clTags.contains(commonTags.get(i))) {

                            // Go to the next common tag
                            i++;

                        } else {

                            // Remove the common tag because it is not common
                            commonTags.remove(i);
                        }
                    }
                }

                for (final String tag : clTags) {

                    if (filterSet.contains(tag)) {
                        // no need to repeat the ones in the filter (this only disturbs the tags cloud)
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

            final String tag = tmEntry.getKey();

            // If the tag is not a common tag
            // And if it is not the sub-checklist tag
            if ((commonTags == null || !commonTags.contains(tag)) && !tag.equals("subchecklist")) {

                result.add(new TagcloudEntry(tag, tmEntry.getValue()));
            }
        }

        if (commonTags != null) {
            filterSet.addAll(commonTags);
        }

        final List<String> newFilters = new ArrayList<>(filterSet);

        newFilters.sort(String::compareToIgnoreCase);

        return new Tagcloud(result, newFilters);
    }


    private static boolean matchesTag(final Collection<String> filterSet, final Checklist cl) {

        if (filterSet == null || filterSet.isEmpty()) {
            return true;
        }

        boolean match = true;

        for (final String tag : filterSet) {

            if (!tag.equals("")) {
                match &= cl.getTags().contains(tag);
            }
        }

        return match;
    }


    private static boolean matchesMilestone(final List<String> filterList, final Checklist cl) {

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
                        // no need to repeat the ones in the filter (this only disturbs the tags cloud)
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


    /**
     * Delete a checklist and remove the link from the parent checklist if any.
     *
     * @param cl The checklist to delete.
     */
    public void deleteChecklist(final Checklist cl, final String userName, final String userId) {

        if (cl == null) {
            return;
        }

        final String clId = cl.getId();
        final String parentClId = cl.getParent();

        // Delete the children checklists if any
        for (final Step stepWalker : cl.getSteps()) {

            final String childClId = stepWalker.getChild();

            if (childClId != null) {

                final Checklist childCl = data.get(childClId);

                // Set the parent to null to avoid the deleteChecklist call to update this checklist
                childCl.setParent(null);

                deleteChecklist(childCl, userName, userId);
            }
        }

        // If there is a parent checklist, remove the link to this deleted checklist
        if (parentClId != null) {

            final Checklist parentCl = data.get(parentClId);

            // This if statement is only there to avoid any delete conflicts
            if (parentCl != null) {

                for (final Step stepWalker : parentCl.getSteps()) {

                    if (clId.equals(stepWalker.getChild())) {
                        stepWalker.setChild(null);
                        parentCl.updateStepState(stepWalker, State.UNKNOWN, userName, userId);
                        ActorRepository.getPersistenceActor().tell(new PersistenceRequest(parentClId), null);
                        break;
                    }
                }
            }
        }

        synchronized (lock) {
            data.remove(clId);
        }

        if (cl.getPersister().getFile() != null) {
            // TODO: delete result ignored
            cl.getPersister().getFile().delete();
        }
    }


    /**
     * Checks if a tag combination is unique.
     *
     * @param tags
     * @param id   the id of the checklist in question (is not taken into account during comparison)
     * @return
     */
    public boolean isTagCombinationUnique(final Set<String> tags, final String id) {

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

    /**
     * Removes the old tag (when not an originalTemplateTag) and adds the new one.
     * @param tagName
     * @param mergeInto 
     */
    public void mergeTag(String tagName, String mergeInto) {
       
        synchronized (lock) {
            for(Checklist walker : data.values()) {
                Set<String> workingSet = new HashSet<>(walker.getTags());
                if(workingSet.contains(tagName)) {
                    if(!walker.getOriginalTemplateTags().contains(tagName)) {
                        workingSet.remove(tagName);
                    }                    
                    if(!workingSet.contains(mergeInto)) {
                        workingSet.add(mergeInto);
                    }                    
                    ActorRepository.getPersistenceActor().tell(new PersistenceRequest(walker.getId()), null);                    
                }
                walker.setTags(workingSet);
            }
        }
    }

    /**
     * Deletes the tags from the checklists (where that tag is not an originalTemplateTag)
     * @param tagName 
     */
    public void deleteTag(String tagName) {
        synchronized (lock) {
            for(Checklist walker : data.values()) {
                if(walker.getTags().contains(tagName) && !walker.getOriginalTemplateTags().contains(tagName)) {
                    walker.getTags().remove(tagName);
                    ActorRepository.getPersistenceActor().tell(new PersistenceRequest(walker.getId()), null);                    
                }
            }
        }
    }

    public static void clearAndLoad() {
        synchronized(lock) {
            data.clear();
            // fixed path... target is to work in a docker container, you can mount it via a volume to whatever you want
            loadData("/opt/checklist/checklists");
        }
    }
}