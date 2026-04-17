package com.example.pentagon.repository;

import com.example.pentagon.domain.enums.InquiryStatus;
import com.example.pentagon.domain.support.Inquiry;
import com.example.pentagon.dto.inquiry.AdminInquiryDetailDTO;
import com.example.pentagon.dto.inquiry.AdminInquiryListDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface InquiryRepository extends JpaRepository<Inquiry, Long> {

    // 이메일로 찾고 + 페이징 처리해서 가져오기
    Page<Inquiry> findAllByUser_EmailOrderByCreatedAtDesc(String email, Pageable pageable);

    @Query(
            value = """
            select new com.example.pentagon.dto.inquiry.AdminInquiryListDTO(
                max(r.id),
                i.id,
                u.id,
                u.name,
                u.role,
                i.title,
                i.content,
                i.status,
                i.createdAt
            )
            from Inquiry i
            join i.user u
            left join InquiryReply r on r.inquiry = i
            where i.status = :status
            group by i.id, u.id, u.name, u.role, i.title, i.content, i.status, i.createdAt
            order by i.id desc
        """,
            countQuery = """
            select count(i)
            from Inquiry i
            where i.status = :status
        """
    )
    Page<AdminInquiryListDTO> findAdminInquiryList(@Param("status") InquiryStatus status, Pageable pageable);

    @Query(
            value = """
            select new com.example.pentagon.dto.inquiry.AdminInquiryListDTO(
                max(r.id),
                i.id,
                u.id,
                u.name,
                u.role,
                i.title,
                i.content,
                i.status,
                i.createdAt
            )
            from Inquiry i
            join i.user u
            left join InquiryReply r on r.inquiry = i
            where u.id = :userId
              and i.status = :status
            group by i.id, u.id, u.name, u.role, i.title, i.content, i.status, i.createdAt
            order by i.id desc
        """,
            countQuery = """
            select count(i)
            from Inquiry i
            where i.user.id = :userId
              and i.status = :status
        """
    )
    Page<AdminInquiryListDTO> findAdminInquiryListByUser(@Param("userId") Long userId,
                                                         @Param("status") InquiryStatus status,
                                                         Pageable pageable);

    @Query(
            value = """
            select new com.example.pentagon.dto.inquiry.AdminInquiryListDTO(
                max(r.id),
                i.id,
                u.id,
                u.name,
                u.role,
                i.title,
                i.content,
                i.status,
                i.createdAt
            )
            from Inquiry i
            join i.user u
            left join InquiryReply r on r.inquiry = i
            group by i.id, u.id, u.name, u.role, i.title, i.content, i.status, i.createdAt
            order by i.id desc
        """,
            countQuery = """
            select count(i)
            from Inquiry i
        """
    )
    Page<AdminInquiryListDTO> findAdminInquiryAllList(Pageable pageable);

    @Query(
            value = """
            select new com.example.pentagon.dto.inquiry.AdminInquiryListDTO(
                max(r.id),
                i.id,
                u.id,
                u.name,
                u.role,
                i.title,
                i.content,
                i.status,
                i.createdAt
            )
            from Inquiry i
            join i.user u
            left join InquiryReply r on r.inquiry = i
            where u.id = :userId
            group by i.id, u.id, u.name, u.role, i.title, i.content, i.status, i.createdAt
            order by i.id desc
        """,
            countQuery = """
            select count(i)
            from Inquiry i
            where i.user.id = :userId
        """
    )
    Page<AdminInquiryListDTO> findAdminInquiryAllListByUserId(@Param("userId") Long userId, Pageable pageable);

    @Query("""
        select new com.example.pentagon.dto.inquiry.AdminInquiryDetailDTO(
            i.id,
            u.id,
            u.name,
            u.email,
            u.phone,
            u.role,
            i.title,
            i.content,
            i.status,
            i.createdAt,
            i.updatedAt,
            r.id,
            r.content,
            r.createdAt
        )
        from Inquiry i
        join i.user u
        left join InquiryReply r on r.inquiry = i
        where i.id = :inquiryId
    """)
    Optional<AdminInquiryDetailDTO> findAdminInquiryDetail(@Param("inquiryId") Long inquiryId);

}
