package liquid.order.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import liquid.core.domain.BaseIdEntity;
import liquid.operation.domain.ServiceSubtype;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import java.util.UUID;

/**
 * Created by redbrick9 on 5/7/14.
 */
@Entity(name = "ORD_SERVICE_ITEM")
public class ServiceItem extends BaseIdEntity {
    @Column
    private String uuid;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "ORDER_ID")
    private Order order;

    @ManyToOne
    @JoinColumn(name = "SERVICE_SUBTYPE_ID")
    private ServiceSubtype serviceSubtype;

    @Column(name = "CURRENCY")
    private Integer currency;

    @Column(name = "QUOTATION")
    private Long quotation;

    @Column(name = "COMMENT")
    private String comment;

    public ServiceItem() {
        this.uuid = UUID.randomUUID().toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ServiceItem that = (ServiceItem) o;

        if (!uuid.equals(that.uuid)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return uuid.hashCode();
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public Order getOrder() {
        return order;
    }

    public void setOrder(Order order) {
        this.order = order;
    }

    public ServiceSubtype getServiceSubtype() {
        return serviceSubtype;
    }

    public void setServiceSubtype(ServiceSubtype serviceSubtype) {
        this.serviceSubtype = serviceSubtype;
    }

    public Integer getCurrency() {
        return currency;
    }

    public void setCurrency(Integer currency) {
        this.currency = currency;
    }

    public Long getQuotation() {
        return quotation;
    }

    public void setQuotation(Long quotation) {
        this.quotation = quotation;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }
}