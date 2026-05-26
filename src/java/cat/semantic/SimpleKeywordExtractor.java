package cat.semantic;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class SimpleKeywordExtractor implements Serializable {

    private static final long serialVersionUID = 1L;

    private static final Set<String> STOP_WORDS_EN = new HashSet<String>(Arrays.asList(
            "the", "a", "an", "and", "or", "of", "for", "to", "in", "on", "at",
            "this", "that", "is", "are", "be", "with", "by", "from", "as", "your",
            "you", "we", "our", "it", "its", "can", "will", "should", "must", "not",
            "all", "any", "but", "into", "than", "then", "there", "their", "them",
            "was", "were", "been", "has", "have", "had", "do", "does", "did"
    ));

    public List<String> extractEnglishKeywords(String text, int maxKeywords) {
        if (text == null || text.trim().isEmpty()) {
            return new ArrayList<String>();
        }

        String normalized = text.toLowerCase(Locale.ENGLISH).replaceAll("[^\\p{L}\\p{Nd}]+", " ");
        String[] tokens = normalized.split("\\s+");

        Map<String, Integer> freq = new HashMap<String, Integer>();

        for (String token : tokens) {
            if (token == null) {
                continue;
            }

            token = token.trim();

            if (token.length() < 3) {
                continue;
            }

            if (STOP_WORDS_EN.contains(token)) {
                continue;
            }

            Integer current = freq.get(token);
            if (current == null) {
                freq.put(token, 1);
            } else {
                freq.put(token, current + 1);
            }
        }

        List<Map.Entry<String, Integer>> entries = new ArrayList<Map.Entry<String, Integer>>(freq.entrySet());

        Collections.sort(entries, new Comparator<Map.Entry<String, Integer>>() {
            @Override
            public int compare(Map.Entry<String, Integer> a, Map.Entry<String, Integer> b) {
                int byFreq = b.getValue().compareTo(a.getValue());
                if (byFreq != 0) {
                    return byFreq;
                }
                return a.getKey().compareTo(b.getKey());
            }
        });

        List<String> result = new ArrayList<String>();

        for (Map.Entry<String, Integer> entry : entries) {
            result.add(entry.getKey());
            if (result.size() >= maxKeywords) {
                break;
            }
        }

        return result;
    }
}