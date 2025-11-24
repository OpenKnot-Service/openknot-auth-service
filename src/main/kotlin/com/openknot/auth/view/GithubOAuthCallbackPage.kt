package com.openknot.auth.view

object GithubOAuthCallbackPage {
    private const val BASE_STYLES = """
          <style>
              :root {
                  --bg: linear-gradient(135deg, #0f172a 0%, #1e293b 40%, #0ea5e9 100%);
                  --card: #0b1220;
                  --accent: #38bdf8;
                  --success: #22c55e;
                  --error: #ef4444;
                  --text: #e2e8f0;
                  --muted: #94a3b8;
                  --shadow: 0 20px 60px rgba(0,0,0,0.35);
                  --border: 1px solid rgba(255,255,255,0.08);
              }
              * { box-sizing: border-box; }
              body {
                  margin: 0;
                  min-height: 100vh;
                  display: grid;
                  place-items: center;
                  font-family: 'Inter', 'Pretendard', -apple-system, BlinkMacSystemFont, 'Segoe UI', sans-serif;
                  background: var(--bg);
                  color: var(--text);
              }
              .card {
                  width: min(520px, 92vw);
                  background: var(--card);
                  border-radius: 16px;
                  padding: 32px 28px;
                  box-shadow: var(--shadow);
                  border: var(--border);
                  text-align: center;
                  position: relative;
                  overflow: hidden;
              }
              .glow {
                  position: absolute;
                  inset: 0;
                  background: radial-gradient(circle at 20% 20%, rgba(56,189,248,0.15), transparent 40%),
                              radial-gradient(circle at 80% 30%, rgba(14,165,233,0.15), transparent 42%),
                              radial-gradient(circle at 50% 80%, rgba(34,197,94,0.12), transparent 38%);
                  pointer-events: none;
                  filter: blur(0.5px);
              }
              .badge {
                  display: inline-flex;
                  align-items: center;
                  gap: 8px;
                  padding: 8px 14px;
                  border-radius: 999px;
                  font-size: 13px;
                  letter-spacing: -0.01em;
                  color: #0b1220;
                  background: #e0f2fe;
                  margin-bottom: 18px;
                  border: 1px solid rgba(14,165,233,0.25);
              }
              .title { margin: 4px 0 10px; font-size: 24px; font-weight: 800; }
              .message { margin: 0 0 10px; font-size: 16px; color: var(--muted); }
              .status { font-size: 14px; color: var(--muted); }
              .success { color: var(--success); }
              .error { color: var(--error); }
          </style>
      """

    fun success(message: String): String = """
          <!doctype html>
          <html>
          <head>
              <meta charset="UTF-8" />
              <title>GitHub 연동 완료</title>
              $BASE_STYLES
          </head>
          <body>
              <div class="card">
                  <div class="glow"></div>
                  <div class="badge">GitHub OAuth</div>
                  <div class="title success">✓ GitHub 연동 완료</div>
                  <p class="message">$message</p>
                  <p class="status">잠시 후 창이 자동으로 닫힙니다...</p>
              </div>
              <script>setTimeout(() => window.close(), 1500);</script>
          </body>
          </html>
      """.trimIndent()

    fun error(message: String): String = """
          <!doctype html>
          <html>
          <head>
              <meta charset="UTF-8" />
              <title>GitHub 연동 실패</title>
              $BASE_STYLES
          </head>
          <body>
              <div class="card">
                  <div class="glow"></div>
                  <div class="badge">GitHub OAuth</div>
                  <div class="title error">✗ GitHub 연동 실패</div>
                  <p class="message">$message</p>
                  <p class="status">잠시 후 창이 자동으로 닫힙니다...</p>
              </div>
              <script>setTimeout(() => window.close(), 2000);</script>
          </body>
          </html>
      """.trimIndent()
}