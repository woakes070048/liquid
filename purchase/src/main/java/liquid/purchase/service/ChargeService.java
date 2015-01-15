package liquid.purchase.service;

import liquid.order.persistence.domain.OrderEntity;
import liquid.order.persistence.domain.OrderEntity_;
import liquid.order.service.OrderService;
import liquid.persistence.domain.ExchangeRateEntity;
import liquid.persistence.domain.ServiceProviderEntity_;
import liquid.persistence.repository.ServiceProviderRepository;
import liquid.purchase.persistence.domain.ChargeEntity;
import liquid.purchase.persistence.domain.ChargeEntity_;
import liquid.purchase.persistence.repository.ChargeRepository;
import liquid.purchase.web.domain.ChargeWay;
import liquid.security.SecurityContext;
import liquid.service.AbstractService;
import liquid.service.ExchangeRateService;
import liquid.transport.persistence.domain.LegEntity;
import liquid.transport.persistence.domain.ShipmentEntity;
import liquid.transport.persistence.repository.LegRepository;
import liquid.transport.service.ShipmentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.domain.Specifications;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.List;

import static org.springframework.data.jpa.domain.Specifications.where;

/**
 * TODO: Comments.
 * User: tao
 * Date: 10/5/13
 * Time: 2:59 PM
 */
@Service
public class ChargeService extends AbstractService<ChargeEntity, ChargeRepository> {
    private static final Logger logger = LoggerFactory.getLogger(ChargeService.class);

    @Autowired
    private ChargeRepository chargeRepository;

    @Autowired
    private ExchangeRateService exchangeRateService;

    @Autowired
    private LegRepository legRepository;

    @Autowired
    private ServiceProviderRepository serviceProviderRepository;

    @Autowired
    private OrderService orderService;

    @Autowired
    private ShipmentService shipmentService;

    // TODO: have to enhance this function.
    @Override
    public void doSaveBefore(ChargeEntity entity) {
        // update charge
        if (null != entity.getId()) return;

        // new charge
        ShipmentEntity shipment = null;
        if (null != entity.getShipment()) {
            shipment = shipmentService.find(entity.getShipment().getId());
            entity.setOrder(shipment.getOrder());
        }
        if (null != entity.getLeg()) {
            LegEntity leg = legRepository.findOne(entity.getLeg().getId());
            shipment = leg.getShipment();
            entity.setOrder(shipment.getOrder());
        }
//        if (null == entity.getOrder()) {
//            OrderEntity order = taskService.findOrderByTaskId(entity.getTaskId());
//            entity.setOrder(order);
//        }

        if (ChargeWay.PER_ORDER.getValue() == entity.getWay()) {
            entity.setTotalPrice(entity.getUnitPrice());
            entity.setUnitPrice(0L);
        } else if (ChargeWay.PER_CONTAINER.getValue() == entity.getWay()) {
            entity.setTotalPrice(entity.getUnitPrice() * shipment.getContainerQty());
        } else {
            logger.warn("{} is out of charge way range.", entity.getWay());
        }

        entity.setCreateRole(SecurityContext.getInstance().getRole());
        // Compute grand total
        OrderEntity order = orderService.find(entity.getOrder().getId());
        if (entity.getCurrency() == 0) {
            order.setGrandTotal(order.getGrandTotal() + entity.getTotalPrice());
        } else {
            order.setGrandTotal(order.getGrandTotal() + Math.round(entity.getTotalPrice() * exchangeRateService.getExchangeRate()));
        }
        orderService.save(order);

    }

    public Iterable<ChargeEntity> getChargesByOrderId(long orderId) {
        return chargeRepository.findByOrderId(orderId);
    }

    @Transactional("transactionManager")
    public void removeCharge(long chargeId) {
        ChargeEntity charge = chargeRepository.findOne(chargeId);
        OrderEntity order = charge.getOrder();

        if (charge.getCurrency() == 0) {
            order.setGrandTotal(order.getGrandTotal() - charge.getTotalPrice());
        } else {
            order.setGrandTotal(order.getGrandTotal() - Math.round(charge.getTotalPrice() * exchangeRateService.getExchangeRate()));
        }

        orderService.save(order);

        chargeRepository.delete(chargeId);
    }

    public Iterable<ChargeEntity> findByLegId(long legId) {
        return chargeRepository.findByLegId(legId);
    }

    public Iterable<ChargeEntity> findByShipmentId(long shipmentId) {
        return chargeRepository.findByShipmentId(shipmentId);
    }

    public Iterable<ChargeEntity> findByTaskId(String taskId) {
        return chargeRepository.findByTaskId(taskId);
    }

    public long total(Iterable<ChargeEntity> charges) {
        long total = 0L;
        for (ChargeEntity charge : charges) {
            if (charge.getCurrency() == 0) {
                total += charge.getTotalPrice();
            } else {
                total += Math.round(charge.getTotalPrice() * exchangeRateService.getExchangeRate());
            }
        }
        return total;
    }

    public Iterable<ChargeEntity> findByOrderId(long orderId) {
        return chargeRepository.findByOrderId(orderId);
    }

    public Page<ChargeEntity> findByOrderId(long orderId, Pageable pageable) {
        return chargeRepository.findByOrderId(orderId, pageable);
    }

    public Iterable<ChargeEntity> findByOrderNo(String orderNo) {
        return chargeRepository.findByOrderOrderNoLike("%" + orderNo + "%");
    }

    public Iterable<ChargeEntity> findBySpName(String spName) {
        return chargeRepository.findBySpNameLike("%" + spName + "%");
    }

    public ChargeEntity find(long id) {
        return chargeRepository.findOne(id);
    }

    public Iterable<ChargeEntity> findAll() {
        return chargeRepository.findAll();
    }

    public Page<ChargeEntity> findAll(Pageable pageable) {
        return chargeRepository.findAll(pageable);
    }

    public Page<ChargeEntity> findAll(final String orderNo, final String spName, final Pageable pageable) {
        List<Specification<ChargeEntity>> specList = new ArrayList<>();

        if (null != orderNo) {
            Specification<ChargeEntity> orderNoSpec = new Specification<ChargeEntity>() {
                @Override
                public Predicate toPredicate(Root<ChargeEntity> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder builder) {
                    return builder.like(root.get(ChargeEntity_.order).get(OrderEntity_.orderNo), "%" + orderNo + "%");
                }
            };
            specList.add(orderNoSpec);
        }


        if (null != spName) {
            Specification<ChargeEntity> spNameSpec = new Specification<ChargeEntity>() {
                @Override
                public Predicate toPredicate(Root<ChargeEntity> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder builder) {
                    return builder.like(root.get(ChargeEntity_.sp).get(ServiceProviderEntity_.name), "%" + spName + "%");
                }
            };
            specList.add(spNameSpec);
        }

        if (specList.size() > 0) {
            Specifications<ChargeEntity> specifications = where(specList.get(0));
            for (int i = 1; i < specList.size(); i++) {
                specifications.and(specList.get(i));
            }
            return repository.findAll(specifications, pageable);
        }

        return repository.findAll(pageable);
    }

    public Iterable<ChargeEntity> findByOrderIdAndCreateRole(long orderId, String createRole) {
        return chargeRepository.findByOrderIdAndCreateRole(orderId, createRole);
    }

    public void setExchangeRate(double value) {
        ExchangeRateEntity exchangeRate = new ExchangeRateEntity();
        exchangeRate.setId(1L);
        exchangeRate.setValue(value);
        exchangeRateService.save(exchangeRate);
    }
}