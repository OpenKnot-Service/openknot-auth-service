pipeline {
    agent any

    environment {
        // Docker settings
        IMAGE_NAME = "openknot-auth-service"
        CONTAINER_NAME = "openknot-auth-service"
        APP_PORT = "8081"

        // Spring settings
        SPRING_PROFILES_ACTIVE = "prod"
    }

    stages {
        stage('Checkout') {
            steps {
                // Clean workspace before checkout
                cleanWs()

                checkout([
                    $class: 'GitSCM',
                    branches: [[name: '*/main']],
                    userRemoteConfigs: [[
                        credentialsId: 'GitHub_Token',
                        url: 'https://github.com/OpenKnot-Service/openknot-auth-service.git'
                    ]],
                    extensions: [
                        [$class: 'CleanBeforeCheckout'],
                        [$class: 'CloneOption', depth: 1, shallow: true]
                    ]
                ])

                // Verify we have the latest code
                sh 'echo "Current commit: $(git rev-parse HEAD)"'
                sh 'echo "Current branch: $(git branch --show-current)"'
            }
        }

        stage('Setup JDK') {
            steps {
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

        stage('Build & Test') {
            steps {
                sh '''#!/bin/bash -e
                    export JAVA_HOME="$PWD/.jdk-temurin-21"
                    export PATH="$JAVA_HOME/bin:$PATH"

                    echo "🏗️  Building and testing..."
                    echo "📦 Testcontainers will automatically manage Redis for tests"

                    chmod +x gradlew
                    ./gradlew clean test bootJar

                    echo "✅ Build and tests completed"
                '''
            }
        }

        stage('Build Docker Image') {
            steps {
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

        stage('Deploy') {
            steps {
                withCredentials([
                    string(credentialsId: 'REDIS_HOST', variable: 'REDIS_HOST_VAR'),
                    string(credentialsId: 'REDIS_PASSWORD', variable: 'REDIS_PASSWORD_VAR'),
                    string(credentialsId: 'JWT_SECRET', variable: 'JWT_SECRET_VAR')
                ]) {
                    sh '''#!/bin/bash -e
                        echo "🚀 Deploying application..."

                        # Stop and remove existing container
                        if docker ps -a --format '{{.Names}}' | grep -q "^${CONTAINER_NAME}$"; then
                            echo "   Stopping existing container..."
                            docker stop ${CONTAINER_NAME} 2>/dev/null || true
                            docker rm -f ${CONTAINER_NAME} 2>/dev/null || true
                        fi

                        # Wait for port to be released
                        echo "⏳ Waiting for port ${APP_PORT} to be released..."
                        for i in {1..30}; do
                            if ! docker ps --format '{{.Ports}}' | grep -q ":${APP_PORT}->"; then
                                echo "✅ Port ${APP_PORT} is available"
                                break
                            fi
                            sleep 1
                        done

                        # Run new container
                        echo "📦 Starting new container..."
                        docker run -d \
                            --name ${CONTAINER_NAME} \
                            -p ${APP_PORT}:${APP_PORT} \
                            -e SPRING_PROFILES_ACTIVE=${SPRING_PROFILES_ACTIVE} \
                            -e REDIS_HOST="${REDIS_HOST_VAR}" \
                            -e REDIS_PASSWORD="${REDIS_PASSWORD_VAR}" \
                            -e JWT_SECRET="${JWT_SECRET_VAR}" \
                            --restart unless-stopped \
                            ${IMAGE_NAME}:latest

                        echo "✅ Application deployed"

                        # Health check
                        echo "🏥 Checking application health..."
                        sleep 5

                        if docker ps | grep -q ${CONTAINER_NAME}; then
                            echo "✅ Container is running"
                            docker logs --tail 20 ${CONTAINER_NAME}
                        else
                            echo "❌ Container failed to start"
                            docker logs ${CONTAINER_NAME}
                            exit 1
                        fi
                    '''
                }
            }
        }
    }

    post {
        failure {
            echo "❌ 빌드/배포 실패! 로그를 확인하세요."
        }
        success {
            echo "✅ 배포가 성공적으로 완료되었습니다!"
        }
    }
}
