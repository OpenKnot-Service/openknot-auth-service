package com.openknot.auth.view

object GithubOAuthCallbackPage {
    private const val BASE_STYLES = """
          <style>
              :root {
                  --bg: linear-gradient(135deg, #0f172a 0%, #1e293b 100%);
                  --card: rgba(15, 23, 42, 0.85);
                  --accent: #64748b;
                  --success: #10b981;
                  --error: #ef4444;
                  --text: #f1f5f9;
                  --muted: #94a3b8;
                  --shadow: 0 20px 60px rgba(0,0,0,0.4);
                  --border: 1px solid rgba(255,255,255,0.1);
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
                  backdrop-filter: blur(12px);
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
                  background: radial-gradient(circle at 50% 0%, rgba(100,116,139,0.08), transparent 50%);
                  pointer-events: none;
              }
              .badge {
                  display: inline-flex;
                  align-items: center;
                  gap: 8px;
                  padding: 8px 14px;
                  border-radius: 999px;
                  font-size: 13px;
                  letter-spacing: -0.01em;
                  color: #f1f5f9;
                  background: rgba(100,116,139,0.2);
                  margin-bottom: 18px;
                  border: 1px solid rgba(148,163,184,0.3);
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