package com.example.pentagon.repository;

import com.example.pentagon.domain.User;
import com.example.pentagon.dto.user.AdminUserDetailDTO;
import com.example.pentagon.dto.user.AdminUserListDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    // ✅ 관리자 목록 (탈퇴 이메일 제외)
    @Query(
            value = """
                select new com.example.pentagon.dto.user.AdminUserListDTO(
                    u.id, u.name, u.email, u.phone, u.active, u.role
                )
                from User u
                where lower(u.email) not like 'deleted\\_%@deleted.local' escape '\\'
                order by u.id desc
            """,
            countQuery = """
                select count(u.id)
                from User u
                where lower(u.email) not like 'deleted\\_%@deleted.local' escape '\\'
            """
    )
    Page<AdminUserListDTO> findAdminUserList(Pageable pageable);

    // ✅ 관리자 검색 (탈퇴 이메일 제외 + 괄호 중요)
    @Query(
            value = """
                select new com.example.pentagon.dto.user.AdminUserListDTO(
                    u.id, u.name, u.email, u.phone, u.active, u.role
                )
                from User u
                where lower(u.email) not like 'deleted\\_%@deleted.local' escape '\\'
                  and (
                       lower(u.name) like lower(concat('%', :keyword, '%'))
                    or lower(u.email) like lower(concat('%', :keyword, '%'))
                  )
                order by u.id desc
            """,
            countQuery = """
                select count(u.id)
                from User u
                where lower(u.email) not like 'deleted\\_%@deleted.local' escape '\\'
                  and (
                       lower(u.name) like lower(concat('%', :keyword, '%'))
                    or lower(u.email) like lower(concat('%', :keyword, '%'))
                  )
            """
    )
    Page<AdminUserListDTO> searchAdminUserList(@Param("keyword") String keyword, Pageable pageable);

    // ✅ 상세도 탈퇴 유저는 막고 싶으면 이것도 같이
    @Query("""
        select new com.example.pentagon.dto.user.AdminUserDetailDTO(
            u.id, u.name, u.email, u.phone, u.active, u.role,
            case when exists (
                select 1
                from Subscription s
                where s.user = u and s.active = 1
            ) then true else false end
        )
        from User u
        where u.id = :userId
          and lower(u.email) not like 'deleted\\_%@deleted.local' escape '\\'
    """)
    Optional<AdminUserDetailDTO> findUserDetailWithActiveSubscription(@Param("userId") Long userId);
}
