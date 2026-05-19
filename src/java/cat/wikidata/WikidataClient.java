package cat.wikidata;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URLEncoder;
import java.net.URL;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;

public class WikidataClient {

    // Trust-all HTTPS for local dev only
    static {
        try {
            TrustManager[] trustAllCerts = new TrustManager[]{
                    new X509TrustManager() {
                        @Override
                        public X509Certificate[] getAcceptedIssuers() {
                            return new X509Certificate[0];
                        }

                        @Override
                        public void checkClientTrusted(X509Certificate[] certs, String authType) {
                            // no-op
                        }

                        @Override
                        public void checkServerTrusted(X509Certificate[] certs, String authType) {
                            // no-op
                        }
                    }
            };

            SSLContext sc = SSLContext.getInstance("TLS");
            sc.init(null, trustAllCerts, new SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());

            HostnameVerifier allHostsValid = new HostnameVerifier() {
                @Override
                public boolean verify(String hostname, SSLSession session) {
                    return true;
                }
            };
            HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);

            System.out.println("DEBUG SSL: trust-all HTTPS INITIALIZED");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public List<WikidataEntry> searchEntries(String text, String languageCode) {
        List<WikidataEntry> results = new ArrayList<>();

        if (text == null || text.trim().isEmpty()) {
            return results;
        }

        try {
            List<WikidataEntry> liveResults = searchEntriesLive(text.trim(), languageCode);
            if (liveResults != null && !liveResults.isEmpty()) {
                return liveResults;
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }

        results.add(new WikidataEntry(
                "TEST_" + text,
                "Dummy description for \"" + text + "\" (" + languageCode + ")",
                "https://www.wikidata.org/wiki/Q42"
        ));

        return results;
    }

    private List<WikidataEntry> searchEntriesLive(String text, String languageCode) throws Exception {
        List<WikidataEntry> results = new ArrayList<>();

        String lang = (languageCode == null || languageCode.trim().isEmpty())
                ? "en"
                : languageCode.trim();

        String urlStr =
                "https://www.wikidata.org/w/api.php?action=wbsearchentities" +
                "&format=json" +
                "&language=" + URLEncoder.encode(lang, "UTF-8") +
                "&uselang=" + URLEncoder.encode(lang, "UTF-8") +
                "&limit=5" +
                "&search=" + URLEncoder.encode(text, "UTF-8");

        System.out.println("DEBUG Wikidata URL = " + urlStr);

        URL url = new URL(urlStr);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Accept", "application/json");
        conn.setRequestProperty(
                "User-Agent",
                "CAT-StudentDemo/1.0 (contact: vanya@example.com)"
        );
        conn.setConnectTimeout(10000);
        conn.setReadTimeout(10000);

        int code = conn.getResponseCode();
        System.out.println("DEBUG Wikidata HTTP code = " + code);

        if (code != 200) {
            BufferedReader errorReader = null;
            try {
                if (conn.getErrorStream() != null) {
                    errorReader = new BufferedReader(
                            new InputStreamReader(conn.getErrorStream(), "UTF-8"));
                    StringBuilder errorSb = new StringBuilder();
                    String line;
                    while ((line = errorReader.readLine()) != null) {
                        errorSb.append(line);
                    }
                    System.out.println("DEBUG Wikidata error response = " + errorSb.toString());
                }
            } finally {
                if (errorReader != null) {
                    errorReader.close();
                }
            }

            throw new RuntimeException("Wikidata HTTP error: " + code);
        }

        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(conn.getInputStream(), "UTF-8"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
        }

        String jsonText = sb.toString();
        System.out.println("DEBUG Wikidata response = " + jsonText);

        try (JsonReader jsonReader = Json.createReader(new StringReader(jsonText))) {
            JsonObject root = jsonReader.readObject();
            JsonArray search = root.getJsonArray("search");

            if (search == null) {
                return results;
            }

            for (int i = 0; i < search.size(); i++) {
                JsonObject item = search.getJsonObject(i);

                String id = item.containsKey("id") ? item.getString("id") : "";
                String label = item.containsKey("label") ? item.getString("label") : id;
                String description = item.containsKey("description")
                        ? item.getString("description")
                        : "No description";
                String uri = "https://www.wikidata.org/wiki/" + id;

                results.add(new WikidataEntry(label, description, uri));
            }
        }

        return results;
    }
}