package liquid.service;

import liquid.dto.EarningDto;
import liquid.metadata.ChargeWay;
import liquid.persistence.domain.*;
import liquid.persistence.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * TODO: Comments.
 * User: tao
 * Date: 10/5/13
 * Time: 2:59 PM
 */
@Service
public class ChargeService {
    private static final Logger logger = LoggerFactory.getLogger(ChargeService.class);

    @Autowired
    private ChargeRepository chargeRepository;

    @Autowired
    private ChargeTypeRepository ctRepository;

    @Autowired
    private ExchangeRateRepository exchangeRateRepository;

    @Autowired
    private LegRepository legRepository;

    @Autowired
    private ServiceProviderRepository serviceProviderRepository;

    @Autowired
    private OrderService orderService;

    @Autowired
    private RouteService routeService;

    public Iterable<Charge> getChargesByOrderId(long orderId) {
        return chargeRepository.findByOrderId(orderId);
    }

    public List<Charge> getChargesByTaskId(String taskId) {
        return chargeRepository.findByTaskId(taskId);
    }

    public Charge newCharge(long legId) {
        Charge charge = new Charge();

        Leg leg = legRepository.findOne(legId);
        charge.setSp(leg.getSp());

        return charge;
    }

    @Transactional("transactionManager")
    public Charge addCharge(Charge charge) {
        ServiceProviderEntity sp = serviceProviderRepository.findOne(charge.getSpId());
        charge.setSp(sp);

        Route route = routeService.find(charge.getFormRouteId());
        Leg leg = legRepository.findOne(charge.getFormLegId());

        OrderEntity order = null;
        if (null != route) {
            order = route.getPlanning().getOrder();
            charge.setRoute(route);
        }
        if (null != leg) {
            route = leg.getRoute();
            order = route.getPlanning().getOrder();
            charge.setLeg(leg);
        }

        charge.setOrder(order);
        ChargeType type = ctRepository.findOne(charge.getTypeId());
        charge.setType(type);

        if (ChargeWay.PER_ORDER.getValue() == charge.getWay()) {
            charge.setTotalPrice(charge.getUnitPrice());
            charge.setUnitPrice(0L);
        } else if (ChargeWay.PER_CONTAINER.getValue() == charge.getWay()) {
            charge.setTotalPrice(charge.getUnitPrice() * route.getContainerQty());
        } else {
            logger.warn("{} is out of charge way range.", charge.getWay());
        }
        if (charge.getCurrency() == 0) {
            order.setGrandTotal(order.getGrandTotal() + charge.getTotalPrice());
        } else {
            order.setGrandTotal(order.getGrandTotal() + Math.round(charge.getTotalPrice() * getExchangeRate()));
        }
        orderService.save(order);

        return chargeRepository.save(charge);
    }

    @Transactional("transactionManager")
    public Charge addCharge(Charge charge, String uid) {
        OrderEntity order = orderService.findByTaskId(charge.getTaskId());
        charge.setOrder(order);

        ServiceProviderEntity sp = serviceProviderRepository.findOne(charge.getSpId());
        charge.setSp(sp);

        ChargeType type = ctRepository.findOne(charge.getTypeId());
        charge.setType(type);

        if (ChargeWay.PER_ORDER.getValue() == charge.getWay()) {
            charge.setUnitPrice(0L);
        } else if (ChargeWay.PER_CONTAINER.getValue() == charge.getWay()) {
            charge.setTotalPrice(charge.getUnitPrice() * order.getContainerQty());
        } else {
            logger.warn("{} is out of charge way range.", charge.getWay());
        }

        order.setGrandTotal(order.getGrandTotal() + charge.getTotalPrice());
        orderService.save(order);

        charge.setCreateUser(uid);
        charge.setCreateTime(new Date());
        charge.setUpdateUser(uid);
        charge.setUpdateTime(new Date());

        return chargeRepository.save(charge);
    }

    @Transactional("transactionManager")
    public void removeCharge(long chargeId) {
        Charge charge = chargeRepository.findOne(chargeId);
        OrderEntity order = charge.getOrder();

        if (charge.getCurrency() == 0) {
            order.setGrandTotal(order.getGrandTotal() - charge.getTotalPrice());
        } else {
            order.setGrandTotal(order.getGrandTotal() - Math.round(charge.getTotalPrice() * getExchangeRate()));
        }

        orderService.save(order);

        chargeRepository.delete(chargeId);
    }

    public Iterable<Charge> findByLegId(long legId) {
        return chargeRepository.findByLegId(legId);
    }

    public Iterable<Charge> findByRouteId(long routeId) {
        return chargeRepository.findByRouteId(routeId);
    }

    public Iterable<Charge> findByTaskId(String taskId) {
        return chargeRepository.findByTaskId(taskId);
    }

    public long total(Iterable<Charge> charges) {
        long total = 0L;
        for (Charge charge : charges) {
            if (charge.getCurrency() == 0) {
                total += charge.getTotalPrice();
            } else {
                total += Math.round(charge.getTotalPrice() * getExchangeRate());
            }
        }
        return total;
    }

    public Map<Long, String> getChargeTypes() {
        Map<Long, String> cts = new TreeMap<Long, String>();
        Iterable<ChargeType> iterable = ctRepository.findAll();
        for (ChargeType chargeType : iterable) {
            cts.put(chargeType.getId(), chargeType.getName());
        }
        return cts;
    }

    public Iterable<Charge> findByOrderId(long orderId) {
        return chargeRepository.findByOrderId(orderId);
    }

    public Iterable<Charge> findByOrderNo(String orderNo) {
        return chargeRepository.findByOrderOrderNoLike("%" + orderNo + "%");
    }

    public Iterable<Charge> findBySpName(String spName) {
        return chargeRepository.findBySpNameLike("%" + spName + "%");
    }

    public void save(Charge charge) {
        if (null == charge.getType()) {
            ChargeType type = ctRepository.findOne(charge.getTypeId());
            charge.setType(type);
        }
        chargeRepository.save(charge);
    }

    public Charge find(long id) {
        Charge charge = chargeRepository.findOne(id);
        charge.setTypeId(charge.getType().getId());
        return charge;
    }

    public Iterable<Charge> findAll() {
        return chargeRepository.findAll();
    }

    public Iterable<Charge> findByOrderIdAndCreateRole(long orderId, String createRole) {
        return chargeRepository.findByOrderIdAndCreateRole(orderId, createRole);
    }

    public EarningDto calculateEarning(OrderEntity order, Iterable<Charge> charges) {
        EarningDto earning = new EarningDto();

        double exchangeRate = getExchangeRate();

        earning.setSalesPriceCny(order.getSalesPriceCny());
        earning.setDistyPrice(order.getDistyPrice());
        earning.setGrandTotal(order.getGrandTotal());
        earning.setGrossMargin(earning.getSalesPriceCny() + Math.round(earning.getSalesPriceUsd() * exchangeRate) - order.getGrandTotal());
        earning.setSalesProfit(order.getSalesPriceCny() + Math.round(earning.getSalesPriceUsd() * exchangeRate) - order.getDistyPrice());
        earning.setDistyProfit(earning.getDistyPrice() - order.getGrandTotal());
        return earning;
    }

    public double getExchangeRate() {
        ExchangeRate exchangeRate = exchangeRateRepository.findOne(1L);
        return null == exchangeRate ? 0.00 : exchangeRate.getValue();
    }

    public void setExchangeRate(double value) {
        ExchangeRate exchangeRate = new ExchangeRate();
        exchangeRate.setId(1L);
        exchangeRate.setValue(value);
        exchangeRateRepository.save(exchangeRate);
    }
}
