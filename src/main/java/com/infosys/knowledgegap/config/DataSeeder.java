package com.infosys.knowledgegap.config;

import com.infosys.knowledgegap.entity.AssessmentQuestion;
import com.infosys.knowledgegap.entity.Department;
import com.infosys.knowledgegap.entity.Role;
import com.infosys.knowledgegap.entity.Skill;
import com.infosys.knowledgegap.entity.SkillCategory;
import com.infosys.knowledgegap.enums.RoleType;
import com.infosys.knowledgegap.repository.AssessmentQuestionRepository;
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
    private final AssessmentQuestionRepository assessmentQuestionRepository;

    @Override
    public void run(String... args) {
        seedRoles();
        seedDepartments();
        seedSkills();
        seedAssessmentQuestions();
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

    private void seedAssessmentQuestions() {
        seedQuestionsForSkill("Java", List.of(
                new Q("What does JVM stand for?",
                        List.of("Java Virtual Machine", "Java Verified Method", "Java Variable Manager", "Java Version Module"), 0),
                new Q("Which keyword is used to inherit a class in Java?",
                        List.of("implements", "extends", "inherits", "super"), 1),
                new Q("Which collection type does NOT allow duplicate elements?",
                        List.of("List", "Set", "Map", "Array"), 1),
                new Q("What is the default value of a boolean instance variable?",
                        List.of("true", "false", "null", "0"), 1),
                new Q("Which of these is used for exception handling in Java?",
                        List.of("try-catch", "if-else", "for-loop", "switch-case"), 0)
        ));

        seedQuestionsForSkill("Python", List.of(
                new Q("Which keyword defines a function in Python?",
                        List.of("func", "def", "function", "lambda"), 1),
                new Q("What is the output type of `type([])` in Python?",
                        List.of("list", "tuple", "dict", "set"), 0),
                new Q("Which of these is used to handle exceptions in Python?",
                        List.of("try/except", "try/catch", "catch/throw", "on error"), 0),
                new Q("How do you create a virtual environment in Python?",
                        List.of("python -m venv env", "pip install venv", "python create venv", "venv --new"), 0),
                new Q("What does PEP 8 refer to?",
                        List.of("A Python library", "Python's style guide", "A testing framework", "A package manager"), 1)
        ));

        seedQuestionsForSkill("Communication", List.of(
                new Q("What is the most effective way to ensure a message was understood?",
                        List.of("Speak louder", "Ask for feedback/confirmation", "Repeat it once", "Send it in writing only"), 1),
                new Q("Active listening primarily involves:",
                        List.of("Waiting for your turn to speak", "Fully focusing and responding thoughtfully", "Multitasking while listening", "Interrupting to add value"), 1),
                new Q("In a disagreement at work, the best first step is usually to:",
                        List.of("Escalate immediately", "Understand the other person's perspective", "Avoid the topic entirely", "Assert your position firmly"), 1),
                new Q("Which is a sign of clear written communication?",
                        List.of("Long, detailed paragraphs", "Concise, structured, and unambiguous language", "Heavy use of jargon", "Minimal punctuation"), 1),
                new Q("Non-verbal communication includes:",
                        List.of("Only written text", "Tone, body language, and facial expressions", "Only email formatting", "Font choice"), 1)
        ));

        seedQuestionsForSkill("JavaScript", List.of(
                new Q("Which keyword declares a block-scoped variable in JavaScript?",
                        List.of("var", "let", "global", "static"), 1),
                new Q("What does `===` check for in JavaScript?",
                        List.of("Value only", "Value and type", "Type only", "Reference only"), 1),
                new Q("Which method converts a JSON string into a JavaScript object?",
                        List.of("JSON.parse()", "JSON.stringify()", "JSON.toObject()", "Object.parse()"), 0),
                new Q("What is a Promise used for in JavaScript?",
                        List.of("Styling components", "Handling asynchronous operations", "Declaring variables", "Looping over arrays"), 1),
                new Q("Which array method creates a new array with transformed elements?",
                        List.of("forEach()", "map()", "filter()", "reduce()"), 1)
        ));

        seedQuestionsForSkill("SQL", List.of(
                new Q("Which SQL clause is used to filter grouped results?",
                        List.of("WHERE", "HAVING", "GROUP BY", "ORDER BY"), 1),
                new Q("Which type of JOIN returns all rows from both tables, matched or not?",
                        List.of("INNER JOIN", "LEFT JOIN", "FULL OUTER JOIN", "RIGHT JOIN"), 2),
                new Q("What does the SQL `UNIQUE` constraint ensure?",
                        List.of("A column allows nulls", "No duplicate values in a column", "A column is indexed", "A column is a primary key"), 1),
                new Q("Which command permanently removes a table and its structure?",
                        List.of("DELETE", "TRUNCATE", "DROP", "REMOVE"), 2),
                new Q("What is a primary key used for?",
                        List.of("Sorting rows", "Uniquely identifying each row", "Filtering duplicate columns", "Encrypting data"), 1)
        ));

        seedQuestionsForSkill("Spring Boot", List.of(
                new Q("Which annotation marks a class as a REST controller in Spring Boot?",
                        List.of("@Controller", "@RestController", "@Service", "@Component"), 1),
                new Q("What does Spring Boot's auto-configuration primarily do?",
                        List.of("Auto-writes business logic", "Configures beans based on classpath and properties", "Deploys the app automatically", "Compiles code faster"), 1),
                new Q("Which file is commonly used for Spring Boot application configuration?",
                        List.of("web.xml", "application.yml", "pom.xml", "settings.json"), 1),
                new Q("Which annotation injects a dependency in Spring?",
                        List.of("@Inject", "@Autowired", "@Bean", "@Import"), 1),
                new Q("What does `spring-boot-starter-data-jpa` provide?",
                        List.of("REST API tools", "Database access via Hibernate/JPA", "Security filters", "Testing utilities"), 1)
        ));

        seedQuestionsForSkill("React", List.of(
                new Q("What hook is used to manage state in a functional component?",
                        List.of("useEffect", "useState", "useRef", "useMemo"), 1),
                new Q("What does JSX allow you to write?",
                        List.of("SQL inside JavaScript", "HTML-like syntax inside JavaScript", "CSS inside HTML", "Python inside React"), 1),
                new Q("Which hook runs side effects after render?",
                        List.of("useState", "useEffect", "useContext", "useCallback"), 1),
                new Q("How does data typically flow in React?",
                        List.of("Two-way binding by default", "Unidirectional, from parent to child via props", "Only through global variables", "Randomly between components"), 1),
                new Q("What is the purpose of a `key` prop in a list?",
                        List.of("Styling", "Helping React identify which items changed", "Setting default values", "Encrypting props"), 1)
        ));

        seedQuestionsForSkill("Docker", List.of(
                new Q("What is a Docker image?",
                        List.of("A running instance of a container", "A read-only template used to create containers", "A virtual machine", "A network configuration file"), 1),
                new Q("Which file defines how a Docker image is built?",
                        List.of("docker-compose.yml", "Dockerfile", "image.json", "container.yaml"), 1),
                new Q("What command lists all running containers?",
                        List.of("docker ps", "docker list", "docker show", "docker containers"), 0),
                new Q("What is the purpose of a Docker volume?",
                        List.of("Scaling containers", "Persisting data outside the container lifecycle", "Building images faster", "Networking between hosts"), 1),
                new Q("What does `docker-compose` help manage?",
                        List.of("Single container builds only", "Multi-container applications", "Only container images", "Kubernetes clusters"), 1)
        ));

        seedQuestionsForSkill("Kubernetes", List.of(
                new Q("What is the smallest deployable unit in Kubernetes?",
                        List.of("Container", "Pod", "Node", "Cluster"), 1),
                new Q("What does a Kubernetes Service do?",
                        List.of("Stores configuration secrets", "Provides stable networking access to a set of pods", "Builds container images", "Schedules cron jobs only"), 1),
                new Q("Which component schedules pods onto nodes?",
                        List.of("kubelet", "scheduler", "etcd", "kube-proxy"), 1),
                new Q("What is a Deployment used for in Kubernetes?",
                        List.of("Manual pod creation only", "Declaratively managing replicas and rollouts of pods", "Only storage management", "DNS resolution"), 1),
                new Q("What does `kubectl` refer to?",
                        List.of("A container runtime", "The Kubernetes command-line tool", "A monitoring dashboard", "A cloud provider"), 1)
        ));

        seedQuestionsForSkill("AWS", List.of(
                new Q("Which AWS service provides scalable object storage?",
                        List.of("EC2", "S3", "RDS", "Lambda"), 1),
                new Q("What is AWS Lambda used for?",
                        List.of("Running serverless functions", "Managing virtual machines", "Object storage", "DNS routing"), 0),
                new Q("Which service is a managed relational database in AWS?",
                        List.of("DynamoDB", "RDS", "S3", "CloudFront"), 1),
                new Q("What does IAM stand for in AWS?",
                        List.of("Internet Access Manager", "Identity and Access Management", "Instance Allocation Module", "Infrastructure Automation Manager"), 1),
                new Q("Which AWS service is used for content delivery (CDN)?",
                        List.of("CloudFront", "CloudWatch", "CloudTrail", "CloudFormation"), 0)
        ));

        seedQuestionsForSkill("CI/CD", List.of(
                new Q("What does CI in CI/CD stand for?",
                        List.of("Code Integration", "Continuous Integration", "Container Isolation", "Custom Instance"), 1),
                new Q("What is the main goal of Continuous Deployment?",
                        List.of("Manual approval for every release", "Automatically releasing code changes to production", "Writing more tests", "Slowing down releases for safety"), 1),
                new Q("Which of these is a common CI/CD tool?",
                        List.of("Jenkins", "Photoshop", "Excel", "Figma"), 0),
                new Q("What is a build pipeline?",
                        List.of("A single manual deployment step", "An automated sequence of build, test, and deploy stages", "A database schema", "A network diagram"), 1),
                new Q("Why are automated tests important in a CI/CD pipeline?",
                        List.of("They slow down releases", "They catch issues early before deployment", "They replace code reviews entirely", "They are optional and rarely used"), 1)
        ));

        seedQuestionsForSkill("Git", List.of(
                new Q("What command creates a new branch in Git?",
                        List.of("git branch <name>", "git new <name>", "git create <name>", "git init <name>"), 0),
                new Q("What does `git commit` do?",
                        List.of("Uploads code to a remote server", "Saves a snapshot of staged changes locally", "Deletes tracked files", "Creates a new repository"), 1),
                new Q("What is the purpose of `.gitignore`?",
                        List.of("Lists files Git should track only", "Lists files/folders Git should NOT track", "Stores commit history", "Configures branch permissions"), 1),
                new Q("What does `git merge` do?",
                        List.of("Deletes a branch", "Combines changes from one branch into another", "Creates a new repository", "Reverts the last commit"), 1),
                new Q("What is a merge conflict?",
                        List.of("A network error during push", "Competing changes Git cannot automatically reconcile", "A missing commit message", "An invalid branch name"), 1)
        ));

        seedQuestionsForSkill("Linux", List.of(
                new Q("Which command lists files in a directory?",
                        List.of("ls", "dir", "list", "show"), 0),
                new Q("What does `chmod` change?",
                        List.of("File ownership", "File permissions", "File name", "File location"), 1),
                new Q("Which command shows currently running processes?",
                        List.of("ps", "run", "proc", "task"), 0),
                new Q("What is the root user in Linux?",
                        List.of("A regular user account", "The superuser with full system privileges", "A guest account", "A network service"), 1),
                new Q("Which command is used to search inside files for text?",
                        List.of("find", "grep", "locate", "search"), 1)
        ));

        seedQuestionsForSkill("Leadership", List.of(
                new Q("Effective delegation primarily involves:",
                        List.of("Assigning tasks without context", "Matching tasks to the right people with clear expectations", "Doing all the work yourself", "Avoiding accountability"), 1),
                new Q("A good leader responds to team mistakes by:",
                        List.of("Publicly blaming the individual", "Focusing on learning and improvement", "Ignoring the issue entirely", "Reassigning blame to another team"), 1),
                new Q("Which best describes situational leadership?",
                        List.of("Using the same style for every situation", "Adapting your leadership style to the person and context", "Only leading during emergencies", "Avoiding decisions entirely"), 1),
                new Q("Giving constructive feedback should be:",
                        List.of("Vague and infrequent", "Specific, timely, and actionable", "Only negative", "Delivered publicly to motivate others"), 1),
                new Q("What builds trust within a team most effectively?",
                        List.of("Micromanaging every task", "Consistency between words and actions", "Withholding information", "Frequent last-minute changes"), 1)
        ));

        seedQuestionsForSkill("Problem Solving", List.of(
                new Q("What is typically the first step in structured problem solving?",
                        List.of("Implement a solution immediately", "Clearly define the actual problem", "Assign blame", "Skip to brainstorming solutions"), 1),
                new Q("Root cause analysis aims to:",
                        List.of("Treat surface-level symptoms only", "Identify the underlying cause of an issue", "Avoid investigating the issue", "Assign responsibility quickly"), 1),
                new Q("Which is a common problem-solving technique?",
                        List.of("5 Whys", "Random guessing", "Ignoring constraints", "Avoiding data"), 0),
                new Q("Why is it useful to consider multiple solutions before choosing one?",
                        List.of("It wastes time", "It helps evaluate trade-offs and pick the best fit", "Only one solution is ever valid", "It's not useful"), 1),
                new Q("After implementing a solution, what should you do?",
                        List.of("Assume it worked without checking", "Evaluate results and adjust if needed", "Immediately move to unrelated tasks", "Avoid documenting the outcome"), 1)
        ));

        seedQuestionsForSkill("Teamwork", List.of(
                new Q("Which behavior most strengthens team collaboration?",
                        List.of("Withholding information from teammates", "Openly sharing updates and asking for help when needed", "Working in isolation", "Avoiding team meetings"), 1),
                new Q("How should conflicting opinions in a team ideally be resolved?",
                        List.of("Through open discussion and compromise", "By avoiding the topic", "By the loudest voice winning", "By escalating immediately without discussion"), 0),
                new Q("What does psychological safety in a team mean?",
                        List.of("Avoiding all disagreement", "Feeling safe to speak up without fear of punishment", "Working without any feedback", "Following orders without question"), 1),
                new Q("A strong team member typically:",
                        List.of("Takes credit for others' work", "Supports teammates and shares credit fairly", "Avoids helping others", "Competes against teammates"), 1),
                new Q("Why are clear roles important in a team?",
                        List.of("They reduce confusion and duplicated effort", "They limit collaboration", "They are unnecessary for small teams", "They replace the need for communication"), 0)
        ));
    }

    private record Q(String text, List<String> options, int correctIndex) {}

    private void seedQuestionsForSkill(String skillName, List<Q> questions) {
        Skill skill = skillRepository.findByName(skillName).orElse(null);
        if (skill == null) return;
        if (!assessmentQuestionRepository.findBySkillId(skill.getId()).isEmpty()) return;

        for (Q q : questions) {
            assessmentQuestionRepository.save(AssessmentQuestion.builder()
                    .skill(skill)
                    .questionText(q.text())
                    .options(q.options())
                    .correctOptionIndex(q.correctIndex())
                    .difficultyWeight(1)
                    .build());
        }
    }
}
