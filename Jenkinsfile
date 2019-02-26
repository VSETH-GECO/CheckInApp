node {
    if (env.BRANCH_NAME == 'master' || env.BRANCH_NAME == 'dev') {

        stage('Clone Repository') {
            checkout scm
        }

        stage('Maven Build') {
            docker.image('thyrlian/android-sdk') {
                sh './gradlew clean build assembleRelease'
            }

            archiveArtifacts artifacts: 'GECO CheckIn/build/outputs/apk/*.apk', fingerprint: true
        }
    }
}