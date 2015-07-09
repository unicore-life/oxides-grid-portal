package pl.edu.icm.oxides;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.servlet.ModelAndView;
import pl.edu.icm.oxides.authn.SamlAuthenticationHandler;
import pl.edu.icm.oxides.simulation.OxidesSimulationsPage;
import pl.edu.icm.oxides.unicore.UnicoreGridHandler;
import pl.edu.icm.oxides.user.AuthenticationSession;
import pl.edu.icm.oxides.user.OxidesUsersPage;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.List;
import java.util.UUID;

import static java.util.Optional.ofNullable;

@Controller
@SessionAttributes("authenticationSession")
@RequestMapping(value = "/oxides")
public class OxidesController {
    private final SamlAuthenticationHandler samlAuthenticationHandler;
    private final UnicoreGridHandler unicoreGridHandler;
    private final OxidesWelcomePage oxidesWelcomePage;
    private final OxidesSimulationsPage oxidesSimulationsPage;
    private final OxidesUsersPage oxidesUsersPage;
    private AuthenticationSession authenticationSession;

    @Autowired
    public OxidesController(SamlAuthenticationHandler samlAuthenticationHandler,
                            UnicoreGridHandler unicoreGridHandler,
                            OxidesWelcomePage oxidesWelcomePage,
                            OxidesSimulationsPage oxidesSimulationsPage,
                            OxidesUsersPage oxidesUsersPage,
                            AuthenticationSession authenticationSession) {
        this.samlAuthenticationHandler = samlAuthenticationHandler;
        this.unicoreGridHandler = unicoreGridHandler;
        this.oxidesWelcomePage = oxidesWelcomePage;
        this.oxidesSimulationsPage = oxidesSimulationsPage;
        this.oxidesUsersPage = oxidesUsersPage;
        this.authenticationSession = authenticationSession;
    }

    @RequestMapping(value = {"", "/"}, method = RequestMethod.GET)
    public ModelAndView welcomePage() {
        return oxidesWelcomePage.modelWelcomePage(ofNullable(authenticationSession));
    }

    @RequestMapping(value = "/simulations", method = RequestMethod.GET)
    public ModelAndView simulationsPage() {
        return oxidesSimulationsPage.modelSimulationsPage(authenticationSession);
    }

    @RequestMapping(value = "/simulations/{uuid}", method = RequestMethod.GET)
    public ModelAndView oneSimulationPage(@PathVariable("uuid") UUID simulationUuid) {
        return oxidesSimulationsPage.modelOneSimulationPage(authenticationSession, simulationUuid);
    }

    @RequestMapping(value = "/simulations/submit", method = RequestMethod.GET)
    public ModelAndView submitSimulationPage() {
        return oxidesSimulationsPage.modelSubmitSimulationPage(authenticationSession);
    }

    @RequestMapping(value = "/preferences", method = RequestMethod.GET)
    public ModelAndView preferencesPage() {
        return oxidesUsersPage.modelPreferencesPage(ofNullable(authenticationSession));
    }

    @RequestMapping(value = "/logout", method = RequestMethod.GET)
    public String signOut(HttpSession session) {
        return oxidesUsersPage.signOutAndRedirect(session);
    }

    /*
    ==========================================================================================================
     */

    @RequestMapping(value = "/unicore-sites", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<List> listSites(HttpSession session) {
        logSessionData("SITES", session, authenticationSession);
        return unicoreGridHandler.listUserSites(authenticationSession);
    }

    @RequestMapping(value = "/unicore-storages", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<List> listStorages(HttpSession session) {
        logSessionData("STORAGES", session, authenticationSession);
        return unicoreGridHandler.listUserStorages(authenticationSession);
    }

    @RequestMapping(value = "/unicore-jobs", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<List> listJobs(HttpSession session) {
        logSessionData("JOBS", session, authenticationSession);
        return unicoreGridHandler.listUserJobs(authenticationSession);
    }

    @RequestMapping(value = "/unicore-resources", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<List> listResources(HttpSession session) {
        logSessionData("RESOURCES", session, authenticationSession);
        return unicoreGridHandler.listUserResources(authenticationSession);
    }

    /*
    ==========================================================================================================
     */

    @RequestMapping(value = "/authn", method = RequestMethod.GET)
    public void performAuthenticationRequest(HttpSession session, HttpServletResponse response,
                                             @RequestParam(value = "returnUrl", required = false) String returnUrl) {
        if (authenticationSession.getReturnUrl() == null && returnUrl != null) {
            authenticationSession.setReturnUrl(returnUrl);
        }
        logSessionData("SAML-G", session, authenticationSession);
        samlAuthenticationHandler.performAuthenticationRequest(response, authenticationSession);
    }

    @RequestMapping(value = "/authn", method = RequestMethod.POST)
    public void processAuthenticationResponse(HttpServletRequest request, HttpServletResponse response) {
        logSessionData("SAML-P", request.getSession(), authenticationSession);
        samlAuthenticationHandler.processAuthenticationResponse(request, response, authenticationSession);
    }

    private void logSessionData(String logPrefix, HttpSession session, AuthenticationSession authnSession) {
        log.info(String.format("%10s: %s", logPrefix, session.getId()));
        log.info(String.format("%10s: %s", logPrefix, authnSession));
    }

    private Log log = LogFactory.getLog(OxidesController.class);
}
