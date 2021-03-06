package liquid.transport.model;

import liquid.operation.domain.ServiceProvider;
import liquid.order.domain.Order;
import liquid.transport.domain.LegEntity;
import liquid.transport.domain.RailContainer;
import liquid.transport.domain.ShipmentEntity;
import liquid.util.DateUtil;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by redbrick9 on 9/11/14.
 */
public class RailTransport extends TransportBase {
    private Long id;

    public Long orderId;

    private Long shipmentId;

    private Long legId;

    private Long fleetId;

    private String trucker;

    private String plateNo;

    private String loadedAt;

    private String arrivedStationAt;

    private String planNo;

    private String plannedShippedAt;

    private String actualShippedAt;

    public static RailContainer toEntity(RailTransport transport) {
        RailContainer entity = new RailContainer();
        entity.setId(transport.getId());
        entity.setOrder(Order.newInstance(Order.class, transport.getOrderId()));
        entity.setShipment(ShipmentEntity.newInstance(ShipmentEntity.class, transport.getShipmentId()));
        entity.setLeg(LegEntity.newInstance(LegEntity.class, transport.getLegId()));
        entity.setFleet(ServiceProvider.newInstance(ServiceProvider.class, transport.getFleetId()));
        entity.setTrucker(transport.getTrucker());
        entity.setPlateNo(transport.getPlateNo());
        entity.setLoadingToc(DateUtil.dateOf(transport.getLoadedAt()));
        entity.setStationToa(DateUtil.dateOf(transport.getArrivedStationAt()));
        entity.setTransPlanNo(transport.getPlanNo());
        entity.setEts(DateUtil.dateOf(transport.getPlannedShippedAt()));
        entity.setAts(DateUtil.dateOf(transport.getActualShippedAt()));
        return entity;
    }

    public RailContainer toEntity() {
        return toEntity(this);
    }

    public static Collection<RailContainer> toEntities(RailTransport[] transportSet) {
        List<RailContainer> entities = new ArrayList<RailContainer>();
        for (RailTransport transport : transportSet) {
            RailContainer entity = toEntity(transport);
            entities.add(entity);
        }
        return entities;
    }

    public static RailTransport valueOf(RailContainer entity) {
        RailTransport value = new RailTransport();
        value.setId(entity.getId());
        value.setOrderId(entity.getOrder().getId());
        value.setShipmentId(entity.getShipment().getId());
        value.setLegId(entity.getLeg().getId());
        value.setSource(entity.getLeg().getSrcLoc().getName());
        value.setDestination(entity.getLeg().getDstLoc().getName());
        value.setFleetId(entity.getFleet().getId());
        value.setTrucker(entity.getTrucker());
        value.setPlateNo(entity.getPlateNo());
        value.setLoadedAt(DateUtil.stringOf(entity.getLoadingToc()));
        value.setArrivedStationAt(DateUtil.stringOf(entity.getStationToa()));
        value.setPlanNo(entity.getTransPlanNo());
        value.setPlannedShippedAt(DateUtil.stringOf(entity.getEts()));
        value.setActualShippedAt(DateUtil.stringOf(entity.getAts()));
        return value;
    }

    public static RailTransport[] valueOf(Collection<RailContainer> entities) {
        RailTransport[] railTransport = new RailTransport[entities.size()];
        int i = 0;
        for (RailContainer entity : entities) {
            railTransport[i] = RailTransport.valueOf(entity);
            i++;
        }
        return railTransport;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public Long getShipmentId() {
        return shipmentId;
    }

    public void setShipmentId(Long shipmentId) {
        this.shipmentId = shipmentId;
    }

    public Long getLegId() {
        return legId;
    }

    public void setLegId(Long legId) {
        this.legId = legId;
    }

    public Long getFleetId() {
        return fleetId;
    }

    public void setFleetId(Long fleetId) {
        this.fleetId = fleetId;
    }

    public String getTrucker() {
        return trucker;
    }

    public void setTrucker(String trucker) {
        this.trucker = trucker;
    }

    public String getPlateNo() {
        return plateNo;
    }

    public void setPlateNo(String plateNo) {
        this.plateNo = plateNo;
    }

    public String getLoadedAt() {
        return loadedAt;
    }

    public void setLoadedAt(String loadedAt) {
        this.loadedAt = loadedAt;
    }

    public String getArrivedStationAt() {
        return arrivedStationAt;
    }

    public void setArrivedStationAt(String arrivedStationAt) {
        this.arrivedStationAt = arrivedStationAt;
    }

    public String getPlanNo() {
        return planNo;
    }

    public void setPlanNo(String planNo) {
        this.planNo = planNo;
    }

    public String getPlannedShippedAt() {
        return plannedShippedAt;
    }

    public void setPlannedShippedAt(String plannedShippedAt) {
        this.plannedShippedAt = plannedShippedAt;
    }

    public String getActualShippedAt() {
        return actualShippedAt;
    }

    public void setActualShippedAt(String actualShippedAt) {
        this.actualShippedAt = actualShippedAt;
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("RailTransport{");
        sb.append("shipmentId=").append(shipmentId);
        sb.append(", fleetId=").append(fleetId);
        sb.append(", trucker='").append(trucker).append('\'');
        sb.append(", plateNo='").append(plateNo).append('\'');
        sb.append(", loadedAt='").append(loadedAt).append('\'');
        sb.append(", arrivedStationAt='").append(arrivedStationAt).append('\'');
        sb.append(", planNo='").append(planNo).append('\'');
        sb.append(", plannedShippedAt='").append(plannedShippedAt).append('\'');
        sb.append(", actualShippedAt='").append(actualShippedAt).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
