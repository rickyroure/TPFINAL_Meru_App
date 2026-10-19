package com.meru.app.shop.repository;

import com.meru.app.shop.domain.ItemOrden;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ItemOrdenRepository extends JpaRepository<ItemOrden, Long> {
}
