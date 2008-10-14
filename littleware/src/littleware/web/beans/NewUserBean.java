package littleware.web.beans;

import java.security.*;
import java.util.*;
import java.util.UUID;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.rmi.RemoteException;
import javax.mail.*;
import javax.mail.internet.*;

import littleware.security.*;
import littleware.security.auth.*;
import littleware.base.*;
import littleware.asset.*;
import littleware.apps.addressbook.*;

/**
 * Bean manages new user setup.
 * Collects user name, e-mail, and vital stats.
 * Creates new user with auto-generated password,
 * adds the user to the group.web_registered group,
 * sets up a 1000 updates/day quota for the user,
 * and e-mails the user/password data off to the user.
 * Connects to littleware asset server as the user specified
 * by web.admin in littleware.properties.
 */
public class NewUserBean extends AbstractBean {

    private static final Logger  olog = Logger.getLogger( NewUserBean.class.getName() );
    private static SessionHelper ohelp_admin = null;
    private static UUID          ou_acl_everybody = null;
    private static boolean       ob_initialized = false;

    static {
        setupSession();
    }

    /**
     * Little utility to setup the session connections to the asset server
     */
    private static synchronized void setupSession() {
        if (!ob_initialized) {
            try {
                if (null == ohelp_admin) {
                    PrivilegedAction<SessionHelper> act_getadmin =
                            new PrivilegedAction<SessionHelper>() {

                                public SessionHelper run() {
                                    return BeanUtil.getWebAdminHelper();
                                }
                            };

                    ohelp_admin = (SessionHelper) AccessController.doPrivileged(act_getadmin);
                }
                ou_acl_everybody = ohelp_admin.getService( ServiceType.ASSET_SEARCH ).
                        getByName( AclManager.ACL_EVERYBODY_READ, SecurityAssetType.ACL ).
                        getObjectId();
                ob_initialized = true;
            } catch ( Exception ex ) {
                olog.log( Level.SEVERE, "Failed to setup NewUserBean statics", ex );
                throw new AssertionFailedException( "Failed NewUserBean statics setup", ex );
            }
        }
    }
    private String os_name = null;
    private String os_email = null;
    private UsaState on_state = null;
    private String os_city = null;
    private SessionHelper om_helper = ohelp_admin;

    /**
     * Do-nothing constructor
     */
    public NewUserBean() {
        if (null == om_helper) {
            setupSession();
            om_helper = ohelp_admin;
        }
    }

    public String getName() {
        return os_name;
    }

    public void setName(String s_name) {
        os_name = s_name;
    }

    public String getEmail() {
        return os_email;
    }

    public void setEmail(String s_email) {
        os_email = s_email;
    }

    public String getCity() {
        return os_city;
    }

    public void setCity(String s_city) {
        os_city = s_city;
    }

    public UsaState getUsaState() {
        return on_state;
    }

    public void setUsaState(UsaState n_state) {
        on_state = n_state;
    }

    private String os_group;
    
    public String getNewUserGroupName() {
        return os_group;
    }
    public void setNewUserGroupName( String s_group ) {
        os_group = s_group;
    }
    
    /**
     * Really internal method to send welcome e-mail - public to allow testing.
     */
    public void sendWelcomeMessage(String s_name, String s_to,
            String s_password) throws MessagingException 
    {
        Session mail_session = BeanUtil.setupMailSession();
        Message mail_message = new MimeMessage(mail_session);
        InternetAddress[] v_to = {
            new InternetAddress(s_to)
        };

        mail_message.setFrom(new InternetAddress(BeanUtil.getWebMasterEmail()));
        mail_message.setRecipients(Message.RecipientType.TO, v_to);
        mail_message.setSubject("Welcome to Littleware");
        mail_message.setSentDate(new Date());
        mail_message.setText("Welcome to http://littleware.frickjack.com!\n\nUsername: " + s_name +
                "\nPassword: " + s_password + "\n\n" +
                "Change your password under 'My Account' after you log in the first time.\n");
        Transport.send(mail_message);
    }


    /**
     * Create the user, quota, account-info assets, and 
     * save them all to the repository.
     */
    public String newUserAction() {
        try {
            AccountManager m_account = om_helper.getService(ServiceType.ACCOUNT_MANAGER);
            AssetManager m_asset = om_helper.getService(ServiceType.ASSET_MANAGER);
            LittleUser user_new = SecurityAssetType.USER.create();
            String s_password = UUIDFactory.getFactory().create().toString();

            user_new.setName(os_name);
            user_new.setHomeId(BeanUtil.getWebHomeId());
            m_account.createUser(user_new, s_password);

            // Setup quota
            littleware.security.Quota quota_new = SecurityAssetType.QUOTA.create();
            quota_new.setFromId(user_new.getObjectId());
            quota_new.setName("littleware_quota");
            quota_new.setQuotaLimit(1000);
            quota_new.setHomeId(BeanUtil.getWebHomeId());
            quota_new.setOwnerId(AccountManager.UUID_ADMIN);
            quota_new.setAclId( ou_acl_everybody );
            quota_new = (littleware.security.Quota) m_asset.saveAsset(quota_new, "Quota for " + user_new.getName());

            if ( null != getNewUserGroupName() ) {
                // Add to web group
                LittleGroup p_web = (LittleGroup) m_account.getPrincipal( getNewUserGroupName () );
                p_web.addMember(user_new);
                p_web = (LittleGroup) m_asset.saveAsset(p_web, "Add new member: " + user_new.getName());
            }

            // Setup Contact info: User - Link - Contact - Address
            Asset a_link = AddressAssetType.LINK.create();
            a_link.setName("contact");
            a_link.setFromId(user_new.getObjectId());
            a_link.setHomeId(user_new.getHomeId());
            a_link.setOwnerId(user_new.getObjectId());

            Contact contact_user = AddressAssetType.CONTACT.create();
            contact_user.setHomeId(a_link.getHomeId());
            contact_user.setLastName("unknown");
            contact_user.setFirstName("unknown");
            contact_user.setOwnerId(user_new.getObjectId());
            contact_user = (Contact) m_asset.saveAsset(contact_user, "bootstrap user contact information");

            a_link.setToId(contact_user.getObjectId());
            a_link = m_asset.saveAsset(a_link, "Link to new contact information");

            littleware.apps.addressbook.Address addr_bootstrap = AddressAssetType.ADDRESS.create();
            addr_bootstrap.setHomeId(user_new.getHomeId());
            addr_bootstrap.setName("default");
            try {
                addr_bootstrap.setEmail(new InternetAddress(getEmail()));
            } catch (AddressException e) {
                olog.log(Level.WARNING, "Failure setting bogus bootstrap e-mail address, caught: " + e);
            }
            addr_bootstrap.setAddressType(AddressType.HOME);
            addr_bootstrap.setOwnerId(user_new.getObjectId());
            contact_user.addAddress(addr_bootstrap, 0);
            contact_user = (Contact) m_asset.saveAsset(contact_user, "bootstrap user contact information");

            sendWelcomeMessage(getName(), getEmail(), s_password);

            setLastResult( ActionResult.Ok );
        } catch (RemoteException e) {
            // Retry the first time
            olog.log(Level.INFO, "RemoteException on login, caught: " + e + ", " +
                    BaseException.getStackTrace(e)
                    );
            setLastResult(ActionResult.Failed );
        } catch (MessagingException e) {
            olog.log(Level.INFO, "Created new user, but failed to send e-mail, caught: " + e);
            setLastResult( ActionResult.OkNoEmail );
        } catch (IllegalNameException e) {
            setLastResult(ActionResult.BadName );            
        } catch (AlreadyExistsException e) {
            setLastResult(ActionResult.BadName );
        } catch (Exception e) {
            olog.log(Level.INFO, "Failed login for " + getName() + ", caught: " + e +
                    ", " + BaseException.getStackTrace(e));
            setLastResult(ActionResult.Failed );
        }
        return getLastResult().toString();
    }
}

// littleware asset management system
// Copyright (C) 2007 Reuben Pasquini http://littleware.frickjack.com

