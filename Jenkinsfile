node {
    if (env.BRANCH_NAME == 'master' || env.BRANCH_NAME == 'dev') {
        def image

        stage('Clone Repository') {
            checkout scm
        }

        stage('Maven Build') {
            docker.image('thyrlian/android-sdk') {
                sh 'gradlew build'
            }

            archiveArtifacts artifacts: 'app/build/outputs/apk/*.apk', fingerprint: true
        }
    }
}