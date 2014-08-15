/*
 * Created by robm on 06/08/2014.
 */

import org.bladerunnerjs.model.App;
import org.bladerunnerjs.model.Aspect;
import org.bladerunnerjs.testing.specutility.engine.SpecTest;
import org.junit.Before;
import org.junit.Test;

public class AppManifestPluginTest extends SpecTest {
    private App app;
    private Aspect aspect;
    private StringBuffer response;

    @Before
    public void initTestObjects() throws Exception {
        given(brjs).hasContentPlugins(new AppManifestPlugin()).and(brjs).hasBeenCreated();
        app = brjs.app("app");
        aspect = app.aspect("default");
        response = new StringBuffer();
    }

    @Test
    public void doesPluginCreateManifestCorrectly() throws Exception {
        given(app).hasBeenCreated().and(aspect).hasBeenCreated();
        when(aspect).requestReceivedInDev("manifest.webapp", response);
        then(response).containsText("\"name\":\"app\"");

        when(aspect).requestReceivedInDev("manifest.webapp", response);
        then(response).containsText("\"default_locale\":\"en\"");

        when(aspect).requestReceivedInDev("manifest.webapp", response);
        then(response).containsText("\"icons\":[{\"128\":\"/img/icon-128.png\"}]");
    }
}