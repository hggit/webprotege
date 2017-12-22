package edu.stanford.bmir.protege.web.client.user;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.web.bindery.event.shared.EventBus;
import edu.stanford.bmir.protege.web.client.app.ClientObjectReader;
import edu.stanford.bmir.protege.web.client.app.UserInSessionDecoder;
import edu.stanford.bmir.protege.web.client.dispatch.DispatchServiceCallback;
import edu.stanford.bmir.protege.web.client.dispatch.DispatchServiceManager;
import edu.stanford.bmir.protege.web.client.dispatch.actions.GetCurrentUserInSessionAction;
import edu.stanford.bmir.protege.web.client.dispatch.actions.GetCurrentUserInSessionResult;
import edu.stanford.bmir.protege.web.client.events.UserLoggedInEvent;
import edu.stanford.bmir.protege.web.client.events.UserLoggedOutEvent;
import edu.stanford.bmir.protege.web.shared.access.ActionId;
import edu.stanford.bmir.protege.web.shared.access.BuiltInAction;
import edu.stanford.bmir.protege.web.shared.app.UserInSession;
import edu.stanford.bmir.protege.web.shared.inject.ApplicationSingleton;
import edu.stanford.bmir.protege.web.shared.user.LogOutUserAction;
import edu.stanford.bmir.protege.web.shared.user.UserDetails;
import edu.stanford.bmir.protege.web.shared.user.UserId;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;
import static edu.stanford.bmir.protege.web.client.user.LoggedInUser.LoggedInUserState.LOGGED_IN_USER_CHANGED;
import static edu.stanford.bmir.protege.web.client.user.LoggedInUser.LoggedInUserState.LOGGED_IN_USER_UNCHANGED;

/**
 * Author: Matthew Horridge<br>
 * Stanford University<br>
 * Bio-Medical Informatics Research Group<br>
 * Date: 05/04/2013
 */
@ApplicationSingleton
public class LoggedInUserManager {

    @Nonnull
    private final LoggedInUser loggedInUser;

    @Nonnull
    private final DispatchServiceManager dispatchServiceManager;

    @Inject
    public LoggedInUserManager(@Nonnull LoggedInUser loggedInUser,
                               @Nonnull DispatchServiceManager dispatchServiceManager) {
        this.loggedInUser = loggedInUser;
        this.dispatchServiceManager = checkNotNull(dispatchServiceManager);
    }

    /**
     * Reads the initial logged in user from the web page source.
     * This should really only be called once on page load.
     */
    public void readInitialUserInSession() {
        readUserInSession();
    }

    /**
     * Gets the id of the currently logged in user.
     * @return The id of the currently logged in user.  Not {@code null}.  The returned id may correspond to the id
     * of the guest user.
     */
    @Nonnull
    public UserId getLoggedInUserId() {
        return loggedInUser.getCurrentUserId();
    }

    /**
     * Sets the logged in user.  An event will be fired (asynchronously) to indicate whether the user has logged in or out.
     * @param userInSession The user in the session that is the logged in user.
     * @throws NullPointerException if {@code userId} is {@code null}.
     */
    public void setLoggedInUser(@Nonnull final UserInSession userInSession) {
        loggedInUser.setLoggedInUser(userInSession);
    }

    /**
     * If the current user is not the guest user calling this method will log out the current user.  An event will
     * be fired (asynchronously) when the current user has been logged out.
     */
    public void logOutCurrentUser() {
        if(loggedInUser.getCurrentUserId().isGuest()) {
            return;
        }
        dispatchServiceManager.execute(new LogOutUserAction(), result -> {
            loggedInUser.setLoggedInUser(result.getUserInSession());
        });
    }

    public Set<ActionId> getLoggedInUserApplicationActions() {
        return loggedInUser.getUserInSession().getAllowedApplicationActions();
    }

    public boolean isAllowedApplicationAction(ActionId actionId) {
        return loggedInUser.isAllowedApplicationAction(actionId);
    }

    public boolean isAllowedApplicationAction(BuiltInAction action) {
        return isAllowedApplicationAction(action.getActionId());
    }

    private void restoreUserFromServerSideSession(final Optional<AsyncCallback<UserDetails>> callback) {
        dispatchServiceManager.execute(new GetCurrentUserInSessionAction(), new DispatchServiceCallback<GetCurrentUserInSessionResult>() {
            @Override
            public void handleExecutionException(Throwable cause) {
                callback.ifPresent(userDetailsAsyncCallback -> userDetailsAsyncCallback.onFailure(cause));
            }

            @Override
            public void handleSuccess(GetCurrentUserInSessionResult result) {
                loggedInUser.setLoggedInUser(result.getUserInSession());
                callback.ifPresent(userDetailsAsyncCallback -> userDetailsAsyncCallback.onSuccess(result.getUserInSession().getUserDetails()));
            }

        });
    }

    private void readUserInSession() {
        UserInSessionDecoder decoder = new UserInSessionDecoder();
        UserInSession userInSession  = ClientObjectReader.create("userInSession", decoder).read();
        loggedInUser.setLoggedInUser(userInSession);
    }
}
