package kullervo16.checklist.model;

import java.util.Collection;
import java.util.List;

public class Tagcloud {

    private final Collection<TagcloudEntry> entries;

    private final Collection<String> selection;


    public Tagcloud(final Collection<TagcloudEntry> entries, final Collection<String> selection) {
        this.entries = entries;
        this.selection = selection;
    }


    public Collection<TagcloudEntry> getEntries() {
        return entries;
    }


    public Collection<String> getSelection() {
        return selection;
    }
}
