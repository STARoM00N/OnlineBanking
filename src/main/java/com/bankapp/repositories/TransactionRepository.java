package com.bankapp.repositories;

import com.bankapp.models.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    // ดึงรายการธุรกรรมของผู้ใช้
    List<Transaction> findByUserId(Long userId);

    // Query to get total income (deposits) for a user
    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t WHERE t.user.id = :userId AND t.type = 'DEPOSIT'")
    BigDecimal findTotalIncomeByUserId(Long userId);

    // Query to get total expenses (withdrawals) for a user
    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t WHERE t.user.id = :userId AND t.type = 'WITHDRAWAL'")
    BigDecimal findTotalExpensesByUserId(Long userId);
}
