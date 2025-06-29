package com.yourkoder.letsmeet.util.database;

import software.amazon.awssdk.enhanced.dynamodb.AttributeConverter;
import software.amazon.awssdk.enhanced.dynamodb.AttributeValueType;
import software.amazon.awssdk.enhanced.dynamodb.EnhancedType;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SetToListAttributeConverter implements AttributeConverter<Set<String>> {
    @Override
    public AttributeValue transformFrom(Set<String> input) {
        if (input == null) {
            return AttributeValue.builder().nul(true).build();
        }
        List<String> list = new ArrayList<>(input); // Convert Set to List
        return AttributeValue.builder().l(list.stream().map(s -> AttributeValue.builder()
                .s(s).build()).toList()).build();
    }

    @Override
    public Set<String> transformTo(AttributeValue input) {
        if (input.nul() != null && input.nul()) {
            return null;
        }
        if (input.l() == null) {
            return new HashSet<>();
        }
        Set<String> set = new HashSet<>();
        for (AttributeValue value : input.l()) {
            if (value.s() != null) {
                set.add(value.s());
            }
        }
        return set;
    }

    @Override
    public EnhancedType<Set<String>> type() {
        return EnhancedType.setOf(EnhancedType.of(String.class));
    }

    @Override
    public AttributeValueType attributeValueType() {
        return AttributeValueType.L;
    }
}