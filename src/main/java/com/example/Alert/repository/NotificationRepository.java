package com.example.Alert.repository;

import com.example.Alert.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;


public interface NotificationRepository extends JpaRepository<Notification, Long>, QuerydslPredicateExecutor<Notification> {

}
