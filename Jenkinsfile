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
                sh '''#!/bin/bash -e
                chmod +x gradlew || true

                if [ ! -d "$PWD/.jdk-temurin-21" ]; then
                  echo "Downloading Temurin JDK 21..."
                  curl -L -o /tmp/temurin21.tar.gz https://github.com/adoptium/temurin21-binaries/releases/download/jdk-21.0.6%2B7/OpenJDK21U-jdk_x64_linux_hotspot_21.0.6_7.tar.gz
                  mkdir -p "$PWD/.jdk-temurin-21"
                  tar -xzf /tmp/temurin21.tar.gz -C "$PWD/.jdk-temurin-21" --strip-components=1
                fi

                export JAVA_HOME="$PWD/.jdk-temurin-21"
                export PATH="$JAVA_HOME/bin:$PATH"

                docker rm -f test-redis || true
                docker run -d --name test-redis -p 16379:6379 redis:7.2-alpine > /dev/null

                echo "Waiting for Redis to be ready..."
                for i in {1..30}; do
                  if docker exec test-redis redis-cli PING > /dev/null 2>&1; then
                    break
                  fi
                  echo "Redis not ready yet... ($i/30)"
                  sleep 1
                done
                docker exec test-redis redis-cli PING

                trap "docker rm -f test-redis || true" EXIT

                export IT_REDIS_HOST=127.0.0.1
                export IT_REDIS_PORT=16379

                ./gradlew clean test bootJar
                '''
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
