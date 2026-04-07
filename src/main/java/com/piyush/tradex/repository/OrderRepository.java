package com.piyush.tradex.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.piyush.tradex.enitity.Order;
import com.piyush.tradex.enitity.User;
import com.piyush.tradex.enums.OrderStatus;
import com.piyush.tradex.enums.OrderType;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

       List<Order> findByUser(User user);

       // Find the first PENDING opposite order that exactly matches price and
       // quantity.
       @Query("SELECT o FROM Order o WHERE o.orderType = :orderType " +
                     "AND o.price = :price " +
                     "AND o.quantity = :quantity " +
                     "AND o.status = :status " +
                     "ORDER BY o.createdAt ASC")
       List<Order> findFirstMatchingOrders(
                     @Param("orderType") OrderType orderType,
                     @Param("price") double price,
                     @Param("quantity") int quantity,
                     @Param("status") OrderStatus status);
}
