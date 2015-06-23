package pl.edu.icm.oxides.unicore.central.factory;

import de.fzj.unicore.uas.StorageFactory;
import de.fzj.unicore.wsrflite.xmlbeans.client.RegistryClient;
import eu.unicore.util.httpclient.IClientConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Repository;
import org.w3.x2005.x08.addressing.EndpointReferenceType;
import pl.edu.icm.oxides.authn.AuthenticationSession;
import pl.edu.icm.oxides.config.GridConfig;
import pl.edu.icm.oxides.unicore.GridClientHelper;

import java.util.List;
import java.util.stream.Collectors;

@Repository
public class UnicoreFactoryStorage {
    private final GridConfig gridConfig;
    private final GridClientHelper clientHelper;

    @Autowired
    public UnicoreFactoryStorage(GridConfig gridConfig, GridClientHelper clientHelper) {
        this.gridConfig = gridConfig;
        this.clientHelper = clientHelper;
    }

    @Cacheable(value = "unicoreSessionFactoryStorageList", key = "#authenticationSession.uuid")
    public List<UnicoreFactoryStorageEntity> retrieveServiceList(AuthenticationSession authenticationSession) {
        IClientConfiguration clientConfiguration = clientHelper.createClientConfiguration(authenticationSession);
        return collectFactoryStorageServiceList(clientConfiguration);
    }

    private List<UnicoreFactoryStorageEntity> collectFactoryStorageServiceList(IClientConfiguration clientConfiguration) {
        String registryUrl = gridConfig.getRegistry();
        EndpointReferenceType registryEpr = EndpointReferenceType.Factory.newInstance();
        registryEpr.addNewAddress().setStringValue(registryUrl);
        try {
            return new RegistryClient(registryEpr, clientConfiguration)
                    .listAccessibleServices(StorageFactory.SMF_PORT)
                    .parallelStream()
                    .map(endpointReferenceType -> new UnicoreFactoryStorageEntity(endpointReferenceType))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error(String.format("Error retrieving Storage Factory from UNICORE Registry <%s>!", registryUrl), e);
            // TODO: should be used RuntimeException?
            throw new UnavailableFactoryStorageException(e);
        }
    }

    private Log log = LogFactory.getLog(UnicoreFactoryStorage.class);
}
