package liquid.operation.service;

import liquid.operation.domain.TaxRate;

import java.util.Collection;

/**
 * Created by Tao Ma on 4/10/15.
 */
public interface TaxRateService {
    TaxRate find(Long id);

    Collection<TaxRate> findAll();

    Collection<TaxRate> findEnabled();

    TaxRate save(TaxRate taxRate);

    TaxRate enable(Long id);

    TaxRate disable(Long id);
}
