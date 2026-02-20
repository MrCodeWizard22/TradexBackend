package com.piyush.tradex.repository;

import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.JpaRepository;
import com.piyush.tradex.enitity.StockEntity;

@Repository
public interface StockEntityRepository extends JpaRepository<StockEntity, Long> {
    StockEntity findBySymbol(String symbol);
    boolean existsBySymbol(String symbol);
    void deleteBySymbol(String symbol);
}
