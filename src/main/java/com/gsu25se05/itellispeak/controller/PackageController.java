package com.gsu25se05.itellispeak.controller;

import com.gsu25se05.itellispeak.dto.Response;
import com.gsu25se05.itellispeak.dto.apackage.PackageRequestDTO;
import com.gsu25se05.itellispeak.dto.apackage.PackageResponseDTO;
import com.gsu25se05.itellispeak.service.PackageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/package")
@CrossOrigin("**")
@SecurityRequirement(name = "api")
public class PackageController {

    @Autowired
    private PackageService packageService;

    @Operation(summary = "Admin tạo mới Package")
    @PostMapping
    public Response<PackageResponseDTO> createPackage(@RequestBody PackageRequestDTO requestDTO) {
        return packageService.createPackage(requestDTO);
    }

    @GetMapping
    public Response<List<PackageResponseDTO>> getAllPackages() {
        return packageService.getAllPackages();
    }

    @GetMapping("/{id}")
    public Response<PackageResponseDTO> getPackageById(@PathVariable Long id) {
        return packageService.getPackageById(id);
    }

    @Operation(summary = "Admin cập nhật Package")
    @PutMapping("/{id}")
    public ResponseEntity<Response<PackageResponseDTO>> updatePackage(
            @PathVariable Long id,
            @RequestBody PackageRequestDTO requestDTO) {
        Response<PackageResponseDTO> response = packageService.updatePackage(id, requestDTO);
        return ResponseEntity.status(response.getCode()).body(response);
    }

    @Operation(summary = "Admin xoá Package")
    @DeleteMapping("/{id}")
    public ResponseEntity<Response<Void>> deletePackage(@PathVariable Long id) {
        Response<Void> response = packageService.deletePackage(id);
        return ResponseEntity.status(response.getCode()).body(response);
    }
}
