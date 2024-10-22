package study.querydsl;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import com.querydsl.jpa.impl.JPAQueryFactory;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import study.querydsl.entity.Hello;
import study.querydsl.entity.QHello;

@SpringBootTest
@Transactional
@Rollback(value = false)
class QueryDslApplicationTests {

	@Autowired
//	@PersistenceContext
	EntityManager em;
	
	@Test
	void contextLoads() {
		
		Hello hello = new Hello();
		
		em.persist(hello);
		
		JPAQueryFactory query = new JPAQueryFactory(em);
		
		QHello qHello = new QHello("h");
		
		Hello result = query
						.selectFrom(qHello)
						.fetchOne();
		
		assertThat(result).isEqualTo(hello);
		assertThat(result.getId()).isEqualTo(hello.getId());
		
	}

}
