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

        return new Response<>(200, "Successfully retrieved package list", responseList);
    }

    public Response<PackageResponseDTO> getPackageById(Long id) {
        Package pkg = packageRepository.findByPackageIdAndIsDeletedFalse(id);

        if (pkg == null) {
            return new Response<>(404, "Package not found", null);
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

        return new Response<>(200, "Package found", responseDTO);
    }

    public Response<PackageResponseDTO> createPackage(PackageRequestDTO requestDTO) {

        User user = accountUtils.getCurrentAccount();
        if (user == null) throw new NotLoginException("Please log in to continue");

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
        return new Response<>(200, "Package created successfully", responseDTO);
    }

    public Response<PackageResponseDTO> updatePackage(Long id, PackageRequestDTO requestDTO) {
        User user = accountUtils.getCurrentAccount();
        if (user == null) {
            return new Response<>(401, "Please log in to continue", null);
        }

        Package existingPackage = packageRepository.findByPackageIdAndIsDeletedFalse(id);
        if (existingPackage == null) {
            return new Response<>(404, "Package not found", null);
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

        return new Response<>(200, "Package updated successfully", responseDTO);
    }

    public Response<Void> deletePackage(Long id) {
        User user = accountUtils.getCurrentAccount();
        if (user == null) {
            return new Response<>(401, "Please log in to continue", null);
        }

        Package existingPackage = packageRepository.findByPackageIdAndIsDeletedFalse(id);
        if (existingPackage == null) {
            return new Response<>(404, "Package not found", null);
        }

        existingPackage.setIsDeleted(true);
        existingPackage.setUpdateAt(LocalDateTime.now());
        packageRepository.save(existingPackage);

        return new Response<>(200, "Package deleted successfully", null);
    }

}
