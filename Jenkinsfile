pipeline {
  agent any
  stages {
    stage('build') {
      steps {
        sh './gradlew build'
      }
    }
    stage('') {
      steps {
        mail(subject: 'Jenkins', body: 'Build completed', to: 'inbox@tomclaw.com')
      }
    }
  }
}