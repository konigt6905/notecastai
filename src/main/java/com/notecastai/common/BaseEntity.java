package com.notecastai.common;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.Version;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.*;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;

@SuperBuilder
@MappedSuperclass
@Getter
@Setter(AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@FilterDef(
        name = "ownerFilter",
        parameters = @ParamDef(name = "currentUserId", type = Long.class)
)
@Filter(name = "ownerFilter", condition = "created_by = :currentUserId")
@SoftDelete(columnName = "inactive")
public abstract class BaseEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @CreationTimestamp
    @JsonIgnore
    @Column(name = "created_date", updatable = false)
    private Instant createdDate;

    @UpdateTimestamp
    @JsonIgnore
    @Column(name = "updated_date")
    private Instant updatedDate;

    @Version
    @JsonIgnore
    @Column(name = "version")
    private Integer version;

    @CreatedBy
    @Column(name = "created_by")
    private Long createdBy;

    @LastModifiedBy
    @Column(name = "updated_by")
    private Long updatedBy;

}