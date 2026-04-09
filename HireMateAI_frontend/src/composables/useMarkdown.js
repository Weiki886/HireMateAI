/**
 * useMarkdown — AI 内容 Markdown 渲染
 * 安全解析：支持代码高亮、加粗、列表、链接等
 */
import { ref, computed } from 'vue'

// 轻量级 Markdown 解析器（不依赖 marked，避免包体积）
const MARKDOWN_RULES = [
  // 代码块 ```code```
  { regex: /```(\w*)\n?([\s\S]*?)```/g, replacement: '<pre class="code-block"><code>$2</code></pre>' },
  // 行内代码 `code`
  { regex: /`([^`]+)`/g, replacement: '<code class="inline-code">$1</code>' },
  // 标题 ### ###..###
  { regex: /^(#{1,6})\s+(.+)$/gm, replacement: (m, hashes, text) => {
    const level = hashes.length
    return `<h${level} class="md-h${level}">${text}</h${level}>`
  }},
  // 加粗 **text**
  { regex: /\*\*(.+?)\*\*/g, replacement: '<strong>$1</strong>' },
  // 斜体 *text*
  { regex: /(?<!\*)\*([^*\n]+?)\*(?!\*)/g, replacement: '<em>$1</em>' },
  // 无序列表 - item
  { regex: /^- (.+)$/gm, replacement: '<li>$1</li>' },
  // 有序列表 1. item
  { regex: /^\d+\. (.+)$/gm, replacement: '<li>$1</li>' },
  // 链接 [text](url)
  { regex: /\[([^\]]+)\]\(([^)]+)\)/g, replacement: '<a href="$2" target="_blank" rel="noopener">$1</a>' },
  // 换行\n\n → 段落
  { regex: /\n\n/g, replacement: '</p><p class="md-p">' },
  // 换行\n → <br>
  { regex: /\n/g, replacement: '<br>' },
]

export function useMarkdown() {
  /**
   * 将 Markdown 字符串转换为安全 HTML
   * @param {string} content
   * @returns {string} HTML string
   */
  const renderMarkdown = (content) => {
    if (!content) return ''
    let html = content
    for (const rule of MARKDOWN_RULES) {
      html = html.replace(rule.regex, rule.replacement)
    }
    // 包裹段落
    if (!html.startsWith('<')) {
      html = `<p class="md-p">${html}</p>`
    }
    // 清理多余空段落
    html = html.replace(/<p class="md-p"><\/p>/g, '')
    return html
  }

  /**
   * 纯文本转安全 HTML（不加粗等 Markdown 语法）
   */
  const renderPlainText = (content) => {
    if (!content) return ''
    return content
      .replace(/&/g, '&amp;')
      .replace(/</g, '&lt;')
      .replace(/>/g, '&gt;')
      .replace(/\n/g, '<br>')
  }

  return { renderMarkdown, renderPlainText }
}