package com.bankapp.services;

import com.bankapp.models.Transaction;
import com.bankapp.models.User;
import com.bankapp.repositories.UserRepository;
import com.bankapp.repositories.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class TransactionService {

    @Autowired
    private TransactionRepository transactionRepository;

    public void saveTransaction(Transaction transaction) {
        transactionRepository.save(transaction);  // Save to the database
    }

    @Autowired
    private UserRepository userRepository;

    // คำนวณรายรับทั้งหมด (การฝาก)
    public BigDecimal getTotalIncome(Long userId) {
        BigDecimal income = transactionRepository.findByUserId(userId).stream()
                .filter(transaction -> transaction.getType().equals("DEPOSIT"))  // คัดกรองการฝากเงิน
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);  // รวมรายรับ
        return income;
    }

    // คำนวณรายจ่ายทั้งหมด (การถอน)
    public BigDecimal getTotalExpenses(Long userId) {
        BigDecimal expenses = transactionRepository.findByUserId(userId).stream()
                .filter(transaction -> transaction.getType().equals("WITHDRAWAL"))  // คัดกรองการถอนเงิน
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);  // รวมรายจ่าย
        return expenses;
    }

    // การฝากเงิน
    public void deposit(Long userId, BigDecimal amount) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        user.setBalance(user.getBalance().add(amount));  // เพิ่มยอดเงินในบัญชี
        userRepository.save(user);

        // สร้างธุรกรรมฝากเงิน
        Transaction transaction = new Transaction();
        transaction.setUser(user);
        transaction.setType("DEPOSIT");
        transaction.setAmount(amount);  // จำนวนเงินที่ฝาก
        transaction.setCreatedAt(LocalDateTime.now());
        transactionRepository.save(transaction);
    }

    // การถอนเงิน
    public void withdraw(Long userId, BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be greater than zero");
        }
        User user = userRepository.findById(userId).orElseThrow(() -> new IllegalArgumentException("User not found"));
        if (user.getBalance().compareTo(amount) < 0) {
            throw new IllegalArgumentException("Insufficient balance");
        }

        // ลดยอดเงินในบัญชี
        user.setBalance(user.getBalance().subtract(amount));
        userRepository.save(user);

        // สร้างธุรกรรมถอนเงิน
        Transaction transaction = new Transaction();
        transaction.setUser(user);
        transaction.setAmount(amount);  // จำนวนเงินที่ถอน
        transaction.setType("WITHDRAWAL");
        transaction.setCreatedAt(LocalDateTime.now());  // ตั้งเวลา
        transactionRepository.save(transaction);
    }

    public void transfer(Long senderId, Long receiverId, BigDecimal amount) {
        User sender = userRepository.findById(senderId)
                .orElseThrow(() -> new IllegalArgumentException("Sender not found"));
        User receiver = userRepository.findById(receiverId)
                .orElseThrow(() -> new IllegalArgumentException("Receiver not found"));

        if (sender.getBalance().compareTo(amount) < 0) {
            throw new IllegalArgumentException("Insufficient funds. Your balance is not enough to transfer.");
        }

        sender.setBalance(sender.getBalance().subtract(amount));
        receiver.setBalance(receiver.getBalance().add(amount));
        userRepository.save(sender);
        userRepository.save(receiver);

        Transaction senderTransaction = new Transaction();
        senderTransaction.setUser(sender);
        senderTransaction.setType("TRANSFER_OUT");
        senderTransaction.setAmount(amount.negate());
        senderTransaction.setCreatedAt(LocalDateTime.now());
        transactionRepository.save(senderTransaction);

        Transaction receiverTransaction = new Transaction();
        receiverTransaction.setUser(receiver);
        receiverTransaction.setType("TRANSFER_IN");
        receiverTransaction.setAmount(amount);
        receiverTransaction.setCreatedAt(LocalDateTime.now());
        transactionRepository.save(receiverTransaction);
    }

    // คำนวณยอดเงินคงเหลือ
    public BigDecimal getUserBalance(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        return user.getBalance();
    }

    public List<Transaction> getTransactionHistory(Long userId) {
        return transactionRepository.findByUserId(userId);
    }
}

