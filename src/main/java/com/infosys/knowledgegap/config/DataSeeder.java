package com.infosys.knowledgegap.config;

import com.infosys.knowledgegap.entity.Department;
import com.infosys.knowledgegap.entity.Role;
import com.infosys.knowledgegap.entity.Skill;
import com.infosys.knowledgegap.entity.SkillCategory;
import com.infosys.knowledgegap.enums.RoleType;
import com.infosys.knowledgegap.repository.DepartmentRepository;
import com.infosys.knowledgegap.repository.RoleRepository;
import com.infosys.knowledgegap.repository.SkillCategoryRepository;
import com.infosys.knowledgegap.repository.SkillRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final DepartmentRepository departmentRepository;
    private final SkillCategoryRepository skillCategoryRepository;
    private final SkillRepository skillRepository;

    @Override
    public void run(String... args) {
        seedRoles();
        seedDepartments();
        seedSkills();
    }

    private void seedRoles() {
        seedRole(RoleType.EMPLOYEE, "Standard employee with access to personal skill profile and learning paths");
        seedRole(RoleType.TEAM_LEAD_MANAGER, "Team lead / manager with visibility into team skill gaps");
        seedRole(RoleType.HR_SPECIALIST, "HR specialist managing workforce skill data and training programs");
        seedRole(RoleType.DEPARTMENT_HEAD, "Department head with department-wide gap intelligence access");
        seedRole(RoleType.LEARNING_DEVELOPMENT_ADMIN, "L&D admin managing training catalog and recommendations");
        seedRole(RoleType.SYSTEM_ADMINISTRATOR, "System administrator with full platform access");
    }

    private void seedRole(RoleType type, String description) {
        roleRepository.findByName(type).orElseGet(() ->
                roleRepository.save(Role.builder().name(type).description(description).build())
        );
    }

    private void seedDepartments() {
        List<String> departments = List.of("Engineering", "Product", "HR", "Sales", "Finance", "Design");
        for (String name : departments) {
            if (!departmentRepository.existsByName(name)) {
                departmentRepository.save(Department.builder().name(name).description(name + " department").build());
            }
        }
    }

    private void seedSkills() {
        Map<String, List<String>> categoryToSkills = Map.of(
                "Programming Languages", List.of("Java", "Python", "JavaScript", "SQL"),
                "Frameworks & Tools", List.of("Spring Boot", "React", "Docker", "Kubernetes"),
                "Cloud & DevOps", List.of("AWS", "CI/CD", "Git", "Linux"),
                "Soft Skills", List.of("Communication", "Leadership", "Problem Solving", "Teamwork")
        );

        categoryToSkills.forEach((categoryName, skills) -> {
            SkillCategory category = skillCategoryRepository.findByName(categoryName)
                    .orElseGet(() -> skillCategoryRepository.save(
                            SkillCategory.builder().name(categoryName).description(categoryName).build()));

            for (String skillName : skills) {
                if (!skillRepository.existsByName(skillName)) {
                    skillRepository.save(Skill.builder()
                            .name(skillName)
                            .description(skillName)
                            .category(category)
                            .active(true)
                            .build());
                }
            }
        });
    }
}
