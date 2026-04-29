/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSF/JSFManagedBean.java to edit this template
 */
package cat.view;

import cat.model.User;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
import java.io.Serializable;

@ManagedBean(name = "userProfileBean")
@SessionScoped
public class UserProfileBean implements Serializable {

    public User getUser() {
        LoginBean loginBean = (LoginBean) FacesContext.getCurrentInstance()
                .getExternalContext()
                .getSessionMap()
                .get("loginBean");
        return loginBean != null ? loginBean.getCurrentUser() : null;
    }
}