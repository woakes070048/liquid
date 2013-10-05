package liquid.persistence.domain;

import javax.persistence.*;

/**
 * TODO: Comments.
 * User: tao
 * Date: 10/2/13
 * Time: 4:28 PM
 */
@Entity(name = "TRANS_BARGE")
public class TransBarge extends BaseTrans {

    @Transient
    private String spId;

    @ManyToOne
    @JoinColumn(name = "SP_ID")
    private ServiceProvider sp;

    public String getSpId() {
        return spId;
    }

    public void setSpId(String spId) {
        this.spId = spId;
    }

    public ServiceProvider getSp() {
        return sp;
    }

    public void setSp(ServiceProvider sp) {
        this.sp = sp;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder(super.toString());
        sb.append("TransBarge{");
        sb.append("spId='").append(spId).append('\'');
        sb.append(", sp=").append(sp);
        sb.append('}');
        return sb.toString();
    }
}
