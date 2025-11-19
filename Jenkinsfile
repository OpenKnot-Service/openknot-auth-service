pipeline {
    agent any

    environment {
        // Production credentials
        REDIS_HOST = credentials('REDIS_HOST')
        REDIS_PASSWORD = credentials('REDIS_PASSWORD')
        JWT_SECRET = credentials('JWT_SECRET')

        // Docker settings
        IMAGE_NAME = "openknot-auth-service"
        CONTAINER_NAME = "openknot-auth-service"
        APP_PORT = "8081"

        // Test Redis settings
        TEST_REDIS_CONTAINER = "test-redis"
        TEST_REDIS_PORT = "16379"

        // Spring settings
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

        stage('Setup JDK') {
            steps {
                script {
                    sh '''#!/bin/bash -e
                        if [ ! -d "$PWD/.jdk-temurin-21" ]; then
                            echo "📥 Downloading Temurin JDK 21..."
                            curl -sL -o /tmp/temurin21.tar.gz \
                                https://github.com/adoptium/temurin21-binaries/releases/download/jdk-21.0.6%2B7/OpenJDK21U-jdk_x64_linux_hotspot_21.0.6_7.tar.gz
                            mkdir -p "$PWD/.jdk-temurin-21"
                            tar -xzf /tmp/temurin21.tar.gz -C "$PWD/.jdk-temurin-21" --strip-components=1
                            rm /tmp/temurin21.tar.gz
                            echo "✅ JDK 21 installed"
                        else
                            echo "✅ JDK 21 already exists"
                        fi
                    '''
                }
            }
        }

        stage('Setup Test Redis') {
            steps {
                script {
                    sh """#!/bin/bash -e
                        echo "🔧 Setting up test Redis container..."

                        # Clean up existing container
                        docker rm -f ${TEST_REDIS_CONTAINER} 2>/dev/null || true

                        # Start Redis container
                        docker run -d \
                            --name ${TEST_REDIS_CONTAINER} \
                            -p ${TEST_REDIS_PORT}:6379 \
                            redis:7.2-alpine

                        # Wait for Redis to be ready
                        echo "⏳ Waiting for Redis to be ready..."
                        for i in {1..30}; do
                            if docker exec ${TEST_REDIS_CONTAINER} redis-cli PING > /dev/null 2>&1; then
                                echo "✅ Redis is ready"
                                docker exec ${TEST_REDIS_CONTAINER} redis-cli PING
                                exit 0
                            fi
                            echo "   Attempt \$i/30..."
                            sleep 1
                        done

                        echo "❌ Redis failed to start"
                        exit 1
                    """
                }
            }
        }

        stage('Build & Test') {
            steps {
                script {
                    sh """#!/bin/bash -e
                        export JAVA_HOME="\$PWD/.jdk-temurin-21"
                        export PATH="\$JAVA_HOME/bin:\$PATH"

                        # Configure Spring Boot to use test Redis
                        export SPRING_DATA_REDIS_HOST=localhost
                        export SPRING_DATA_REDIS_PORT=${TEST_REDIS_PORT}

                        echo "🏗️  Building and testing..."
                        chmod +x gradlew
                        ./gradlew clean test bootJar

                        echo "✅ Build and tests completed"
                    """
                }
            }
            post {
                always {
                    sh "docker rm -f ${TEST_REDIS_CONTAINER} || true"
                }
            }
        }

        stage('Build Docker Image') {
            steps {
                script {
                    sh """
                        echo "🐳 Building Docker image..."
                        docker build \
                            -t ${IMAGE_NAME}:${BUILD_NUMBER} \
                            -t ${IMAGE_NAME}:latest \
                            .
                        echo "✅ Docker image built"
                    """
                }
            }
        }

        stage('Deploy') {
            steps {
                script {
                    sh """
                        echo "🚀 Deploying application..."

                        # Stop and remove existing container
                        docker stop ${CONTAINER_NAME} 2>/dev/null || true
                        docker rm ${CONTAINER_NAME} 2>/dev/null || true

                        # Run new container
                        docker run -d \
                            --name ${CONTAINER_NAME} \
                            -p ${APP_PORT}:${APP_PORT} \
                            -e SPRING_PROFILES_ACTIVE=${SPRING_PROFILES_ACTIVE} \
                            -e REDIS_HOST=${REDIS_HOST} \
                            -e REDIS_PASSWORD=${REDIS_PASSWORD} \
                            -e JWT_SECRET=${JWT_SECRET} \
                            --restart unless-stopped \
                            ${IMAGE_NAME}:latest

                        echo "✅ Application deployed"

                        # Health check
                        echo "🏥 Waiting for application to be healthy..."
                        sleep 5

                        if docker ps | grep -q ${CONTAINER_NAME}; then
                            echo "✅ Container is running"
                        else
                            echo "❌ Container failed to start"
                            docker logs ${CONTAINER_NAME}
                            exit 1
                        fi
                    """
                }
            }
        }
    }

    post {
        always {
            sh "docker rm -f ${TEST_REDIS_CONTAINER} 2>/dev/null || true"
        }
        failure {
            echo "❌ 빌드/배포 실패! 로그를 확인하세요."
        }
        success {
            echo "✅ 배포가 성공적으로 완료되었습니다!"
        }
    }
}
