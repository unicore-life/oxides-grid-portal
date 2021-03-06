package pl.edu.icm.oxides.user;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.ModelAndView;
import pl.edu.icm.oxides.portal.security.OxidesForbiddenException;
import pl.edu.icm.oxides.portal.security.PortalAccessHelper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static pl.edu.icm.oxides.portal.security.PortalAccess.VALID;

@Service
public class OxidesUserPage {
    private final PortalAccessHelper accessHelper;

    @Autowired
    public OxidesUserPage(PortalAccessHelper accessHelper) {
        this.accessHelper = accessHelper;
    }

    public ModelAndView modelPreferencesPage(Optional<OxidesPortalGridSession> authenticationSession) {
        checkIfAccessIsValid(authenticationSession);

        ModelAndView modelAndView = new ModelAndView("preferences");
        modelAndView.addObject("commonName",
                authenticationSession
                        .map(OxidesPortalGridSession::getAttributes)
                        .map(userAttributes -> userAttributes.getCommonName())
                        .orElse("")
        );
        modelAndView.addObject("emailAddress",
                authenticationSession
                        .map(OxidesPortalGridSession::getAttributes)
                        .map(userAttributes -> userAttributes.getEmailAddress())
                        .orElse("")
        );
        modelAndView.addObject("custodianDN",
                authenticationSession
                        .map(OxidesPortalGridSession::getAttributes)
                        .map(userAttributes -> userAttributes.getCustodianDN())
                        .orElse("")
        );
        modelAndView.addObject("memberGroups",
                authenticationSession
                        .map(OxidesPortalGridSession::getAttributes)
                        .map(UserAttributes::getMemberGroups)
                        .map(memberGroups -> (List) new ArrayList<>(memberGroups))
                        .orElse(Collections.emptyList())
        );
        return modelAndView;
    }

    private void checkIfAccessIsValid(Optional<OxidesPortalGridSession> authenticationSession) {
        Boolean accessNotValid = authenticationSession
                .map(accessHelper::determineSessionAccess)
                .map(portalAccess -> portalAccess != VALID)
                .orElse(true);

        if (accessNotValid) {
            String commonName = authenticationSession
                    .map(OxidesPortalGridSession::getAttributes)
                    .map(UserAttributes::getCommonName)
                    .orElse("(anonymous)");
            log.info("Preferences page is not allowed for user: " + commonName);
            throw new OxidesForbiddenException("You are not allowed or activated!");
        }
    }

    private Log log = LogFactory.getLog(OxidesUserPage.class);
}
