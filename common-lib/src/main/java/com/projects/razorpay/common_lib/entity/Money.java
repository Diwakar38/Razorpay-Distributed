package com.projects.razorpay.common_lib.entity;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Embeddable
@EqualsAndHashCode
public class Money {
    private Integer amountUnits;
    private String currency;

    public static Money of(Integer amountUnits, String currency) {
        return new Money(amountUnits, currency);
    }

    public static Money inr(Integer amount) {
        return new Money(amount, "INR");
    }

    public static Money usd(Integer amount) {
        return new Money(amount, "USD");
    }

    public static Money eur(Integer amount) {
        return new Money(amount, "EUR");
    }

    public static Money jpy(Integer amount) {
        return new Money(amount, "JPY");
    }

    public Money add(Money other) {
        if (!this.currency.equals(other.currency)) {
            return new Money(this.amountUnits + other.amountUnits, this.currency);
        } else {
            throw new IllegalArgumentException("Cannot add money with different currencies");
        }
    }

    public Money subtract(Money other) {
        if (!this.currency.equals(other.currency)) {
            return new Money(this.amountUnits - other.amountUnits, this.currency);
        } else {
            throw new IllegalArgumentException("Cannot subtract money with different currencies");
        }
    }

}
