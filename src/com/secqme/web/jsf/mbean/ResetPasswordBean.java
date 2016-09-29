package com.secqme.web.jsf.mbean;

import com.secqme.CoreException;
import com.secqme.ErrorType;
import com.secqme.manager.UserManager;
import com.secqme.web.jsf.util.MessageController;
import com.secqme.web.jsf.util.NavigationRules;
import org.apache.log4j.Logger;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedProperty;
import javax.faces.component.UIComponent;
import javax.faces.component.UIInput;
import javax.faces.context.FacesContext;
import javax.faces.event.ComponentSystemEvent;
import javax.ws.rs.NotFoundException;
import java.io.Serializable;
import java.util.ResourceBundle;

/**
 * Created by edward on 28/08/2015.
 */
public class ResetPasswordBean implements Serializable {

    private static final Logger myLog = Logger.getLogger(ResetPasswordBean.class);

    private ResourceBundle bundle;
    private UserManager userManager;

    private String resetToken;
    private String newPassword;
    private String newConfirmPassword;

    @PostConstruct
    public void init() {
        resetToken = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("code");
    }

    public ResourceBundle getBundle() {
        return bundle;
    }

    public void setBundle(ResourceBundle bundle) {
        this.bundle = bundle;
    }

    public UserManager getUserManager() {
        return userManager;
    }

    public void setUserManager(UserManager userManager) {
        this.userManager = userManager;
    }

    public String getResetToken() {
        return resetToken;
    }

    public void setResetToken(String resetToken) {
        this.resetToken = resetToken;
    }

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }

    public String getNewConfirmPassword() {
        return newConfirmPassword;
    }

    public void setNewConfirmPassword(String newConfirmPassword) {
        this.newConfirmPassword = newConfirmPassword;
    }

    public void verifyToken() {
        myLog.debug("Verify reset password token: " + resetToken);

        if (!userManager.isValidResetPasswordToken(resetToken)) {
            FacesContext fc = FacesContext.getCurrentInstance();
            fc.getApplication().getNavigationHandler().handleNavigation(fc, null, NavigationRules.RESET_PASSWORD_INVALID.getNaviPath());
        }
    }

    public String resetUserPassword() {
        String naviPath = NavigationRules.RESET_PASSWORD_INVALID.getNaviPath();
        
        try {
            userManager.resetPasswordUsingToken(resetToken, newPassword);
            naviPath = NavigationRules.RESET_PASSWORD_SUCCESS.getNaviPath();
        } catch (CoreException ex) {
            myLog.debug(ex.getMessage(), ex);
        }

        return naviPath;
    }

    public void validatePassword(ComponentSystemEvent event) {
        FacesContext fc = FacesContext.getCurrentInstance();

        UIComponent components = event.getComponent();

        // get password
        UIInput uiInputPassword = (UIInput) components.findComponent("newPassword");
        String password = uiInputPassword.getLocalValue() == null ? ""
                : uiInputPassword.getLocalValue().toString();
        String passwordId = uiInputPassword.getClientId();

        // get confirm password
        UIInput uiInputConfirmPassword = (UIInput) components.findComponent("newConfirmPassword");
        String confirmPassword = uiInputConfirmPassword.getLocalValue() == null ? ""
                : uiInputConfirmPassword.getLocalValue().toString();

        // Let required="true" do its job.
        if (password.isEmpty() || confirmPassword.isEmpty()) {
            return;
        }

        if (!password.equals(confirmPassword)) {
            FacesMessage msg = new FacesMessage(bundle.getString("resetpassword.error.notmatch"));
            msg.setSeverity(FacesMessage.SEVERITY_ERROR);
            fc.addMessage(passwordId, msg);
            fc.renderResponse();
        }
    }
}
