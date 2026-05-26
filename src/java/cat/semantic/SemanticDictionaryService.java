package cat.semantic;

import cat.semantic.LocalTerm;
import cat.wikidata.WikidataClient;
import cat.wikidata.WikidataEntry;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class SemanticDictionaryService implements Serializable {

    private static final long serialVersionUID = 1L;

    private final WikidataClient wikidataClient = new WikidataClient();
    private final SimpleKeywordExtractor keywordExtractor = new SimpleKeywordExtractor();

    public List<SemanticTermCandidate> enrichSegment(String sourceText, String languageCode) {
        List<SemanticTermCandidate> result = new ArrayList<SemanticTermCandidate>();

        if (sourceText == null || sourceText.trim().isEmpty()) {
            return result;
        }

        List<String> keywords = keywordExtractor.extractEnglishKeywords(sourceText, 5);

        if (keywords.isEmpty()) {
            keywords.add(sourceText.trim());
        }

        for (String keyword : keywords) {
            LocalTerm term = new LocalTerm(keyword, languageCode);
            List<WikidataEntry> entries = wikidataClient.searchEntries(keyword, languageCode);

            if (entries != null) {
                for (WikidataEntry entry : entries) {
                    if (entry != null && entry.getUri() != null) {
                        term.addWikidataUri(entry.getUri());
                    }
                }
            }

            result.add(new SemanticTermCandidate(term, entries));
        }

        return result;
    }

    public static class SemanticTermCandidate implements Serializable {

        private static final long serialVersionUID = 1L;

        private LocalTerm term;
        private List<WikidataEntry> candidates;

        public SemanticTermCandidate(LocalTerm term, List<WikidataEntry> candidates) {
            this.term = term;
            this.candidates = candidates;
        }

        public LocalTerm getTerm() {
            return term;
        }

        public void setTerm(LocalTerm term) {
            this.term = term;
        }

        public List<WikidataEntry> getCandidates() {
            return candidates;
        }

        public void setCandidates(List<WikidataEntry> candidates) {
            this.candidates = candidates;
        }
    }
}