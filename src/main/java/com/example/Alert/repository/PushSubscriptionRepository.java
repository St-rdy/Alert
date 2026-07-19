package com.example.Alert.repository;

import com.example.Alert.entity.PushSubscription;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PushSubscriptionRepository extends JpaRepository<PushSubscription, Long> {
        Optional<PushSubscription> findByUserId(Long userId);
    }
