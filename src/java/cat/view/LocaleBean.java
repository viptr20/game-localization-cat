package cat.view;

import java.io.Serializable;
import java.util.Locale;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;

@ManagedBean(name = "localeBean")
@SessionScoped
public class LocaleBean implements Serializable {

    private static final long serialVersionUID = 1L;

    private String language = "en";
    private Locale currentLocale = new Locale("en");

    public void changeLanguage() {
        currentLocale = new Locale(language);
        FacesContext.getCurrentInstance().getViewRoot().setLocale(currentLocale);
    }

    public Locale getCurrentLocale() {
        if (currentLocale == null) {
            currentLocale = new Locale(language != null ? language : "en");
        }
        return currentLocale;
    }

    public boolean isRtl() {
        return "ar".equals(getCurrentLocale().getLanguage());
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }
}