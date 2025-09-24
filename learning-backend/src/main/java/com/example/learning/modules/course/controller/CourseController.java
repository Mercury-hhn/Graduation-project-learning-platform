package com.example.learning.modules.course.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.learning.common.model.ApiResponse;
import com.example.learning.modules.course.entity.Course;
import com.example.learning.modules.course.entity.Resource;
import com.example.learning.modules.course.entity.Section;
import com.example.learning.modules.course.service.CourseService;
import com.example.learning.storage.FileStorageService;
import jakarta.validation.constraints.NotNull;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 课程相关接口：提供分页查询、详情、教学端管理与资源上传。
 */
@RestController
@RequestMapping("/api")
public class CourseController {

    private final CourseService courseService;
    private final FileStorageService fileStorageService;

    public CourseController(CourseService courseService, FileStorageService fileStorageService) {
        this.courseService = courseService;
        this.fileStorageService = fileStorageService;
    }

    /**
     * 分页查询课程。
     */
    @GetMapping("/courses")
    public ApiResponse<Page<Course>> list(@RequestParam(required = false) String kw,
                                          @RequestParam(required = false) String tag,
                                          @RequestParam(required = false) String sort,
                                          @RequestParam(defaultValue = "1") long page,
                                          @RequestParam(defaultValue = "10") long size) {
        return ApiResponse.ok(courseService.queryCourses(kw, tag, sort, page, size));
    }

    /**
     * 课程详情，包含章节与资源。
     */
    @GetMapping("/courses/{id}")
    public ApiResponse<Map<String, Object>> detail(@PathVariable Long id) {
        CourseService.CourseDetail detail = courseService.detail(id);
        Map<String, Object> map = new HashMap<>();
        map.put("course", detail.course());
        List<Map<String, Object>> sectionList = detail.sections().stream().map(section -> {
            Map<String, Object> s = new HashMap<>();
            s.put("id", section.getId());
            s.put("title", section.getTitle());
            s.put("sort", section.getSort());
            s.put("resources", detail.sectionResources().getOrDefault(section.getId(), List.of()));
            return s;
        }).toList();
        map.put("sections", sectionList);
        return ApiResponse.ok(map);
    }

    /**
     * 新增课程。
     */
    @PreAuthorize("hasAnyAuthority('TEACHER','ADMIN')")
    @PostMapping("/courses")
    public ApiResponse<Course> create(Course course) {
        return ApiResponse.ok(courseService.createCourse(course));
    }

    /**
     * 更新课程。
     */
    @PreAuthorize("hasAnyAuthority('TEACHER','ADMIN')")
    @PutMapping("/courses")
    public ApiResponse<Void> update(Course course) {
        courseService.updateCourse(course);
        return ApiResponse.ok(null);
    }

    /**
     * 删除课程。
     */
    @PreAuthorize("hasAnyAuthority('TEACHER','ADMIN')")
    @DeleteMapping("/courses/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        courseService.deleteCourse(id);
        return ApiResponse.ok(null);
    }

    /**
     * 新增章节。
     */
    @PreAuthorize("hasAnyAuthority('TEACHER','ADMIN')")
    @PostMapping("/sections")
    public ApiResponse<Section> createSection(Section section) {
        return ApiResponse.ok(courseService.createSection(section));
    }

    /**
     * 上传资源并创建记录。
     */
    @PreAuthorize("hasAnyAuthority('TEACHER','ADMIN')")
    @PostMapping("/resources/upload")
    public ApiResponse<Resource> upload(@RequestParam("file") MultipartFile file,
                                        @RequestParam Long courseId,
                                        @RequestParam(required = false) Long sectionId,
                                        @RequestParam String type) {
        FileStorageService.StoredFile stored = fileStorageService.store(file);
        Resource resource = new Resource();
        resource.setCourseId(courseId);
        resource.setSectionId(sectionId);
        resource.setType(type);
        resource.setUrl(stored.url());
        resource.setSize(stored.size());
        return ApiResponse.ok(courseService.createResource(resource));
    }
}