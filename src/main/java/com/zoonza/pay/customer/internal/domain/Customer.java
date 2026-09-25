package com.zoonza.pay.customer.internal.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Customer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Embedded
    private Name name;

    @Embedded
    private PhoneNumber phoneNumber;

    @Column(nullable = false,  updatable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    private Customer(
            Name name,
            PhoneNumber phoneNumber,
            Instant createdAt,
            Instant updatedAt
    ) {
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static Customer register(Name name, PhoneNumber phoneNumber) {
        Instant now = Instant.now();
        return new Customer(
                name,
                phoneNumber,
                now,
                now
        );
    }
}
