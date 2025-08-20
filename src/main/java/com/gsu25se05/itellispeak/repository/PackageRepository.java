package com.gsu25se05.itellispeak.repository;

import com.gsu25se05.itellispeak.entity.Package;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PackageRepository extends JpaRepository<Package, Long> {
    List<Package> findByIsDeletedFalse();
    Package findByPackageIdAndIsDeletedFalse(Long packageId);
    Optional<Package> findByPackageName(String packageName);
}
