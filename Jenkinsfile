pipeline {
    agent any

    tools {
        // Ensure you have Maven and JDK configured in Jenkins Global Tool Configuration
        maven 'Maven 3.9'
        jdk 'Java 21'
    }

    parameters {
        choice(name: 'ENV', choices: ['qa', 'dev'], description: 'Environment for test execution')
        choice(name: 'BROWSER', choices: ['CHROME', 'FIREFOX', 'EDGE'], description: 'Browser for test execution')
        string(name: 'GROUPS', defaultValue: '', description: 'Comma-separated TestNG groups; blank runs the full suite')
        string(name: 'THREAD_COUNT', defaultValue: '2', description: 'TestNG method thread count')
        booleanParam(name: 'HEADLESS', defaultValue: true, description: 'Run browsers headless')
        booleanParam(name: 'RETRY_ENABLED', defaultValue: false, description: 'Enable retry analyzer for retryable tests')
        booleanParam(name: 'ALLOW_PASSWORDLESS_SKIPS', defaultValue: false, description: 'Allow password-backed scenarios to skip when APP_PASSWORD is missing')
    }

    environment {
        // Pass parameters to Docker Compose
        BROWSER = "${params.BROWSER}"
        ENV = "${params.ENV}"
        GROUPS = "${params.GROUPS}"
        THREAD_COUNT = "${params.THREAD_COUNT}"
        HEADLESS = "${params.HEADLESS}"
        RETRY_ENABLED = "${params.RETRY_ENABLED}"
        ALLOW_PASSWORDLESS_SKIPS = "${params.ALLOW_PASSWORDLESS_SKIPS}"
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Quality Gates') {
            steps {
                sh './mvnw -DskipTests validate spotless:check checkstyle:check pmd:check spotbugs:check'
            }
        }

        stage('Run Tests with Docker Compose') {
            steps {
                // --build ensures the test-runner image is updated with current code
                // --exit-code-from test-runner ensures Jenkins build status matches test results
                withCredentials([string(credentialsId: 'sauce-demo-password', variable: 'APP_PASSWORD')]) {
                    sh 'docker compose up --build --exit-code-from test-runner'
                }
            }
        }
    }

    post {
        always {
            // Clean up containers, networks, and volumes
            sh 'docker compose down --remove-orphans'
            
            // Generate Allure report (history is handled by the plugin)
            allure includeProperties: false, 
                   jdk: '', 
                   results: [[path: 'target/allure-results']]
            
            // Archive artifacts extracted from the container via volume mount
            archiveArtifacts artifacts: 'target/allure-results/**, target/allure-report/**, target/surefire-reports/**, target/logs/**', allowEmptyArchive: true
        }
        
        failure {
            echo 'Tests failed. Check Allure report for details.'
        }
    }
}
