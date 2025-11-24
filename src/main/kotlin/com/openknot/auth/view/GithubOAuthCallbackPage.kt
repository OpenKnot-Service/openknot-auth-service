package com.openknot.auth.view

object GithubOAuthCallbackPage {
    fun success(message: String): String {
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <title>GitHub 연동 완료</title>
                <style>
                    body {
                        font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', sans-serif;
                        display: flex;
                        justify-content: center;
                        align-items: center;
                        height: 100vh;
                        margin: 0;
                        background: #f5f5f5;
                    }
                    .message {
                        text-align: center;
                        padding: 2rem;
                        background: white;
                        border-radius: 8px;
                        box-shadow: 0 2px 8px rgba(0,0,0,0.1);
                    }
                    .success { color: #22c55e; }
                </style>
            </head>
            <body>
                <div class="message">
                    <h2 class="success">✓ GitHub 연동 완료</h2>
                    <p>${message}</p>
                    <p style="color: #666; font-size: 14px;">잠시 후 창이 자동으로 닫힙니다...</p>
                </div>
                <script>
                    setTimeout(() => window.close(), 1500);
                </script>
            </body>
            </html>
        """.trimIndent()
    }

    fun error(message: String): String {
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <title>GitHub 연동 실패</title>
                <style>
                    body {
                        font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', sans-serif;
                        display: flex;
                        justify-content: center;
                        align-items: center;
                        height: 100vh;
                        margin: 0;
                        background: #f5f5f5;
                    }
                    .message {
                        text-align: center;
                        padding: 2rem;
                        background: white;
                        border-radius: 8px;
                        box-shadow: 0 2px 8px rgba(0,0,0,0.1);
                    }
                    .error { color: #ef4444; }
                </style>
            </head>
            <body>
                <div class="message">
                    <h2 class="error">✗ GitHub 연동 실패</h2>
                    <p>${message}</p>
                    <p style="color: #666; font-size: 14px;">잠시 후 창이 자동으로 닫힙니다...</p>
                </div>
                <script>
                    setTimeout(() => window.close(), 2000);
                </script>
            </body>
            </html>
        """.trimIndent()
    }
}
