package com.piyush.tradex.repository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.JpaRepository;
import com.piyush.tradex.enitity.Wallet;

@Repository
public interface WalletRepository extends JpaRepository<Wallet, Long> {
    Wallet findByUserUserId(long userId);
    boolean existsByUserUserId(long userId);
    void deleteByUserUserId(long userId);
}
