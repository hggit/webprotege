package edu.stanford.bmir.protege.web.client.inject;

import com.google.gwt.core.client.GWT;
import com.google.gwt.place.shared.PlaceHistoryHandler;
import edu.stanford.bmir.protege.web.client.WebProtegeInitializer;
import edu.stanford.bmir.protege.web.client.place.WebProtegeActivityManager;
import edu.stanford.bmir.protege.web.client.workspace.ApplicationPresenter;
import edu.stanford.bmir.protege.web.shared.app.ClientApplicationProperties;

/**
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 21/12/15
 */
public class WebProtegeClientInjector {


    private static WebProtegeClientInjector instance;

    private ClientApplicationComponent injector;

    public WebProtegeClientInjector() {
        injector = DaggerClientApplicationComponent.create();
    }

    public static WebProtegeClientInjector get() {
        // It is important that this is loaded lazily.  The reason for this is that a Module has to be generated
        // by the GWT code generator that sets up bindings from PortletFactory to PortletFactoryGenerated.
        if(instance == null) {
            instance = new WebProtegeClientInjector();
        }
        return instance;
    }

    public ApplicationPresenter getApplicationPresenter() {
        return getInjector().getApplicationPresenter();
    }

    public WebProtegeInitializer getWebProtegeInitializer() {
        return getInjector().getWebProtegeInitializer();
    }

    public ClientApplicationProperties getClientApplicationProperties() {
        return getInjector().getClientApplicationProperties();
    }

    public WebProtegeActivityManager getActivityManager() {
        return getInjector().getActivityManager();
    }

    public PlaceHistoryHandler getPlaceHistoryHandler() {
        return getInjector().getPlaceHistoryHandler();
    }

    private ClientApplicationComponent getInjector() {
        return injector;
    }
}
