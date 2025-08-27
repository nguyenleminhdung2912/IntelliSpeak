package com.gsu25se05.itellispeak.service;

import com.gsu25se05.itellispeak.dto.Response;
import com.gsu25se05.itellispeak.dto.auth.reponse.UserDTO;
import com.gsu25se05.itellispeak.dto.transaction.PackageBriefDTO;
import com.gsu25se05.itellispeak.dto.transaction.TransactionDTO;
import com.gsu25se05.itellispeak.entity.Transaction;
import com.gsu25se05.itellispeak.entity.User;
import com.gsu25se05.itellispeak.entity.UserUsage;
import com.gsu25se05.itellispeak.repository.TransactionRepository;
import com.gsu25se05.itellispeak.repository.UserUsageRepository;
import com.gsu25se05.itellispeak.utils.AccountUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TransactionService {
    @Autowired
    TransactionRepository transactionRepository;

    @Autowired
    UserUsageRepository userUsageRepository;

    @Autowired
    AccountUtils accountUtils;


    public List<TransactionDTO> getAllTransactionDetails() {
        return transactionRepository.findAll().stream()
                .map(this::mapToDetailDTO)
                .toList();
    }

    public Response<List<TransactionDTO>> getMyTransactionHistory() {
        User current = accountUtils.getCurrentAccount();
        if (current == null) {
            return new Response<>(401, "Please log in to continue", null);
        }

        List<TransactionDTO> data = transactionRepository
                .findByUserOrderByCreateAtDesc(current)
                .stream()
                .map(this::mapToDetailDTO)
                .toList();

        return new Response<>(200, "Success", data);
    }


    private TransactionDTO mapToDetailDTO(Transaction t) {
        // Package -> PackageBriefDTO
        var p = t.getAPackage();
        PackageBriefDTO pDto = null;
        if (p != null) {
            pDto = PackageBriefDTO.builder()
                    .packageId(p.getPackageId())
                    .packageName(p.getPackageName())
                    .description(p.getDescription())
                    .price(p.getPrice())
                    .interviewCount(p.getInterviewCount())
                    .cvAnalyzeCount(p.getCvAnalyzeCount())
                    .jdAnalyzeCount(p.getJdAnalyzeCount())
                    .build();
        }

        // User -> UserDTO (điền đủ usage)
        UserDTO uDto = mapUserToDTO(t.getUser());

        return TransactionDTO.builder()
                .id(t.getId())
                .orderCode(t.getOrderCode())
                .transactionStatus(t.getTransactionStatus())
                .amount(t.getAmount())
                .description(t.getDescription())
                .createAt(t.getCreateAt())
                .aPackage(pDto)
                .user(uDto)
                .build();
    }

    private UserDTO mapUserToDTO(User user) {
        if (user == null) return null;

        // Lấy usage
        var usageOpt = userUsageRepository.findByUser(user);
        int cvUsed  = usageOpt.map(UserUsage::getCvAnalyzeUsed).orElse(0);
        int jdUsed  = usageOpt.map(UserUsage::getJdAnalyzeUsed).orElse(0);
        int itvUsed = usageOpt.map(UserUsage::getInterviewUsed).orElse(0);

        String email = user.getEmail();
        String username = (email != null && email.contains("@")) ? email.substring(0, email.indexOf('@')) : "";

        Long packageId = null;
        String packageName = null;
        if (user.getAPackage() != null) {
            packageId = user.getAPackage().getPackageId();
            packageName = user.getAPackage().getPackageName();
        }

        return UserDTO.builder()
                .userId(user.getUserId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .userName(username)
                .email(user.getEmail())
                .role(user.getRole())
                .packageId(packageId)
                .packageName(packageName)
                .birthday(user.getBirthday())
                .avatar(user.getAvatar())
                .status(user.getStatus())
                .phone(user.getPhone())
                .bio(user.getBio())
                .website(user.getWebsite())
                .github(user.getGithub())
                .linkedin(user.getLinkedin())
                .facebook(user.getFacebook())
                .youtube(user.getYoutube())
                .createAt(user.getCreateAt())
                .updateAt(user.getUpdateAt())
                .isDeleted(user.getIsDeleted())
                .cvAnalyzeUsed(cvUsed)
                .jdAnalyzeUsed(jdUsed)
                .interviewUsed(itvUsed)
                .build();
    }
}
