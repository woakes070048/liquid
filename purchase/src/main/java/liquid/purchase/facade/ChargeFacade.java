package liquid.purchase.facade;

import liquid.facade.Facade;
import liquid.purchase.persistence.domain.ChargeEntity;
import liquid.purchase.web.domain.Charge;
import liquid.persistence.domain.ServiceProviderEntity;
import liquid.persistence.domain.ServiceSubtypeEntity;
import liquid.purchase.service.ChargeService;
import liquid.transport.persistence.domain.LegEntity;
import liquid.transport.persistence.domain.ShipmentEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Created by redbrick9 on 6/9/14.
 */
@Service
public class ChargeFacade implements Facade<Charge, ChargeEntity> {

    @Autowired
    private ChargeService chargeService;

    public ChargeEntity save(Charge charge) {
        ChargeEntity entity = convert(charge);
        return chargeService.save(entity);
    }

    @Override
    public ChargeEntity convert(Charge charge) {
        ChargeEntity entity = new ChargeEntity();
        entity.setId(charge.getId());
        if (null != charge.getShipmentId())
            entity.setShipment(ShipmentEntity.newInstance(ShipmentEntity.class, charge.getShipmentId()));
        if (null != charge.getLegId())
            entity.setLeg(LegEntity.newInstance(LegEntity.class, charge.getLegId()));
        entity.setServiceSubtype(ServiceSubtypeEntity.newInstance(ServiceSubtypeEntity.class, charge.getServiceSubtypeId()));
        entity.setSp(ServiceProviderEntity.newInstance(ServiceProviderEntity.class, charge.getServiceProviderId()));
        entity.setWay(charge.getWay());
        entity.setCurrency(charge.getCurrency());
        entity.setUnitPrice(charge.getUnitPrice());
        entity.setTaskId(charge.getTaskId());
        entity.setComment(charge.getComment());
        return entity;
    }

    @Override
    public Charge convert(ChargeEntity entity) {
        return null;
    }
}