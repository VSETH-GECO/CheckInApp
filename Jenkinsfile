node {
    if (env.BRANCH_NAME == 'master' || env.BRANCH_NAME == 'dev') {

        stage('Clone Repository') {
            checkout scm
        }
		

        stage('Android Build') {
            docker.image('thyrlian/android-sdk:latest').inside {
				withCredentials([string(credentialsId: 'keystore-password', variable: 'PASSWORD')]) {
					sh 'sed -i "s/#KEYPASSWORD#/$PASSWORD/g" "./GECO CheckIn/build.gradle"'
				}
				sh 'chmod +x gradlew'
                sh './gradlew clean build assembleRelease'
            }
        }
		
		stage('Archive Artifacts') {
			sh 'ls'
			sh 'ls ./GECO CheckIn'
			sh 'ls ./GECO CheckIn/build'
			sh 'ls ./GECO CheckIn/build/outputs'
			sh 'ls ./GECO CheckIn/build/outputs/apk'
			sh 'ls ./GECO CheckIn/build/outputs/apk/release'
			archiveArtifacts artifacts: '**/*.apk', fingerprint: true
		}
    }
}