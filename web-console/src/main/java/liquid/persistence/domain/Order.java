package liquid.persistence.domain;

import org.hibernate.validator.constraints.NotEmpty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.*;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.util.Collection;

/**
 * TODO: Comments.
 * User: tao
 * Date: 9/28/13
 * Time: 2:59 PM
 */
@Entity(name = "ORDER_BASE")
public class Order extends BaseEntity {
    private static final Logger logger = LoggerFactory.getLogger(Order.class);

    @Transient
    private long customerId;

    @ManyToOne
    @JoinColumn(name = "CUSTOMER_ID")
    private Customer customer;

    @ManyToOne
    @JoinColumn(name = "SRC_LOC_ID")
    private Location srcLoc;

    @Transient
    private String origination;

    @ManyToOne
    @JoinColumn(name = "DST_LOC_ID")
    private Location dstLoc;

    @Transient
    private String destination;

    @Column(name = "CONSIGNEE")
    @NotNull
    @NotEmpty
    private String consignee;

    @Transient
    private long cargoId;

    @OneToOne
    @JoinColumn(name = "CARGO_ID")
    private Cargo cargo;

    @Min(1)
    // unit kilogram
    @Column(name = "WEIGHT")
    private int cargoWeight;

    // 0: domestic; 1: foreign
    @Column(name = "TRADE_TYPE")
    private int tradeType;

    // 0: yard; 1: truck
    @Column(name = "LOADING_TYPE")
    private int loadingType;

    @Column(name = "HAS_DELIVERY")
    private boolean hasDelivery;

    @Column(name = "CONTAINER_TYPE")
    private int containerType;

    @Column(name = "CONTAINER_CAP")
    private int containerCap;

    @Min(1)
    @Column(name = "CONTAINER_QTY")
    private int containerQty;

    @Min(1)
    // unit yuan
    @Column(name = "PRICE")
    private long price;

    @Column(name = "EXT_EXP")
    private long extExp = 0L;

    @Column(name = "EXT_EXP_COMMENT")
    private String extExpComment;

    // 1 saved; 2: submitted
    @Column(name = "STATUS")
    private int status;

    public static Logger getLogger() {
        return logger;
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    public Location getSrcLoc() {
        return srcLoc;
    }

    public void setSrcLoc(Location srcLoc) {
        this.srcLoc = srcLoc;
    }

    public Location getDstLoc() {
        return dstLoc;
    }

    public void setDstLoc(Location dstLoc) {
        this.dstLoc = dstLoc;
    }

    public String getConsignee() {
        return consignee;
    }

    public void setConsignee(String consignee) {
        this.consignee = consignee;
    }

    public Cargo getCargo() {
        return cargo;
    }

    public void setCargo(Cargo cargo) {
        this.cargo = cargo;
    }

    public int getCargoWeight() {
        return cargoWeight;
    }

    public void setCargoWeight(int cargoWeight) {
        this.cargoWeight = cargoWeight;
    }

    public int getTradeType() {
        return tradeType;
    }

    public void setTradeType(int tradeType) {
        this.tradeType = tradeType;
    }

    public int getLoadingType() {
        return loadingType;
    }

    public void setLoadingType(int loadingType) {
        this.loadingType = loadingType;
    }

    public boolean isHasDelivery() {
        return hasDelivery;
    }

    public void setHasDelivery(boolean hasDelivery) {
        this.hasDelivery = hasDelivery;
    }

    public int getContainerType() {
        return containerType;
    }

    public void setContainerType(int containerType) {
        this.containerType = containerType;
    }

    public int getContainerCap() {
        return containerCap;
    }

    public void setContainerCap(int containerCap) {
        this.containerCap = containerCap;
    }

    public int getContainerQty() {
        return containerQty;
    }

    public void setContainerQty(int containerQty) {
        this.containerQty = containerQty;
    }

    public long getPrice() {
        return price;
    }

    public void setPrice(long price) {
        this.price = price;
    }

    public long getExtExp() {
        return extExp;
    }

    public void setExtExp(long extExp) {
        this.extExp = extExp;
    }

    public String getExtExpComment() {
        return extExpComment;
    }

    public void setExtExpComment(String extExpComment) {
        this.extExpComment = extExpComment;
    }

    public int getStatus() {
        return status;
    }

    // Transient

    public void setStatus(int status) {
        this.status = status;
    }

    public long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(long customerId) {
        this.customerId = customerId;
    }

    public long getCargoId() {
        return cargoId;
    }

    public void setCargoId(long cargoId) {
        this.cargoId = cargoId;
    }

    public String getOrigination() {
        return origination;
    }

    public void setOrigination(String origination) {
        this.origination = origination;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder(super.toString());
        sb.append("Order{");
        sb.append("customerId=").append(customerId);
        sb.append(", customer=").append(customer);
        sb.append(", srcLoc=").append(srcLoc);
        sb.append(", origination='").append(origination).append('\'');
        sb.append(", dstLoc=").append(dstLoc);
        sb.append(", destination='").append(destination).append('\'');
        sb.append(", consignee='").append(consignee).append('\'');
        sb.append(", cargoId=").append(cargoId);
        sb.append(", cargo=").append(cargo);
        sb.append(", cargoWeight=").append(cargoWeight);
        sb.append(", tradeType=").append(tradeType);
        sb.append(", loadingType=").append(loadingType);
        sb.append(", hasDelivery=").append(hasDelivery);
        sb.append(", containerType=").append(containerType);
        sb.append(", containerCap=").append(containerCap);
        sb.append(", containerQty=").append(containerQty);
        sb.append(", price=").append(price);
        sb.append(", extExp=").append(extExp);
        sb.append(", extExpComment='").append(extExpComment).append('\'');
        sb.append(", status=").append(status);
        sb.append('}');
        return sb.toString();
    }
}
