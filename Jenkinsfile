node {
    if (env.BRANCH_NAME == 'master' || env.BRANCH_NAME == 'dev') {

        stage('Clone Repository') {
            checkout scm
        }
		
		withCredentials([string(credentialsId: 'keystore-password', variable: 'PASSWORD')]) {
			def password = $PASSWORD
		}

        stage('Android Build') {
			docker.image('thyrlian/android-sdk:latest').inside {
				sh 'sed -i 's/#KEYPASSWORD#/${password}/g' "./GECO CheckIn/build.gradlew"'
				sh 'chmod +x gradlew'
				sh './gradlew clean build assembleRelease'
			}
        }
		
		stage('Archive Artifacts') {
			archiveArtifacts artifacts: '**/build/outputs/apk/*.apk', fingerprint: true
		}
    }
}