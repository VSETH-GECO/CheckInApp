node {
    if (env.BRANCH_NAME == 'master' || env.BRANCH_NAME == 'dev') {

        stage('Clone Repository') {
            checkout scm
        }

        stage('Android Build') {
            docker.image('thyrlian/android-sdk:latest') {
                sh './gradlew clean build assembleRelease'
            }

            
        }
		
		stage('Archive Artifacts') {
			archiveArtifacts artifacts: 'GECO CheckIn/build/outputs/apk/*.apk', fingerprint: true
		}
    }
}