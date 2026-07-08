package com.infosys.knowledgegap.config;

import com.infosys.knowledgegap.entity.AssessmentQuestion;
import com.infosys.knowledgegap.entity.CompetencyRequirement;
import com.infosys.knowledgegap.entity.Department;
import com.infosys.knowledgegap.entity.Role;
import com.infosys.knowledgegap.entity.RoleCompetencyFramework;
import com.infosys.knowledgegap.entity.Skill;
import com.infosys.knowledgegap.entity.SkillCategory;
import com.infosys.knowledgegap.enums.ProficiencyLevel;
import com.infosys.knowledgegap.enums.RoleType;
import com.infosys.knowledgegap.repository.AssessmentQuestionRepository;
import com.infosys.knowledgegap.repository.CompetencyRequirementRepository;
import com.infosys.knowledgegap.repository.DepartmentRepository;
import com.infosys.knowledgegap.repository.RoleCompetencyFrameworkRepository;
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
    private final RoleCompetencyFrameworkRepository frameworkRepository;
    private final CompetencyRequirementRepository requirementRepository;

    @Override
    public void run(String... args) {
        seedRoles();
        seedDepartments();
        seedSkills();
        seedAssessmentQuestions();
        seedCompetencyFrameworks();
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

    // Q record: text, optional code snippet (null for non-coding questions), options, correct index
    private record Q(String text, String code, List<String> options, int correctIndex) {
        static Q of(String text, List<String> options, int correctIndex) {
            return new Q(text, null, options, correctIndex);
        }
        static Q withCode(String text, String code, List<String> options, int correctIndex) {
            return new Q(text, code, options, correctIndex);
        }
    }

    private void seedAssessmentQuestions() {

        seedQuestionsForSkill("Java", List.of(
                Q.of("What does JVM stand for?",
                        List.of("Java Virtual Machine", "Java Verified Method", "Java Variable Manager", "Java Version Module"), 0),
                Q.of("Which keyword is used to inherit a class in Java?",
                        List.of("implements", "extends", "inherits", "super"), 1),
                Q.of("Which collection type does NOT allow duplicate elements?",
                        List.of("List", "Set", "Map", "Array"), 1),
                Q.of("What is the default value of a boolean instance variable?",
                        List.of("true", "false", "null", "0"), 1),
                Q.of("Which of these is used for exception handling in Java?",
                        List.of("try-catch", "if-else", "for-loop", "switch-case"), 0),
                Q.withCode("What is the output of this code?",
                        "int x = 5;\nint y = 2;\nSystem.out.println(x / y);",
                        List.of("2.5", "2", "2.0", "Compile error"), 1),
                Q.withCode("What will this code print?",
                        "String a = \"hello\";\nString b = \"hello\";\nSystem.out.println(a == b);",
                        List.of("true", "false", "Compile error", "null"), 0),
                Q.withCode("What is the output?",
                        "for (int i = 0; i < 3; i++) {\n    if (i == 1) continue;\n    System.out.print(i);\n}",
                        List.of("012", "02", "01", "0123"), 1),
                Q.withCode("What does this code print?",
                        "int[] arr = {1, 2, 3};\nSystem.out.println(arr.length);",
                        List.of("2", "3", "4", "Compile error"), 1),
                Q.withCode("What is the result of this code?",
                        "public static int add(int a, int b) {\n    return a + b;\n}\n\nSystem.out.println(add(2, 3));",
                        List.of("5", "23", "Compile error", "0"), 0)
        ));

        seedQuestionsForSkill("Python", List.of(
                Q.of("Which keyword defines a function in Python?",
                        List.of("func", "def", "function", "lambda"), 1),
                Q.of("What is the output type of `type([])` in Python?",
                        List.of("list", "tuple", "dict", "set"), 0),
                Q.of("Which of these is used to handle exceptions in Python?",
                        List.of("try/except", "try/catch", "catch/throw", "on error"), 0),
                Q.of("How do you create a virtual environment in Python?",
                        List.of("python -m venv env", "pip install venv", "python create venv", "venv --new"), 0),
                Q.of("What does PEP 8 refer to?",
                        List.of("A Python library", "Python's style guide", "A testing framework", "A package manager"), 1),
                Q.withCode("What is the output of this code?",
                        "x = [1, 2, 3]\ny = x\ny.append(4)\nprint(x)",
                        List.of("[1, 2, 3]", "[1, 2, 3, 4]", "Error", "[4]"), 1),
                Q.withCode("What does this print?",
                        "def greet(name=\"World\"):\n    return f\"Hello, {name}!\"\n\nprint(greet())",
                        List.of("Hello, World!", "Hello, name!", "Error", "Hello, !"), 0),
                Q.withCode("What is the output?",
                        "print(3 // 2)",
                        List.of("1.5", "1", "2", "Error"), 1),
                Q.withCode("What does this code output?",
                        "nums = [1, 2, 3, 4]\nsquares = [n**2 for n in nums if n % 2 == 0]\nprint(squares)",
                        List.of("[4, 16]", "[1, 4, 9, 16]", "[2, 4]", "Error"), 0),
                Q.withCode("What is printed here?",
                        "a, b = 1, 2\na, b = b, a\nprint(a, b)",
                        List.of("1 2", "2 1", "Error", "None None"), 1)
        ));

        seedQuestionsForSkill("JavaScript", List.of(
                Q.of("Which keyword declares a block-scoped variable in JavaScript?",
                        List.of("var", "let", "global", "static"), 1),
                Q.of("What does `===` check for in JavaScript?",
                        List.of("Value only", "Value and type", "Type only", "Reference only"), 1),
                Q.of("Which method converts a JSON string into a JavaScript object?",
                        List.of("JSON.parse()", "JSON.stringify()", "JSON.toObject()", "Object.parse()"), 0),
                Q.of("What is a Promise used for in JavaScript?",
                        List.of("Styling components", "Handling asynchronous operations", "Declaring variables", "Looping over arrays"), 1),
                Q.of("Which array method creates a new array with transformed elements?",
                        List.of("forEach()", "map()", "filter()", "reduce()"), 1),
                Q.withCode("What is the output?",
                        "console.log(typeof null);",
                        List.of("'null'", "'object'", "'undefined'", "'number'"), 1),
                Q.withCode("What does this log?",
                        "let arr = [1, 2, 3];\nconsole.log(arr.map(x => x * 2));",
                        List.of("[1, 2, 3]", "[2, 4, 6]", "[1, 4, 9]", "Error"), 1),
                Q.withCode("What is printed?",
                        "console.log(1 + '1');",
                        List.of("2", "'11'", "NaN", "Error"), 1),
                Q.withCode("What is the output?",
                        "function foo() {\n  console.log(this);\n}\nconst obj = { foo };\nobj.foo();",
                        List.of("undefined", "the global object", "obj", "null"), 2),
                Q.withCode("What does this print?",
                        "console.log([1,2,3].includes(2));",
                        List.of("true", "false", "2", "Error"), 0)
        ));

        seedQuestionsForSkill("SQL", List.of(
                Q.of("Which SQL clause is used to filter grouped results?",
                        List.of("WHERE", "HAVING", "GROUP BY", "ORDER BY"), 1),
                Q.of("Which type of JOIN returns all rows from both tables, matched or not?",
                        List.of("INNER JOIN", "LEFT JOIN", "FULL OUTER JOIN", "RIGHT JOIN"), 2),
                Q.of("What does the SQL `UNIQUE` constraint ensure?",
                        List.of("A column allows nulls", "No duplicate values in a column", "A column is indexed", "A column is a primary key"), 1),
                Q.of("Which command permanently removes a table and its structure?",
                        List.of("DELETE", "TRUNCATE", "DROP", "REMOVE"), 2),
                Q.of("What is a primary key used for?",
                        List.of("Sorting rows", "Uniquely identifying each row", "Filtering duplicate columns", "Encrypting data"), 1),
                Q.withCode("What does this query return?",
                        "SELECT COUNT(*) FROM employees WHERE department = 'Engineering';",
                        List.of("All employee names", "The number of employees in Engineering", "All departments", "An error"), 1),
                Q.withCode("What is wrong with this query?",
                        "SELECT name, COUNT(*) FROM employees;",
                        List.of("Nothing, it's valid", "Missing GROUP BY for the non-aggregated column", "Missing WHERE clause", "COUNT(*) is invalid syntax"), 1),
                Q.withCode("What does this query do?",
                        "UPDATE employees SET department = 'Sales' WHERE id = 5;",
                        List.of("Deletes employee with id 5", "Changes department of employee id 5 to Sales", "Creates a new employee", "Selects employee id 5"), 1),
                Q.withCode("What is the result of this query?",
                        "SELECT * FROM employees ORDER BY salary DESC LIMIT 1;",
                        List.of("The lowest paid employee", "The highest paid employee", "All employees sorted", "An error"), 1),
                Q.withCode("What does this query check?",
                        "SELECT * FROM orders WHERE order_date IS NULL;",
                        List.of("Orders with a valid date", "Orders missing a date value", "All orders", "Orders from today"), 1)
        ));

        seedQuestionsForSkill("Spring Boot", List.of(
                Q.of("Which annotation marks a class as a REST controller in Spring Boot?",
                        List.of("@Controller", "@RestController", "@Service", "@Component"), 1),
                Q.of("What does Spring Boot's auto-configuration primarily do?",
                        List.of("Auto-writes business logic", "Configures beans based on classpath and properties", "Deploys the app automatically", "Compiles code faster"), 1),
                Q.of("Which file is commonly used for Spring Boot application configuration?",
                        List.of("web.xml", "application.yml", "pom.xml", "settings.json"), 1),
                Q.of("Which annotation injects a dependency in Spring?",
                        List.of("@Inject", "@Autowired", "@Bean", "@Import"), 1),
                Q.of("What does `spring-boot-starter-data-jpa` provide?",
                        List.of("REST API tools", "Database access via Hibernate/JPA", "Security filters", "Testing utilities"), 1),
                Q.withCode("What HTTP method does this endpoint respond to?",
                        "@PostMapping(\"/users\")\npublic ResponseEntity<User> create(@RequestBody User user) { ... }",
                        List.of("GET", "POST", "PUT", "DELETE"), 1),
                Q.withCode("What does `@PathVariable` do here?",
                        "@GetMapping(\"/users/{id}\")\npublic User getUser(@PathVariable Long id) { ... }",
                        List.of("Reads a query parameter", "Binds the {id} URL segment to the method parameter", "Reads the request body", "Sets a response header"), 1),
                Q.withCode("What will happen when this endpoint is called without a request body?",
                        "@PostMapping(\"/items\")\npublic Item create(@Valid @RequestBody ItemRequest request) { ... }",
                        List.of("It runs normally with null request", "Spring returns a 400 Bad Request", "It throws a 500 error only", "It ignores validation"), 1),
                Q.withCode("What scope does this bean have by default?",
                        "@Service\npublic class UserService { ... }",
                        List.of("Prototype (new instance each time)", "Singleton (one shared instance)", "Request-scoped", "Session-scoped"), 1),
                Q.withCode("What does this configuration do?",
                        "@Bean\npublic PasswordEncoder passwordEncoder() {\n    return new BCryptPasswordEncoder();\n}",
                        List.of("Registers a bean for password hashing", "Encrypts the database", "Creates a user", "Disables security"), 0)
        ));

        seedQuestionsForSkill("React", List.of(
                Q.of("What hook is used to manage state in a functional component?",
                        List.of("useEffect", "useState", "useRef", "useMemo"), 1),
                Q.of("What does JSX allow you to write?",
                        List.of("SQL inside JavaScript", "HTML-like syntax inside JavaScript", "CSS inside HTML", "Python inside React"), 1),
                Q.of("Which hook runs side effects after render?",
                        List.of("useState", "useEffect", "useContext", "useCallback"), 1),
                Q.of("How does data typically flow in React?",
                        List.of("Two-way binding by default", "Unidirectional, from parent to child via props", "Only through global variables", "Randomly between components"), 1),
                Q.of("What is the purpose of a `key` prop in a list?",
                        List.of("Styling", "Helping React identify which items changed", "Setting default values", "Encrypting props"), 1),
                Q.withCode("What will happen when the button is clicked?",
                        "function Counter() {\n  const [count, setCount] = useState(0);\n  return <button onClick={() => setCount(count + 1)}>{count}</button>;\n}",
                        List.of("Nothing happens", "The displayed count increases by 1 each click", "It throws an error", "The component unmounts"), 1),
                Q.withCode("What does this useEffect do?",
                        "useEffect(() => {\n  console.log('mounted');\n}, []);",
                        List.of("Runs on every render", "Runs only once, after the initial render", "Never runs", "Runs before render"), 1),
                Q.withCode("What is wrong with this code?",
                        "function List({ items }) {\n  return items.map(item => <li>{item}</li>);\n}",
                        List.of("Nothing, it's correct", "Missing a unique key prop on each <li>", "map() cannot be used in JSX", "items must be a string"), 1),
                Q.withCode("What does this component render when `isLoggedIn` is false?",
                        "function Greeting({ isLoggedIn }) {\n  return isLoggedIn ? <h1>Welcome back!</h1> : <h1>Please sign in.</h1>;\n}",
                        List.of("Welcome back!", "Please sign in.", "Nothing", "An error"), 1),
                Q.withCode("What does this code do?",
                        "const [user, setUser] = useState(null);\nuseEffect(() => {\n  fetchUser().then(setUser);\n}, []);",
                        List.of("Fetches user data once on mount and stores it in state", "Fetches user data on every render", "Never fetches data", "Deletes the user"), 0)
        ));

        seedQuestionsForSkill("Docker", List.of(
                Q.of("What is a Docker image?",
                        List.of("A running instance of a container", "A read-only template used to create containers", "A virtual machine", "A network configuration file"), 1),
                Q.of("Which file defines how a Docker image is built?",
                        List.of("docker-compose.yml", "Dockerfile", "image.json", "container.yaml"), 1),
                Q.of("What command lists all running containers?",
                        List.of("docker ps", "docker list", "docker show", "docker containers"), 0),
                Q.of("What is the purpose of a Docker volume?",
                        List.of("Scaling containers", "Persisting data outside the container lifecycle", "Building images faster", "Networking between hosts"), 1),
                Q.of("What does `docker-compose` help manage?",
                        List.of("Single container builds only", "Multi-container applications", "Only container images", "Kubernetes clusters"), 1),
                Q.withCode("What does this Dockerfile instruction do?",
                        "FROM openjdk:17-jre-slim",
                        List.of("Installs Docker itself", "Sets the base image for the build", "Runs the application", "Creates a volume"), 1),
                Q.withCode("What does this line do in a Dockerfile?",
                        "COPY target/app.jar app.jar",
                        List.of("Deletes a file", "Copies a file from host into the image", "Runs a command", "Exposes a port"), 1),
                Q.withCode("What does this instruction configure?",
                        "EXPOSE 8080",
                        List.of("Sets an environment variable", "Documents which port the container listens on", "Runs the app on port 8080", "Creates a firewall rule"), 1),
                Q.withCode("What does this command do?",
                        "docker run -p 8080:8080 myapp",
                        List.of("Builds the image", "Runs a container and maps host port 8080 to container port 8080", "Stops all containers", "Deletes the image"), 1),
                Q.withCode("What does this instruction do?",
                        "CMD [\"java\", \"-jar\", \"app.jar\"]",
                        List.of("Builds the jar file", "Specifies the default command to run when the container starts", "Installs Java", "Copies the jar file"), 1)
        ));

        seedQuestionsForSkill("Kubernetes", List.of(
                Q.of("What is the smallest deployable unit in Kubernetes?",
                        List.of("Container", "Pod", "Node", "Cluster"), 1),
                Q.of("What does a Kubernetes Service do?",
                        List.of("Stores configuration secrets", "Provides stable networking access to a set of pods", "Builds container images", "Schedules cron jobs only"), 1),
                Q.of("Which component schedules pods onto nodes?",
                        List.of("kubelet", "scheduler", "etcd", "kube-proxy"), 1),
                Q.of("What is a Deployment used for in Kubernetes?",
                        List.of("Manual pod creation only", "Declaratively managing replicas and rollouts of pods", "Only storage management", "DNS resolution"), 1),
                Q.of("What does `kubectl` refer to?",
                        List.of("A container runtime", "The Kubernetes command-line tool", "A monitoring dashboard", "A cloud provider"), 1),
                Q.withCode("What does this command do?",
                        "kubectl get pods",
                        List.of("Deletes all pods", "Lists all pods in the current namespace", "Creates a new pod", "Scales a deployment"), 1),
                Q.withCode("What does `replicas: 3` configure?",
                        "spec:\n  replicas: 3\n  template:\n    spec:\n      containers:\n      - name: app",
                        List.of("3 different applications", "3 identical running copies of the pod", "3 nodes in the cluster", "3 namespaces"), 1),
                Q.withCode("What does this command do?",
                        "kubectl scale deployment myapp --replicas=5",
                        List.of("Deletes the deployment", "Scales the deployment to 5 pod replicas", "Creates 5 new services", "Restarts the cluster"), 1),
                Q.withCode("What is the purpose of this readiness probe config?",
                        "readinessProbe:\n  httpGet:\n    path: /health\n    port: 8080",
                        List.of("Encrypts traffic", "Checks if the pod is ready to receive traffic", "Deletes unhealthy pods immediately", "Scales the pod automatically"), 1),
                Q.withCode("What does this command show?",
                        "kubectl logs mypod",
                        List.of("The pod's configuration", "The container logs for that pod", "The cluster's IP address", "The deployment history"), 1)
        ));

        seedQuestionsForSkill("AWS", List.of(
                Q.of("Which AWS service provides scalable object storage?",
                        List.of("EC2", "S3", "RDS", "Lambda"), 1),
                Q.of("What is AWS Lambda used for?",
                        List.of("Running serverless functions", "Managing virtual machines", "Object storage", "DNS routing"), 0),
                Q.of("Which service is a managed relational database in AWS?",
                        List.of("DynamoDB", "RDS", "S3", "CloudFront"), 1),
                Q.of("What does IAM stand for in AWS?",
                        List.of("Internet Access Manager", "Identity and Access Management", "Instance Allocation Module", "Infrastructure Automation Manager"), 1),
                Q.of("Which AWS service is used for content delivery (CDN)?",
                        List.of("CloudFront", "CloudWatch", "CloudTrail", "CloudFormation"), 0),
                Q.withCode("What does this AWS CLI command do?",
                        "aws s3 cp file.txt s3://my-bucket/",
                        List.of("Downloads a file from S3", "Uploads file.txt to an S3 bucket", "Deletes a bucket", "Lists bucket contents"), 1),
                Q.withCode("What does this IAM policy statement allow?",
                        "{\n  \"Effect\": \"Allow\",\n  \"Action\": \"s3:GetObject\",\n  \"Resource\": \"arn:aws:s3:::my-bucket/*\"\n}",
                        List.of("Deleting objects in the bucket", "Reading (getting) objects from the bucket", "Creating new buckets", "Full admin access"), 1),
                Q.withCode("What does this command do?",
                        "aws ec2 describe-instances",
                        List.of("Creates a new EC2 instance", "Lists details of existing EC2 instances", "Terminates all instances", "Stops billing"), 1),
                Q.withCode("What is the purpose of this CloudFormation snippet?",
                        "Resources:\n  MyBucket:\n    Type: AWS::S3::Bucket",
                        List.of("Deletes an S3 bucket", "Declares an S3 bucket resource to be created", "Configures IAM roles", "Sets up a VPC"), 1),
                Q.withCode("What does this command retrieve?",
                        "aws lambda invoke --function-name myFunction output.json",
                        List.of("Deletes a Lambda function", "Invokes the Lambda function and saves its output", "Lists all Lambda functions", "Creates a new function"), 1)
        ));

        seedQuestionsForSkill("CI/CD", List.of(
                Q.of("What does CI in CI/CD stand for?",
                        List.of("Code Integration", "Continuous Integration", "Container Isolation", "Custom Instance"), 1),
                Q.of("What is the main goal of Continuous Deployment?",
                        List.of("Manual approval for every release", "Automatically releasing code changes to production", "Writing more tests", "Slowing down releases for safety"), 1),
                Q.of("Which of these is a common CI/CD tool?",
                        List.of("Jenkins", "Photoshop", "Excel", "Figma"), 0),
                Q.of("What is a build pipeline?",
                        List.of("A single manual deployment step", "An automated sequence of build, test, and deploy stages", "A database schema", "A network diagram"), 1),
                Q.of("Why are automated tests important in a CI/CD pipeline?",
                        List.of("They slow down releases", "They catch issues early before deployment", "They replace code reviews entirely", "They are optional and rarely used"), 1),
                Q.withCode("What does this GitHub Actions step do?",
                        "- name: Run tests\n  run: mvn test",
                        List.of("Deploys the app", "Runs the Maven test suite", "Builds a Docker image", "Pushes to GitHub"), 1),
                Q.withCode("What triggers this pipeline?",
                        "on:\n  push:\n    branches: [main]",
                        List.of("A scheduled cron job", "A push to the main branch", "A manual button click only", "A pull request only"), 1),
                Q.withCode("What does this pipeline stage likely do?",
                        "stage('Build') {\n    steps {\n        sh 'mvn clean package'\n    }\n}",
                        List.of("Runs unit tests only", "Compiles and packages the application", "Deploys to production", "Sends a notification"), 1),
                Q.withCode("What is the purpose of this step?",
                        "- name: Build Docker image\n  run: docker build -t myapp .",
                        List.of("Runs the test suite", "Builds a Docker image from the Dockerfile", "Deploys to Kubernetes", "Pushes code to Git"), 1),
                Q.withCode("What does this rollback strategy line suggest?",
                        "deploy:\n  strategy: blue-green",
                        List.of("Deploys directly with no safety net", "Runs two environments to allow safe switch-over/rollback", "Deletes the old environment immediately", "Skips testing"), 1)
        ));

        seedQuestionsForSkill("Git", List.of(
                Q.of("What command creates a new branch in Git?",
                        List.of("git branch <name>", "git new <name>", "git create <name>", "git init <name>"), 0),
                Q.of("What does `git commit` do?",
                        List.of("Uploads code to a remote server", "Saves a snapshot of staged changes locally", "Deletes tracked files", "Creates a new repository"), 1),
                Q.of("What is the purpose of `.gitignore`?",
                        List.of("Lists files Git should track only", "Lists files/folders Git should NOT track", "Stores commit history", "Configures branch permissions"), 1),
                Q.of("What does `git merge` do?",
                        List.of("Deletes a branch", "Combines changes from one branch into another", "Creates a new repository", "Reverts the last commit"), 1),
                Q.of("What is a merge conflict?",
                        List.of("A network error during push", "Competing changes Git cannot automatically reconcile", "A missing commit message", "An invalid branch name"), 1),
                Q.withCode("What does this command sequence do?",
                        "git add .\ngit commit -m \"fix bug\"\ngit push",
                        List.of("Deletes all changes", "Stages, commits, and pushes changes to remote", "Only stages changes", "Creates a new branch"), 1),
                Q.withCode("What does this command do?",
                        "git checkout -b feature/login",
                        List.of("Deletes the feature branch", "Creates and switches to a new branch called feature/login", "Merges into main", "Lists all branches"), 1),
                Q.withCode("What does this do?",
                        "git rebase main",
                        List.of("Deletes the main branch", "Replays current branch commits on top of main", "Merges main into current branch with a merge commit", "Reverts all commits"), 1),
                Q.withCode("What is the effect of this command?",
                        "git reset --hard HEAD~1",
                        List.of("Undoes the last commit and discards its changes", "Creates a new commit", "Pushes to remote", "Creates a backup branch"), 0),
                Q.withCode("What does this command show?",
                        "git log --oneline",
                        List.of("A list of branches", "A condensed one-line-per-commit history", "The current diff", "Remote repository URLs"), 1)
        ));

        seedQuestionsForSkill("Linux", List.of(
                Q.of("Which command lists files in a directory?",
                        List.of("ls", "dir", "list", "show"), 0),
                Q.of("What does `chmod` change?",
                        List.of("File ownership", "File permissions", "File name", "File location"), 1),
                Q.of("Which command shows currently running processes?",
                        List.of("ps", "run", "proc", "task"), 0),
                Q.of("What is the root user in Linux?",
                        List.of("A regular user account", "The superuser with full system privileges", "A guest account", "A network service"), 1),
                Q.of("Which command is used to search inside files for text?",
                        List.of("find", "grep", "locate", "search"), 1),
                Q.withCode("What does this command do?",
                        "chmod +x script.sh",
                        List.of("Deletes the script", "Makes the script executable", "Renames the script", "Copies the script"), 1),
                Q.withCode("What does this command display?",
                        "df -h",
                        List.of("Running processes", "Disk space usage in human-readable format", "Network connections", "Memory usage only"), 1),
                Q.withCode("What does this pipeline do?",
                        "cat access.log | grep \"ERROR\" | wc -l",
                        List.of("Counts the lines in access.log", "Counts how many lines contain 'ERROR'", "Deletes error lines", "Displays the whole file"), 1),
                Q.withCode("What does this command do?",
                        "kill -9 1234",
                        List.of("Restarts process 1234", "Forcefully terminates process with PID 1234", "Lists process 1234's details", "Pauses process 1234"), 1),
                Q.withCode("What does this do?",
                        "crontab -e",
                        List.of("Edits scheduled cron jobs for the current user", "Deletes all cron jobs", "Lists running services", "Edits system users"), 0)
        ));

        seedQuestionsForSkill("Communication", List.of(
                Q.of("What is the most effective way to ensure a message was understood?",
                        List.of("Speak louder", "Ask for feedback/confirmation", "Repeat it once", "Send it in writing only"), 1),
                Q.of("Active listening primarily involves:",
                        List.of("Waiting for your turn to speak", "Fully focusing and responding thoughtfully", "Multitasking while listening", "Interrupting to add value"), 1),
                Q.of("In a disagreement at work, the best first step is usually to:",
                        List.of("Escalate immediately", "Understand the other person's perspective", "Avoid the topic entirely", "Assert your position firmly"), 1),
                Q.of("Which is a sign of clear written communication?",
                        List.of("Long, detailed paragraphs", "Concise, structured, and unambiguous language", "Heavy use of jargon", "Minimal punctuation"), 1),
                Q.of("Non-verbal communication includes:",
                        List.of("Only written text", "Tone, body language, and facial expressions", "Only email formatting", "Font choice"), 1),
                Q.of("When giving feedback to a colleague, it's best to:",
                        List.of("Be vague to avoid hurting feelings", "Be specific and focus on behavior, not personality", "Only mention it during annual reviews", "Send it anonymously"), 1),
                Q.of("What is the best way to communicate a delay to a stakeholder?",
                        List.of("Wait until they ask", "Proactively inform them with a reason and new timeline", "Only mention it if it becomes serious", "Blame the team"), 1),
                Q.of("Which best supports communication in a remote team?",
                        List.of("Assuming everyone reads every message immediately", "Clear, written updates and regular check-ins", "Avoiding video calls entirely", "Only communicating urgent issues"), 1),
                Q.of("What does empathetic communication involve?",
                        List.of("Focusing only on your own viewpoint", "Considering the other person's feelings and context", "Ignoring emotional context", "Speaking as briefly as possible"), 1),
                Q.of("A well-structured presentation typically:",
                        List.of("Has no clear order", "Follows a logical flow with a clear beginning, middle, and end", "Includes as much detail as possible", "Skips the conclusion"), 1)
        ));

        seedQuestionsForSkill("Leadership", List.of(
                Q.of("Effective delegation primarily involves:",
                        List.of("Assigning tasks without context", "Matching tasks to the right people with clear expectations", "Doing all the work yourself", "Avoiding accountability"), 1),
                Q.of("A good leader responds to team mistakes by:",
                        List.of("Publicly blaming the individual", "Focusing on learning and improvement", "Ignoring the issue entirely", "Reassigning blame to another team"), 1),
                Q.of("Which best describes situational leadership?",
                        List.of("Using the same style for every situation", "Adapting your leadership style to the person and context", "Only leading during emergencies", "Avoiding decisions entirely"), 1),
                Q.of("Giving constructive feedback should be:",
                        List.of("Vague and infrequent", "Specific, timely, and actionable", "Only negative", "Delivered publicly to motivate others"), 1),
                Q.of("What builds trust within a team most effectively?",
                        List.of("Micromanaging every task", "Consistency between words and actions", "Withholding information", "Frequent last-minute changes"), 1),
                Q.of("A key responsibility of a leader during a crisis is:",
                        List.of("Avoiding communication until it's resolved", "Providing clear, calm, and honest updates", "Assigning blame quickly", "Making all decisions without input"), 1),
                Q.of("What is the value of setting clear goals for a team?",
                        List.of("It limits creativity", "It aligns effort and provides a measure of success", "It's only useful for large teams", "It replaces the need for check-ins"), 1),
                Q.of("How should a leader handle underperformance?",
                        List.of("Ignore it and hope it improves", "Address it directly with support and a clear plan", "Publicly criticize the person", "Immediately terminate them"), 1),
                Q.of("What does servant leadership prioritize?",
                        List.of("The leader's personal success", "Supporting and empowering team members", "Strict top-down control", "Avoiding all feedback"), 1),
                Q.of("Why is recognizing team achievements important?",
                        List.of("It's unnecessary if pay is competitive", "It reinforces positive behavior and boosts morale", "It should only come from HR", "It has no real impact"), 1)
        ));

        seedQuestionsForSkill("Problem Solving", List.of(
                Q.of("What is typically the first step in structured problem solving?",
                        List.of("Implement a solution immediately", "Clearly define the actual problem", "Assign blame", "Skip to brainstorming solutions"), 1),
                Q.of("Root cause analysis aims to:",
                        List.of("Treat surface-level symptoms only", "Identify the underlying cause of an issue", "Avoid investigating the issue", "Assign responsibility quickly"), 1),
                Q.of("Which is a common problem-solving technique?",
                        List.of("5 Whys", "Random guessing", "Ignoring constraints", "Avoiding data"), 0),
                Q.of("Why is it useful to consider multiple solutions before choosing one?",
                        List.of("It wastes time", "It helps evaluate trade-offs and pick the best fit", "Only one solution is ever valid", "It's not useful"), 1),
                Q.of("After implementing a solution, what should you do?",
                        List.of("Assume it worked without checking", "Evaluate results and adjust if needed", "Immediately move to unrelated tasks", "Avoid documenting the outcome"), 1),
                Q.of("When facing an ambiguous problem, a good first step is to:",
                        List.of("Guess a solution quickly", "Gather more information and clarify the scope", "Wait for someone else to solve it", "Avoid asking questions"), 1),
                Q.of("What is the benefit of breaking a large problem into smaller parts?",
                        List.of("It makes the problem harder", "It makes each part easier to analyze and solve", "It's unnecessary busywork", "It avoids the need for a solution"), 1),
                Q.of("Which mindset is most helpful in problem solving?",
                        List.of("Assuming there's only one right answer", "Staying curious and open to different approaches", "Avoiding any risk", "Rushing to the first idea"), 1),
                Q.of("Why involve others when solving a complex problem?",
                        List.of("It slows things down unnecessarily", "Diverse perspectives often reveal blind spots", "It's only needed for technical problems", "It's rarely helpful"), 1),
                Q.of("What should you do if your first solution attempt fails?",
                        List.of("Give up on the problem", "Analyze why it failed and try an adjusted approach", "Repeat the exact same solution", "Blame the tools used"), 1)
        ));

        seedQuestionsForSkill("Teamwork", List.of(
                Q.of("Which behavior most strengthens team collaboration?",
                        List.of("Withholding information from teammates", "Openly sharing updates and asking for help when needed", "Working in isolation", "Avoiding team meetings"), 1),
                Q.of("How should conflicting opinions in a team ideally be resolved?",
                        List.of("Through open discussion and compromise", "By avoiding the topic", "By the loudest voice winning", "By escalating immediately without discussion"), 0),
                Q.of("What does psychological safety in a team mean?",
                        List.of("Avoiding all disagreement", "Feeling safe to speak up without fear of punishment", "Working without any feedback", "Following orders without question"), 1),
                Q.of("A strong team member typically:",
                        List.of("Takes credit for others' work", "Supports teammates and shares credit fairly", "Avoids helping others", "Competes against teammates"), 1),
                Q.of("Why are clear roles important in a team?",
                        List.of("They reduce confusion and duplicated effort", "They limit collaboration", "They are unnecessary for small teams", "They replace the need for communication"), 0),
                Q.of("How should a team handle a member who is struggling?",
                        List.of("Ignore it and let them fail", "Offer support and check in on what they need", "Publicly call it out", "Reassign all their work without discussion"), 1),
                Q.of("What is the value of regular team check-ins?",
                        List.of("They waste time", "They surface blockers early and keep everyone aligned", "They're only useful for large teams", "They replace the need for planning"), 1),
                Q.of("How should credit be handled after a successful team project?",
                        List.of("Only the team lead should be recognized", "Contributions from all members should be acknowledged", "Credit doesn't matter", "Only the loudest member should be credited"), 1),
                Q.of("What is a healthy way to give peer feedback?",
                        List.of("Anonymously and vaguely", "Directly, respectfully, and with specific examples", "Only through a manager", "Never, to avoid conflict"), 1),
                Q.of("Why is trust important in a team?",
                        List.of("It's not really necessary for task completion", "It enables open communication and effective collaboration", "It only matters in social settings", "It slows down decision-making"), 1)
        ));
    }

    private void seedQuestionsForSkill(String skillName, List<Q> questions) {
        Skill skill = skillRepository.findByName(skillName).orElse(null);
        if (skill == null) return;

        List<AssessmentQuestion> existing = assessmentQuestionRepository.findBySkillId(skill.getId());
        if (existing.size() >= questions.size()) return; // already fully seeded

        // Re-seed cleanly: remove partial old set, insert the full current set
        if (!existing.isEmpty()) {
            assessmentQuestionRepository.deleteAll(existing);
        }

        for (Q q : questions) {
            assessmentQuestionRepository.save(AssessmentQuestion.builder()
                    .skill(skill)
                    .questionText(q.text())
                    .codeSnippet(q.code())
                    .options(q.options())
                    .correctOptionIndex(q.correctIndex())
                    .difficultyWeight(1)
                    .build());
        }
    }

    /**
     * Seeds a handful of realistic role competency frameworks so Gap Analysis has
     * real data to compare against immediately. To see your own gap report, set your
     * Employee Profile's "Current Role Title" to exactly match one of these seeded
     * role titles (e.g. "Software Developer") via PUT /api/v1/employee-profile/me.
     */
    private void seedCompetencyFrameworks() {
        seedFramework("Software Developer", Map.of(
                "Java", ProficiencyLevel.ADVANCED,
                "SQL", ProficiencyLevel.INTERMEDIATE,
                "Git", ProficiencyLevel.INTERMEDIATE,
                "Spring Boot", ProficiencyLevel.INTERMEDIATE,
                "Problem Solving", ProficiencyLevel.ADVANCED
        ));

        seedFramework("Senior Software Developer", Map.of(
                "Java", ProficiencyLevel.EXPERT,
                "Spring Boot", ProficiencyLevel.ADVANCED,
                "SQL", ProficiencyLevel.ADVANCED,
                "Docker", ProficiencyLevel.INTERMEDIATE,
                "CI/CD", ProficiencyLevel.INTERMEDIATE,
                "Leadership", ProficiencyLevel.INTERMEDIATE
        ));

        seedFramework("Frontend Developer", Map.of(
                "JavaScript", ProficiencyLevel.ADVANCED,
                "React", ProficiencyLevel.ADVANCED,
                "Git", ProficiencyLevel.INTERMEDIATE,
                "Problem Solving", ProficiencyLevel.INTERMEDIATE
        ));

        seedFramework("DevOps Engineer", Map.of(
                "Docker", ProficiencyLevel.ADVANCED,
                "Kubernetes", ProficiencyLevel.ADVANCED,
                "AWS", ProficiencyLevel.ADVANCED,
                "CI/CD", ProficiencyLevel.ADVANCED,
                "Linux", ProficiencyLevel.ADVANCED
        ));

        seedFramework("Data Analyst", Map.of(
                "SQL", ProficiencyLevel.ADVANCED,
                "Python", ProficiencyLevel.INTERMEDIATE,
                "Problem Solving", ProficiencyLevel.ADVANCED,
                "Communication", ProficiencyLevel.INTERMEDIATE
        ));

        seedFramework("HR Specialist", Map.of(
                "Communication", ProficiencyLevel.ADVANCED,
                "Leadership", ProficiencyLevel.INTERMEDIATE,
                "Problem Solving", ProficiencyLevel.INTERMEDIATE,
                "Teamwork", ProficiencyLevel.ADVANCED
        ));

        seedFramework("Team Lead", Map.of(
                "Leadership", ProficiencyLevel.ADVANCED,
                "Communication", ProficiencyLevel.ADVANCED,
                "Problem Solving", ProficiencyLevel.ADVANCED,
                "Teamwork", ProficiencyLevel.ADVANCED,
                "Java", ProficiencyLevel.INTERMEDIATE
        ));
    }

    private void seedFramework(String roleTitle, Map<String, ProficiencyLevel> skillRequirements) {
        if (frameworkRepository.findByRoleTitleAndCurrentTrue(roleTitle).isPresent()) return;

        RoleCompetencyFramework framework = frameworkRepository.save(
                RoleCompetencyFramework.builder()
                        .roleTitle(roleTitle)
                        .version("v1")
                        .current(true)
                        .build());

        skillRequirements.forEach((skillName, requiredLevel) -> {
            Skill skill = skillRepository.findByName(skillName).orElse(null);
            if (skill == null) return;
            requirementRepository.save(CompetencyRequirement.builder()
                    .framework(framework)
                    .skill(skill)
                    .requiredLevel(requiredLevel)
                    .mandatory(true)
                    .build());
        });
    }
}
