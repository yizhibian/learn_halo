package run.halo.app.model.entity;

import java.util.Date;
import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import javax.persistence.PrePersist;
import javax.persistence.PreRemove;
import javax.persistence.PreUpdate;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import run.halo.app.utils.DateUtils;

/**
 * Base entity.
 *
 * @author johnniang
 * @date 3/20/19
 */
@Data
@ToString
/*
* 这个注解是关键 JPA注解
* 应用于实体类的父类中, 该注解作用的类不会映射到数据表，
* 但其属性都将映射到子类所对应的数据表。
* 也就是说不同实体类的通用属性可在相同的父类中定义，
* 子类继承父类后，父类中的这些通用属性会持久化到子类对应的数据表中。
* */
@MappedSuperclass
@EqualsAndHashCode
public class BaseEntity {

    /**
     * Create time.
     */
    @Column(name = "create_time")
    @Temporal(TemporalType.TIMESTAMP)
    private Date createTime;

    /**
     * Update time.
     */
    @Column(name = "update_time")
    @Temporal(TemporalType.TIMESTAMP)
    private Date updateTime;

    @PrePersist//@PrePersist 事件在实体对象插入到数据库的过程中发生
    protected void prePersist() {
        Date now = DateUtils.now();
        if (createTime == null) {
            createTime = now;
        }

        if (updateTime == null) {
            updateTime = now;
        }
    }

    @PreUpdate // @PreUpdate 事件在实体的状态同步到数据库之前触发
    protected void preUpdate() {
        updateTime = new Date();
    }

    @PreRemove// @PreRemove 事件在实体从数据库删除之前触发
    /*
    * 貌似已经废弃
    * 以前的版本好像喜欢给数据库字段deleted表示已经删除这样
    * 所以删除的数据也会有数据在数据库内
    * */
    protected void preRemove() {
        updateTime = new Date();
    }

}
