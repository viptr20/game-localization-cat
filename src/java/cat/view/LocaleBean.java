package cat.view;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
import java.io.Serializable;
import java.util.Locale;

@ManagedBean(name = "localeBean")
@SessionScoped
public class LocaleBean implements Serializable {

    private String language = "en";

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public Locale getCurrentLocale() {
        if (language == null || language.trim().isEmpty()) {
            language = "en";
        }
        return new Locale(language);
    }

    public void changeLanguage() {
        FacesContext.getCurrentInstance()
                .getViewRoot()
                .setLocale(getCurrentLocale());
    }

    public String applyLanguage() {
        changeLanguage();
        return null;
    }

    public boolean isRtl() {
        String lang = getCurrentLocale().getLanguage();
        return "ar".equals(lang) || "he".equals(lang) || "iw".equals(lang);
    }
}