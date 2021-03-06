package liquid.accounting.service;

import liquid.accounting.domain.PayableSummary;
import liquid.accounting.domain.Purchase;
import liquid.accounting.domain.PurchaseStatus;
import liquid.accounting.domain.Purchase_;
import liquid.accounting.repository.ChargeRepository;
import liquid.accounting.repository.PurchaseRepository;
import liquid.core.model.SearchBarForm;
import liquid.core.service.AbstractService;
import liquid.operation.domain.ServiceProvider_;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.domain.Specifications;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.List;

import static org.springframework.data.jpa.domain.Specifications.where;

/**
 * Created by mat on 9/27/14.
 */
@Service
public class PurchaseServiceImpl extends AbstractService<Purchase, PurchaseRepository> implements PurchaseService {

    @Autowired
    private ChargeRepository chargeRepository;

    @Autowired
    private PurchaseRepository purchaseRepository;

    @Autowired
    private PayableSummaryService payableSummaryService;

    @Override
    public void doSaveBefore(Purchase entity) {}

    @Deprecated
    @Override
    public void deleteByLegId(Long legId) {
        chargeRepository.deleteByLegId(legId);
    }

    public Purchase find(Long id) {
        return purchaseRepository.findOne(id);
    }

    @Override
    public List<Purchase> findByOrderId(Long orderId) {
        return purchaseRepository.findByOrderId(orderId);
    }

    @Override
    public List<Purchase> findByServiceProviderId(Long serviceProviderId, SearchBarForm searchBarForm) {
        Specification<Purchase> spIdSpec = new Specification<Purchase>() {
            @Override
            public Predicate toPredicate(Root<Purchase> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder builder) {
                return builder.equal(root.get(Purchase_.sp).get(ServiceProvider_.id), serviceProviderId);
            }
        };
        Specifications<Purchase> specifications = where(spIdSpec);

        Specification<Purchase> statesSpec = new Specification<Purchase>() {
            @Override
            public Predicate toPredicate(Root<Purchase> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder builder) {
                return builder.equal(root.get(Purchase_.status), PurchaseStatus.VALID);
            }
        };
        specifications = specifications.and(statesSpec);

        if (null != searchBarForm.getStartDate() && null != searchBarForm.getEndDate()) {
            Specification<Purchase> dateSpec = new Specification<Purchase>() {
                @Override
                public Predicate toPredicate(Root<Purchase> root,
                                             CriteriaQuery<?> criteriaQuery,
                                             CriteriaBuilder builder) {
                    return builder.between(root.get(Purchase_.createdAt),
                            searchBarForm.getStartDate(), searchBarForm.getEndDate());
                }
            };
            specifications = specifications.and(dateSpec);
        }

        return repository.findAll(specifications);
    }

    @Transactional("transactionManager")
    @Override
    public Purchase addOne(Purchase purchase) {
        purchase.setStatus(PurchaseStatus.VALID);
        purchase = save(purchase);

        PayableSummary payableSummary = payableSummaryService.findByServiceProviderId(purchase.getSp().getId());
        if (null == payableSummary) {
            payableSummary = new PayableSummary();
            payableSummary.setServiceProvider(purchase.getSp());
        }
        switch (purchase.getCurrency()) {
            case CNY:
                payableSummary.setTotalCny(payableSummary.getTotalCny().add(purchase.getPriceInclOfTax()));
                break;
            case USD:
                payableSummary.setTotalUsd(payableSummary.getTotalUsd().add(purchase.getTotalAmount()));
                break;
            default:
                break;
        }
        payableSummaryService.save(payableSummary);

        return purchase;
    }

    @Transactional("transactionManager")
    @Override
    public Purchase voidOne(Purchase purchase) {
        Purchase originalOne = purchaseRepository.findOne(purchase.getId());
        originalOne.setStatus(PurchaseStatus.INVALID);
        originalOne.setComment(purchase.getComment());
        originalOne = purchaseRepository.save(originalOne);

        PayableSummary payableSummary = payableSummaryService.findByServiceProviderId(originalOne.getSp().getId());
        if (null == payableSummary) {
            payableSummary = new PayableSummary();
            payableSummary.setServiceProvider(originalOne.getSp());
        }
        switch (originalOne.getCurrency()) {
            case CNY:
                payableSummary.setTotalCny(payableSummary.getTotalCny().subtract(originalOne.getPriceInclOfTax()));
                break;
            case USD:
                payableSummary.setTotalUsd(payableSummary.getTotalUsd().subtract(originalOne.getTotalAmount()));
                break;
            default:
                break;
        }
        payableSummaryService.save(payableSummary);

        return originalOne;
    }
}
