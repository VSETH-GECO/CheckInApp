node {
    if (env.BRANCH_NAME == 'master' || env.BRANCH_NAME == 'dev') {

        stage('Clone Repository') {
            checkout scm
        }

        stage('Android Build') {
			withCredentials([string(credentialsId: 'keystore-password', variable: 'PASSWORD')]) {
				docker.image('thyrlian/android-sdk:latest').inside {
					sh 'sed -i 's/#KEYPASSWORD#/$PASSWORD/g' "./GECO CheckIn/build.gradlew"'
					sh 'chmod +x gradlew'
					sh './gradlew clean build assembleRelease'
				}
			}
        }
		
		stage('Archive Artifacts') {
			archiveArtifacts artifacts: '**/build/outputs/apk/*.apk', fingerprint: true
		}
    }
}