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
    sh """
        curl -s -X POST \\
          -H 'X-Atlassian-Token: no-check' \\
          -u 'newprojectcv.1999@gmail.com:${token}' \\
          -F 'file=@${filePath}' \\
          'https://mi-ecosistema.atlassian.net/rest/api/3/issue/${issueKey}/attachments'
    """
}