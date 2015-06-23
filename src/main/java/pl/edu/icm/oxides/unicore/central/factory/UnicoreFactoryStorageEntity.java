package pl.edu.icm.oxides.unicore.central.factory;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.w3.x2005.x08.addressing.EndpointReferenceType;

import java.io.Serializable;

public class UnicoreFactoryStorageEntity implements Serializable {
    private final String uri;

    public UnicoreFactoryStorageEntity(EndpointReferenceType uri) {
        this.uri = uri.getAddress().getStringValue();
    }

    public String getUri() {
        return uri;
    }

    @JsonIgnore
    public EndpointReferenceType getEpr() {
        EndpointReferenceType epr = EndpointReferenceType.Factory.newInstance();
        epr.addNewAddress().setStringValue(this.uri);
        return epr;
    }
}
