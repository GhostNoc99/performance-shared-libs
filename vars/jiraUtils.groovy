def transition(String issueKey, String transitionId, String token) {
    sh """
        curl -s -X POST \\
          -H 'Content-Type: application/json' \\
          -u 'newprojectcv.1999@gmail.com:${token}' \\
          -d '{"transition": {"id": "${transitionId}"}}' \\
          'https://mi-ecosistema.atlassian.net/rest/api/3/issue/${issueKey}/transitions'
    """
}

def comment(String issueKey, String mensaje, String token) {
    sh """
        curl -s -X POST \\
          -H 'Content-Type: application/json' \\
          -u 'newprojectcv.1999@gmail.com:${token}' \\
          -d '{"body": {"type": "doc", "version": 1, "content": [{"type": "paragraph", "content": [{"type": "text", "text": "${mensaje}"}]}]}}' \\
          'https://mi-ecosistema.atlassian.net/rest/api/3/issue/${issueKey}/comment'
    """
}

def attach(String issueKey, String filePath, String token) {
    if (fileExists(filePath)) {
        sh """
            curl -s -X POST \\
              -H 'X-Atlassian-Token: no-check' \\
              -u 'newprojectcv.1999@gmail.com:${token}' \\
              -F 'file=@${filePath}' \\
              'https://mi-ecosistema.atlassian.net/rest/api/3/issue/${issueKey}/attachments'
        """
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