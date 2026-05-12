pipeline {
    agent any

    tools {
        // Ensure you have Maven and JDK configured in Jenkins Global Tool Configuration
        maven 'Maven 3.9'
        jdk 'Java 21'
    }

    parameters {
        choice(name: 'ENV', choices: ['qa', 'dev'], description: 'Environment for test execution')
        choice(name: 'BROWSER', choices: ['CHROME', 'FIREFOX'], description: 'Browser for test execution')
    }

    environment {
        // Pass parameters to Docker Compose
        BROWSER = "${params.BROWSER}"
        ENV = "${params.ENV}"
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Run Tests with Docker Compose') {
            steps {
                // --build ensures the test-runner image is updated with current code
                // --exit-code-from test-runner ensures Jenkins build status matches test results
                sh 'docker compose up --build --exit-code-from test-runner'
            }
        }
    }

    post {
        always {
            // Clean up containers, networks, and volumes
            sh 'docker compose down'
            
            // Generate Allure report (history is handled by the plugin)
            allure includeProperties: false, 
                   jdk: '', 
                   results: [[path: 'target/allure-results']]
            
            // Archive artifacts extracted from the container via volume mount
            archiveArtifacts artifacts: 'target/allure-results/**, target/surefire-reports/**', allowEmptyArchive: true
        }
        
        failure {
            echo 'Tests failed. Check Allure report for details.'
        }
    }
}
