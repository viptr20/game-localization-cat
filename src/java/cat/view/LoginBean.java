package cat.view;

import cat.dao.UserDAO;
import cat.model.User;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import java.io.Serializable;

@ManagedBean(name = "loginBean")
@SessionScoped
public class LoginBean implements Serializable {

    private static final long serialVersionUID = 1L;

    private String username;
    private String password;
    private String email;
    private User currentUser;

    private final UserDAO userDAO = new UserDAO();

    public String doLogin() {
        FacesContext context = FacesContext.getCurrentInstance();

        try {
            if (isBlank(username) || isBlank(password)) {
                context.addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_WARN,
                                "Missing credentials",
                                "Please enter username and password."));
                return null; // оставаш на login.xhtml, само показваш съобщение
            }

            // Trim само username, паролата я оставяме така, както е
            currentUser = userDAO.validate(username.trim(), password);

            if (currentUser != null) {
                context.addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_INFO,
                                "Login successful",
                                "Welcome, " + currentUser.getFullName()));

                // за сигурност
                password = null;

                // Тези outcomes съвпадат с faces-config.xml
                return "success";
            }

            // не намерен user
            currentUser = null;
            password = null;

            context.addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Invalid credentials",
                            "Wrong username or password."));
            return "failure";

        } catch (Exception e) {
            e.printStackTrace();
            context.addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Login error",
                            "A system error occurred while trying to sign in."));
            return null;
        }
    }

    public void sendResetLink() {
        FacesContext context = FacesContext.getCurrentInstance();

        if (isBlank(email)) {
            context.addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_WARN,
                            "Missing e-mail",
                            "Please enter your e-mail address."));
            return;
        }

        context.addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO,
                        "Reset link sent",
                        "If the e-mail exists in the system, a reset link was sent to " + email.trim()));
    }

    public String goToForgotPassword() {
        // navigation-rule в faces-config.xml очаква outcome "case1"
        return "case1";
    }

    public String logout() {
        ExternalContext externalContext = FacesContext.getCurrentInstance().getExternalContext();
        externalContext.invalidateSession();
        // navigation-rule от други страници към login.xhtml
        return "login";
    }

    public boolean isLoggedIn() {
        return currentUser != null;
    }

    public boolean isAdmin() {
        return currentUser != null && "ADMIN".equalsIgnoreCase(currentUser.getRole());
    }

    public String getDisplayName() {
        return currentUser != null ? currentUser.getFullName() : "";
    }

    public String getRole() {
        return currentUser != null ? currentUser.getRole() : "";
    }

    public User getCurrentUser() {
        return currentUser;
    }

    // ===== helpers =====

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    // ===== getters / setters =====

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}