package liquid.accounting.service;

import liquid.accounting.domain.Purchase;
import liquid.accounting.domain.PurchaseStatement;
import liquid.accounting.domain.PurchaseStatus;
import liquid.accounting.repository.PurchaseStatementRepository;
import liquid.core.service.AbstractService;
import liquid.operation.domain.ServiceProvider;
import liquid.operation.service.ServiceProviderService;
import liquid.order.domain.Order;
import liquid.order.domain.OrderStatus;
import liquid.order.service.OrderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by mat on 7/17/16.
 */
@Service
public class PurchaseStatementServiceImpl extends AbstractService<PurchaseStatement, PurchaseStatementRepository>
        implements PurchaseStatementService  {

    private static final Logger logger = LoggerFactory.getLogger("PurchaseStatementServiceImpl");

    @Autowired
    private PurchaseStatementRepository purchaseStatementRepository;

    @Autowired
    private ServiceProviderService serviceProviderService;

    @Autowired
    private PurchaseService purchaseService;

    @Override
    public List<PurchaseStatement> findStatementByServiceProviderId(Long serviceProviderId) {
        return purchaseStatementRepository.findByServiceProviderId(serviceProviderId);
    }

    @Transactional(value = "transactionManager")
    @Override
    public PurchaseStatement save(Long serviceProviderId, Long[] purchaseIds) {
        ServiceProvider serviceProvider = serviceProviderService.find(serviceProviderId);
        List<Purchase> purchases = new ArrayList<Purchase>(purchaseIds.length);
        BigDecimal totalCny = BigDecimal.ZERO;
        BigDecimal totalUsd = BigDecimal.ZERO;
        for (int i = 0; i < purchaseIds.length; i++) {
            Purchase purchase = purchaseService.find(purchaseIds[i]);
            purchase.setStatus(PurchaseStatus.STATED);
            purchase = purchaseService.save(purchase);
            purchases.add(purchase);
            switch (purchase.getCurrency()) {
                case CNY:
                    totalCny = totalCny.add(purchase.getTotalAmount());
                    break;
                case USD:
                    totalUsd = totalCny.add(purchase.getTotalAmount());
                    break;
                default:
                    logger.warn("Illegal currency {}", purchase.getCurrency());
                    break;
            }
        }

        PurchaseStatement purchaseStatement = new PurchaseStatement();
        purchaseStatement.setServiceProvider(serviceProvider);
        purchaseStatement.setTotalCny(totalCny);
        purchaseStatement.setTotalUsd(totalUsd);
        purchaseStatement.setPurchases(purchases);
        return save(purchaseStatement);
    }

    @Transactional(value = "transactionManager")
    @Override
    public PurchaseStatement find(Long id) {
        PurchaseStatement statement = super.find(id);
        statement.getPurchases().size();
        return statement;
    }

    @Override
    public void doSaveBefore(PurchaseStatement entity) {}
}