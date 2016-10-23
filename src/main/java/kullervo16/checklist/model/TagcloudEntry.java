
package kullervo16.checklist.model;

/**
 * DTO to pass the info to jqcloud.
 *
 * @author jef
 */
public class TagcloudEntry {

    private final String text;

    private final int weight;


    public TagcloudEntry(final String text, final int weight) {
        this.text = text;
        this.weight = weight;
    }


    public String getText() {
        return text;
    }


    public int getWeight() {
        return weight;
    }
}
