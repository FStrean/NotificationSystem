package ru.notification.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.notification.entity.RawData;

public interface RawDataDAO extends JpaRepository<RawData, Long> {
}
