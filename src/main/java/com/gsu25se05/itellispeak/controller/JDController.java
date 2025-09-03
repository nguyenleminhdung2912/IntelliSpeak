package com.gsu25se05.itellispeak.controller;

import com.gsu25se05.itellispeak.dto.Response;
import com.gsu25se05.itellispeak.dto.jd.CvJdMatchResultDTO;
import com.gsu25se05.itellispeak.dto.jd.GetAllJdDTO;
import com.gsu25se05.itellispeak.entity.CompanyJD;
import com.gsu25se05.itellispeak.entity.JD;
import com.gsu25se05.itellispeak.exception.auth.AuthAppException;
import com.gsu25se05.itellispeak.exception.auth.NotFoundException;
import com.gsu25se05.itellispeak.exception.auth.NotLoginException;
import com.gsu25se05.itellispeak.service.CompanyJDService;
import com.gsu25se05.itellispeak.service.JDService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/jd")
@CrossOrigin("**")
@SecurityRequirement(name = "api")
public class JDController {

    private final JDService jdService;
    private final CompanyJDService companyJDService;

    public JDController(JDService jdService, CompanyJDService companyJDService) {
        this.jdService = jdService;
        this.companyJDService = companyJDService;
    }

    @PostMapping("/analyze")
    public ResponseEntity<?> analyzeJD(@RequestParam("file") MultipartFile file) {
        try {
            JD jd = jdService.analyzeAndSaveJD(file);
            return ResponseEntity.ok(jd);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "System error: " + e.getMessage()));
        }
    }

    @GetMapping("/{jdId}/match-cv")
    @Operation(summary = "So khớp JD với CV active")
    public ResponseEntity<Response<CvJdMatchResultDTO>> matchJdWithActiveCv(@PathVariable Long jdId) {
        try {
            Response<CvJdMatchResultDTO> result = jdService.matchCurrentUsersActiveCvWithJdAI(jdId);
            return ResponseEntity.ok(new Response<>(200, "AI match computed successfully.", result.getData()));
        } catch (NotLoginException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new Response<>(401, e.getMessage(), null));
        } catch (NotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new Response<>(404, e.getMessage(), null));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new Response<>(400, "Error: " + e.getMessage(), null));
        }
    }


    @GetMapping("/{id}")
    public ResponseEntity<?> getJDById(@PathVariable Long id) {
        try {
            JD jd = jdService.getJDById(id);
            return ResponseEntity.ok(jd);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/list")
    public ResponseEntity<Response<List<GetAllJdDTO>>> getJDList() {
        try {
            Response<List<GetAllJdDTO>> result = jdService.getAllJDsByUser();

            Response<List<GetAllJdDTO>> response = Response.<List<GetAllJdDTO>>builder()
                    .code(200)
                    .message("The CV list has been retrieved successfully.")
                    .data(result.getData())
                    .build();

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Response<List<GetAllJdDTO>> errorResponse = new Response<>(400, "Error: " + e.getMessage(), null);
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    @Operation(summary = "Xoá JD cá nhân (soft delete)")
    @DeleteMapping("/{jdId}")
    public ResponseEntity<Response<Void>> deleteJD(@PathVariable Long jdId) {
        try {
            jdService.deleteJd(jdId);
            return ResponseEntity.ok(new Response<>(200, "JD deleted successfully.", null));
        } catch (NotLoginException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new Response<>(401, e.getMessage(), null));
        } catch (NotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new Response<>(404, e.getMessage(), null));
        } catch (AuthAppException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new Response<>(403, e.getMessage(), null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new Response<>(500, "Error: " + e.getMessage(), null));
        }
    }

    @PostMapping("/company/upload")
    public ResponseEntity<Response<CompanyJD>> uploadAndAnalyzeCompanyJD(@RequestParam("file") MultipartFile file) {
        try {
            CompanyJD companyJD = companyJDService.uploadAndAnalyzeCompanyJD(file);
            Response<CompanyJD> response = new Response<>(200, "Fetch data successfully.", companyJD);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new Response<>(400, "Error: " + e.getMessage(), null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new Response<>(400, "Error: " + e.getMessage(), null));
        }
    }

    @GetMapping("/company/jd/{company_jd_id}")
    @Operation(summary = "Lấy 1 JD của 1 Company")
    public ResponseEntity<Response<CompanyJD>> getCompanyJDWithEvaluates(@PathVariable Long company_jd_id) {
        try {
            CompanyJD companyJD = companyJDService.getCompanyJDWithEvaluates(company_jd_id);
            Response<CompanyJD> response = new Response<>(200, "Fetch data successfully.", companyJD);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException | NotFoundException | AuthAppException e) {
            return ResponseEntity.badRequest().body(new Response<>(400, "Error: " + e.getMessage(), null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new Response<>(400, "Error: " + e.getMessage(), null));
        }
    }

    @GetMapping("/company/{companyId}")
    @Operation(summary = "Lấy danh sách JD của 1 Company")
    public ResponseEntity<Response<List<CompanyJD>>> getCompanyJDsByCompanyId(@PathVariable Long companyId) {
        try {
            List<CompanyJD> jds = companyJDService.getCompanyJDsByCompanyId(companyId);
            Response<List<CompanyJD>> response = new Response<>(200, "Fetch data successfully.", jds);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException | NotFoundException | AuthAppException e) {
            return ResponseEntity.badRequest().body(new Response<>(400, "Error: " + e.getMessage(), null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new Response<>(400, "Error: " + e.getMessage(), null));
        }
    }

    @GetMapping("/company/uploadedJD")
    @Operation(summary = "Lấy danh sách JD của Company của HR đó")
    public ResponseEntity<Response<List<CompanyJD>>> getCompanyUploadedJD() {
        try {
            List<CompanyJD> jds = companyJDService.getCompanyUploadedJD();
            Response<List<CompanyJD>> response = new Response<>(200, "Fetch data successfully.", jds);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException | NotFoundException | AuthAppException e) {
            return ResponseEntity.badRequest().body(new Response<>(400, "Error: " + e.getMessage(), null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new Response<>(400, "Error: " + e.getMessage(), null));
        }
    }

    @Operation(summary = "Xoá Company JD (HR chỉ xoá JD thuộc công ty mình)")
    @DeleteMapping("/company/{companyJdId}")
    public ResponseEntity<Response<Void>> deleteCompanyJD(@PathVariable Long companyJdId) {
        try {
            companyJDService.deleteCompanyJD(companyJdId);
            return ResponseEntity.ok(new Response<>(200, "Company JD deleted successfully.", null));
        } catch (NotLoginException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new Response<>(401, e.getMessage(), null));
        } catch (NotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new Response<>(404, e.getMessage(), null));
        } catch (AuthAppException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new Response<>(403, e.getMessage(), null));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(new Response<>(400, e.getMessage(), null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new Response<>(500, "Error: " + e.getMessage(), null));
        }
    }
}
