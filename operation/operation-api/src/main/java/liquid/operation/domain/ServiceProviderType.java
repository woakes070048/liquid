package liquid.operation.domain;

import liquid.core.converter.Text;
import liquid.core.domain.BaseUpdateEntity;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.validator.constraints.NotEmpty;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.validation.constraints.NotNull;

/**
 * Service Provider Type which is major business of the service provider.
 * User: tao
 * Date: 10/25/13
 * Time: 7:31 PM
 */
@Deprecated
@Entity(name = "OPS_SERVICE_PROVIDER_TYPE")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class ServiceProviderType extends BaseUpdateEntity implements Text {
    @NotNull
    @NotEmpty
    @Column(name = "NAME")
    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toText() {
        return String.valueOf(id);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ServiceProviderType that = (ServiceProviderType) o;

        return !(id != null ? !id.equals(that.id) : that.id != null);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}
