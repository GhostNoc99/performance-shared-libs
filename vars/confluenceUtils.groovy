def publish(Map config) {
    def m     = config.metrics
    def emoji = config.status == 'SUCCESS' ? '✅' : '❌'
    def estado = config.status == 'SUCCESS' ? 'Finalizado' : 'Fallido'
    def fecha  = new Date().format('yyyy-MM-dd HH:mm')
    def title  = "${config.issueKey} — ${config.testType?.toUpperCase()} — ${fecha}"

    sh """
        curl -s -X POST \\
        -H 'Content-Type: application/json' \\
        -u 'newprojectcv.1999@gmail.com:${config.token}' \\
        -d '{
            "type": "page",
            "title": "${title}",
            "space": {"key": "PERF"},
            "ancestors": [{"id": "688129"}],
            "body": {
                "storage": {
                    "value": "<h2>${emoji} ${estado}</h2><p><strong>Issue:</strong> <a href=\\"https://mi-ecosistema.atlassian.net/browse/${config.issueKey}\\">${config.issueKey}</a> | <strong>Tipo:</strong> ${config.testType?.toUpperCase()} | <strong>Build:</strong> #${config.buildNumber} | <strong>Estado:</strong> ${config.status}</p><h3>Summary</h3><table><tbody><tr><th>Métrica</th><th>Valor</th></tr><tr><td>Iteraciones</td><td>${m.iterations}</td></tr><tr><td>Requests/seg</td><td>${m.reqRate}</td></tr><tr><td>Error Rate</td><td>${m.errorRate}</td></tr><tr><td>Avg</td><td>${m.avg}</td></tr><tr><td>Min</td><td>${m.min}</td></tr><tr><td>Max</td><td>${m.max}</td></tr><tr><td>P50</td><td>${m.p50}</td></tr><tr><td>P90</td><td>${m.p90}</td></tr><tr><td>P95</td><td>${m.p95}</td></tr><tr><td>P99</td><td>${m.p99}</td></tr></tbody></table><p><strong>Reporte adjunto en Jira:</strong> <a href=\\"https://mi-ecosistema.atlassian.net/browse/${config.issueKey}\\">${config.reportName}</a></p>",
                    "representation": "storage"
                }
            }
        }' \\
        'https://mi-ecosistema.atlassian.net/wiki/rest/api/content'
    """
    echo "✅ Confluence actualizado!"
}