package com.freightos.admin.adapter.out.persistence.attributevalue;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Embeddable
@Getter
@NoArgsConstructor
@EqualsAndHashCode
public class AttributeValueId implements Serializable {

    @Column(name = "attribute_key", nullable = false, length = 80)
    private String attributeKey;

    @Column(name = "value", nullable = false, length = 100)
    private String value;

    public AttributeValueId(String attributeKey, String value) {
        this.attributeKey = attributeKey;
        this.value        = value;
    }
}
