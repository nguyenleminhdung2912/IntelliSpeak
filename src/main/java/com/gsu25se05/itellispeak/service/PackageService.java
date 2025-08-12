package com.gsu25se05.itellispeak.service;

import com.gsu25se05.itellispeak.dto.Response;
import com.gsu25se05.itellispeak.dto.apackage.PackageRequestDTO;
import com.gsu25se05.itellispeak.dto.apackage.PackageResponseDTO;
import com.gsu25se05.itellispeak.entity.User;
import com.gsu25se05.itellispeak.exception.auth.NotLoginException;
import com.gsu25se05.itellispeak.repository.PackageRepository;
import com.gsu25se05.itellispeak.utils.AccountUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.gsu25se05.itellispeak.entity.Package;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PackageService {

    @Autowired
    private PackageRepository packageRepository;

    @Autowired
    private AccountUtils accountUtils;

    public Response<List<PackageResponseDTO>> getAllPackages() {
        List<Package> packages = packageRepository.findByIsDeletedFalse();

        List<PackageResponseDTO> responseList = packages.stream()
                .map(pkg -> new PackageResponseDTO(
                        pkg.getPackageId(),
                        pkg.getPackageName(),
                        pkg.getDescription(),
                        pkg.getPrice(),
                        pkg.getInterviewCount(),
                        pkg.getCvAnalyzeCount(),
                        pkg.getJdAnalyzeCount(),
                        pkg.getCreateAt(),
                        pkg.getUpdateAt()
                )).collect(Collectors.toList());

        return new Response<>(200, "Lấy danh sách gói thành công", responseList);
    }

    public Response<PackageResponseDTO> getPackageById(Long id) {
        Package pkg = packageRepository.findByPackageIdAndIsDeletedFalse(id);

        if (pkg == null) {
            return new Response<>(404, "Không tìm thấy gói", null);
        }

        PackageResponseDTO responseDTO = new PackageResponseDTO(
                pkg.getPackageId(),
                pkg.getPackageName(),
                pkg.getDescription(),
                pkg.getPrice(),
                pkg.getInterviewCount(),
                pkg.getCvAnalyzeCount(),
                pkg.getJdAnalyzeCount(),
                pkg.getCreateAt(),
                pkg.getUpdateAt()
        );

        return new Response<>(200, "Tìm thấy gói", responseDTO);
    }

    public Response<PackageResponseDTO> createPackage(PackageRequestDTO requestDTO) {

        User user = accountUtils.getCurrentAccount();
        if (user == null) throw new NotLoginException("Vui lòng đăng nhập để tiếp tục");

        Package newPackage = new Package();
        newPackage.setPackageName(requestDTO.getPackageName());
        newPackage.setDescription(requestDTO.getDescription());
        newPackage.setPrice(requestDTO.getPrice());
        newPackage.setCvAnalyzeCount(requestDTO.getCvAnalyzeCount());
        newPackage.setInterviewCount(requestDTO.getInterviewCount());
        newPackage.setJdAnalyzeCount(requestDTO.getJdAnalyzeCount());
        newPackage.setCreateAt(LocalDateTime.now());
        newPackage.setUpdateAt(LocalDateTime.now());
        newPackage.setIsDeleted(false);

        Package savedPackage = packageRepository.save(newPackage);

        PackageResponseDTO responseDTO = new PackageResponseDTO(
                savedPackage.getPackageId(),
                savedPackage.getPackageName(),
                savedPackage.getDescription(),
                savedPackage.getPrice(),
                savedPackage.getInterviewCount(),
                savedPackage.getCvAnalyzeCount(),
                savedPackage.getJdAnalyzeCount(),
                savedPackage.getCreateAt(),
                savedPackage.getUpdateAt()
        );
        return new Response<>(200, "Tạo gói thành công", responseDTO);
    }

    public Response<PackageResponseDTO> updatePackage(Long id, PackageRequestDTO requestDTO) {
        User user = accountUtils.getCurrentAccount();
        if (user == null) {
            return new Response<>(401, "Vui lòng đăng nhập để tiếp tục", null);
        }

        Package existingPackage = packageRepository.findByPackageIdAndIsDeletedFalse(id);
        if (existingPackage == null) {
            return new Response<>(404, "Không tìm thấy gói", null);
        }

        if (requestDTO.getPrice() != null && requestDTO.getPrice() < 2000) {
            return new Response<>(400, "Giá phải từ 2000 trở lên", null);
        }

        if (requestDTO.getPackageName() != null) {
            existingPackage.setPackageName(requestDTO.getPackageName());
        }
        if (requestDTO.getDescription() != null) {
            existingPackage.setDescription(requestDTO.getDescription());
        }
        if (requestDTO.getPrice() != null) {
            existingPackage.setPrice(requestDTO.getPrice());
        }
        if (requestDTO.getInterviewCount() != null) {
            existingPackage.setInterviewCount(requestDTO.getInterviewCount());
        }
        if (requestDTO.getCvAnalyzeCount() != null) {
            existingPackage.setCvAnalyzeCount(requestDTO.getCvAnalyzeCount());
        }
        if (requestDTO.getJdAnalyzeCount() != null) {
            existingPackage.setJdAnalyzeCount(requestDTO.getJdAnalyzeCount());
        }

        existingPackage.setUpdateAt(LocalDateTime.now());

        Package updatedPackage = packageRepository.save(existingPackage);

        PackageResponseDTO responseDTO = new PackageResponseDTO(
                updatedPackage.getPackageId(),
                updatedPackage.getPackageName(),
                updatedPackage.getDescription(),
                updatedPackage.getPrice(),
                updatedPackage.getInterviewCount(),
                updatedPackage.getCvAnalyzeCount(),
                updatedPackage.getJdAnalyzeCount(),
                updatedPackage.getCreateAt(),
                updatedPackage.getUpdateAt()
        );

        return new Response<>(200, "Cập nhật gói thành công", responseDTO);
    }

    public Response<Void> deletePackage(Long id) {
        User user = accountUtils.getCurrentAccount();
        if (user == null) {
            return new Response<>(401, "Vui lòng đăng nhập để tiếp tục", null);
        }

        Package existingPackage = packageRepository.findByPackageIdAndIsDeletedFalse(id);
        if (existingPackage == null) {
            return new Response<>(404, "Không tìm thấy gói", null);
        }

        existingPackage.setIsDeleted(true);
        existingPackage.setUpdateAt(LocalDateTime.now());
        packageRepository.save(existingPackage);

        return new Response<>(200, "Xóa gói thành công", null);
    }

}
