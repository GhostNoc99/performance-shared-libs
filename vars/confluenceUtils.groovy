def publish(Map config) {
    def m = config.metrics
    sh """
        curl -s -X POST \\
        -H 'Content-Type: application/json' \\
        -u 'newprojectcv.1999@gmail.com:${config.token}' \\
        -d '{
            "type": "page",
            "title": "${config.title}",
            "space": {"key": "PERF"},
            "ancestors": [{"id": "688129"}],
            "body": {
                "storage": {
                    "value": "<h2>${config.emoji} ${config.estado}</h2><p><strong>Issue:</strong> ${config.issueKey} | <strong>Tipo:</strong> ${config.testType} | <strong>Build:</strong> #${config.buildNumber}</p><h3>Summary</h3><table><tbody><tr><th>Métrica</th><th>Valor</th></tr><tr><td>Iteraciones</td><td>${m.iterations}</td></tr><tr><td>Requests/seg</td><td>${m.reqRate}</td></tr><tr><td>Error Rate</td><td>${m.errorRate}</td></tr><tr><td>P95</td><td>${m.p95}</td></tr></tbody></table>",
                    "representation": "storage"
                }
            }
        }' \\
        'https://mi-ecosistema.atlassian.net/wiki/rest/api/content'
    """
}