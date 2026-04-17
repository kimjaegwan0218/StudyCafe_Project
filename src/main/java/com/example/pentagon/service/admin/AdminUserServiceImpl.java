package com.example.pentagon.service.admin;


//import jakarta.transaction.Transactional;
import com.example.pentagon.domain.User;
import com.example.pentagon.dto.user.AdminUserDetailDTO;
import com.example.pentagon.dto.user.AdminUserListDTO;
import com.example.pentagon.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@RequiredArgsConstructor
public class AdminUserServiceImpl implements AdminUserService {

    private final UserRepository userRepository;



    private PageRequest pageRequest(int page, int size) {
        return PageRequest.of(page, size);
    }

    @Override
    public AdminUserDetailDTO getUserDetail(Long id) {
        return userRepository.findUserDetailWithActiveSubscription(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 유저 없음!"));
    }

    @Override
    public Page<AdminUserListDTO> getAllUsers(int page, int size) {
        return userRepository.findAdminUserList(pageRequest(page, size));
    }

    @Override
    public Page<AdminUserListDTO> searchUsers(String keyword, int page, int size) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return userRepository.findAdminUserList(pageRequest(page, size));
        }
        return userRepository.searchAdminUserList(keyword.trim(), pageRequest(page, size));
    }

    @Override
    @Transactional
    public void setActive(Long userId, boolean active) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("유저 없음: " + userId));

        // ✅ 탈퇴 유저는 동결/해제 못 하게
        if (isDeletedEmail(user.getEmail())) {
            throw new IllegalStateException("탈퇴 처리된 유저는 상태 변경할 수 없습니다.");
        }

        user.setActive(active);
    }
    private boolean isDeletedEmail(String email) {
        if (email == null) return false;
        String e = email.toLowerCase();
        return e.startsWith("deleted_") && e.endsWith("@deleted.local");
    }

}
