pipeline {
    agent any

    environment {
        REDIS_HOST = credentials('REDIS_HOST')
        REDIS_PASSWORD = credentials('REDIS_PASSWORD')
        JWT_SECRET = credentials('JWT_SECRET')

        IMAGE_NAME = "openknot-auth-service"
        CONTAINER_NAME = "openknot-auth-service"
        APP_PORT = "8081"
        SPRING_PROFILES_ACTIVE = "prod"
    }

    stages {
        stage('Checkout') {
            steps {
                git branch: 'main',
                    credentialsId: 'GitHub_Token',
                    url: 'https://github.com/OpenKnot-Service/openknot-auth-service.git'
            }
        }

        stage('Build & Test') {
            steps {
                sh "chmod +x gradlew || true"
                sh "./gradlew clean test bootJar"
            }
        }

        stage('Build Docker Image') {
                    steps {
                        sh """
                            docker build \
                              -t ${IMAGE_NAME}:${BUILD_NUMBER} \
                              -t ${IMAGE_NAME}:latest \
                              .
                        """
                    }
                }

        stage('Deploy') {
            steps {
                sh """
                    docker stop ${CONTAINER_NAME} || true
                    docker rm ${CONTAINER_NAME} || true

                    docker run -d \
                      --name ${CONTAINER_NAME} \
                      -p ${APP_PORT}:${APP_PORT} \
                      -e SPRING_PROFILES_ACTIVE=${SPRING_PROFILES_ACTIVE} \
                      -e REDIS_HOST=${REDIS_HOST} \
                      -e REDIS_PASSWORD=${REDIS_PASSWORD} \
                      -e JWT_SECRET=${JWT_SECRET} \
                      --restart unless-stopped \
                      ${IMAGE_NAME}:latest
                """
            }
        }
    }

    post {
        failure {
            echo "빌드/배포 실패! 로그 확인 필요"
        }
        success {
            echo "배포 완료!"
        }
    }
}