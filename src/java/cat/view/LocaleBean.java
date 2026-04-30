package cat.view;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
import java.io.Serializable;
import java.util.Locale;

@ManagedBean(name = "localeBean")
@SessionScoped
public class LocaleBean implements Serializable {

    private static final long serialVersionUID = 1L;

    // language code: "bg", "en", "pt", "ar", "hy"
    private String language = "bg";
    private Locale currentLocale = new Locale(language);

    public String changeLanguage() {
        if (language == null || language.isEmpty()) {
            language = "bg";
        }
        currentLocale = new Locale(language);
        FacesContext.getCurrentInstance().getViewRoot().setLocale(currentLocale);

        // след смяна на езика – redirect към dashboard
        // за да се създаде ново view и да се rebuild-нат графиките
        return "dashboard?faces-redirect=true";
    }

    public Locale getCurrentLocale() {
        if (currentLocale == null) {
            currentLocale = new Locale(language != null ? language : "bg");
        }
        return currentLocale;
    }

    public boolean isRtl() {
        String lang = getCurrentLocale().getLanguage();
        return "ar".equals(lang);
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }
}