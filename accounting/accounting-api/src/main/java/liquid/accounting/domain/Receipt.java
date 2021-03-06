package liquid.accounting.domain;

import liquid.core.domain.BaseUpdateEntity;
import liquid.operation.domain.Customer;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.Date;

/**
 * Created by Tao Ma on 1/8/15.
 */
@Entity(name = "ACC_RECEIPT")
public class Receipt extends BaseUpdateEntity {
    @NotNull
    @ManyToOne
    @JoinColumn(name = "CUSTOMER_ID")
    private Customer customer;

    @Column(precision = 19, scale = 4, name = "AMOUNT_CNY")
    private BigDecimal amountCny;

    @Column(precision = 19, scale = 4, name = "AMOUNT_USD")
    private BigDecimal amountUsd;

    @Column(name = "RECIPIENT_ID")
    private Long recipientId;

    @Column(name = "RECEIVED_AT")
    private Date receivedAt;

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    public BigDecimal getAmountCny() {
        return amountCny;
    }

    public void setAmountCny(BigDecimal amountCny) {
        this.amountCny = amountCny;
    }

    public BigDecimal getAmountUsd() {
        return amountUsd;
    }

    public void setAmountUsd(BigDecimal amountUsd) {
        this.amountUsd = amountUsd;
    }

    public Long getRecipientId() {
        return recipientId;
    }

    public void setRecipientId(Long recipientId) {
        this.recipientId = recipientId;
    }

    public Date getReceivedAt() {
        return receivedAt;
    }

    public void setReceivedAt(Date receivedAt) {
        this.receivedAt = receivedAt;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Receipt{");
        sb.append("super=").append(super.toString()).append('\'');
        sb.append(", customer=").append(customer);
        sb.append(", amountCny=").append(amountCny);
        sb.append(", amountUsd=").append(amountUsd);
        sb.append(", recipientId=").append(recipientId);
        sb.append(", receivedAt=").append(receivedAt);
        sb.append('}');
        return sb.toString();
    }
}
