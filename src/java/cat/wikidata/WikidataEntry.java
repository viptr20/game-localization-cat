package cat.wikidata;

import java.io.Serializable;

public class WikidataEntry implements Serializable {

    private static final long serialVersionUID = 1L;

    private String label;
    private String description;
    private String uri;

    public WikidataEntry(String label, String description, String uri) {
        this.label = label;
        this.description = description;
        this.uri = uri;
    }

    public String getLabel() {
        return label;
    }

    public String getDescription() {
        return description;
    }

    public String getUri() {
        return uri;
    }
}