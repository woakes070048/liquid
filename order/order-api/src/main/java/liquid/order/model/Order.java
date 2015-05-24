package liquid.order.model;

import liquid.operation.domain.Goods;
import liquid.operation.domain.Location;
import liquid.operation.domain.RailPlanType;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by redbrick9 on 5/27/14.
 */
public class Order extends BaseOrder {
    private Integer tradeType = 0;
    private String verificationSheetSn;
    private String tradeTypeName;

    private Integer loadingType = 0;
    private String loadingTypeName;
    private String loadingAddress;
    private String loadingContact;
    private String loadingPhone;
    private String loadingEstimatedTime;

    private Long railwayId;
    private String planReportTime;
    private RailPlanType railPlanType;
    private Goods planGoods;
    private String programNo;

    @NotNull
    private Location railSource;

    @NotNull
    private Location railDestination;

    private String comment;
    private Boolean sameDay;

    @Valid
    private List<ServiceItem> serviceItems;

    private BigDecimal distyCny = BigDecimal.ZERO;
    private BigDecimal distyUsd = BigDecimal.ZERO;
    private BigDecimal grandTotal = BigDecimal.ZERO;

    public Order() {
        serviceItems = new ArrayList<>();
    }

    public Integer getTradeType() {
        return tradeType;
    }

    public void setTradeType(Integer tradeType) {
        this.tradeType = tradeType;
    }

    public String getTradeTypeName() {
        return tradeTypeName;
    }

    public void setTradeTypeName(String tradeTypeName) {
        this.tradeTypeName = tradeTypeName;
    }

    public String getVerificationSheetSn() {
        return verificationSheetSn;
    }

    public void setVerificationSheetSn(String verificationSheetSn) {
        this.verificationSheetSn = verificationSheetSn;
    }

    public Integer getLoadingType() {
        return loadingType;
    }

    public void setLoadingType(Integer loadingType) {
        this.loadingType = loadingType;
    }

    public String getLoadingTypeName() {
        return loadingTypeName;
    }

    public void setLoadingTypeName(String loadingTypeName) {
        this.loadingTypeName = loadingTypeName;
    }

    public String getLoadingAddress() {
        return loadingAddress;
    }

    public void setLoadingAddress(String loadingAddress) {
        this.loadingAddress = loadingAddress;
    }

    public String getLoadingContact() {
        return loadingContact;
    }

    public void setLoadingContact(String loadingContact) {
        this.loadingContact = loadingContact;
    }

    public String getLoadingPhone() {
        return loadingPhone;
    }

    public void setLoadingPhone(String loadingPhone) {
        this.loadingPhone = loadingPhone;
    }

    public String getLoadingEstimatedTime() {
        return loadingEstimatedTime;
    }

    public void setLoadingEstimatedTime(String loadingEstimatedTime) {
        this.loadingEstimatedTime = loadingEstimatedTime;
    }

    public Long getRailwayId() {
        return railwayId;
    }

    public void setRailwayId(Long railwayId) {
        this.railwayId = railwayId;
    }

    public String getPlanReportTime() {
        return planReportTime;
    }

    public void setPlanReportTime(String planReportTime) {
        this.planReportTime = planReportTime;
    }

    public RailPlanType getRailPlanType() {
        return railPlanType;
    }

    public void setRailPlanType(RailPlanType railPlanType) {
        this.railPlanType = railPlanType;
    }

    public String getProgramNo() {
        return programNo;
    }

    public void setProgramNo(String programNo) {
        this.programNo = programNo;
    }

    public Goods getPlanGoods() {
        return planGoods;
    }

    public void setPlanGoods(Goods planGoods) {
        this.planGoods = planGoods;
    }

    public Location getRailSource() {
        return railSource;
    }

    public void setRailSource(Location railSource) {
        this.railSource = railSource;
    }

    public Location getRailDestination() {
        return railDestination;
    }

    public void setRailDestination(Location railDestination) {
        this.railDestination = railDestination;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public Boolean getSameDay() {
        return sameDay;
    }

    public void setSameDay(Boolean sameDay) {
        this.sameDay = sameDay;
    }

    public List<ServiceItem> getServiceItems() {
        return serviceItems;
    }

    public void setServiceItems(List<ServiceItem> serviceItems) {
        this.serviceItems = serviceItems;
    }

    public BigDecimal getDistyCny() {
        return distyCny;
    }

    public void setDistyCny(BigDecimal distyCny) {
        this.distyCny = distyCny;
    }

    public BigDecimal getDistyUsd() {
        return distyUsd;
    }

    public void setDistyUsd(BigDecimal distyUsd) {
        this.distyUsd = distyUsd;
    }

    public BigDecimal getGrandTotal() {
        return grandTotal;
    }

    public void setGrandTotal(BigDecimal grandTotal) {
        this.grandTotal = grandTotal;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("{Class=Order");
        sb.append(", tradeType=").append(tradeType);
        sb.append(", verificationSheetSn='").append(verificationSheetSn).append('\'');
        sb.append(", tradeTypeName='").append(tradeTypeName).append('\'');
        sb.append(", loadingType=").append(loadingType);
        sb.append(", loadingTypeName='").append(loadingTypeName).append('\'');
        sb.append(", loadingAddress='").append(loadingAddress).append('\'');
        sb.append(", loadingContact='").append(loadingContact).append('\'');
        sb.append(", loadingPhone='").append(loadingPhone).append('\'');
        sb.append(", loadingEstimatedTime='").append(loadingEstimatedTime).append('\'');
        sb.append(", railwayId=").append(railwayId);
        sb.append(", planReportTime='").append(planReportTime).append('\'');
        sb.append(", railPlanType='").append(railPlanType).append('\'');
        sb.append(", programNo='").append(programNo).append('\'');
        sb.append(", railSource='").append(railSource).append('\'');
        sb.append(", railDestination='").append(railDestination).append('\'');
        sb.append(", comment='").append(comment).append('\'');
        sb.append(", sameDay=").append(sameDay);
        sb.append(", serviceItems=").append(serviceItems);
        sb.append(", distyCny=").append(distyCny);
        sb.append(", distyUsd=").append(distyUsd);
        sb.append(", grandTotal=").append(grandTotal);
        sb.append(", ").append(super.toString());
        sb.append('}');
        return sb.toString();
    }
}
