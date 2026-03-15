package com.example.pdf_extratct.loginpage.credittransaction;

import com.example.pdf_extratct.factory.TestDataFactory;
import com.example.pdf_extratct.loginpage.user.UserEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.boot.test.mock.mockito.MockBean;
import com.example.pdf_extratct.uploadfiles.storage.service.StorageService;
import org.springframework.test.context.ActiveProfiles;

import java.sql.Timestamp;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class CreditTransactionRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private CreditTransactionRepository repository;

    @MockBean
    private StorageService storageService;

    @Test
    @DisplayName("Deve salvar uma transação de credito e recuperar pelo findByUserOrderByCreatedAtDesc paginado")
    void shouldFindTransactionsByUserPaginated() {
        UserEntity testUser = TestDataFactory.createUser("user_transactions", "user_transactions@test.com", "123456");
        testUser = entityManager.persistAndFlush(testUser);

        CreditTransactionEntity txOld = CreditTransactionEntity.builder()
                .user(testUser)
                .amount(10)
                .balanceBefore(0)
                .balanceAfter(10)
                .type(TransactionType.PURCHASE)
                .description("Old purchase")
                .createdAt(Timestamp.valueOf(LocalDateTime.now().minusDays(1)))
                .build();
        entityManager.persist(txOld);

        CreditTransactionEntity txNew = CreditTransactionEntity.builder()
                .user(testUser)
                .amount(50)
                .balanceBefore(10)
                .balanceAfter(60)
                .type(TransactionType.BONUS)
                .description("New bonus")
                .createdAt(Timestamp.valueOf(LocalDateTime.now()))
                .build();
        entityManager.persist(txNew);

        entityManager.flush();
        entityManager.clear();

        PageRequest pageRequest = PageRequest.of(0, 10);
        Page<CreditTransactionEntity> resultPage = repository.findByUserOrderByCreatedAtDesc(testUser, pageRequest);

        assertNotNull(resultPage);
        assertEquals(2, resultPage.getTotalElements());

        CreditTransactionEntity firstResult = resultPage.getContent().get(0);
        assertEquals(TransactionType.BONUS, firstResult.getType());
        assertEquals(50, firstResult.getAmount());

        CreditTransactionEntity secondResult = resultPage.getContent().get(1);
        assertEquals(TransactionType.PURCHASE, secondResult.getType());
        assertEquals(10, secondResult.getAmount());
    }

    @Test
    @DisplayName("O usuário não deve acessar as transações do outro usuário")
    void shouldNotFindOtherUserTransactions() {
        UserEntity activeUser = TestDataFactory.createUser("active_user", "active@test.com", "111");
        activeUser = entityManager.persist(activeUser);

        UserEntity otherUser = TestDataFactory.createUser("other_user", "other@test.com", "222");
        otherUser = entityManager.persist(otherUser);

        CreditTransactionEntity txOther = CreditTransactionEntity.builder()
                .user(otherUser)
                .amount(500)
                .balanceBefore(0)
                .balanceAfter(500)
                .type(TransactionType.PURCHASE)
                .description("Other purchase")
                .createdAt(Timestamp.valueOf(LocalDateTime.now()))
                .build();
        entityManager.persistAndFlush(txOther);

        PageRequest pageRequest = PageRequest.of(0, 10);
        Page<CreditTransactionEntity> resultPage = repository.findByUserOrderByCreatedAtDesc(activeUser, pageRequest);

        assertTrue(resultPage.isEmpty());
        assertEquals(0, resultPage.getTotalElements());
    }
}
