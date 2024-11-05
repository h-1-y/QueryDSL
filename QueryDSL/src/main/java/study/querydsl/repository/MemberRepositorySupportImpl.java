package study.querydsl.repository;

import static study.querydsl.entity.QMember.member;
import static study.querydsl.entity.QTeam.team;

import java.lang.reflect.Member;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.util.StringUtils;

import com.querydsl.core.QueryResults;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.impl.JPAQuery;

import study.querydsl.dto.MemberSearchCondition;
import study.querydsl.dto.MemberTeamDto;
import study.querydsl.dto.QMemberTeamDto;

public class MemberRepositorySupportImpl extends QuerydslRepositorySupport implements MemberRepositoryCustom {

	public MemberRepositorySupportImpl() {
		super(Member.class);
	}
	
	@Override
	public List<MemberTeamDto> search(MemberSearchCondition condition) {
		
		List<MemberTeamDto> result = from(member)
									.leftJoin(member.team, team)
									.where(
											  usernameEq(condition.getUsername())
											, teamNameEq(condition.getTeamName())
											, ageGoe(condition.getAgeGoe())
											, ageLoe(condition.getAgeLoe())
										  )
									.select(
											new QMemberTeamDto(
																  member.id.as("memberId")
																, member.username
																, member.age
																, team.id.as("teamId")
																, team.name.as("teamName")
															  )
										)
									.fetch();
		
		return result;
		
	}

	@Override
	public Page<MemberTeamDto> searchPageSimple(MemberSearchCondition condition, Pageable pageable) {
		
		JPQLQuery<MemberTeamDto> jpqlQuery = from(member)
										.leftJoin(member.team, team)
										.where(
												  usernameEq(condition.getUsername())
												, teamNameEq(condition.getTeamName())
												, ageGoe(condition.getAgeGoe())
												, ageLoe(condition.getAgeLoe())
											  )
										.select(
												new QMemberTeamDto(
																	  member.id.as("memberId")
																	, member.username
																	, member.age
																	, team.id.as("teamId")
																	, team.name.as("teamName")
																  )
											);
		
		JPQLQuery<MemberTeamDto> query = getQuerydsl().applyPagination(pageable, jpqlQuery);
		
		List<MemberTeamDto> content = query.fetchResults().getResults();
		long total = query.fetchResults().getTotal();
		
		return new PageImpl<MemberTeamDto>(content, pageable, total);
		
	}
	
	@Override
	public Page<MemberTeamDto> searchPageComplex(MemberSearchCondition condition, Pageable pageable) {
		
		JPQLQuery<MemberTeamDto> jpqlQuery = from(member)
				.leftJoin(member.team, team)
				.where(
						  usernameEq(condition.getUsername())
						, teamNameEq(condition.getTeamName())
						, ageGoe(condition.getAgeGoe())
						, ageLoe(condition.getAgeLoe())
					  )
				.select(
						new QMemberTeamDto(
											  member.id.as("memberId")
											, member.username
											, member.age
											, team.id.as("teamId")
											, team.name.as("teamName")
										  )
					);
		
		JPQLQuery<MemberTeamDto> query = getQuerydsl().applyPagination(pageable, jpqlQuery);
		List<MemberTeamDto> result = query.fetchResults().getResults();
		
		long count = from(member)
		.where(
				  usernameEq(condition.getUsername())
				, teamNameEq(condition.getTeamName())
				, ageGoe(condition.getAgeGoe())
				, ageLoe(condition.getAgeLoe())
			  )
		.select(member).fetchCount();
				
		return PageableExecutionUtils.getPage(result, pageable, () -> count);
//		return new PageImpl<MemberTeamDto>(result, pageable, total);
		
	}
	
	private BooleanExpression usernameEq(String username) {
		return StringUtils.hasText(username) ? member.username.eq(username) : null;
	}

	private BooleanExpression teamNameEq(String teamName) {
		return StringUtils.hasText(teamName) ? team.name.eq(teamName) : null;
	}

	private BooleanExpression ageGoe(Integer ageGoe) {
		return ageGoe != null ? member.age.goe(ageGoe) : null;
	}

	private BooleanExpression ageLoe(Integer ageLoe) {
		return ageLoe != null ? member.age.loe(ageLoe) : null;
	}
	
}
