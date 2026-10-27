def publish(Map config) {
    def m         = config.metrics
    def emoji     = config.status == 'SUCCESS' ? '✅' : '❌'
    def estado    = config.status == 'SUCCESS' ? 'Finalizado' : 'Fallido'
    def fecha     = new Date().format('yyyy-MM-dd HH:mm')
    def servicio  = config.serviceName ?: config.reportName
    def ancestor  = config.ancestorId ?: '557057'   // página "ME" en el espacio PERF
    def title     = "${config.issueKey} — ${config.testType?.toUpperCase()} — ${servicio} — ${fecha}"

    def html = "<h2>${emoji} ${estado}</h2>" +
        "<p><strong>Issue:</strong> <a href=\"https://mi-ecosistema.atlassian.net/browse/${config.issueKey}\">${config.issueKey}</a>" +
        " | <strong>Servicio:</strong> ${servicio}" +
        " | <strong>Tipo:</strong> ${config.testType?.toUpperCase()}" +
        " | <strong>Build:</strong> #${config.buildNumber}" +
        " | <strong>Estado:</strong> ${config.status}</p>" +
        "<h3>Summary</h3><table><tbody>" +
        "<tr><th>Métrica</th><th>Valor</th></tr>" +
        "<tr><td>Iteraciones</td><td>${m.iterations}</td></tr>" +
        "<tr><td>Requests/seg</td><td>${m.reqRate}</td></tr>" +
        "<tr><td>Error Rate</td><td>${m.errorRate}</td></tr>" +
        "<tr><td>Avg</td><td>${m.avg}</td></tr>" +
        "<tr><td>Min</td><td>${m.min}</td></tr>" +
        "<tr><td>Max</td><td>${m.max}</td></tr>" +
        "<tr><td>P50</td><td>${m.p50}</td></tr>" +
        "<tr><td>P90</td><td>${m.p90}</td></tr>" +
        "<tr><td>P95</td><td>${m.p95}</td></tr>" +
        "<tr><td>P99</td><td>${m.p99}</td></tr>" +
        "</tbody></table>" +
        "<p><strong>Reporte:</strong> ${config.reportName} (adjunto como artifact en Jenkins, build #${config.buildNumber})</p>"

    def payload = [
        type:      'page',
        title:     title.toString(),
        space:     [key: 'PERF'],
        ancestors: [[id: ancestor.toString()]],
        body:      [storage: [value: html.toString(), representation: 'storage']]
    ]

    writeFile file: 'confluence-payload.json',
              text: groovy.json.JsonOutput.toJson(payload),
              encoding: 'UTF-8'

    withEnv(["ATLASSIAN_TOKEN=${config.token}"]) {
        def http = sh(
            script: '''
                curl -s -o confluence-response.json -w "%{http_code}" -X POST \
                  -H 'Content-Type: application/json' \
                  -u "newprojectcv.1999@gmail.com:$ATLASSIAN_TOKEN" \
                  --data-binary @confluence-payload.json \
                  'https://mi-ecosistema.atlassian.net/wiki/rest/api/content'
            ''',
            returnStdout: true
        ).trim()

        if (http == '200') {
            echo "✅ Confluence actualizado: ${title}"
        } else {
            echo "⚠️ Confluence respondió HTTP ${http} para '${title}'"
            sh 'cat confluence-response.json || true'
        }
    }
}