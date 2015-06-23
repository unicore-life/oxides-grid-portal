package pl.edu.icm.oxides;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.SessionAttributes;
import pl.edu.icm.oxides.authn.AuthenticationSession;
import pl.edu.icm.oxides.authn.SamlRequestHandler;
import pl.edu.icm.oxides.authn.SamlResponseHandler;
import pl.edu.icm.oxides.unicore.UnicoreGridHandler;
import pl.edu.icm.oxides.unicore.central.tss.UnicoreSiteEntity;
import pl.edu.icm.oxides.unicore.site.job.UnicoreJobEntity;
import pl.edu.icm.oxides.unicore.site.resource.UnicoreResourceEntity;
import pl.edu.icm.oxides.unicore.site.storage.UnicoreSiteStorageEntity;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.List;

@Controller
@SessionAttributes("authenticationSession")
@RequestMapping(value = "/oxides")
public class OxidesController {
    private final SamlRequestHandler samlRequestHandler;
    private final SamlResponseHandler samlResponseHandler;
    private final UnicoreGridHandler unicoreGridHandler;
    private AuthenticationSession authenticationSession;

    @Autowired
    public OxidesController(SamlRequestHandler samlRequestHandler, SamlResponseHandler samlResponseHandler,
                            UnicoreGridHandler unicoreGridHandler, AuthenticationSession authenticationSession) {
        this.samlRequestHandler = samlRequestHandler;
        this.samlResponseHandler = samlResponseHandler;
        this.unicoreGridHandler = unicoreGridHandler;
        this.authenticationSession = authenticationSession;
    }

    @RequestMapping(value = "/")
    public void mainView(HttpSession session, HttpServletResponse response) throws IOException {
        if (authenticationSession.getReturnUrl() == null) {
            authenticationSession.setReturnUrl("/oxides/final");
        }
        logSessionData("TEST-0", session, authenticationSession);
        response.sendRedirect("/oxides/authn");
    }

    @RequestMapping(value = "/final")
    @ResponseBody
    public String finalPage(HttpSession session, HttpServletResponse response) throws IOException {
        logSessionData("TEST-F", session, authenticationSession);
        return authenticationSession.toString() + "<p><a href=\"/oxides/linked\">link</a></p>";
    }

    @RequestMapping(value = "/linked")
    @ResponseBody
    public String linkedPage(HttpSession session, HttpServletResponse response) throws IOException {
        logSessionData("LINKED", session, authenticationSession);
        return authenticationSession.toString();
    }

    @RequestMapping(value = "/unicore-sites")
    @ResponseBody
    public List<UnicoreSiteEntity> listSites(HttpSession session, HttpServletResponse response) {
        logSessionData("SITES", session, authenticationSession);
        return unicoreGridHandler.listUserSites(authenticationSession, response);
    }

    @RequestMapping(value = "/unicore-storages")
    @ResponseBody
    public List<UnicoreSiteStorageEntity> listStorages(HttpSession session, HttpServletResponse response) {
        logSessionData("STORAGES", session, authenticationSession);
        return unicoreGridHandler.listUserStorages(authenticationSession, response);
    }

    @RequestMapping(value = "/unicore-jobs")
    @ResponseBody
    public List<UnicoreJobEntity> listJobs(HttpSession session, HttpServletResponse response) {
        logSessionData("JOBS", session, authenticationSession);
        return unicoreGridHandler.listUserJobs(authenticationSession, response);
    }

    @RequestMapping(value = "/unicore-resources")
    @ResponseBody
    public List<UnicoreResourceEntity> listResources(HttpSession session, HttpServletResponse response) {
        logSessionData("RESOURCES", session, authenticationSession);
        return unicoreGridHandler.listUserResources(authenticationSession, response);
    }

    @RequestMapping(value = "/authn", method = RequestMethod.GET)
    public void performAuthenticationRequest(HttpSession session, HttpServletResponse response) {
        logSessionData("SAML-G", session, authenticationSession);
        samlRequestHandler.performAuthenticationRequest(response, authenticationSession);
    }

    @RequestMapping(value = "/authn", method = RequestMethod.POST)
    public void processAuthenticationResponse(HttpServletRequest request, HttpServletResponse response) {
        logSessionData("SAML-P", request.getSession(), authenticationSession);
        samlResponseHandler.processAuthenticationResponse(request, response, authenticationSession);
    }

    private void logSessionData(String logPrefix, HttpSession session, AuthenticationSession authnSession) {
        log.info(String.format("%10s: %s", logPrefix, session.getId()));
        log.info(String.format("%10s: %s", logPrefix, authnSession));
    }

    private Log log = LogFactory.getLog(OxidesController.class);
}
