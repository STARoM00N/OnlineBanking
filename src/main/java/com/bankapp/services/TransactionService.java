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

    public void deposit(Long userId, BigDecimal amount) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        user.setBalance(user.getBalance().add(amount));
        userRepository.save(user);

        Transaction transaction = new Transaction();
        transaction.setUser(user);
        transaction.setType("DEPOSIT");
        transaction.setAmount(amount);
        transaction.setCreatedAt(LocalDateTime.now());
        transactionRepository.save(transaction);
    }

    // การสร้างธุรกรรมใหม่ในฟังก์ชัน withdraw
    public void withdraw(Long userId, BigDecimal amount) {
        // ตรวจสอบยอดเงินในบัญชี และทำการถอนเงิน
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be greater than zero");
        }
        User user = userRepository.findById(userId).orElseThrow(() -> new IllegalArgumentException("User not found"));
        if (user.getBalance().compareTo(amount) < 0) {
            throw new IllegalArgumentException("Insufficient balance");
        }

        // สร้างธุรกรรมใหม่
        Transaction transaction = new Transaction();
        transaction.setUser(user);
        transaction.setAmount(amount);
        transaction.setType("WITHDRAW");
        transaction.setCreatedAt(LocalDateTime.now());  // ตั้งค่า createdAt เป็นเวลาปัจจุบัน

        // ลดยอดเงินในบัญชี
        user.setBalance(user.getBalance().subtract(amount));
        userRepository.save(user);

        // บันทึกธุรกรรม
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

    public BigDecimal getUserBalance(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        return user.getBalance();
    }

    public List<Transaction> getTransactionHistory(Long userId) {
        return transactionRepository.findByUserId(userId);
    }
}

