package com.projects.razorpay.payment_service.entity;

import com.projects.razorpay.common_lib.entity.BaseEntity;
import com.projects.razorpay.common_lib.enums.PaymentActor;
import com.projects.razorpay.common_lib.enums.PaymentEvent;
import com.projects.razorpay.common_lib.enums.PaymentStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "payment_transaction_log", indexes = {
        @Index(name = "idx_payment_transaction_payment_id", columnList = "payment_id")
})
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PaymentTransactionLog extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "payment_id", nullable = false)
    private Payment payment;

    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    private PaymentStatus from_status;

    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    private PaymentStatus to_status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private PaymentEvent event_type;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private PaymentActor actor;

    @Column(length = 200)
    private String reason;

    @Column(nullable = false)
    private LocalDateTime occuredAt;
}
