package com.freightos.admin.application.code.exchangerate.projection;

import com.freightos.admin.domain.code.exchangerate.entity.ExchangeRateKind;

import java.math.BigDecimal;

public record ExchangeRateValue(ExchangeRateKind kind, BigDecimal rate) {}
