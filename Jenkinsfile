node {
    if (env.BRANCH_NAME == 'master' || env.BRANCH_NAME == 'dev') {

        stage('Clone Repository') {
            checkout scm
        }
		

        stage('Android Build') {
            docker.image('thyrlian/android-sdk:latest').inside {
				withCredentials([string(credentialsId: 'keystore-password', variable: 'PASSWORD')]) {
					sh 'sed -i \'s/#KEYPASSWORD#/$PASSWORD/g\' "./GECO CheckIn/build.gradlew"'
				}
				sh 'chmod +x gradlew'
                sh './gradlew clean build assembleRelease'
            }
        }
		
		stage('Archive Artifacts') {
			archiveArtifacts artifacts: '**/build/outputs/apk/*.apk', fingerprint: true
		}
    }
}