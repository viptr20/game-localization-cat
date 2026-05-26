package cat.semantic;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

public class LocalTerm implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private String text;
    private String languageCode;

    // Wikidata candidates
    private Set<String> wikidataUris = new HashSet<>();
    private String preferredWikidataUri;

    public LocalTerm(String text, String languageCode) {
        this.text = text;
        this.languageCode = languageCode;
    }

    public String getText() {
        return text;
    }

    public String getLanguageCode() {
        return languageCode;
    }

    public Set<String> getWikidataUris() {
        return wikidataUris;
    }

    public void addWikidataUri(String uri) {
        wikidataUris.add(uri);
    }

    public String getPreferredWikidataUri() {
        return preferredWikidataUri;
    }

    public void setPreferredWikidataUri(String preferredWikidataUri) {
        this.preferredWikidataUri = preferredWikidataUri;
    }
}
