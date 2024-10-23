package study.querydsl;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import com.querydsl.jpa.impl.JPAQueryFactory;

import jakarta.persistence.EntityManager;
import study.querydsl.entity.Member;
import study.querydsl.entity.QMember;
import study.querydsl.entity.Team;

@SpringBootTest
@Transactional
public class QueryDslBasicTest {

	@Autowired
	EntityManager em;
	
	// 해당 어노테이션을 붙이면 모든 테스트 케이스를 돌릴때 해당 메소드가 실행 되고 실행됨 
	@BeforeEach
	public void before() {
		
		Team teamA = new Team("teamA");
		Team teamB = new Team("teamB");
		
		em.persist(teamA);
		em.persist(teamB);
		
		Member member1 = new Member("member1", 1, teamA);
		Member member2 = new Member("member2", 2, teamA);
		Member member3 = new Member("member3", 3, teamB);
		Member member4 = new Member("member4", 4, teamB);
		
		em.persist(member1);
		em.persist(member2);
		em.persist(member3);
		em.persist(member4);
		
	}
	
	@Test
	public void startJPQL() {
		
		// find member1
		Member findMember = em.createQuery("select m from Member m where m.username = :username", Member.class)
							.setParameter("username", "member1")
							.getSingleResult();
		
		assertThat(findMember.getUsername()).isEqualTo("member1");
		
	}
	
	@Test
	public void startQuerydsl() {
		
		JPAQueryFactory queryFactory = new JPAQueryFactory(em);
		
		QMember m = new QMember("m");
		
		Member findMember = queryFactory
							.select(m)
							.from(m)
							.where(m.username.eq("member1"))
							.fetchOne();
		
		assertThat(findMember.getUsername()).isEqualTo("member1");
		
	}
	
}
