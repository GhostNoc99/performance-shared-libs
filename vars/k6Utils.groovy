def run(Map config) {
    sh """
        mkdir -p k6/reports
        k6 run k6/dynamic-test.js \\
        --env URL=${config.url} \\
        --env METHOD=${config.method} \\
        --env HEADERS='${config.headers}' \\
        --env BODY='${config.body}' \\
        --env VUS=${config.vus} \\
        --env DURATION=${config.duration} \\
        --env TEST_TYPE=${config.testType} \\
        --env REPORT_NAME=${config.reportName} \\
        --env ISSUE_KEY=${config.issueKey} \\
        --out influxdb=http://host.docker.internal:8086/k6
    """
}

def qualityGate(String testType, String jsonPath) {
    def p95Threshold   = testType == 'stress' ? 2000 : 500
    def errorThreshold = testType == 'stress' ? 5.0  : 1.0

    echo "🔍 Evaluando Quality Gate..."
    echo "Tipo: ${testType.toUpperCase()}"
    echo "Umbral p95:   < ${p95Threshold}ms"
    echo "Umbral error: < ${errorThreshold}%"

    if (!fileExists(jsonPath)) {
        echo "⚠️ JSON no encontrado — omitiendo Quality Gate"
        return
    }

    def jsonText = readFile(jsonPath)
    def getVal = { String key ->
        def m = (jsonText =~ /"${key}"\s*:\s*([\d.]+)/)
        return m ? m[0][1].toDouble() : 0.0
    }

    def p95       = getVal('p95')
    def errorRate = getVal('errorRate') * 100

    echo "📊 P95 medido:        ${String.format('%.2f', p95)}ms"
    echo "📊 Error rate medido: ${String.format('%.2f', errorRate)}%"

    def p95Pass   = p95 < p95Threshold
    def errorPass = errorRate < errorThreshold

    if (p95Pass && errorPass) {
        echo "✅ QUALITY GATE PASSED"
        echo "   P95: ${String.format('%.2f', p95)}ms < ${p95Threshold}ms ✅"
        echo "   Error: ${String.format('%.2f', errorRate)}% < ${errorThreshold}% ✅"
        currentBuild.result = 'SUCCESS'
    } else {
        def msg = "❌ QUALITY GATE FAILED\n"
        if (!p95Pass)   msg += "   P95: ${String.format('%.2f', p95)}ms >= ${p95Threshold}ms ❌\n"
        if (!errorPass) msg += "   Error: ${String.format('%.2f', errorRate)}% >= ${errorThreshold}% ❌\n"
        currentBuild.result = 'FAILURE'
        error(msg)
    }
}

def parseMetrics(String jsonPath) {
    if (!fileExists(jsonPath)) return [
        iterations: '0', reqRate: '0/s', errorRate: '0.00%',
        avg: '0ms', min: '0ms', max: '0ms',
        p50: '0ms', p90: '0ms', p95: '0ms', p99: '0ms'
    ]

    def jsonText = readFile(jsonPath)
    def getVal = { String key ->
        def m = (jsonText =~ /"${key}"\s*:\s*([\d.]+)/)
        return m ? m[0][1] : '0'
    }
    return [
        iterations: getVal('iterations'),
        reqRate:    String.format("%.2f", getVal('reqRate').toDouble()) + '/s',
        errorRate:  String.format("%.4f", getVal('errorRate').toDouble() * 100) + '%',
        avg:        String.format("%.2f", getVal('avg').toDouble()) + 'ms',
        min:        String.format("%.2f", getVal('min').toDouble()) + 'ms',
        max:        String.format("%.2f", getVal('max').toDouble()) + 'ms',
        p50:        String.format("%.2f", getVal('p50').toDouble()) + 'ms',
        p90:        String.format("%.2f", getVal('p90').toDouble()) + 'ms',
        p95:        String.format("%.2f", getVal('p95').toDouble()) + 'ms',
        p99:        String.format("%.2f", getVal('p99').toDouble()) + 'ms',
    ]
}