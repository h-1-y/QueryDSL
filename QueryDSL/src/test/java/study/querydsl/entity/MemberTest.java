package study.querydsl.entity;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;

@SpringBootTest
@Transactional
@Rollback(value = false)
class MemberTest {

	@Autowired
	EntityManager em;
	
	@Test
	public void testEntity() {
		
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
		
		em.flush();
		em.clear();
		
		List<Member> result = em.createQuery("select m from Member m", Member.class).getResultList();
		
		for ( Member m : result ) {
			System.out.print("m ====== " + m);
			System.out.println(" / member team ====== " + m.getTeam().getName());
		}
		
	}

}
