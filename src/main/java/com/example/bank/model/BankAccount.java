package com.example.bank.model;

import com.example.bank.exception.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name="accounts")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "account_type")
@DiscriminatorValue("DEBIT")
public class BankAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(
            name = "account_number",
            nullable = false,
            unique = true,
            length = 20
    )
    private String accountNumber;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name= "user_id", nullable = false)
    private User user;

    @Column(name = "owner", nullable = false, length = 100)
    private String owner;

    @Column(name = "balance", nullable = false,
            precision = 12, scale = 2)
    private BigDecimal balance;


    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private AccountStatus status = AccountStatus.ACTIVE;

    // LAZY — транзакции не загружаются при findById счёта
    // cascade ALL — при удалении счёта удалятся все его транзакции
    // orphanRemoval — при удалении из списка удалится из БД
    @OneToMany(mappedBy = "account",
            fetch = FetchType.LAZY,
            cascade = CascadeType.ALL,
            orphanRemoval = true)
    private List<Transaction> transactions = new ArrayList<>();




    public BankAccount(String owner) {
        this.owner = owner;
        this.balance = BigDecimal.ZERO.setScale(2);
        this.status = AccountStatus.ACTIVE;

    }

    protected BankAccount(String owner, BigDecimal initialBalance) {
        if(owner == null || owner.isBlank() || owner.length() < 2) {
            throw new IllegalArgumentException();
        }
        if(initialBalance == null || initialBalance.compareTo(BigDecimal.ZERO) < 0) {
            throw  new InvalidAmountException();
        }
        this.owner = owner;
        this.balance = initialBalance.setScale(2);
    }

    protected BankAccount() {

    }


    public Long getId() { return id; }
    public String getAccountNumber() { return accountNumber; }
    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public String getOwner() { return owner; }
    public void setOwner(String owner) { this.owner = owner; }
    public BigDecimal getBalance() { return balance; }
    public boolean isActive() {
        return status == AccountStatus.ACTIVE;
    }

    public AccountStatus getStatus() {
        return status;
    }

    public void block() {
        if (status == AccountStatus.CLOSED) {
            throw new InvalidAccountStatusTransitionException(
                    "Счёт с id = " + id + ": Нельзя заблокировать закрытый счёт"
            );
        }

        status = AccountStatus.BLOCKED;
    }

    public void activate() {
        if (status == AccountStatus.CLOSED) {
            throw new InvalidAccountStatusTransitionException(
                    "Счёт с id = " + id + ": Нельзя активировать закрытый счёт"
            );
        }

        status = AccountStatus.ACTIVE;
    }

    public void close() {
        if (status == AccountStatus.CLOSED) {
            throw new InvalidAccountStatusTransitionException(
                    "Счёт с id = " + id + ": Счёт уже закрыт"
            );
        }

        if (balance.compareTo(BigDecimal.ZERO) != 0) {
            throw new CannotCloseAccountException(
                    "Счёт с id = " + id +
                            ": Нельзя закрыть счёт с ненулевым балансом"
            );
        }

        status = AccountStatus.CLOSED;
    }

    public List<Transaction> getTransactions() { return transactions; }
    public void addTransaction(Transaction tx) {
        tx.setAccount(this);
        transactions.add(tx);}

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BankAccount other)) return false;
        return id != null && id.equals(other.id);
    }

    @Override
    public int hashCode() {return getClass().hashCode();}

    @Override
    public String toString() {
        return "BankAccount{id=" + id + ", owner=" + owner + ", balance=" + balance +
                ", status=" + status +"}";
    }

    protected void applyInterest(BigDecimal interest){
        if(!isActive()){
            throw new InactiveAccountException();
        }
        if(interest == null || interest.compareTo(BigDecimal.ZERO) <= 0){
            throw new InvalidAmountException();
        }
        balance = balance.add(interest);
    }

    protected void increaseBalance(BigDecimal amount) {
        balance = balance.add(amount);
    }

    protected void decreaseBalance(BigDecimal amount) {
        balance = balance.subtract(amount);
    }

    public void deposit(BigDecimal amount) {

        if(!isActive()) {
            throw new InactiveAccountException(id, status);
        }
        if(amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidAmountException();
        }
        balance = balance.add(amount);

    }
    public void withdraw(BigDecimal amount) {


        if(!isActive()) {
            throw new InactiveAccountException(id, status);
        }
        if(amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidAmountException();
        }
        if (balance.compareTo(amount) < 0) {
            throw new InsufficientFundsException(amount,balance);
        }

        balance = balance.subtract(amount);
    }



}