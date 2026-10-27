def transition(String issueKey, String transitionId, String token) {
    withEnv(["ATLASSIAN_TOKEN=${token}"]) {
        sh """
            curl -s -X POST \\
              -H 'Content-Type: application/json' \\
              -u "newprojectcv.1999@gmail.com:\$ATLASSIAN_TOKEN" \\
              -d '{"transition": {"id": "${transitionId}"}}' \\
              'https://mi-ecosistema.atlassian.net/rest/api/3/issue/${issueKey}/transitions'
        """
    }
}

// El mensaje puede traer saltos de línea, comillas, | y *.
// Se arma el JSON con JsonOutput (una línea = un párrafo ADF) y se envía desde archivo.
def comment(String issueKey, String mensaje, String token) {
    def paragraphs = []
    for (String line : mensaje.split('\n')) {
        if (line.trim()) {
            paragraphs << [type: 'paragraph', content: [[type: 'text', text: line]]]
        }
    }

    def payload = [body: [type: 'doc', version: 1, content: paragraphs]]

    writeFile file: 'jira-comment.json',
              text: groovy.json.JsonOutput.toJson(payload),
              encoding: 'UTF-8'

    withEnv(["ATLASSIAN_TOKEN=${token}"]) {
        def http = sh(
            script: '''
                curl -s -o jira-comment-response.json -w "%{http_code}" -X POST \
                  -H 'Content-Type: application/json' \
                  -u "newprojectcv.1999@gmail.com:$ATLASSIAN_TOKEN" \
                  --data-binary @jira-comment.json \
                  "https://mi-ecosistema.atlassian.net/rest/api/3/issue/''' + issueKey + '''/comment"
            ''',
            returnStdout: true
        ).trim()

        if (http == '201') {
            echo "✅ Comentario publicado en ${issueKey}"
        } else {
            echo "⚠️ Jira respondió HTTP ${http} al comentar en ${issueKey}"
            sh 'cat jira-comment-response.json || true'
        }
    }
}

def attach(String issueKey, String filePath, String token) {
    if (fileExists(filePath)) {
        withEnv(["ATLASSIAN_TOKEN=${token}"]) {
            sh """
                curl -s -X POST \\
                  -H 'X-Atlassian-Token: no-check' \\
                  -u "newprojectcv.1999@gmail.com:\$ATLASSIAN_TOKEN" \\
                  -F 'file=@${filePath}' \\
                  'https://mi-ecosistema.atlassian.net/rest/api/3/issue/${issueKey}/attachments'
            """
        }
    } else {
        echo "⚠️ Reporte no encontrado en ${filePath} — omitiendo adjunto"
    }
}

def update(Map config) {
    // config: issueKey, status, reportPath, reportName, testType, buildNumber, token, metrics
    def status         = config.status ?: 'SUCCESS'
    def emoji          = status == 'SUCCESS' ? '✅' : '❌'
    def transitionId   = status == 'SUCCESS' ? '3' : '4'
    def transitionName = status == 'SUCCESS' ? 'Finalizado' : 'Fallido'
    def mensaje        = "${emoji} Performance Test completado. Build #${config.buildNumber} - Estado: ${status}"

    echo "🔄 Cambiando estado Jira a ${transitionName}..."
    transition(config.issueKey, transitionId, config.token)

    echo "📝 Comentando en ${config.issueKey}..."
    comment(config.issueKey, mensaje, config.token)

    echo "📎 Adjuntando reporte..."
    attach(config.issueKey, config.reportPath, config.token)
}