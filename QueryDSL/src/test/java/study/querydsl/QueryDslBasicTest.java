package study.querydsl;

import static org.assertj.core.api.Assertions.assertThat;
import static study.querydsl.entity.QMember.member;
import static study.querydsl.entity.QTeam.team;
import static com.querydsl.jpa.JPAExpressions.*;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Commit;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.QueryResults;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.ExpressionUtils;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.PersistenceUnit;
import study.querydsl.dto.MemberDto;
import study.querydsl.dto.QMemberDto;
import study.querydsl.dto.UserDto;
import study.querydsl.entity.Member;
import study.querydsl.entity.QMember;
import study.querydsl.entity.Team;

@SpringBootTest
@Transactional
//@Rollback(value = false)
public class QueryDslBasicTest {

	@Autowired
	EntityManager em;
	
	JPAQueryFactory queryFactory;
	
	// 해당 어노테이션을 붙이면 모든 테스트 케이스를 돌릴때 해당 메소드가 실행 되고 실행됨 
	@BeforeEach
	public void before() {
		
		queryFactory = new JPAQueryFactory(em);
		
		Team teamA = new Team("teamA");
		Team teamB = new Team("teamB");
		
		em.persist(teamA);
		em.persist(teamB);
		
		Member member1 = new Member("member1", 10, teamA);
		Member member2 = new Member("member2", 20, teamA);
		Member member3 = new Member("member3", 30, teamB);
		Member member4 = new Member("MEMBER4", 40, teamB);
		
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
		
//		QMember m = new QMember("m");
//		QMember m = QMember.member;
		
		// static import 해서 사용 권장
		Member findMember = queryFactory
							.select(member)
							.from(member)
							.where(member.username.eq("member1"))
							.fetchOne();
		
		assertThat(findMember.getUsername()).isEqualTo("member1");
		
	}
	
	@Test
	public void search() {
		
		Member findMember = queryFactory.selectFrom(member)
							.where(member.username.eq("member1").and(member.age.eq(10)))
							.fetchOne();
		
		assertThat(findMember.getUsername()).isEqualTo("member1");
		assertThat(findMember.getAge()).isEqualTo(1);
		
	}
	
	@Test
	public void searchAndParam() {
		
		// where 조건 and의 경우 쉼표로 구분 가능
		Member findMember = queryFactory.selectFrom(member)
							.where(
									  member.username.eq("member1")
									, member.age.eq(10)
								  )
							.fetchOne();
		
		assertThat(findMember.getUsername()).isEqualTo("member1");
		assertThat(findMember.getAge()).isEqualTo(1);
		
	}
	
	@Test
	public void resultFetch() {
		
		// List 조회 
		List<Member> fetch = queryFactory
								.selectFrom(member)
								.fetch();
		// 단건 조회 
		Member fetchOne = queryFactory
							.selectFrom(member)
							.where(member.username.eq("member1"))
							.fetchOne();
		
		// 첫번째 데이터 조회 
		Member fetchFirst = queryFactory
							.selectFrom(member)
							.fetchFirst();
		
		// List & Count 조회
		QueryResults<Member> fetchResult = queryFactory
											.selectFrom(member)
											.fetchResults();
		
		fetchResult.getTotal();
		List<Member> content = fetchResult.getResults();
		
		// Count 조회
		long count = queryFactory
				.selectFrom(member)
				.fetchCount();
		
	}
	
	/*
	 * 회원 정렬 순서
	 * 1. 회원 나이 내림차순 
	 * 2. 회원 이름 올림차순
	 * 단 2에서 회원 이름이 없으면 마지막에 출력(nulls last)
	 */
	@Test
	public void sort() {
		
		em.persist(new Member(null, 100));
		em.persist(new Member("member5", 100));
		em.persist(new Member("member6", 100));
		
		List<Member> result = queryFactory
			.selectFrom(member)
			.where(member.age.eq(100))
			.orderBy(member.age.desc(), member.username.asc().nullsLast())
			.fetch();
		
		Member member5 = result.get(0);
		Member member6 = result.get(1);
		Member memberNull = result.get(2);
		
		assertThat(result.size()).isEqualTo(3);
		assertThat(member5.getUsername()).isEqualTo("member5");
		assertThat(member6.getUsername()).isEqualTo("member6");
		assertThat(memberNull.getUsername()).isNull();
		
	}
	
	@Test
	public void paging() {
		
		List<Member> result = queryFactory
				.selectFrom(member)
				.orderBy(member.username.desc())
				.offset(1)
				.limit(2)
				.fetch();
		
		assertThat(result.size()).isEqualTo(2);
		
	}
	
	@Test
	public void paging2() {
		
		QueryResults<Member> result = queryFactory
				.selectFrom(member)
				.orderBy(member.username.desc())
				.offset(1)
				.limit(2)
				.fetchResults();
		
		assertThat(result.getTotal()).isEqualTo(4);
		assertThat(result.getLimit()).isEqualTo(2);
		assertThat(result.getOffset()).isEqualTo(1);
		assertThat(result.getResults().size()).isEqualTo(2);
	}
	
	@Test
	public void group() {
		
		List<Tuple> result = queryFactory
							.select(
									  member.count()
									, member.age.sum()
									, member.age.avg()
									, member.age.max()
									, member.age.min()
								   )	
							.from(member)
							.fetch();
		
		Tuple tuple = result.get(0);
		
		assertThat(tuple.get(member.count())).isEqualTo(4);
		assertThat(tuple.get(member.age.sum())).isEqualTo(100);
		assertThat(tuple.get(member.age.avg())).isEqualTo(25);
		assertThat(tuple.get(member.age.max())).isEqualTo(40);
		assertThat(tuple.get(member.age.min())).isEqualTo(10);
		
	}
	
	/*
	 * 팀의 이름과 각 팀의 평균 연령을 구해라.
	 */
	@Test
	public void group2() {
		
		List<Tuple> result = queryFactory
							.select(team.name, member.age.avg())
							.from(member)
							.join(member.team, team)
							.groupBy(team.id)
							.fetch();
		
		Tuple teamA = result.get(0);
		Tuple teamB = result.get(1);
		
		assertThat(teamA.get(team.name)).isEqualTo("teamA");
		assertThat(teamA.get(member.age.avg())).isEqualTo(15);
		
		assertThat(teamB.get(team.name)).isEqualTo("teamB");
		assertThat(teamB.get(member.age.avg())).isEqualTo(35);
		
	}
	
	@Test
	public void join() {
		
		List<Member> result = queryFactory
								.selectFrom(member)
								.join(member.team, team)
								.where(team.name.eq("teamA"))
								.fetch();
		
		assertThat(result).extracting("username").containsExactly("member1", "member2");
		
	}
 	
	@Test
	public void thetaJoin() {
		
		em.persist(new Member("teamA"));
		em.persist(new Member("teamB"));
		em.persist(new Member("teamC"));
		
		List<Member> result = queryFactory
								.select(member)
								.from(member, team)
								.where(member.username.eq(team.name))
								.fetch();
		
		assertThat(result).extracting("username").containsExactly("teamA", "teamB");
		
	}
	
	/*
	 * 회원과 팀을 조인하면서 팀 이름이 teamA인 팀만 조인, 회원은 모두 조회
	 * JPQL : select m, t from Member m left join m.team t on. t.name = 'teamA'
	 */
	@Test
	public void join_on_filtering() {
		
		List<Tuple> result = queryFactory
								.select(member, team)
								.from(member)
								.leftJoin(member.team, team).on(team.name.eq("teamA"))
								.fetch();
		
		for ( Tuple t : result ) System.out.println("t ========== " + t);
		
	}
	
	/*
	 * 연관관계가 없는 엔티티를 외부 조인 
	 * 회원의 이름이 팀 이름과 같은 대상 외부 조인 
	 * */
	@Test
	public void join_on_no_realtion() {
		
		em.persist(new Member("teamA"));
		em.persist(new Member("teamB"));
		em.persist(new Member("teamC"));
		
		List<Tuple> result = queryFactory
								.select(member, team)
								.from(member)
								.leftJoin(team).on(member.username.eq(team.name))
								.fetch();
		
		for ( Tuple t : result ) System.out.println("t ========== " + t);
		
	}
	
	
	@PersistenceUnit
	EntityManagerFactory emf;
	
	@Test
	public void fetchJoinNo() {
		
		em.flush();
		em.clear();
		
		Member findMember = queryFactory
							.selectFrom(member)
							.where(member.username.eq("member1"))
							.fetchOne();
		
		// 영속성으로 관리되는 엔티티인지 아닌지 검증해주는 로직
		boolean loaded = emf.getPersistenceUnitUtil().isLoaded(findMember.getTeam());
		
		assertThat(loaded).as("페치 조인 미적용").isFalse();
		
	}
	
	@Test
	public void fetchJoinUse() {
		
		em.flush();
		em.clear();
		
		Member findMember = queryFactory
							.selectFrom(member)
							.join(member.team, team).fetchJoin()
							.where(member.username.eq("member1"))
							.fetchOne();
		
		// 영속성으로 관리되는 엔티티인지 아닌지 검증해주는 로직
		boolean loaded = emf.getPersistenceUnitUtil().isLoaded(findMember.getTeam());
		
		assertThat(loaded).as("페치 조인 적용").isTrue();
		
	}
	
	/*
	 * JPAExpressions
	 * 
	 * sub query
	 * 나이가 제일 많은 회원 조회
	 * 
	 */
	@Test
	public void subQuery() {
		
		QMember memberSub = new QMember("memberSub");
		
		List<Member> result = queryFactory
								.selectFrom(member)
								.where(
										member.age.eq(
													 select(memberSub.age.max())
													.from(memberSub)
												)
										)
								.fetch();
		
		assertThat(result).extracting("age").containsExactly(40);
		
	}
	
	/*
	 * JPAExpressions
	 * 
	 * sub query
	 * 나이가 평균 이상 회원 조회
	 * 
	 */
	@Test
	public void subQueryGoe() {
		
		QMember memberSub = new QMember("memberSub");
		
		List<Member> result = queryFactory
								.selectFrom(member)
								.where(
										member.age.goe(
													 select(memberSub.age.avg())
													.from(memberSub)
												)
										)
								.fetch();
		
		assertThat(result).extracting("age").containsExactly(30, 40);
		
	}
	
	/*
	 * JPAExpressions
	 * 
	 * sub query
	 * 나이가 10살 이상인 회원 조회 from IN
	 * 
	 */
	@Test
	public void subQueryIn() {
		
		QMember memberSub = new QMember("memberSub");
		
		List<Member> result = queryFactory
								.selectFrom(member)
								.where(
										member.age.in(
													 select(memberSub.age)
													.from(memberSub)
													.where(memberSub.age.gt(10))
												)
										)
								.fetch();
		
		assertThat(result).extracting("age").containsExactly(20, 30, 40);
		
	}
	
	/*
	 * JPAExpressions
	 * 
	 * sub query
	 * 나이가 평균 이상 회원 조회
	 * 
	 */
	@Test
	public void selectSubQuery() {
		
		QMember memberSub = new QMember("memberSub");
		
		List<Tuple> result = queryFactory
								.select(
										  member.username
										,    select(memberSub.age.avg())
											.from(memberSub)
										)
								.from(member)
								.fetch();
		
		for ( Tuple t : result ) System.out.println("t ======== " + t);
		
	}
	
	
	// case문
	@Test
	public void basicCase() {
		
		List<String> result = queryFactory
								.select(
										member.age
										.when(10).then("열살")
										.when(20).then("스무살")
										.otherwise("기타")
										)
								.from(member)
								.fetch();
		
		for ( String str : result ) System.out.println("str ==== " + str);
		
	}
	
	// case문
	@Test
	public void complexCase() {
		
		List<String> result = queryFactory
		.select(
					new CaseBuilder()
					.when(member.age.between(0, 20)).then("0~20살")
					.when(member.age.between(21, 30)).then("21~30살")
					.otherwise("기타")
				)
		.from(member)
		.fetch();
		
		for ( String str : result ) System.out.println("str ==== " + str);
		
	}
	
	
	// 상수
	@Test
	public void constant() {
		
		List<Tuple> result = queryFactory
								.select(member.username, Expressions.constant("A"))
								.from(member)
								.fetch();
		
		for ( Tuple t : result ) System.out.println("t ===== " + t);
		
	}
	
	// 문자 더하기
	@Test
	public void concat() {
		
		List<String> result = queryFactory
								.select(member.username.concat("_").concat(member.age.stringValue()))
								.from(member)
								.fetch();
		
		for ( String str : result ) System.out.println("str ======= " + str);
		
	}
	
	// 대상이 하나일때
	@Test
	public void simpleProjection() {
		
		List<String> result = queryFactory
							.select(member.username)
							.from(member)
							.fetch();
		
		for ( String str : result ) System.out.println("str ===== " + str);
		
	}
	
	// 대상이 여러개일때 Tuple
	@Test
	public void tupleProjection() {
		
		List<Tuple> result = queryFactory
							.select(member.username, member.age)
							.from(member)
							.fetch();
		
		for ( Tuple t : result ) {
			String username = t.get(member.username);
			int age = t.get(member.age);
			System.out.print("t ====== " + username);
			System.out.println(" / t ====== " + age);
		}
		
	}
	
	// DTO for JPQL
	@Test
	public void findDtoByJPQL() {
		
		List<MemberDto> result = em.createQuery("select new study.querydsl.dto.MemberDto(m.username, m.age) from Member m", MemberDto.class)
									.getResultList();
		
		for ( MemberDto dto : result )
			System.out.println("dto ======= " + dto);
		
	}
	
	// DTO for QueryDSL 3가지 방법
	// 1. 프로퍼티 접근
	// 2. 필드 직접 접근
	// 3. 생성자 사용
	
	// DTO for QueryDSL Setter
	// bean
	@Test
	public void findDtoBySetter() {
		
		List<MemberDto> result = queryFactory
								.select(
											Projections.bean(MemberDto.class, member.username, member.age)
										)
								.from(member)
								.fetch();
		
		for ( MemberDto dto : result )
			System.out.println("dto ======= " + dto);
		
	}
	
	// DTO for QueryDSL field
	// fields
	@Test
	public void findDtoByField() {
		
		List<MemberDto> result = queryFactory
								.select(
											Projections.fields(MemberDto.class, member.username, member.age)
										)
								.from(member)
								.fetch();
		
		for ( MemberDto dto : result )
			System.out.println("dto ======= " + dto);
		
	}
	
	// DTO for QueryDSL field
	// fields
	// 필드명이 매칭되지 않을 경우 null로 반환
	// 별칭(as) 사용 
	@Test
	public void findUserDtoByField() {
		
		QMember memberSub = new QMember("memberSub");
		
		List<UserDto> result = queryFactory
								.select(
											Projections.fields(
																  UserDto.class, 
																  member.username.as("name")
																, ExpressionUtils.as(
																						  JPAExpressions.select(memberSub.age.max()).from(memberSub)
																						, "age"
																					)
															  )
										)
								.from(member)
								.fetch();
		
		for ( UserDto dto : result )
			System.out.println("dto ======= " + dto);
		
	}
	
	// DTO for QueryDSL Constructor
	// constructor
	@Test
	public void findDtoByConstructor() {
		
		List<MemberDto> result = queryFactory
								.select(
											Projections.constructor(MemberDto.class, member.username, member.age)
										)
								.from(member)
								.fetch();
		
		for ( MemberDto dto : result )
			System.out.println("dto ======= " + dto);
		
	}
	
	// @QueryProjection
	@Test
	public void finDtoByQueryProjection() {
		
		List<MemberDto> result = queryFactory
								.select(new QMemberDto(member.username, member.age))
								.from(member)
								.fetch();
		
		for ( MemberDto dto : result )
			System.out.println("dto ======= " + dto);
		
	}
	
	// 동적 쿼리 
	// BooleanBuilder
	@Test
	public void dynamicQuery_BooleanBuilder() {
		
		// parameter
		String usernameParam = "member1";
		Integer ageParam = 10;
		
		List<Member> result = searchMember1(usernameParam, ageParam);
		
		assertThat(result.size()).isEqualTo(1);
		
	}

	private List<Member> searchMember1(String usernameParam, Integer ageParam) {
		
		BooleanBuilder builder = new BooleanBuilder();
		
		if ( usernameParam != null ) builder.and(member.username.eq(usernameParam));
		if ( ageParam != null ) builder.and(member.age.eq(ageParam));
		
		return queryFactory
				.selectFrom(member)
				.where(builder)
				.fetch();
	}
	
	// where문 다중 파라미터 사용
	@Test
	public void dynamicQuery_whereParam() {
		
		// parameter
		String usernameParam = "member1";
		Integer ageParam = 10;
		
		List<Member> result = searchMember2(usernameParam, ageParam);
		
		assertThat(result.size()).isEqualTo(1);
		
	}
	
	private List<Member> searchMember2(String usernameParam, Integer ageParam) {
		return queryFactory
				.selectFrom(member)
//				.where(usernameEq(usernameParam), ageEq(ageParam))
				.where(allEq(usernameParam, ageParam))
				.fetch();
	}

	private BooleanExpression usernameEq(String usernameParam) {
		return usernameParam != null ? member.username.eq(usernameParam) : null;
	}
	
	private BooleanExpression ageEq(Integer ageParam) {
		return ageParam != null ? member.age.eq(ageParam) : null;
	}
	
	private BooleanExpression allEq(String usernameParam, Integer ageParam) {
		return usernameEq(usernameParam).and(ageEq(ageParam));
	}
	
	// 벌크 연산
	@Test
	public void bulkUpdate() {
		
		long count = queryFactory
					.update(member)
					.set(member.username, "비회원")
					.where(member.age.lt(28))
					.execute();
		
		// 벌크 연산 후 DB에는 변경사항이 반영되지만 
		// 영속성 컨텍스트에는 반영이 되지 않는다. 
		List<Member> result = queryFactory
								.selectFrom(member)
								.fetch();
		
		// Member를 조회해보면 영속성 컨텍스트에서 관리되던 데이터를 그대로 가져와
		// DB의 변경사항과 불일치함
		for ( Member m : result ) System.out.println("m ========= " + m);
		
		// 그래서 fulsh, clear 해줘야함
		em.flush();
		em.clear();
		
		// 정상적으로 다시 가져온 모습
		result = queryFactory
				.selectFrom(member)
				.fetch();
		
		for ( Member m : result ) System.out.println("m ========= " + m);
		
		assertThat(count).isEqualTo(2);
		
	}
	
	@Test
	public void bulkAdd() {
		
		long count = queryFactory
					.update(member)
					.set(member.age, member.age.add(1)) // 더하기 
//					.set(member.age, member.age.multiply(1)) // 곱하기
					.execute();
		
		assertThat(count).isEqualTo(4);
		
	}
	
	@Test
	public void bulkDelete() {
		
		long count = queryFactory
					.delete(member)
					.where(member.age.gt(18))
					.execute();
		
		assertThat(count).isEqualTo(3);
		
	}
	
	@Test
	public void sqlFunction() {
		
		List<String> result = queryFactory
		.select(Expressions.stringTemplate("function('replace', {0}, {1}, {2})", member.username, "member", "M"))
		.from(member)
		.fetch();
		
		for ( String str : result ) System.out.println("str ===== " + str);
		
	}
	
	@Test
	public void sqlFunction2() {
		
		List<String> result = queryFactory
		.select(member.username)
		.from(member)
//		.where(member.username.eq(Expressions.stringTemplate("function('lower', {0})", member.username)))
		.where(member.username.eq(member.username.lower()))
		.fetch();
		
		for ( String str : result ) System.out.println("str ===== " + str);
		
	}
	
}








